package com.eft.positivelauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import com.eft.libpositive.events.PositiveTransEvent;
import com.eft.positivelauncher.TransactionResponse;
import com.eft.positivelauncher.activities.CustomActivity;
import com.eft.positivelauncher.activities.MainActivity;
import com.eft.libpositive.PosIntegrate;
import com.eft.libpositive.wrappers.HistoryTransResult;
import com.eft.libpositive.wrappers.PositiveError;
import com.eft.libpositive.wrappers.PositiveReportResult;
import com.eft.libpositive.wrappers.PositiveTransResult;
import com.eft.positivelauncher.weblink.WebLinkIntegrate;

import static com.eft.libpositive.messages.IMessages.SERVICE_EVENT;
import static com.eft.libpositive.messages.IMessages.TRANSACTION_RESULT_EVENT;
import static com.eft.libpositive.messages.IMessages.TRANSACTION_STATUS_EVENT;

/**************************************************************************************************/
/**************************************************************************************************/

/*
   This class provides the receiver that is declared in the manifest.
   It is used to receive the response to a transaction and debug the result.
   Once the result is received it brings this app back to the front with a call to startActivity
*/

/**************************************************************************************************/

/**************************************************************************************************/
public class PositiveLaunchReceiver extends BroadcastReceiver {

    /* These need to match up with the manifest for app and the service (DOP NOT CHANGE) */
    private static final String TAG = "PositiveLaunchReceiver";
    ArrayList<TransactionResponse> responseList;
    ArrayList<HistoryTransResult> transHistoryList;
    ArrayList<TransactionResponse> statusResponseList;
    public static ArrayList<String> statusEventsList = new ArrayList<>();
    String resultType;
    String responseTitle;

    public static PositiveTransResult lastResult = null;
    @Override
    public void onReceive(Context context, Intent intent) {

        if (WebLinkIntegrate.enabled)
            return;

        if (intent != null) {
            if (TRANSACTION_RESULT_EVENT.equals(intent.getAction())) {
                Log.i(TAG, "TRANSACTION_RESULT_EVENT = " + intent.getAction());

                if ( intent.hasExtra("ReceiverResultType"))
                    resultType = intent.getStringExtra("ReceiverResultType");

                if (resultType != null && resultType.equals("Reports")) {
                    PositiveReportResult result = PosIntegrate.unpackReport(context, intent);
                    if (result != null) {
                        if (result.getReportType().equals("XReport")) {
                            responseTitle = "X Report ";
                        } else {
                            responseTitle = "Z Report ";
                        }
                        populateReportList(result);
                    }

                } else if (resultType != null && resultType.equals("History Reports")) {
                    transHistoryList = intent.getParcelableArrayListExtra("HistoryList");
                    responseTitle = "History Report ";
                    populateHistoryList();

                } else {
                    // Unpack the transaction result. This will return null of the event was not for us, or if there was an error
                    PositiveTransResult result = PosIntegrate.unpackResult(context, intent);

                    if (result != null) {

                        lastResult = result;
                        boolean transFound = intent.getBooleanExtra("TransResponse", false);

                        if (transFound) {
                            //Populate Response list here
                            responseTitle = "Transaction Response ";
                            populateResponseList(result);
                            MainActivity.lastReceivedUTI = result.getUTI();
                            // Debug the result
                            printDebugLog(result);

                        } else {
                            Log.i(TAG, "Transaction not found");
                            PositiveError error = result.getError();
                            String errorText = result.getErrorText();
                            Toast.makeText(context, "Transaction Not Found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                populateStatusEvents();
                displayTransactionDetails(context);

            } else if (TRANSACTION_STATUS_EVENT.equals(intent.getAction())) {
                String statusEvent = intent.getStringExtra("StatusEvent");
                Log.d(TAG, "Status event " + statusEvent);
                statusEventsList.add(statusEvent);
            }
        } else {
            Log.d(TAG, "Receiver intent is null");
        }

    }

    private void printDebugLog(PositiveTransResult result) {

        Log.i(TAG, "Transaction Type = " + result.getTransType());
        Log.i(TAG, "UTI = " + result.getUTI());

        Log.i(TAG, "Amount = " + result.getAmountTrans());
        Log.i(TAG, "Approved = " + result.isTransApproved());
        Log.i(TAG, "Cancelled = " + result.isTransCancelled());
        Log.i(TAG, "SigRequired = " + result.isCvmSigRequired());
        Log.i(TAG, "PINVerified = " + result.isCvmPinVerified());
        Log.i(TAG, "Currency = " + result.getTransCurrencyCode());
        Log.i(TAG, "Tid = " + result.getTerminalId());
        Log.i(TAG, "Mid = " + result.getMerchantId());
        Log.i(TAG, "Version = " + result.getSoftwareVersion());

        if (result.isTransDetails()) {
            Log.i(TAG, "Transaction Details:");
            Log.i(TAG, "ReceiptNumber = " + result.getReceiptNumber());
            Log.i(TAG, "RRN = " + result.getRetrievalReferenceNumber());
            Log.i(TAG, "ResponseCode = " + result.getResponseCode());
            Log.i(TAG, "Stan = " + result.getStan());
            Log.i(TAG, "AuthCode = " + result.getAuthorisationCode());
            Log.i(TAG, "MerchantTokenId = " + result.getMerchantTokenId());

            String cardType = result.getCardType();
            Log.i(TAG, "CardType = " + cardType);

            if (cardType.compareTo("EMV") == 0 || cardType.compareTo("CTLS") == 0) {

                Log.i(TAG, "AID = " + result.getEmvAid());
                Log.i(TAG, "TSI = " + result.getEmvTsi());
                Log.i(TAG, "TVR = " + result.getEmvTvr());
                Log.i(TAG, "CardHolder = " + result.getEmvCardholderName());
                Log.i(TAG, "Cryptogram = " + byteArrayToHexString(result.getEmvCryptogram()));
                Log.i(TAG, "CryptogramType = " + result.getEmvCryptogramType());

            }
            Log.i(TAG, "PAN = " + result.getCardPan());
            Log.i(TAG, "ExpiryDate = " + result.getCardExpiryDate());
            Log.i(TAG, "StartDate = " + result.getCardStartDate());
            Log.i(TAG, "Scheme = " + result.getCardScheme());
            Log.i(TAG, "PSN = " + result.getCardPanSequenceNumber());
        }
    }

    private void populateStatusEvents() {
        statusResponseList = new ArrayList<>();
        for (String statusEvent : statusEventsList) {
            statusResponseList.add(new TransactionResponse(statusEvent, ""));
        }
    }

    private void populateHistoryList() {

        responseList = new ArrayList<>();
        for (HistoryTransResult historyItem : transHistoryList) {

            responseList.add(new TransactionResponse("Type", "" + historyItem.getTransType()));
            responseList.add(new TransactionResponse("Amount", "" + historyItem.getTransAmount() / 100));
            responseList.add(new TransactionResponse("Status", "" + historyItem.getTransApproved()));
            responseList.add(new TransactionResponse("Date Time", "" + historyItem.getTransDate()));
            responseList.add(new TransactionResponse("PAN", "" + historyItem.getTransPan()));
            responseList.add(new TransactionResponse("RNN", "" + historyItem.getRnn()));
            responseList.add(new TransactionResponse("Receipt No", "" + historyItem.getReceiptNo()));
            responseList.add(new TransactionResponse("", ""));
        }

    }

    private void displayTransactionDetails(Context context) {

        Log.d(TAG, "StatusList  :" + statusEventsList.size());
        Intent responseintent = new Intent(context, CustomActivity.class);
        responseintent.putExtra("fragment_to_launch", "ChooseOptionFragment");
        responseintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        responseintent.putParcelableArrayListExtra("TransResponseList", responseList);
        responseintent.putParcelableArrayListExtra("StatusList", statusResponseList);
        responseintent.putExtra("Title", responseTitle);
        context.startActivity(responseintent);
    }

    private void populateReportList(PositiveReportResult result) {


        responseList = new ArrayList<>();
        responseList.add(new TransactionResponse("SALE:                 x " + result.getSaleCount(), "" + result.getSaleAmount() / 100));
        responseList.add(new TransactionResponse("REFUND:            x " + result.getRefundCount(), "" + result.getRefundAmount() / 100));
        responseList.add(new TransactionResponse("COMPLETION:  x " + result.getCompletionCount(), "" + result.getCompletionAmount() / 100));
        long totalCount = result.getSaleCount() + result.getRefundCount() + result.getCompletionCount();
        long totalAmount = result.getSaleAmount() - result.getRefundAmount() + result.getCompletionAmount();
        responseList.add(new TransactionResponse("NET TOTAL:         " + totalCount, "" + totalAmount / 100));
        responseList.add(new TransactionResponse("CASHBACK:      x " + result.getCashbackCount(), "" + result.getCashbackAmount() / 100));
        responseList.add(new TransactionResponse("GRATUITY:        x " + result.getGratuityCount(), "" + result.getGratuityAmount() / 100));

    }

    private void populateResponseList(PositiveTransResult result) {
        responseList = new ArrayList<>();
        responseList.add(new TransactionResponse("Type", "" + result.getTransType()));
        responseList.add(new TransactionResponse("UTI", "" + result.getUTI()));
        responseList.add(new TransactionResponse("Amount", "" + result.getAmountTrans()));
        responseList.add(new TransactionResponse("Discount", "" + result.getAmountDiscount()));
        responseList.add(new TransactionResponse("Approved", "" + result.isTransApproved()));
        responseList.add(new TransactionResponse("Cancelled", "" + result.isTransCancelled()));
        responseList.add(new TransactionResponse("SigRequired", "" + result.isCvmSigRequired()));
        responseList.add(new TransactionResponse("PINVerified", "" + result.isCvmPinVerified()));
        responseList.add(new TransactionResponse("Currency", "" + result.getTransCurrencyCode()));
        responseList.add(new TransactionResponse("Version", "" + result.getSoftwareVersion()));
        responseList.add(new TransactionResponse("terminalId", "" + result.getTerminalId()));
        responseList.add(new TransactionResponse("MerchantId", "" + result.getMerchantId()));

        if (result.isTransDetails()) {
            responseList.add(new TransactionResponse("ReceiptNumber", "" + result.getReceiptNumber()));
            if (result.getRetrievalReferenceNumber() != null) {
                responseList.add(new TransactionResponse("RRN", "" + result.getRetrievalReferenceNumber()));
            }
            if (result.getResponseCode() != null) {
                responseList.add(new TransactionResponse("ResponseCode", "" + result.getResponseCode()));
            }

            if (result.getStan() != null) {
                responseList.add(new TransactionResponse("Stan", "" + result.getStan()));
            }
            if (result.getAuthorisationCode() != null) {
                responseList.add(new TransactionResponse("AuthCode", "" + result.getAuthorisationCode()));
            }
            if (result.getMerchantTokenId() != null) {
                responseList.add(new TransactionResponse("MerchantTokenId", "" + result.getMerchantTokenId()));
            }

            String cardType = result.getCardType();
            Log.i(TAG, "CardType = " + cardType);
            responseList.add(new TransactionResponse("CardType", "" + cardType));
            if (cardType.compareTo("EMV") == 0 || cardType.compareTo("CTLS") == 0) {
                responseList.add(new TransactionResponse("AID", "" + result.getEmvAid()));
                responseList.add(new TransactionResponse("TSI", "" + result.getEmvTsi()));
                responseList.add(new TransactionResponse("TVR", "" + result.getEmvTvr()));
                responseList.add(new TransactionResponse("CardHolder", "" + result.getEmvCardholderName()));
                responseList.add(new TransactionResponse("Cryptogram", "" + byteArrayToHexString(result.getEmvCryptogram())));
                responseList.add(new TransactionResponse("CryptogramType", "" + result.getEmvCryptogramType()));

            }

            if (result.getCardPan() != null) {
                responseList.add(new TransactionResponse("PAN", "" + result.getCardPan()));
            }
            if (result.getCardExpiryDate() != null) {
                responseList.add(new TransactionResponse("ExpiryDate", "" + result.getCardExpiryDate()));
            }
            if (result.getCardStartDate() != null) {
                responseList.add(new TransactionResponse("StartDate", "" + result.getCardStartDate()));
            }
            if (result.getCardScheme() != null && !result.getCardScheme().equals("")) {
                responseList.add(new TransactionResponse("Scheme", "" + result.getCardScheme()));
            }
            if (result.getCardPanSequenceNumber() != null) {
                responseList.add(new TransactionResponse("PSN", "" + result.getCardPanSequenceNumber()));
            }

        }

    }


    public String byteArrayToHexString(final byte[] byteArray) {
        if (byteArray == null) {
            return "";
            //throw new IllegalArgumentException("Argument 'byteArray' cannot be null");
        }
        int readBytes = byteArray.length;
        StringBuilder hexData = new StringBuilder();
        int onebyte;
        for (int i = 0; i < readBytes; i++) {
            onebyte = (0x000000ff & byteArray[i]) | 0xffffff00;
            hexData.append(Integer.toHexString(onebyte).substring(6));
        }
        return hexData.toString().toUpperCase();
    }

}

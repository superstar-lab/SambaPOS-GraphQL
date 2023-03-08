package com.eft.positivelauncher.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eft.libpositive.PosIntegrate;
import com.eft.positivelauncher.R;
import com.eft.positivelauncher.Transaction;
import com.eft.positivelauncher.adapters.TransAdapter;
import com.eft.positivelauncher.weblink.WebLinkIntegrate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_HISTORYREPORT;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_UTI;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_XREPORT;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_ZREPORT;
import static com.eft.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_RECONCILIATION;
import static com.eft.positivelauncher.receiver.PositiveLaunchReceiver.statusEventsList;

/**************************************************************************************************/
/**************************************************************************************************/

/*
   This class sends transaction requests to the positivesvc so that the positive payment app can
   run a transaction.

   NOTES:
   1. The transaction amount is in the smallest currency denomination so for example 100 = $1.00
   2. The transaction currency can not be changed from this app, that must be done by configuration
      of the positive app
   3. List of transaction options displayed to choose from like sale, refund, reverse last, reverse by UTI,
        preauth, completion, query last transaction, cancel transaction.
   4. Options displayed to print X, Z and History reports.
   5. The reversal example here is attempting to reverse the last transaction.
   6. Reverse by UTI shows you how to reverse a transaction by passing in the UTI.
   7. Query Last Transaction show you how to query a previous transaction by the UTI number
   8. Cancel Transaction allows you to enter time in milliseconds and it cancels the transaction after that time.
      If the transaction is approved by that time it will not cancel the transaction.
   9. If a transaction is not available or supported by positive app, it displays transaction not found message.
   10. See PositiveLaunchReceiver for unpacking of results from both the transaction and the batch upload


*/

/**************************************************************************************************/

/**************************************************************************************************/
public class MainActivity extends AppCompatActivity implements TransAdapter.ItemClickListener {

//     Just a TAG used for debugging
    private static final String TAG = MainActivity.class.getSimpleName();
    public static String lastReceivedUTI; // used to make life easy when testing reversals of the last transaction
    public static String lastReceivedRRN; // used to make life easy when testing completions of the last transaction
    public static String lastReceivedAmount; // used to make life easy when testing completions of the last transaction
    RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private TransAdapter mAdapter;
    ArrayList<Transaction> adapterList;
    public static final int SALE = 0;
    public static final int REFUND = 1;
    public static final int REVERSE_LAST = 2;
    public static final int REVERSE_BY_UTI = 3;
    public static final int PREAUTH = 4;
    public static final int COMPLETION = 5;
    public static final int QUERY_TRANSACTION = 6;
    public static final int CANCEL_TRANSACTION = 7;
    private final int PRINT_X = 8;
    private final int PRINT_Z = 9;
    private final int PRINT_HISTORY = 10;
    private final int EXIT = 11;

    public static final String INPUT_AMOUNT_FRAGMENT = "InputAmountFragment";
    public static final String CHOOSE_OPTION_FRAGMENT = "ChooseOptionFragment";
    public static final String RESPONSE_FRAGMENT = "ResponseFragment";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);
        populateArrayList();
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_trans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new
                DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.
                getDrawable(getBaseContext(), R.drawable.line_divider)
        );
        recyclerView.addItemDecoration(divider);
        mAdapter = new TransAdapter(this, adapterList);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    private static void exec(String cmd) {

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                Log.i(TAG, "tasklist: " + line);
            process.waitFor();
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFileFromAssets(Context ctx, String srcFile, String destFile) throws IOException {
        Log.i(TAG, "prepare copy ASSERT/" + srcFile + " file to" + destFile);

        InputStream iptStm = null;
        OutputStream optStm = null;

        try {
            iptStm = ctx.getAssets().open(srcFile);
            Log.i(TAG, "AssetsFilePath:" + srcFile + " FileSize:" + (iptStm == null ? 0 : iptStm.available()));
            Log.i(TAG, "strDesFilePath:" + destFile);

            if (iptStm == null) {
                Log.i(TAG, "file[" + srcFile + "]not exists in the ASSERT,don't need to copy!");
                return;
            }

            File file = new File(destFile);
            if (!file.exists()) {// file not exists,need to copy
                if (!file.createNewFile()) {
                    return;
                }
                exec("chmod 766 " + file);
            } else {
                if (!file.delete() || !file.createNewFile()) {
                    return;
                }
                exec("chmod 766 " + file);
            }

            optStm = new FileOutputStream(file);

            int nLen;

            byte[] buff = new byte[1024];
            while ((nLen = iptStm.read(buff)) > 0) {
                optStm.write(buff, 0, nLen);
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (optStm != null) {
                optStm.close();
            }
            if (iptStm != null) {
                iptStm.close();
            }
        }
    }

    private void populateArrayList() {
        adapterList = new ArrayList<>();
        adapterList.add(new Transaction(R.drawable.msale,getResources().getString(R.string.run_sale)));
        adapterList.add(new Transaction(R.drawable.mrefund,getResources().getString(R.string.run_refund)));
        adapterList.add(new Transaction(R.drawable.mreversal,getResources().getString(R.string.reverse_last)));
        adapterList.add(new Transaction(R.drawable.mreversal,getResources().getString(R.string.reverse_uti)));
        adapterList.add(new Transaction(R.drawable.mpreauth,getResources().getString(R.string.preauth)));
        adapterList.add(new Transaction(R.drawable.mcompletion,getResources().getString(R.string.completion)));
        adapterList.add(new Transaction(R.drawable.mquerytrans,getResources().getString(R.string.query_transaction)));
        adapterList.add(new Transaction(R.drawable.mcancel,getResources().getString(R.string.cancel_trans)));
        adapterList.add(new Transaction(R.drawable.mdailybatch,getResources().getString(R.string.x_report)));
        adapterList.add(new Transaction(R.drawable.mreconciliation,getResources().getString(R.string.z_report)));
        adapterList.add(new Transaction(R.drawable.mhistoryreport,getResources().getString(R.string.history_report)));
        adapterList.add(new Transaction(R.drawable.mexit,getResources().getString(R.string.exit)));
    }

    @Override
    public void onItemClick(View view, int position) {
        statusEventsList.clear();
        switch (position) {

            case SALE: {
                /* Send a request to run a transaction for 100 pence (in the configured app currency */
                Log.i(TAG, "Run Auto Sale");
                Intent intent = new Intent(this, CustomActivity.class);
                intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                intent.putExtra("Screen_Title", "Enter Sale Amount");
                intent.putExtra("Trans_Type", SALE);
                startActivity(intent);
                break;
            }

            case REFUND: {
                /* Send a request to run a transaction for 100 pence (in the configured app currency */
                Log.i(TAG, "Run Auto Refund");
                Intent intent = new Intent(this, CustomActivity.class);
                intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                intent.putExtra("Screen_Title", "Enter Refund Amount");
                intent.putExtra("Trans_Type", REFUND);
                startActivity(intent);
                break;
            }

            case REVERSE_LAST: {
                Log.i(TAG, "Run Auto Reverse Last");

                Intent intent = new Intent(this, CustomActivity.class);
                intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                intent.putExtra("Screen_Title", "Enter Reversal Amount");
                intent.putExtra("Trans_Type", REVERSE_LAST);
                startActivity(intent);
                break;
            }

            case REVERSE_BY_UTI: {
                Log.i(TAG, "Run Uti Reversal");
                /* attempt to reverse the last transaction */
                Log.d(TAG, "LastUTI: "+lastReceivedUTI);
                if (lastReceivedUTI != null && lastReceivedUTI.length() > 0) {
                    Intent intent = new Intent(this, CustomActivity.class);
                    intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                    intent.putExtra("Screen_Title", "Enter Reversal Amount");
                    intent.putExtra("Trans_Type", REVERSE_BY_UTI);
                    startActivity(intent);
                } else {
                    Log.i(TAG, "NO UTI TO Run Reversal on");
                    Toast.makeText(this, "No UTI TO Run Reversal on", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case PREAUTH: {
                /* Send a request to run a transaction for 100 pence (in the configured app currency */
                Log.i(TAG, "Run PreAuth");
                Intent intent = new Intent(this, CustomActivity.class);
                intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                intent.putExtra("Screen_Title", "Enter PreAuth Amount");
                intent.putExtra("Trans_Type", PREAUTH);
                startActivity(intent);
                break;
            }

            case COMPLETION: {
                /* Send a request to run a transaction for 100 pence (in the configured app currency */
                Log.i(TAG, "Run Completion");
                Intent intent = new Intent(this, CustomActivity.class);
                intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                if (WebLinkIntegrate.enabled)
                    intent.putExtra("Screen_Title", "Enter Total Amount And ID For Completion");
                else
                    intent.putExtra("Screen_Title", "Enter Total Amount And RRN For Completion");
                intent.putExtra("Trans_Type", COMPLETION);
                startActivity(intent);
                break;
            }

            case QUERY_TRANSACTION: {
                /* Send a request to reverse a transaction by UTI */
                if (lastReceivedUTI != null && lastReceivedUTI.length() > 0) {
                    Log.i(TAG, "Run Query TransRec on:" + lastReceivedUTI);
                    HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                    args.put(CT_UTI, lastReceivedUTI);

                    if (WebLinkIntegrate.enabled) {
                        WebLinkIntegrate.queryTransaction(this, args);
                    }
                    else
                        PosIntegrate.queryTransaction(this, args);

                } else {
                    Log.i(TAG, "NO UTI TO Run Query on");
                    Toast.makeText(this, "No UTI TO Run Query on", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case CANCEL_TRANSACTION: {


                if (WebLinkIntegrate.enabled) {
                    Toast.makeText(this, "Not Supported in WebLink mode", Toast.LENGTH_SHORT).show();
                    break;
                }

                Intent intent = new Intent(this, CustomActivity.class);
                intent.putExtra("fragment_to_launch", INPUT_AMOUNT_FRAGMENT);
                intent.putExtra("Screen_Title", "Enter Sale Amount And The Time After Which You Want Cancel Transaction");
                intent.putExtra("Trans_Type", CANCEL_TRANSACTION);
                startActivity(intent);

                break;
            }

            case PRINT_X: {

                if (WebLinkIntegrate.enabled) {
                    Toast.makeText(this, "Not Supported in WebLink mode", Toast.LENGTH_SHORT).show();
                    break;
                }
                HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                args.put(CT_XREPORT, "true");
                PosIntegrate.executeReport(this, TRANSACTION_TYPE_RECONCILIATION, args);
                break;
            }

            case PRINT_Z: {

                if (WebLinkIntegrate.enabled) {
                    Toast.makeText(this, "Not Supported in WebLink mode", Toast.LENGTH_SHORT).show();
                    break;
                }
                HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                args.put(CT_ZREPORT, "true");
                PosIntegrate.executeReport(this,TRANSACTION_TYPE_RECONCILIATION, args);
                break;

            }

            case PRINT_HISTORY: {

                if (WebLinkIntegrate.enabled) {
                    Toast.makeText(this, "Not Supported in WebLink mode", Toast.LENGTH_SHORT).show();
                    break;
                }

                HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                args.put(CT_HISTORYREPORT, "true");
                PosIntegrate.executeReport(this, TRANSACTION_TYPE_RECONCILIATION, args);
                break;
            }
            case EXIT: {
                Log.i(TAG, "Exit");
                System.exit(0);
                break;
            }
        }
    }


    public class TransType{
        private String tranactionName;

    }

}


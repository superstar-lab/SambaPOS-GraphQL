package com.eft.positivelauncher.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.eft.positivelauncher.R;
import com.eft.libpositive.PosIntegrate;
import com.eft.positivelauncher.TransactionResponse;
import com.eft.positivelauncher.activities.MainActivity;
import com.eft.positivelauncher.weblink.WebLinkIntegrate;

import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_AMOUNT_CASHBACK;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_LANGUAGE;
import static com.eft.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_REVERSAL;
import static com.eft.positivelauncher.activities.CustomActivity.hideKeyboard;
import static com.eft.positivelauncher.activities.MainActivity.CANCEL_TRANSACTION;
import static com.eft.positivelauncher.activities.MainActivity.COMPLETION;
import static com.eft.positivelauncher.activities.MainActivity.PREAUTH;
import static com.eft.positivelauncher.activities.MainActivity.REFUND;
import static com.eft.positivelauncher.activities.MainActivity.REVERSE_BY_UTI;
import static com.eft.positivelauncher.activities.MainActivity.REVERSE_LAST;
import static com.eft.positivelauncher.activities.MainActivity.SALE;
import static com.eft.positivelauncher.activities.MainActivity.lastReceivedUTI;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_AMOUNT;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_CANCELLED_TIMEOUT;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_RRN;
import static com.eft.libpositive.PosIntegrate.CONFIG_TYPE.CT_UTI;
import static com.eft.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_COMPLETION;
import static com.eft.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_PREAUTH;
import static com.eft.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_REFUND;
import static com.eft.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_SALE;

public class InputAmountFragment extends Fragment {

    private String screenTitle;
    private int trans_Type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            screenTitle = this.getArguments().getString("Screen_Title");
            trans_Type = this.getArguments().getInt("Trans_Type");
            // Inflate the layout for this fragment

            return inflater.inflate(R.layout.input_amount_fragment, container, false);
        }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        final EditText editTextAmount = view.findViewById(R.id.ed_enter_amount);
        final EditText editTextRRN = view.findViewById(R.id.ed_enter_rrn_number);
        final EditText editTextCancelTime = view.findViewById(R.id.ed_enter_cancel_time);

        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        if (WebLinkIntegrate.enabled) {
            if(trans_Type == COMPLETION || trans_Type == REFUND) {
                editTextRRN.setVisibility(View.VISIBLE);
                editTextRRN.setHint("Enter ID Here");
                if (MainActivity.lastReceivedRRN != null && MainActivity.lastReceivedRRN.length() > 0)
                    editTextRRN.setText(MainActivity.lastReceivedRRN);
                if (MainActivity.lastReceivedAmount != null && MainActivity.lastReceivedAmount.length() > 0)
                    editTextAmount.setText(MainActivity.lastReceivedAmount);
                editTextRRN.setInputType(InputType.TYPE_CLASS_TEXT );

            } else if (trans_Type == REVERSE_LAST || trans_Type == REVERSE_BY_UTI) {
                if (MainActivity.lastReceivedAmount != null && MainActivity.lastReceivedAmount.length() > 0)
                    editTextAmount.setText(MainActivity.lastReceivedAmount);
            }
            //if(trans_Type == CANCEL_TRANSACTION) {
            //    editTextCancelTime.setVisibility(View.VISIBLE);
            //}
        } else {
            if(trans_Type == COMPLETION) {
                editTextRRN.setVisibility(View.VISIBLE);
            }
            if(trans_Type == CANCEL_TRANSACTION) {
                editTextCancelTime.setVisibility(View.VISIBLE);
            }
        }


        TextView textViewTitle = view.findViewById(R.id.tv_enter_amount);
        textViewTitle.setText(screenTitle);

        Button nextButton = view.findViewById(R.id.button_submit);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide Keyboard
                hideKeyboard(view.getContext());
                HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                args.put(CT_AMOUNT, editTextAmount.getText().toString());
                //args.put(CT_AMOUNT_CASHBACK, editTextAmount.getText().toString());
                args.put(CT_LANGUAGE, "en_GB");

                if (WebLinkIntegrate.enabled) {
                    switch (trans_Type) {
                        case SALE:
                            WebLinkIntegrate.executeTransaction(getActivity(), "SALE", args);
                            break;
                        case REFUND:
                            args.put(CT_RRN, editTextRRN.getText().toString());
                            WebLinkIntegrate.executeTransaction(getActivity(), "REFUND", args);
                            break;
                        case REVERSE_LAST:
                            WebLinkIntegrate.executeTransaction(getActivity(), "REVERSAL", args);
                            break;
                        case REVERSE_BY_UTI:
                            args.put(CT_UTI, lastReceivedUTI);
                            PosIntegrate.executeReversal(getActivity(), args);
                            break;
                        case PREAUTH:
                            WebLinkIntegrate.executeTransaction(getActivity(), "PREAUTH", args);
                            break;
                        case COMPLETION:
                            args.put(CT_RRN, editTextRRN.getText().toString());
                            WebLinkIntegrate.executeTransaction(getActivity(), "COMPLETION", args);
                            break;
                        case CANCEL_TRANSACTION:
                            args.put(CT_CANCELLED_TIMEOUT, editTextCancelTime.getText().toString());
                            PosIntegrate.executeTransaction(getActivity(), TRANSACTION_TYPE_SALE, args);
                            break;
                    }
                } else {

                    switch (trans_Type) {
                        case SALE:
                            PosIntegrate.executeTransaction(getActivity(), TRANSACTION_TYPE_SALE, args);
                            break;
                        case REFUND:
                            PosIntegrate.executeTransaction(getActivity(), TRANSACTION_TYPE_REFUND, args);
                            break;
                        case REVERSE_LAST:
                            PosIntegrate.executeReversal(getActivity(), args);
                            break;
                        case REVERSE_BY_UTI:
                            args.put(CT_UTI, lastReceivedUTI);
                            PosIntegrate.executeReversal(getActivity(), args);
                            break;
                        case PREAUTH:
                            PosIntegrate.executeTransaction(getActivity(), TRANSACTION_TYPE_PREAUTH, args);
                            break;
                        case COMPLETION:
                            args.put(CT_RRN, editTextRRN.getText().toString());
                            PosIntegrate.executeTransaction(getActivity(), TRANSACTION_TYPE_COMPLETION, args);
                            break;
                        case CANCEL_TRANSACTION:
                            args.put(CT_CANCELLED_TIMEOUT, editTextCancelTime.getText().toString());
                            PosIntegrate.executeTransaction(getActivity(), TRANSACTION_TYPE_SALE, args);
                            break;
                    }
                }

            }
        });
    }
    
}

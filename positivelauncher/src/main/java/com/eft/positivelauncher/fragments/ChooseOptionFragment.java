package com.eft.positivelauncher.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import com.eft.positivelauncher.R;
import com.eft.positivelauncher.TransactionResponse;

public class ChooseOptionFragment extends Fragment implements View.OnClickListener {

    private String screenTitle;
    private String responseScreenTitle;
    private int trans_Type;
    Button btnViewTransResults;
    Button btnViewStatusEvents;

    ArrayList<TransactionResponse> responseList;
    ArrayList<TransactionResponse> statusResponseList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        responseList = getArguments().getParcelableArrayList("TransResponseList");
        statusResponseList = getArguments().getParcelableArrayList("StatusList");
        responseScreenTitle = getArguments().getString("Title");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        screenTitle = this.getArguments().getString("Choose One Option");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.choose_option_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        btnViewTransResults = view.findViewById(R.id.btn_trans_results);
        btnViewStatusEvents = view.findViewById(R.id.btn_status_events);
        if(null!= responseList && responseList.size()!=0) {
            btnViewTransResults.setOnClickListener(this);
            btnViewStatusEvents.setOnClickListener(this);
        }else{
            //Transaction Not found
            btnViewTransResults.setVisibility(View.GONE);
            btnViewStatusEvents.setVisibility(View.GONE);
            TextView screenTitle = view.findViewById(R.id.tv_screen_title);
            screenTitle.setText("Transaction Not Found");

        }

    }

    @Override
    public void onClick(View v) {
        Fragment fragmentToLaunch = new ResponseFragment();
        Bundle intent = new Bundle();
        switch (v.getId()) {
            case R.id.btn_trans_results:
                intent.putParcelableArrayList("ResponseList", responseList);
                intent.putString("Title", responseScreenTitle);
                break;
            case R.id.btn_status_events:
                intent.putParcelableArrayList("ResponseList", statusResponseList);
                intent.putString("Title", "Status Events");
                break;
        }
        fragmentToLaunch.setArguments(intent);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.custom_fragment, fragmentToLaunch).addToBackStack(null).commit();
    }
}

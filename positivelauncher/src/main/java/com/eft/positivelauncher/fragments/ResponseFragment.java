package com.eft.positivelauncher.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.eft.positivelauncher.R;
import com.eft.positivelauncher.adapters.ResponseAdapter;
import com.eft.positivelauncher.TransactionResponse;

public class ResponseFragment extends Fragment {
    ArrayList<TransactionResponse> responseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.display_response_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView txtTitle = view.findViewById(R.id.textView_Response);
        responseList = getArguments().getParcelableArrayList("ResponseList");
        String title = getArguments().getString("Title");
        if (title == null || title.length() == 0)
            title = "Waiting";
        txtTitle.setText(title);

        RecyclerView trans_result = view.findViewById(R.id.recycler_trans_results);
        trans_result.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration divider = new
                DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.
                getDrawable(getActivity().getBaseContext(), R.drawable.line_divider)
        );
        trans_result.addItemDecoration(divider);

        if (responseList == null) {
            responseList = new ArrayList<TransactionResponse>();
            responseList.add(new TransactionResponse("Waiting", ""));
        }
        ResponseAdapter responseAdapter = new ResponseAdapter(getActivity(), responseList);
        trans_result.setAdapter(responseAdapter);

    }

}

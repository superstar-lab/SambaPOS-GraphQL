package com.eft.positivelauncher.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.eft.positivelauncher.R;
import com.eft.positivelauncher.TransactionResponse;

public class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.MyViewHolder>  {
    private ArrayList<TransactionResponse> mTransList;
    private LayoutInflater mInflater;
    private TransAdapter.ItemClickListener mClickListener;


    // data is passed into the constructor
    public ResponseAdapter(Context context, ArrayList<TransactionResponse> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mTransList = data;
    }


    @Override
    public ResponseAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycl_trans_response_row, parent, false);
        return new ResponseAdapter.MyViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ResponseAdapter.MyViewHolder holder, int position) {
        TransactionResponse transType = mTransList.get(position);
        holder.textTransName.setText(transType.getTransResponseName());
        holder.textTransValue.setText(transType.getTransResponseValue());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mTransList == null)
            return 0;
        return mTransList.size();
    }



    // stores and recycles views as they are scrolled off screen
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textTransName;
        TextView textTransValue;

        MyViewHolder(View itemView) {
            super(itemView);
            textTransName = itemView.findViewById(R.id.tv_trans_response_name);
            textTransValue = itemView.findViewById(R.id.tv_trans_response_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(TransAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
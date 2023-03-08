package com.eft.positivelauncher.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.eft.positivelauncher.R;
import com.eft.positivelauncher.Transaction;

public class TransAdapter extends RecyclerView.Adapter<TransAdapter.MyViewHolder>  {
    private ArrayList<Transaction> mTransList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public TransAdapter(Context context, ArrayList<Transaction> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mTransList = data;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new MyViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Transaction transType = mTransList.get(position);
        holder.textTransType.setText(transType.getTransName());
        holder.imageTransType.setImageResource(transType.getTransDrawable());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return  mTransList == null ? 0 : mTransList.size();
    }



    // stores and recycles views as they are scrolled off screen
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textTransType;
        ImageView imageTransType;

        MyViewHolder(View itemView) {
            super(itemView);
            textTransType = itemView.findViewById(R.id.tv_trans_types);
            imageTransType = itemView.findViewById(R.id.imageView_trans_type);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}




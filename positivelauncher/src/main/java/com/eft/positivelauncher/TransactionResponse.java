package com.eft.positivelauncher;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;

public class TransactionResponse implements Parcelable {
    @Getter
    private String transResponseName;
    @Getter
    private String transResponseValue;


    protected TransactionResponse(Parcel in) {
        transResponseName = in.readString();
        transResponseValue = in.readString();
    }

    public static final Creator<TransactionResponse> CREATOR = new Creator<TransactionResponse>() {
        @Override
        public TransactionResponse createFromParcel(Parcel in) {
            return new TransactionResponse(in);
        }

        @Override
        public TransactionResponse[] newArray(int size) {
            return new TransactionResponse[size];
        }
    };

    public TransactionResponse(String transResponseName, String transResponseValue) {
        this.transResponseName = transResponseName;
        this.transResponseValue = transResponseValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transResponseName);
        dest.writeString(transResponseValue);
    }
}

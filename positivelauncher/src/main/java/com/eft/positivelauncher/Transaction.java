package com.eft.positivelauncher;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public class Transaction {
    @Getter
    private int mTransDrawable;
    @Getter
    private String mTransName;

    public Transaction(int mTransDrawable, String mTransName) {
        this.mTransDrawable = mTransDrawable;
        this.mTransName = mTransName;
    }

}

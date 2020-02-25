package com.niharika.android.groupexpensetracker;

import java.util.Date;

public class Transaction {
    private String mTid;
    private Double mValue;
    private Long mDate;
    private String mType, mPayName, mCategory, mPaymentMethod;
    private String mMemberId;
    private String mDescription;

    public Transaction() {

    }
    public Transaction(String id) {
        mTid = id;
        mDate = new Date().getTime();
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getTid() {
        return mTid;
    }

    public void setTid(String tid) {
        mTid = tid;
    }

    public String getPayName() {
        return mPayName;
    }

    public void setPayName(String payer) {
        mPayName = payer;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getPaymentMethod() {
        return mPaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        mPaymentMethod = paymentMethod;
    }

    public String getMemberId() {
        return mMemberId;
    }

    public void setMemberId(String memberId) {
        mMemberId = memberId;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public Double getValue() {
        return mValue;
    }

    public void setValue(Double value) {
        mValue = value;
    }

    public Long getDate() {
        return mDate;
    }

    public void setDate(Long date) {
        mDate = date;
    }


    public String getDescription() {
        return mDescription;
    }

}
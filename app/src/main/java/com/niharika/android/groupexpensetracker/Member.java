package com.niharika.android.groupexpensetracker;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Member {
    private String mPassword;
    private String mMemberId;
    private String mMemberName;
    private String mUserId; //user id assigned by by firebase
    private String mMobNo;
    private String mEmailId;
    private String mAddress;
    private Boolean mLoggedIn;
    private Date registerDate;

    public String getUserId() {
        return mUserId;
    }
    public void setUserId(String userId) {
        mUserId = userId;
    }
    public Member() { }

    public Boolean getLoggedIn() {
        return mLoggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        mLoggedIn = loggedIn;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }
    public Member(String id) {
        mMemberId=id;
        registerDate=new Date();
    }
    Member (String memberID,String username,String password){
        mMemberId=memberID;
        if(TextUtils.isDigitsOnly(username.substring(1)))
            mMobNo=username;
        else
        mEmailId=username;
        mPassword=password;
        registerDate=new Date();
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getMemberId() {
        return mMemberId;
    }

    public void setMemberId(String memberId) {
        mMemberId = memberId;
    }

    public String getMemberName() {
        return mMemberName;
    }

    public void setMemberName(String memberName) {
        mMemberName = memberName;
    }

    public String getMobNo() {
        return mMobNo;
    }

    public void setMobNo(String mobNo) {
        mMobNo = mobNo;
    }

    public String getEmailId() {
        return mEmailId;
    }

    public void setEmailId(String emailId) {
        mEmailId = emailId;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }


    public String getDisplayName() {
        if (getMemberName() != null)
            return getMemberName();
        else if (getEmailId() != null)
            return getEmailId();
        else
            return getMobNo();
    }
}

package com.niharika.android.groupexpensetracker;
public class Account_Member {
    private String mAccId;
    private String mMemberId;
    private String mRole;

    public Account_Member() {
    }

    public Account_Member(String accId, String memberId, String role) {
        mMemberId = memberId;
        mRole = role;
        mAccId=accId;
    }
    public String getMemberId() {
        return mMemberId;
    }

    public void setMemberId(String memberId) {
        mMemberId = memberId;
    }

    public String getRole() {
        return mRole;
    }

    public void setRole(String role) {
        mRole = role;
    }

    public String getAccId() {
        return mAccId;
    }

    public void setAccId(String accId) {
        mAccId = accId;
    }
}

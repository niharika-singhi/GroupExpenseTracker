package com.niharika.android.groupexpensetracker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by niharika on 3/13/2019.
 */

public class Account {
    private String mAccNo;
    private String mAccName;
    private String mDescription;
    private boolean mDefaultAccount = false;
    private Date mDate;
    private String mCurrency;
    private String mCreatorId;
    private DatabaseReference databaseRef;
    private ArrayList<Member> mMemberList;
    private ArrayList<Transaction> mTransactionList;

    public Account() {
        mDate = new Date();
        mMemberList = new ArrayList<Member>();
        mTransactionList = new ArrayList<Transaction>();
    }

    public Account(String id) {
        mAccNo = id;
        mDate = new Date();
        mMemberList = new ArrayList<Member>();
        mTransactionList = new ArrayList<Transaction>();
    }

    public List<String> extractMemberIds(ArrayList<Transaction> transList) {
        List<String> memList = new ArrayList<String>();
        for (int i = 0; i < transList.size(); i++) {
            memList.add(transList.get(i).getMemberId());
        }
        return memList;
    }

    public List<String> extractMemberName(ArrayList<Member> memberList) {
        List<String> memList = new ArrayList<String>();
        for (int i = 0; i < memberList.size(); i++) {
            Member m = memberList.get(i);
            if (m.getMemberName() != null)
                memList.add(m.getMemberName());
            else if (m.getEmailId() != null)
                memList.add(m.getEmailId());
            else
                memList.add(m.getMobNo());
        }
        return memList;
    }


    public interface FirebaseCallbackMember {
        void onCallback(ArrayList<Member> memberList);
    }

    public interface FirebaseCallbackMemberIds {
        void onCallbackMemberIds(List<String> memberList, String adminId);
    }

    public boolean isDefaultAccount() {
        return mDefaultAccount;
    }

    public void setDefaultAccount(boolean def) {
        mDefaultAccount = def;
    }

    public String getCreatorId() {
        return mCreatorId;
    }

    public void setCreatorId(String creatorId) {
        mCreatorId = creatorId;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }


    public String getAccName() {
        return mAccName;
    }

    public void setAccName(String accName) {
        mAccName = accName;
    }

    public String getAccNo() {
        return mAccNo;
    }

    public void setAccNo(String accNo) {
        mAccNo = accNo;
    }

    public void addMemberToAccount(String userId, String role) {
        databaseRef = FirebaseDatabase.getInstance().getReference("account_member").child(getAccNo());
        databaseRef.child(userId).setValue(new Account_Member(getAccNo(), userId, role));
        databaseRef = FirebaseDatabase.getInstance().getReference("member_account").child(userId);
        databaseRef.child(getAccNo()).setValue(new Account_Member(getAccNo(), userId, role));
    }

    public void updateMember(Member mNew) {
        databaseRef = FirebaseDatabase.getInstance().getReference("member").child(mNew.getMemberId());
        databaseRef.setValue(mNew);
    }

    protected void delAllMembersAccount() {
        for (Member member : mMemberList) {
            delMember(member.getMemberId());
        }
    }

    public void getMemberIds(final FirebaseCallbackMemberIds firebaseCallbackMemberIds) {

        final List<String> account_memberList = new ArrayList<String>();
        final Query query = FirebaseDatabase.getInstance().getReference("account_member").child(getAccNo());
        query.addValueEventListener(new ValueEventListener() {
                                        String admin = null;

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            account_memberList.clear();
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                Account_Member acc_member = postSnapshot.getValue(Account_Member.class);
                                                if (acc_member.getRole().equals("ADMIN"))
                                                    admin = acc_member.getMemberId();
                                                account_memberList.add(acc_member.getMemberId());
                                            }
                                            query.removeEventListener(this);
                                            firebaseCallbackMemberIds.onCallbackMemberIds(account_memberList, admin);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    }
        );

    }


    void getMembers(final FirebaseCallbackMember firebaseCallbackMember, final List<String> acc_memList) {
        mMemberList.clear();
        for (int i = 0; i < acc_memList.size(); i++) {
            final String acc_member = acc_memList.get(i);
            final Query query = FirebaseDatabase.getInstance().getReference("member")
                    .orderByKey().equalTo(acc_member);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Member member = postSnapshot.getValue(Member.class);
                        mMemberList.add(member);
                    }
                    if (mMemberList.size() == acc_memList.size()) {
                        query.removeEventListener(this);
                        firebaseCallbackMember.onCallback(mMemberList);
                        return;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (BuildConfig.DEBUG)
                        Log.d(MainFragment.TAG, "There was error reading data" + databaseError);
                }
            });
        }
    }

    public void delMember(String memberId) {
        databaseRef = FirebaseDatabase.getInstance().getReference("account_member").child(getAccNo()).child(memberId);
        databaseRef.removeValue();
        databaseRef = FirebaseDatabase.getInstance().getReference("member_account").child(memberId).child(getAccNo());
        databaseRef.removeValue();
        //mMemberList.remove(position);
        //this fn is not deleting data from member table
    }

    public Member getMember(String userId) {
        Member member;
        for (int i = 0; i < mMemberList.size(); i++) {
            member = (Member) mMemberList.get(i);
            if (member.getMemberId().equals(userId))
                return member;

        }
        return null;
    }

    protected boolean checkDelTransactionPermissions(String currentMemberId, Transaction transaction) {

        if (getCreatorId().equals(currentMemberId)) {
            if (transaction.getType().equals("IT"))
                return false;
            else if (transaction.getType().equals("ET"))
                return false;
            return true;
        } else if (transaction.getMemberId().equals(currentMemberId)) {
            if (transaction.getType().equals("IT"))
                return false;
            else if (transaction.getType().equals("ET"))
                return false;
            return true;
        }
        return false;
    }


}


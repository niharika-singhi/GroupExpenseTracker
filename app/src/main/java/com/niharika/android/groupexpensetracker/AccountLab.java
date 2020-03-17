package com.niharika.android.groupexpensetracker;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by niharika on 3/13/2019.
 */

public class AccountLab {
    private static AccountLab sAccountLab;
    private Context mContext;
    ArrayList<Account> mAccounts;
    DatabaseReference databaseRef;
    private Boolean loaded = false;
    private Member mUser;
    private String mRegMsg = "Hi,\n\nWelcome to Team Expenses.Track your expenses for free." +
            "\n\n\nTeam\nExpenses",
            mNewMemberMsg = " has invited you to install app Team Expenses.Track your expenses for free." +
                    "\nSteps -\n\t1. Install the app from the following link-" +
                    "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + " \n\t"
                    + "2. Register with your email id.\n\n\nTeam\nExpenses";

    private AccountLab(Context context) {
        //mContext = context; for testing mockito
        mContext = context.getApplicationContext();
        mAccounts = new ArrayList<Account>();
    }

    public static AccountLab get(Context context) {
        if (sAccountLab == null)
            sAccountLab = new AccountLab(context);
        return sAccountLab;
    }

    //for testing mockito
    String getAppName(){
        return mContext.getString(R.string.app_title);

    }

    public String getNewMemberNotificationMsg(String accName, String newMemberName) {
        return getUser().getDisplayName()
                +" has added member "+newMemberName
                +"to account "+accName;
    }

    public String getTransferNotificationMsg(String accName, Double value) {
        return getUser().getDisplayName()+"("+accName+")"+" has transferred "+value.toString();
    }


    protected interface FirebaseCallbackAccounts {
        void onCallback(ArrayList<Account> accountList);
    }

    protected interface FirebaseCallbackSignIn {
        void onCallback(Boolean status);
    }

    protected interface FirebaseCallback {
        void onCallback(Member oldMember);
    }

    protected interface FirebaseCallbackDevice {
        void onCallback(ArrayList<DeviceToken> deviceList);
    }

    protected interface FirebaseCallbackLoadTransaction {
        void onCallback(ArrayList<Transaction> transList);
    }

    protected interface FirebaseCallbackCalculateTransaction {
        void onCallback(Double value);
    }

    protected interface FirebaseCallbackTransfer {
        void onCallback(List<Transfer> transferList);
    }

    protected interface FirebaseCallbackTransferTo {
        void onCallback(Transaction transaction);
    }

    protected interface FirebaseCallbackTransferredTo {
        void onCallback(Transfer transfer);
    }

    protected interface FirebaseCallbackAccoutIds {
        void onCallback(ArrayList<Account_Member> accountIdsList);
    }

    protected void delAccountLab() {
        sAccountLab = null;
    }

    protected void setAccounts(ArrayList<Account> accounts) {
        mAccounts = accounts;
    }

    protected Boolean isLoaded() {
        return loaded;
    }

    protected void setLoaded(Boolean loaded) {
        this.loaded = loaded;
    }

    protected Member getUser() {
        return mUser;
    }

    protected void setUser(Member user) {
        mUser = user;
    }

    protected void setDefaultAccount(final String ano) {
        Account account;
        for (int i = 0; i < mAccounts.size(); i++) {
            account = (Account) mAccounts.get(i);
            if (!account.getAccNo().equals(ano)) {
                mAccounts.get(i).setDefaultAccount(false);
                DatabaseReference updateAcc = FirebaseDatabase.getInstance().getReference("account").child(mUser.getMemberId()).child(account.getAccNo());
                updateAcc.child("defaultAccount").setValue(false);
            }
        }
    }

    protected void addAccount(Account account) {
        databaseRef = FirebaseDatabase.getInstance().getReference("account");
        account.setCreatorId(mUser.getMemberId());
        databaseRef.child(account.getAccNo()).setValue(account);
        account.addMemberToAccount(mUser.getMemberId(), "ADMIN");
        mAccounts.add(account);
        if (account.isDefaultAccount())
            setDefaultAccount(account.getAccNo());
    }

    protected ArrayList<Account> getAccounts() {
        return mAccounts;
    }

    protected void loadAccounts(final FirebaseCallbackAccounts firebaseCallbackAccounts) {
        databaseRef = FirebaseDatabase.getInstance().getReference("member_account").child(mUser.getMemberId());
        databaseRef.addValueEventListener(new ValueEventListener() {
            private ArrayList<Account_Member> member_accountList = new ArrayList<Account_Member>();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                member_accountList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Account_Member account_member = postSnapshot.getValue(Account_Member.class);
                    member_accountList.add(account_member);
                }
                databaseRef.removeEventListener(this);
                if (dataSnapshot.getChildrenCount() == 0) {
                    mAccounts.clear();
                    firebaseCallbackAccounts.onCallback(mAccounts);
                    return;
                }
                mAccounts.clear();
                for (final Account_Member account_member : member_accountList) {
                    final Query query = FirebaseDatabase.getInstance().getReference("account").orderByKey().equalTo(account_member.getAccId());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Account account = postSnapshot.getValue(Account.class);
                                mAccounts.add(account);
                                if (mAccounts.size() == member_accountList.size()) {
                                    loaded = true;
                                    query.removeEventListener(this);
                                    firebaseCallbackAccounts.onCallback(mAccounts);
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError dE) {
                            if (BuildConfig.DEBUG)
                                Log.d(MainFragment.TAG, "There was a database error fetching data" + dE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError dE) {
                if (BuildConfig.DEBUG)
                    Log.d(MainFragment.TAG, "There was a database error fetching data" + dE);
            }
        });
    }

    protected Account getAccount(String ano) {
        Account account;
        for (int i = 0; i < mAccounts.size(); i++) {
            account = (Account) mAccounts.get(i);
            if (account.getAccNo().equals(ano))
                return account;
        }
        return null;
    }

    protected List<String> extractAccountName(ArrayList<Account> accList) {
        List<String> aList = new ArrayList<String>();
        for (int i = 0; i < accList.size(); i++) {
            aList.add(accList.get(i).getAccName());
        }
        return aList;
    }

    protected int getAccountsSize() {
        if (mAccounts != null)
            return mAccounts.size();
        else
            return 0;
    }

    protected String getNewAcctId() {
        databaseRef = FirebaseDatabase.getInstance().getReference("account");
        String accNo = databaseRef.push().getKey();
        return accNo;
    }

    protected void updateAccount(Account account) {
        if (account.isDefaultAccount() == true)
            setDefaultAccount(account.getAccNo());
        databaseRef = FirebaseDatabase.getInstance().getReference("account").child(mUser.getMemberId()).
                child(account.getAccNo());
        databaseRef.setValue(account);
        int i = mAccounts.indexOf(getAccount(account.getAccNo()));
        mAccounts.set(i, account);
    }

    protected void deleteAccount(Account account) {
        deleteAllTransaction(account.getAccNo());
        account.delAllMembersAccount();
        databaseRef = FirebaseDatabase.getInstance().getReference("account").
                child(account.getAccNo());
        databaseRef.removeValue();
        mAccounts.remove(account);
        delTransferTransaction(account.getAccNo());
    }

    private void delTransferTransaction(String accNo) {
        databaseRef = FirebaseDatabase.getInstance().getReference("transfer").child(accNo);
        databaseRef.removeValue();
    }

    protected void deleteTransaction(Transaction transaction, String accNo) {
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction").child(accNo).child(transaction.getTid());
        databaseRef.removeValue();
    }

    protected void deleteAllTransaction(String accNo) {
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction").child(accNo);
        databaseRef.removeValue();
    }

    protected void updateMember(Member member) {

        databaseRef = FirebaseDatabase.getInstance().getReference("member").child(member.getMemberId());
        databaseRef.setValue(member);
    }

    protected void getMember(final FirebaseCallback firebaseCallback, String memberId) {
        final Query query = FirebaseDatabase.getInstance().getReference("member").orderByKey().equalTo(memberId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Member member = null;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    member = postSnapshot.getValue(Member.class);
                }
                query.removeEventListener(this);
                firebaseCallback.onCallback(member);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (BuildConfig.DEBUG)
                    Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
            }
        });
    }

    protected void isLoggedIn(boolean userLoggedIn) {
        databaseRef = FirebaseDatabase.getInstance().getReference("member").child(mUser.getMemberId()).child("mLoggedIn");
        databaseRef.setValue(userLoggedIn);
    }

    protected String getNewMemberId() {
        databaseRef = FirebaseDatabase.getInstance().getReference("member");
        String memberId = databaseRef.push().getKey();
        return memberId;
    }

    protected void addMember(Member member) {
        databaseRef = FirebaseDatabase.getInstance().getReference("member");
        databaseRef.child(member.getMemberId()).setValue(member);
    }

    protected void checkUserExist(final FirebaseCallback firebaseCallback, String login, String typeofLogin) {
        databaseRef = FirebaseDatabase.getInstance().getReference("member");
        final Query query;
        if (typeofLogin.equals("F"))
            typeofLogin = getModeOfLogin(login);

        if (typeofLogin.equals("E") || typeofLogin.equals("G"))
            query = databaseRef.orderByChild("emailId").equalTo(login);
        else
            query = databaseRef.orderByChild("mobNo").equalTo(login);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Member oldMember = null;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    oldMember = postSnapshot.getValue(Member.class);
                }
                query.removeEventListener(this);
                firebaseCallback.onCallback(oldMember);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (BuildConfig.DEBUG)
                    Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
            }
        });
    }

    protected void checkDuplicateUsername(final FirebaseCallback firebaseCallback, final String login, final String memberId, String typeOfLogin) {
        databaseRef = FirebaseDatabase.getInstance().getReference("member");
        final Query query;
        if (typeOfLogin.equals("E"))
            query = databaseRef.orderByChild("emailId").equalTo(login);
        else
            query = databaseRef.orderByChild("mobNo").equalTo(login);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Member oldMember = postSnapshot.getValue(Member.class);
                    if (!oldMember.getMemberId().equals(memberId)) {
                        query.removeEventListener(this);
                        firebaseCallback.onCallback(oldMember);
                        return;
                    }
                }
                query.removeEventListener(this);
                firebaseCallback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (BuildConfig.DEBUG)
                    Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
            }
        });
    }

    protected String getNewTransId() {
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction");
        String tNo = databaseRef.push().getKey();
        return tNo;
    }

    protected void addTransaction(Account account, Transaction t) {
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo());
        databaseRef.child(t.getTid()).setValue(t);
    }

    protected void addInitialTransaction(Account account, double incomeValue) {
        Transaction t = new Transaction(getNewTransId());
        t.setType("I");
        t.setValue(incomeValue);
        t.setDescription("Initial Balance");
        t.setCategory("Income");
        t.setMemberId(getUser().getMemberId());
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo());
        databaseRef.child(t.getTid()).setValue(t);
    }

    void loadTransactionList(Account account, final FirebaseCallbackLoadTransaction firebaseCallbackLoadTransaction) {
        final List mTransactionList = new ArrayList<Transaction>();
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo());
        databaseRef.addValueEventListener(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                  mTransactionList.clear();
                                                  for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                      Transaction transaction = postSnapshot.getValue(Transaction.class);
                                                      mTransactionList.add(transaction);
                                                  }
                                                  databaseRef.removeEventListener(this);
                                                  firebaseCallbackLoadTransaction.onCallback((ArrayList<Transaction>) mTransactionList);
                                              }

                                              @Override
                                              public void onCancelled(@NonNull DatabaseError databaseError) {
                                                  if (BuildConfig.DEBUG)
                                                      Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                              }
                                          }
        );

    }

    protected void getTransaction(Account account, final FirebaseCallbackTransferTo firebaseCallbackTransferTo, String id) {
        final Query query = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo()).orderByChild("tid").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
                                        Transaction transaction;

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                transaction = postSnapshot.getValue(Transaction.class);
                                            }
                                            query.removeEventListener(this);
                                            firebaseCallbackTransferTo.onCallback(transaction);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                        }
                                    }
        );
    }

    protected Double getTotalIncome(List<Transaction> mTransactionList, Date targetDate) {
        Double totalIncome = 0.0;
        for (int i = 0; i < mTransactionList.size(); i++) {
            Transaction t = mTransactionList.get(i);
            Date transDate = new Date(t.getDate());
            if (t.getType().startsWith("I")) {
                if (targetDate != null)
                    if (!transDate.before(targetDate) && !(transDate.compareTo(targetDate) == 0)) {//skip
                    } else
                        totalIncome += t.getValue();
            }
        }
        return totalIncome;
    }

    protected void getTotalIncome(Account account, final FirebaseCallbackCalculateTransaction firebaseCallbackCalculateTransaction,
                                  final String period, final String memberId) {
        final Query query;
        if (memberId != null)
            query = FirebaseDatabase.getInstance().getReference("transaction").
                    child(account.getAccNo()).orderByChild("memberId").equalTo(memberId);
        else
            query = FirebaseDatabase.getInstance().getReference("transaction")
                    .child(account.getAccNo()).orderByChild("type").startAt("I");
        query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Double totalIncome = 0.0;
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                if ((postSnapshot.child("type").getValue(String.class)).startsWith("I")) {
                                                    if (period.equals("A"))//A means all
                                                        totalIncome += postSnapshot.child("value").getValue(Double.class);
                                                    else {
                                                        Long time = postSnapshot.child("date").getValue(Long.class);
                                                        Date date;
                                                        if (time != null) {
                                                            date = new Date(time);
                                                            if (dateInPeriod(date, period)) {
                                                                totalIncome += postSnapshot.child("value").getValue(Double.class);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            query.removeEventListener(this);
                                            firebaseCallbackCalculateTransaction.onCallback(totalIncome);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                        }
                                    }
        );
    }

    protected Double getTotalExpense(List<Transaction> mTransactionList, Date targetDate) {
        Double totalExpense = 0.0;
        for (int i = 0; i < mTransactionList.size(); i++) {
            Transaction t = mTransactionList.get(i);
            Date transDate = new Date(t.getDate());
            if (t.getType().startsWith("E")) {
                if (targetDate != null)
                    if (!transDate.before(targetDate) && !(transDate.compareTo(targetDate) == 0)) {
                    } else
                        totalExpense += t.getValue();
            }
        }
        return totalExpense;
    }

    protected void getTotalExpense(Account account, final FirebaseCallbackCalculateTransaction firebaseCallbackCalculateTransaction, final String period, String memberId) {
        final Query query;
        if (memberId != null)
            query = FirebaseDatabase.getInstance().getReference("transaction").
                    child(account.getAccNo()).orderByChild("memberId").equalTo(memberId);
        else
            query = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo()).orderByChild("type").startAt("E")
                    .endAt("E\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Double totalExpense = 0.0;
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                if ((postSnapshot.child("type").getValue(String.class)).startsWith("E")) {
                                                    if (period.equals("A"))
                                                        totalExpense += postSnapshot.child("value").getValue(Double.class);
                                                    else {
                                                        Long time = postSnapshot.child("date").getValue(Long.class);
                                                        Date date;
                                                        if (time != null) {
                                                            date = new Date(time);
                                                            if (dateInPeriod(date, period)) {
                                                                totalExpense += postSnapshot.child("value").getValue(Double.class);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            query.removeEventListener(this);
                                            firebaseCallbackCalculateTransaction.onCallback(totalExpense);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                        }
                                    }
        );
    }

    //targetTime is not been used in the app til now
    protected void getBalance(Account account, final Long targetTime, final FirebaseCallbackCalculateTransaction firebaseCallbackCalculateTransaction) {
        final Query query;
        query = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo());
        query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Double total = 0.0;
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                if (targetTime != null) {
                                                    Long time = postSnapshot.child("date").getValue(Long.class);
                                                    Date date;
                                                    if (time != null) {
                                                        date = new Date(time);
                                                        if (!dateInPeriod(date, targetTime))
                                                            break;
                                                    }
                                                }
                                                if ((postSnapshot.child("type").getValue(String.class)).startsWith("E")) {
                                                    total -= postSnapshot.child("value").getValue(Double.class);
                                                } else {
                                                    total += postSnapshot.child("value").getValue(Double.class);
                                                }
                                            }
                                            query.removeEventListener(this);
                                            firebaseCallbackCalculateTransaction.onCallback(total);
                                            return;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                        }
                                    }
        );
    }

    protected boolean dateInPeriod(Date transDate, Long time) {
        Date targetDate = new Date(time);
        if (transDate.before(targetDate) || targetDate.compareTo(transDate) == 0) {
            return true;
        }
        return false;
    }

    protected boolean dateInPeriod(Date date, String period) {

        Calendar c;
        if (period.equals("M")) {
            c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            Date startDate = c.getTime();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DAY_OF_MONTH, -1);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            Date endDate = c.getTime();
            if ((date.after(startDate) || date.compareTo(startDate) == 0) && (date.before(endDate)
                    || date.compareTo(endDate) == 0)) {
                if (date.before(endDate)) {
                    return true;
                }
            }
        }
        if (period.equals("W")) {
            c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            Date startWeek = c.getTime();
            c.add(Calendar.DATE, 6);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            Date endWeek = c.getTime();
            if ((date.after(startWeek) || date.compareTo(startWeek) == 0) && (date.before(endWeek)
                    || date.compareTo(endWeek) == 0)) {
                if (date.before(endWeek)) {
                    return true;
                }
            }
        }
        if (period.equals("Y")) {
            c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            Date startYear = c.getTime();
            c.set(Calendar.MONTH, 11);
            c.set(Calendar.DAY_OF_MONTH, 31);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            Date endYear = c.getTime();
            if ((date.after(startYear) || date.compareTo(startYear) == 0) && (date.before(endYear)
                    || date.compareTo(endYear) == 0)) {
                if (date.before(endYear)) {
                    return true;
                }
            }
        }
        if (period.equals("T")) {
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.HOUR_OF_DAY, 0);
            c1.set(Calendar.SECOND, 59);
            Date d1 = c1.getTime();
            Calendar c2 = Calendar.getInstance();
            c2.set(Calendar.MINUTE, 59);
            c2.set(Calendar.HOUR_OF_DAY, 23);
            c2.set(Calendar.SECOND, 59);
            Date d2 = c2.getTime();
            if (date.after(d1) && date.before(d2) || date.compareTo(d1) == 0 || date.compareTo(d2) == 0) {
                return true;
            }
        }
        return false;
    }

    protected Double getAccountBalance(List<Transaction> mTransactionList, Date targetDate) {
        return getTotalIncome(mTransactionList, targetDate) - getTotalExpense(mTransactionList, targetDate);
    }

    protected String getNewTransferId() {
        databaseRef = FirebaseDatabase.getInstance().getReference("transfer");
        String tNo = databaseRef.push().getKey();
        return tNo;
    }

    protected void transfer(Account account, Transfer transfer) {
        databaseRef = FirebaseDatabase.getInstance().getReference("transfer").child(account.getAccNo());
        databaseRef.child(transfer.gettId()).setValue(transfer);
    }


    protected void getTransferredId(Account account, final FirebaseCallbackTransferredTo firebaseCallbackTransferredTo, String giverId) {
        final Query query = FirebaseDatabase.getInstance().getReference("transfer").child(account.getAccNo()).orderByChild("expenseId").equalTo(giverId);
        query.addValueEventListener(new ValueEventListener() {
                                        private String incomeId;

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Transfer transfer = null;
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                transfer = postSnapshot.getValue(Transfer.class);
                                                incomeId = transfer.getIncomeId();
                                            }
                                            query.removeEventListener(this);
                                            firebaseCallbackTransferredTo.onCallback(transfer);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                        }
                                    }
        );
    }

    protected void signInWithPhoneAndPassword(final FirebaseCallbackSignIn firebaseCallbackSignIn, String username, final String password, String typeOfLogin) {
        final Query query = FirebaseDatabase.getInstance().getReference("member").orderByChild("mobNo").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                 @Override
                                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                     for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                         Member member = postSnapshot.getValue(Member.class);
                                                         if (member.getPassword().equals(password))
                                                             firebaseCallbackSignIn.onCallback(true);
                                                         return;
                                                     }
                                                     query.removeEventListener(this);
                                                     firebaseCallbackSignIn.onCallback(false);
                                                     return;
                                                 }

                                                 @Override
                                                 public void onCancelled(@NonNull DatabaseError databaseError) {
                                                     if (BuildConfig.DEBUG)
                                                         Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);
                                                 }
                                             }
        );
    }

    protected boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    boolean checkPermissionRequired(String permission) {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfAlreadyhavePermission(String permission) {
        int result = ContextCompat.checkSelfPermission(mContext, permission);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;

    }

    protected String formatPhNumber(String phoneNumber) {
        String formattedNum;
        PhoneNumberUtil ph = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneProto = null;
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String locale = tm.getNetworkCountryIso().toUpperCase();
        if (TextUtils.isEmpty(locale))
            locale = "IN";
        try {
            phoneProto = ph.parse(phoneNumber, locale);
        } catch (NumberParseException e) {
            Log.d(MainFragment.TAG, "Exception" + e);
        }
        formattedNum = ph.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        return formattedNum;
    }

    protected boolean checkDelAccountPermissions(Account account) {
        if (account.getCreatorId().equals(getUser().getMemberId())) {
            return true;
        }
        return false;
    }

    public String getRegistrationMsg() {
        return mRegMsg;
    }

    protected String getNewMemberMsg() {
        return "Hi,\n Your friend " + getName() + mNewMemberMsg;
    }

    protected String getName() {
        String name = null;
        if (getUser().getMemberName() != null)
            name = getUser().getMemberName();
        else if (getUser().getEmailId() != null)
            name = getUser().getEmailId();
        else if (getUser().getMobNo() != null)
            name = getUser().getMobNo();
        return name;
    }

    protected String getModeOfLogin(String username) {
        if (TextUtils.isDigitsOnly(username.substring(1)))
            return "P";
        return "E";

    }

    protected void copyMembers(Member oldMember, Member newMember) {
        newMember.setMemberId(oldMember.getMemberId());
        if (newMember.getEmailId() == null)
            if (oldMember.getEmailId() != null)
                newMember.setEmailId(oldMember.getEmailId());
        if (newMember.getMobNo() == null)
            if (oldMember.getMobNo() != null)
                newMember.setMobNo(oldMember.getMobNo());
        if (newMember.getMemberName() == null)
            if (oldMember.getMemberName() != null)
                newMember.setMemberName(oldMember.getMemberName());
        if (newMember.getAddress() == null)
            if (oldMember.getAddress() != null)
                newMember.setAddress(oldMember.getAddress());
    }

    protected void updateTransaction(Account account, Transaction tNew) {
        databaseRef = FirebaseDatabase.getInstance().getReference("transaction").child(account.getAccNo()).child(tNew.getTid());
        databaseRef.setValue(tNew);
    }

    protected Transaction getTransaction(String tid, List<Transaction> mTransactionList) {
        Transaction transaction;
        for (int i = 0; i < mTransactionList.size(); i++) {
            transaction = (Transaction) mTransactionList.get(i);
            if (transaction.getTid().equals(tid))
                return transaction;
        }
        return null;
    }

    protected void getTransfers(Account account, final FirebaseCallbackTransfer firebaseCallbackTransfer) {
        final List transferList = new ArrayList<Transfer>();
        databaseRef = FirebaseDatabase.getInstance().getReference("transfer").child(account.getAccNo());
        databaseRef.addValueEventListener(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                  for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                      Transfer transfer = postSnapshot.getValue(Transfer.class);
                                                      transferList.add(transfer);
                                                  }
                                                  databaseRef.removeEventListener(this);
                                                  firebaseCallbackTransfer.onCallback(transferList);
                                              }

                                              @Override
                                              public void onCancelled(@NonNull DatabaseError databaseError) {
                                                  if (BuildConfig.DEBUG)
                                                      Log.d(MainFragment.TAG, "There was a database error fetching data" + databaseError);

                                              }
                                          }
        );
    }
    public void addToken(String newToken) {
        databaseRef = FirebaseDatabase.getInstance().getReference("device").child(newToken);
        databaseRef.setValue(new DeviceToken(newToken,getUser().getMemberId()));
    }

    //sends new Member Notifications
    public void sendNotifications(final Account account, final String msg) {
        account.getMemberIds(new Account.FirebaseCallbackMemberIds() {
            @Override
            public void onCallbackMemberIds(List<String> memberList, String adminId) {
                for(String m:memberList){
                    if(m.equals(adminId)){
                        memberList.remove(m);
                        break;
                    }
                }
                getTokens(new FirebaseCallbackDevice() {
                    @Override
                    public void onCallback(ArrayList<DeviceToken> deviceList) {
                        for(DeviceToken d:deviceList)
                            Log.d(MainFragment.TAG,d.getMemberId());
                        SendPushNotification sendPushNotification =
                                new SendPushNotification(mContext, deviceList,msg);
                        sendPushNotification.execute();

                    }
                },memberList);
            }
        });

    }

    //Sends transfer notification
    public void sendNotifications(final String receiverMemberId, final String msg) {
                ArrayList receiverMemberList=new ArrayList<String>();
                receiverMemberList.add(receiverMemberId);
                getTokens(new FirebaseCallbackDevice() {
                    @Override
                    public void onCallback(ArrayList<DeviceToken> deviceList) {
                        for(DeviceToken d:deviceList)
                            Log.d(MainFragment.TAG,d.getMemberId());
                        SendPushNotification sendPushNotification =
                                new SendPushNotification(mContext, deviceList,msg);
                        sendPushNotification.execute();

                    }
                },receiverMemberList);
            }


    private void getTokens(final FirebaseCallbackDevice firebaseCallbackDevice,final List<String> memberList) {
        final ArrayList <DeviceToken> deviceList=new ArrayList<>();
        for(int i=0;i<memberList.size();i++) {
            final Query query = FirebaseDatabase.getInstance().getReference("device")
                    .orderByChild("memberId").equalTo(memberList.get(i));
            final int finalI = i;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        DeviceToken device = postSnapshot.getValue(DeviceToken.class);
                        deviceList.add(device);
                    }
                    if ((finalI +1)== memberList.size()) {
                        query.removeEventListener(this);
                        firebaseCallbackDevice.onCallback(deviceList);
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


}

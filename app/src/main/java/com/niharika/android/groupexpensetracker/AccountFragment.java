package com.niharika.android.groupexpensetracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private static final String ARG_ACC_ID = "account_id", ARG_MEMBER_ID = "member_id", ARG_TRANS_TYPE = "transaction_type";
    private Account mAccount;
    private Button mExpenseButton, mTransactionsButton, mTransferButton, mIncomeButton, mSummaryButton;
    private String accId;
    private TextView mCurrentBalanceValue, mMonthBalanceValue, mTodayIncomeValue, mWeekIncomeValue, mMonthIncomeValue,
            mYearIncomeValue, mTodayExpenseValue, mWeekExpenseValue, mMonthExpenseValue, mYearExpenseValue, mMembersLink,
            mWeekBalanceValue, mYearBalanceValue, mTodayBalanceValue, mTodayDate, mWeekDate, mMonthDate, mYearDate;
    private RecyclerView mMemberListRecyclerView;
    private int positionClicked = -1, noMembers = 0, REQUEST_CONTACT = 0;
    private MemberAdapter mAdapter;
    private ImageButton mAddMemberContacts, mAddMember, mShowMembersButton;
    private LinearLayout mRecyclerViewColumnLayout, mLLProgress;
    private RelativeLayout mMembersLayout;
    private Boolean loadMembers = false, newMemberFlag = false;
    private static Boolean mButtonState = false;
    private Double mTotalIncome, mTotalExpense, mTodayIncome, mTodayExpense, mWeekIncome, mWeekExpense, mMonthlyIncome, mMonthlyExpense, mYearIncome, mYearExpense,
            mMemberExpenseValue, mMemberIncomeValue = 0.0;

    public static AccountFragment newInstance(String accId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACC_ID, accId);
        AccountFragment fragment = new AccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ACC_ID)) {
            accId = (String) getArguments().getSerializable(ARG_ACC_ID);
            mAccount = AccountLab.get(getActivity()).getAccount(accId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        mMembersLayout = view.findViewById(R.id.member_layout);
        mRecyclerViewColumnLayout = view.findViewById(R.id.recycler_view_columns_layout);
        mMemberListRecyclerView = (RecyclerView) view.findViewById(R.id.member_list_recycler_view);
        mMemberListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAddMemberContacts = (ImageButton) view.findViewById(R.id.add_member_imageButtonContacts);
        mAddMember = (ImageButton) view.findViewById(R.id.add_member_imageButton);
        mMembersLink = (TextView) view.findViewById(R.id.members_link);
        mIncomeButton = (Button) view.findViewById(R.id.add_income_button);
        mLLProgress = (LinearLayout) view.findViewById(R.id.llprogressbar);
        mSummaryButton = (Button) view.findViewById(R.id.summary_button);
        mShowMembersButton = (ImageButton) view.findViewById(R.id.show_members_button);
        mSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else
                    Navigation.findNavController(view).navigate(R.id.summaryFragment);
            }
        });
        mIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    bundle.putSerializable(ARG_TRANS_TYPE, getString(R.string.type_income));
                    Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_transactionFragment, bundle);
                }
            }
        });
        mExpenseButton = (Button) view.findViewById(R.id.add_expense_button);
        mExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    bundle.putSerializable(ARG_TRANS_TYPE, getString(R.string.type_expense));
                    Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_transactionFragment, bundle);
                }
            }
        });

        mTransactionsButton = (Button) view.findViewById(R.id.transaction_button);
        mTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_transactionListFragment, bundle);
                }
            }
        });
        mTransferButton = (Button) view.findViewById(R.id.transfer_income_button);
        mTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    bundle.putSerializable(ARG_TRANS_TYPE, getString(R.string.type_transfer));
                    Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_transactionFragment, bundle);
                }
            }
        });
        mCurrentBalanceValue = (TextView) view.findViewById(R.id.current_balance_value);
        mMonthBalanceValue = (TextView) view.findViewById(R.id.month_balance_value);
        mWeekBalanceValue = (TextView) view.findViewById(R.id.week_balance_value);
        mYearBalanceValue = (TextView) view.findViewById(R.id.yearToDate_balance_value);
        mTodayBalanceValue = (TextView) view.findViewById(R.id.today_balance_value);
        mTodayIncomeValue = (TextView) view.findViewById(R.id.today_income_value);
        mTodayDate = (TextView) view.findViewById(R.id.today_date_label);
        mWeekDate = (TextView) view.findViewById(R.id.week_date_label);
        mMonthDate = (TextView) view.findViewById(R.id.month_date_label);
        mYearDate = (TextView) view.findViewById(R.id.year_date_label);
        AccountLab.get(getActivity()).getTotalIncome(mAccount,
                new AccountLab.FirebaseCallbackCalculateTransaction() {
                    @Override
                    public void onCallback(Double value) {
                        mTodayIncome = value;
                        mTodayIncomeValue.setText(value.toString());
                    }
                }, "T", null);


        mWeekIncomeValue = (TextView) view.findViewById(R.id.week_income_value);
        AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mWeekIncome = value;
                mWeekIncomeValue.setText(value.toString());
            }
        }, "W", null);

        mMonthIncomeValue = (TextView) view.findViewById(R.id.month_income_value);
        AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mMonthlyIncome = value;
                mMonthIncomeValue.setText(value.toString());
            }
        }, "M", null);

        mYearIncomeValue = (TextView) view.findViewById(R.id.year_to_date_income_value);
        AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mYearIncome = value;
                mYearIncomeValue.setText(value.toString());
            }
        }, "Y", null);

        mTodayExpenseValue = (TextView) view.findViewById(R.id.today_expense_value);
        AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mTodayExpense = value;
                mTodayExpenseValue.setText(value.toString());
                Double Balance = mTodayIncome - mTodayExpense;
                if (Balance > 0)
                    mTodayBalanceValue.setTextColor(Color.GREEN);
                else
                    mTodayBalanceValue.setTextColor(Color.RED);
                mTodayBalanceValue.setText(Balance.toString());
            }
        }, "T", null);

        mWeekExpenseValue = (TextView) view.findViewById(R.id.week_expense_value);
        AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mWeekExpense = value;
                mWeekExpenseValue.setText(value.toString());
                Double Balance = mWeekIncome - mWeekExpense;
                if (Balance > 0)
                    mWeekBalanceValue.setTextColor(Color.GREEN);
                else
                    mWeekBalanceValue.setTextColor(Color.RED);
                mWeekBalanceValue.setText(Balance.toString());
            }
        }, "W", null);

        mMonthExpenseValue = (TextView) view.findViewById(R.id.month_expense_value);
        AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mMonthlyExpense = value;
                mMonthExpenseValue.setText(value.toString());
                Double Balance = mMonthlyIncome - mMonthlyExpense;
                if (Balance > 0)
                    mMonthBalanceValue.setTextColor(Color.GREEN);
                else
                    mMonthBalanceValue.setTextColor(Color.RED);
                mMonthBalanceValue.setText(Balance.toString());
            }
        }, "M", null);

        mYearExpenseValue = (TextView) view.findViewById(R.id.year_to_date_expense_value);
        AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mYearExpense = value;
                mYearExpenseValue.setText(value.toString());
                Double yearlyBalance = mYearIncome - mYearExpense;
                mYearBalanceValue.setText(yearlyBalance.toString());
            }
        }, "Y", null);

        AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mTotalIncome = value;
            }
        }, "A", null);
        AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
            @Override
            public void onCallback(Double value) {
                mTotalExpense = value;
                Double yearlyBalance = mTotalIncome - mTotalExpense;
                mCurrentBalanceValue.setText(yearlyBalance.toString());
            }
        }, "A", null);


        mShowMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else if (!mButtonState) {
                    mButtonState = true;
                    showMembers();
                } else {
                    mButtonState = false;
                    showMembers();
                }

            }
        });

        showMembers();
        mAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_editMemberFragment, bundle);
                }
            }
        });
        mMembersLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_memberListFragment, bundle);
                }
            }
        });
        mAddMemberContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else {
                    if (AccountLab.get(getActivity()).checkPermissionRequired("Manifest.permission.READ_CONTACTS"))
                        callIntentImportContacts();
                    else
                        requestForSpecificPermission();
                }
            }
        });
        updateMemberUI();
        return view;
    }

    private void callIntentImportContacts() {
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContact, REQUEST_CONTACT);
    }

    private void showMembers() {
        if (!mButtonState) {
            mMemberListRecyclerView.setVisibility(View.GONE);
            mRecyclerViewColumnLayout.setVisibility(View.GONE);
            mShowMembersButton.setImageResource(R.drawable.ic_hide);
        } else {
            mMemberListRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerViewColumnLayout.setVisibility(View.VISIBLE);
            mShowMembersButton.setImageResource(R.drawable.ic_show);
        }
    }

    public void updateMemberUI() {
        if (mMemberListRecyclerView.getVisibility() == View.VISIBLE)
            mLLProgress.setVisibility(View.VISIBLE);
        mAccount.getMemberIds(new Account.FirebaseCallbackMemberIds() {
            @Override
            public void onCallbackMemberIds(final List<String> account_memberList, String adminId) {
                mAccount.getMembers(new Account.FirebaseCallbackMember() {
                    public void onCallback(ArrayList<Member> memberList) {
                        if (memberList.size() > noMembers) {
                            if (mAdapter == null) {
                                mAdapter = new MemberAdapter(new ArrayList<Member>(memberList));
                                mLLProgress.setVisibility(View.GONE);
                                mMemberListRecyclerView.setAdapter(mAdapter);
                            } else {
                                loadMembers = true;
                                mAdapter.setMembers(new ArrayList<Member>(memberList));
                                mLLProgress.setVisibility(View.GONE);
                                mMemberListRecyclerView.setAdapter(mAdapter);
                            }
                        } else {
                            mLLProgress.setVisibility(View.GONE);
                            mMemberListRecyclerView.setVisibility(View.INVISIBLE);
                            mMembersLayout.setVisibility(View.INVISIBLE);
                            mRecyclerViewColumnLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                }, account_memberList);
            }
        });
    }

    private class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mMemberName, mMemberIncome, mMemberExpense, mMemberBalance, mMemberTodayExpense;
        private Member mMember;

        public MemberHolder(@NonNull View itemView) {
            super(itemView);
            mMemberName = (TextView) itemView.findViewById(R.id.member_name);
            mMemberIncome = (TextView) itemView.findViewById(R.id.total_income);
            mMemberExpense = (TextView) itemView.findViewById(R.id.total_expense);
            mMemberBalance = (TextView) itemView.findViewById(R.id.total_balance);
            mMemberTodayExpense = (TextView) itemView.findViewById(R.id.today_expense);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            positionClicked = getAdapterPosition();
            if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                Navigation.findNavController(view).navigate(R.id.errFragment);
            else if (view.getId() == R.id.member_del_button) {
                mAccount.delMember(mMember.getMemberId());
                updateMemberUI();
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_MEMBER_ID, mMember.getMemberId());
                Navigation.findNavController(view).navigate(R.id.action_accountTabFragment_to_editMemberFragment, bundle);
            }
        }

        public void bindMember(Member member) {
            mMember = member;
            if (mMember.getMemberName() != null)
                mMemberName.setText(mMember.getMemberName());
            else if (mMember.getEmailId() != null)
                mMemberName.setText(mMember.getEmailId());
            else
                mMemberName.setText(mMember.getMobNo());
            AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    mMemberIncomeValue = value;
                    mMemberIncome.setText(((Integer) value.intValue()).toString());
                }
            }, "A", member.getMemberId());
            AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    mMemberTodayExpense.setText(((Integer) value.intValue()).toString());
                }
            }, "T", member.getMemberId());
            AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    mMemberExpenseValue = value;
                    mMemberExpense.setText(((Integer) value.intValue()).toString());
                    Double memberBalance = mMemberIncomeValue - mMemberExpenseValue;
                    if (memberBalance >= 0)
                        mMemberBalance.setTextColor(Color.GREEN);
                    else
                        mMemberBalance.setTextColor(Color.RED);
                    mMemberBalance.setText(((Integer) memberBalance.intValue()).toString());
                }
            }, "A", member.getMemberId());
        }
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberHolder> {
        private List<Member> memberList;

        public MemberAdapter(List<Member> members) {
            memberList = new ArrayList<Member>();
            memberList = members;
        }

        public void setMembers(List<Member> members) {
            memberList.clear();
            memberList = members;
        }

        @NonNull
        @Override
        public MemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_member, parent, false);
            return new MemberHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberHolder holder, int position) {
            Member member = memberList.get(position);
            holder.bindMember(member);
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String mName = c.getString(0);
                c.close();
                final Member member = new Member(AccountLab.get(getActivity()).getNewMemberId());
                member.setMemberName(mName);
                queryFields = new String[]{ContactsContract.Contacts._ID};
                c = getActivity().getContentResolver()
                        .query(contactUri, queryFields, null, null, null);
                String contactID = null;
                if (c.moveToFirst()) {
                    contactID = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                }
                c.close();
                queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                c = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, queryFields, selection, new String[]{contactID},
                                null);
                String contactNumber = null;
                if (c.moveToFirst()) {
                    contactNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactNumber = AccountLab.get(getActivity()).formatPhNumber(contactNumber);
                    member.setMobNo(contactNumber);
                }
                c.close();
                String email = null;
                selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
                c = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, selection, new String[]{contactID}, null);
                if (c != null && c.moveToFirst()) {
                    email = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    member.setEmailId(email);
                }
                final Member newMember = member;

                final String finalEmail = email;
                AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member oldMember) {
                        if (oldMember == null) {
                            if (finalEmail == null) {
                                newMemberFlag = true;
                                addMemberToDb(newMember);
                                return;
                            } else
                                AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
                                    @Override
                                    public void onCallback(Member oldMemberEmail) {
                                        if (oldMemberEmail == null) {
                                            newMemberFlag = true;
                                            addMemberToDb(newMember);
                                        } else {//if member with this email exist then update
                                            //check whether this acct does not have this member id
                                            if (mAccount.getMember(oldMemberEmail.getMemberId()) != null) {
                                                displayMemberNotAdded();
                                            } else {
                                                newMember.setMemberId(oldMemberEmail.getMemberId());
                                                addMemberToDb(newMember);
                                            }
                                        }
                                    }
                                }, finalEmail, "E");
                        } else {//if member with this phone num exist then update
                            //check whether this acct does not have this member id
                            if (mAccount.getMember(oldMember.getMemberId()) != null) {
                                displayMemberNotAdded();
                            } else {
                                newMember.setMemberId(oldMember.getMemberId());
                                addMemberToDb(newMember);
                            }
                        }
                    }
                }, contactNumber, "P");

            } finally {
                c.close();
            }
        }
    }

    private void displayMemberNotAdded() {
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                .setTitle(R.string.alert_dialog_set_member_not_added)
                .setMessage(R.string.alert_dialog_default_member_added_acct)
                .setPositiveButton(android.R.string.yes, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void addMemberToDb(Member member) {
        if (member.getMobNo() == null && member.getEmailId() != null) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(member.getEmailId()).matches())
                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                        .setTitle(R.string.alert_dialog_set_member_not_added)
                        .setMessage(R.string.alert_dialog_default_member_del_msg_text)
                        .setPositiveButton(android.R.string.yes, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            return;

        }
        if (member.getMobNo() == null && member.getEmailId() == null) {
            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                    .setTitle(R.string.alert_dialog_set_member_delete_title)
                    .setMessage(R.string.alert_dialog_default_member_del_msg_empty_text)
                    .setPositiveButton(android.R.string.yes, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;

        }
        if (member.getMobNo() != null && member.getMobNo().length() < 10) {
            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                    .setTitle(R.string.alert_dialog_set_member_delete_title)
                    .setMessage(R.string.alert_dialog_default_member_del_msg_ph_err_text)
                    .setPositiveButton(android.R.string.yes, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;

        }
        AccountLab.get(getActivity()).updateMember(member);
        mAccount.addMemberToAccount(member.getMemberId(), "MEMBER");
        if (newMemberFlag && member.getEmailId() != null) {
            new SendEmailTask().execute(new String[]{member.getEmailId(),
                    AccountLab.get(getActivity()).getNewMemberMsg()});
            newMemberFlag = false;
        }
        updateMemberUI();
        mShowMembersButton.performClick();
    }

    private void requestForSpecificPermission() {
        requestPermissions(new String[]
                {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callIntentImportContacts();
                } else {
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                            .setTitle(R.string.alert_dialog_set_member_not_added)
                            .setMessage(R.string.alert_dialog_no_permission_text)
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}












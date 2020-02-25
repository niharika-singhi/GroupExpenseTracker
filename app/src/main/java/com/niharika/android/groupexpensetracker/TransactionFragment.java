package com.niharika.android.groupexpensetracker;

import androidx.activity.OnBackPressedCallback;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;


public class TransactionFragment extends Fragment {
    private static final String ARG_ACC_ID = "account_id", ARG_TRANS_ID = "transaction_id", ARG_TRANS_TYPE = "transaction_type";
    private Account mAccount;
    private Transaction mTransaction = null, transaction2;
    private Button mOkButton, mCancelButton, mDateButton, mTimeButton, mDeleteButton;
    private TextView mTransactionLabel, mAmount, mPay, mDescription, mCategory, mPaymentMethod, mPayLabel,
            mTransferLabel, mAccountsLabel;
    private String mTransType, transferToId, transferToName;
    private int[] mDateArray;
    private String mFormatDatePattern = "EEEE, MMM dd, yyyy", mFormatTimePattern = "hh:mm:ss", accNo, transId, accNo2, mAddedBy;
    private Spinner mMembersSpinner;
    private ArrayAdapter<String> memberAdapterSpinner, accountAdapterSpinner;
    private Spinner mAccountsSpinner;
    private boolean amtError = false;
    private ArrayList<Member> mMemberList;
    private boolean mNewTransaction;
    private GridLayout mGridLayout;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((MainActivity) getActivity()).showDrawer(false);
        inflater.inflate(R.menu.fragment_add_transaction, menu);
        setFragmentTitle();
    }

    public void setFragmentTitle() {
        getActivity().setTitle("Account: " + mAccount.getAccName());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Navigation.findNavController(getView()).navigate(R.id.action_transactionFragment_to_accountTabFragment);
                return true;
            default:
                if (!saveTransactionDetails())
                    return true;
                InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mCategory.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                NavController navController = findNavController(this);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
        }
    }

    public void setDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        mDateArray[0] = calendar.get(Calendar.YEAR);
        mDateArray[1] = calendar.get(Calendar.MONTH);
        mDateArray[2] = calendar.get(Calendar.DAY_OF_MONTH);
        mDateArray[3] = calendar.get(Calendar.HOUR_OF_DAY);
        mDateArray[4] = calendar.get(Calendar.MINUTE);
    }

    public void loadTransactionDetails() {

        mAmount.setText(mTransaction.getValue().toString());
        mPay.setText(mTransaction.getPayName());
        mCategory.setText(mTransaction.getCategory());
        mDescription.setText(mTransaction.getDescription());
        mPaymentMethod.setText(mTransaction.getPaymentMethod());
        setDateTime(new Date(mTransaction.getDate()));
        mDateButton.setText(DateFormat.format(mFormatDatePattern, mTransaction.getDate()));
        mTimeButton.setText(DateFormat.format(mFormatTimePattern, mTransaction.getDate()).toString());
    }

    public boolean saveTransactionDetails() {
        if(mNewTransaction){
        mTransaction.setType(mTransType);
        String value = mAmount.getText().toString();
        if (TextUtils.isEmpty(value)) {
            mAmount.setError("Please enter value");
            mAmount.requestFocus();
            return false;
        } else
            mTransaction.setValue(Double.valueOf(value));
        if (TextUtils.isEmpty(mCategory.getText()))
            mTransaction.setCategory(getString(R.string.uncategory_label));
        else
            mTransaction.setCategory(mCategory.getText().toString());
        mTransaction.setPayName(mPay.getText().toString());
        mTransaction.setPaymentMethod(mPaymentMethod.getText().toString());
        mTransaction.setDescription(mDescription.getText().toString());
        Calendar t = Calendar.getInstance();
        t.set(mDateArray[0], mDateArray[1], mDateArray[2], mDateArray[3], mDateArray[4]);
        Date tDate = t.getTime();
        mTransaction.setDate(tDate.getTime());
        mTransaction.setMemberId(AccountLab.get(getActivity()).getUser().getMemberId());
        if (mTransType.equals(getString(R.string.type_transfer)) && !amtError) {
            {
                mTransaction.setType(getString(R.string.type_transfer));
                transaction2 = new Transaction(AccountLab.get(getActivity()).getNewTransId());
                transaction2.setType(getString(R.string.type_incomeTransfer));
                transaction2.setCategory(getString(R.string.category_income));
                transaction2.setPayName(mTransaction.getPayName());
                transaction2.setPaymentMethod(mTransaction.getPaymentMethod());
                transaction2.setDescription("Transfer from " + mAccount.getAccName());
                transaction2.setValue(mTransaction.getValue());
                transaction2.setDate(tDate.getTime());
                transaction2.setMemberId(transferToId);
                if (mDescription.getText().toString().equals(""))
                    mTransaction.setDescription("To " + transferToName.substring(0,8) + "(" + (AccountLab.get(getActivity()).getAccount(accNo2).getAccName()) + ")");
                else
                    mTransaction.setDescription(mDescription.getText().toString());
                AccountLab.get(getActivity()).addTransaction(AccountLab.get(getActivity()).getAccount(accNo2), transaction2);
                AccountLab.get(getActivity()).transfer(mAccount, new Transfer(AccountLab.get(getActivity()).getNewTransferId(),
                        transaction2.getTid(), mTransaction.getTid(), mAccount.getAccNo(), accNo2));
            }
        }
        if (!amtError)
            AccountLab.get(getActivity()).addTransaction(mAccount, mTransaction);
        else
            return false;
        return true;
        }
        else {
            if(!mTransaction.getType().equals("ET") && !mTransaction.getType().equals("IT") ) {
                String value = mAmount.getText().toString();
                if (TextUtils.isEmpty(value)) {
                    mAmount.setError("Please enter value");
                    mAmount.requestFocus();
                    return false;
                } else
                    mTransaction.setValue(Double.valueOf(value));
            }
            if (TextUtils.isEmpty(mCategory.getText()))
                mTransaction.setCategory(getString(R.string.uncategory_label));
            else
                mTransaction.setCategory(mCategory.getText().toString());
            mTransaction.setPayName(mPay.getText().toString());
            mTransaction.setPaymentMethod(mPaymentMethod.getText().toString());
            mTransaction.setDescription(mDescription.getText().toString());
            Calendar t = Calendar.getInstance();
            t.set(mDateArray[0], mDateArray[1], mDateArray[2], mDateArray[3], mDateArray[4]);
            Date tDate = t.getTime();
            mTransaction.setDate(tDate.getTime());
            AccountLab.get(getActivity()).updateTransaction(mAccount, mTransaction);
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected()) {
            Navigation.findNavController((MainActivity) getActivity(), R.id.nav_host_fragment).navigate(R.id.errFragment);
            return;
        }
        if (getArguments() != null && getArguments().containsKey(ARG_ACC_ID)) {
            accNo = (String) getArguments().getSerializable(ARG_ACC_ID);
            mAccount = AccountLab.get(getActivity()).getAccount(accNo);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_TRANS_ID)) {
            transId = (String) getArguments().getSerializable(ARG_TRANS_ID);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_TRANS_TYPE)) {
            mTransType = (String) getArguments().getSerializable(ARG_TRANS_TYPE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        mOkButton = (Button) view.findViewById(R.id.ok_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_button);
        mTransactionLabel = (TextView) view.findViewById(R.id.transaction_label);
        mAmount = (TextView) view.findViewById(R.id.amt_value);
        mPay = (TextView) view.findViewById(R.id.pay_name);
        mPayLabel = (TextView) view.findViewById(R.id.pay_name_label);
        mDescription = (TextView) view.findViewById(R.id.description);
        mCategory = (TextView) view.findViewById(R.id.category);
        mDateButton = (Button) view.findViewById(R.id.date_button);
        mTimeButton = (Button) view.findViewById(R.id.time_button);
        mPaymentMethod = (TextView) view.findViewById(R.id.pay_method);
        mTransferLabel = (TextView) view.findViewById(R.id.transfer_to_label);
        mMembersSpinner = (Spinner) view.findViewById(R.id.spinner_members);
        mAccountsSpinner = (Spinner) view.findViewById(R.id.spinner_account);
        mAccountsLabel = (TextView) view.findViewById(R.id.account_label);
        mDeleteButton = (Button) view.findViewById(R.id.del_button);
        mGridLayout = (GridLayout) view.findViewById(R.id.grid_layout);
        mGridLayout.setEnabled(false);
        mDateArray = new int[5];
        mMemberList = new ArrayList<Member>();
        OnBackPressedCallback callback = new OnBackPressedCallback() {
            @Override
            public boolean handleOnBackPressed() {
                // Handle the back button event
                if (getArguments().containsKey(ARG_TRANS_ID)) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(getView()).navigate(R.id.action_transactionFragment_to_transactionListFragment, bundle);
                } else
                    Navigation.findNavController(getView()).navigate(R.id.action_transactionFragment_to_accountTabFragment);
                return true;
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        if (transId == null && mTransType != null) {//its a new transaction
            mNewTransaction=true;
            mDateButton.setText(DateFormat.format(mFormatDatePattern, new Date()));
            mTimeButton.setText(DateFormat.format(mFormatTimePattern, new Date()));
            setDateTime(new Date());
            mTransaction = new Transaction(AccountLab.get(getActivity()).getNewTransId());
            updateUI();
        } else if (transId != null && mTransType == null) {//its a existing transaction
            AccountLab.get(getActivity()).getTransaction(mAccount,
                    new AccountLab.FirebaseCallbackTransferTo() {
                        @Override
                        public void onCallback(Transaction transaction) {
                            mTransaction = transaction;
                            mTransType = transaction.getType();
                            setDeleteButton();
                            updateUI();
                            loadTransactionDetails();
                            if(mTransaction.getType().equals("ET") || mTransaction.getType().equals("IT"))
                                mAmount.setEnabled(false);
                        }
                    }, transId);

        }
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.MyDialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int month, int day) {
                                mDateArray[0] = year;
                                mDateArray[1] = month;
                                mDateArray[2] = day;
                                mDateButton.setText(day + "-" + (month + 1) + "-" + year);

                            }
                        }, mDateArray[0], mDateArray[1], mDateArray[2]);
                datePickerDialog.show();
            }
        });
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.MyDialogTheme,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hour,
                                                  int min) {
                                mDateArray[3] = hour;
                                mDateArray[4] = min;
                                mTimeButton.setText(hour + ":" + min);
                            }
                        }, mDateArray[3], mDateArray[4], false);
                timePickerDialog.show();
            }
        });
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mCategory.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Bundle bundle = new Bundle();
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else if (saveTransactionDetails())
                    if (getArguments().containsKey(ARG_TRANS_ID)) {
                        bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                        Navigation.findNavController(getView()).navigate(R.id.transactionListFragment, bundle);
                    } else {
                        bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                        Navigation.findNavController(getView()).navigate(R.id.accountTabFragment, bundle);
                    }
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            Bundle bundle = new Bundle();

            @Override
            public void onClick(View view) {
                if (getArguments().containsKey(ARG_TRANS_ID)) {
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(view).navigate(R.id.action_transactionFragment_to_transactionListFragment, bundle);
                } else {
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(view).navigate(R.id.action_transactionFragment_to_accountTabFragment, bundle);
                }
            }
        });

        return view;
    }

    void setDeleteButton() {
        if (transId != null) {
            Boolean delPermissions = mAccount.checkDelTransactionPermissions(AccountLab.get(getActivity()).getUser().getMemberId(),
                    mTransaction);
            if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                Navigation.findNavController(getView()).navigate(R.id.errFragment);
            else if (delPermissions) {
                mDeleteButton.setVisibility(View.VISIBLE);
                mDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Boolean[] del = {true};
                        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                                .setTitle(R.string.alert_dialog_set_delete_title)
                                .setMessage(R.string.alert_dialog_delete_permission_text_transaction)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        {
                                            AccountLab.get(getActivity()).deleteTransaction(mTransaction, accNo);
                                            Navigation.findNavController(getView()).navigate(R.id.accountTabFragment);
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }
        }
    }

    private void updateUI() {
        if (mTransType.startsWith("I")) {
            mTransactionLabel.setText(R.string.type_income_label);
            mPayLabel.setText(getActivity().getString(R.string.payer_label));
            mCategory.setText(getActivity().getString(R.string.category_income));
        } else if (mTransType.equals(getActivity().getString(R.string.type_transfer))) {
            mTransactionLabel.setText(getActivity().getString(R.string.type_transfer_label));
            mAccountsLabel.setVisibility(View.VISIBLE);
            mTransferLabel.setVisibility(View.VISIBLE);
            mMembersSpinner.setVisibility(View.VISIBLE);
            mAccountsSpinner.setVisibility(View.VISIBLE);
            mCategory.setText(getActivity().getString(R.string.category_transfer));
            prepareSpinner();
            if (transId == null) {
                mAmount.addTextChangedListener(new TextWatcher() {
                    //This is to check whether the transfer value is less than the account bal
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                    @Override
                    public void afterTextChanged(final Editable s) {
                        if (!TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s))
                            AccountLab.get(getActivity()).getBalance(mAccount, null, new AccountLab.FirebaseCallbackCalculateTransaction() {
                                @Override
                                public void onCallback(Double value) {
                                    if (!TextUtils.isEmpty(s.toString()) && Double.parseDouble(s.toString()) <= value) {
                                        amtError = false;
                                    } else {
                                        amtError = true;
                                        if (Double.parseDouble(s.toString()) == 0)
                                            mAmount.setError("Please enter correct value");
                                        else
                                            mAmount.setError("The transfer amount is higher than the account balance");
                                        mAmount.requestFocus();
                                    }
                                }
                            });
                    }
                });
            } else {
                mAccountsSpinner.setEnabled(false);
                mMembersSpinner.setEnabled(false);
            }
        } else {
            mTransactionLabel.setText(R.string.type_exp_label);
            mCategory.setText(R.string.category_un);
            mPayLabel.setText(R.string.payee_label);

        }
    }


    private void prepareSpinner() {
        if (transId != null && mTransaction != null) {//if its an  existing acct transfer
            AccountLab.get(getActivity()).getTransferredId(mAccount, new AccountLab.FirebaseCallbackTransferredTo() {
                @Override
                public void onCallback(final Transfer transfer) {
                    accNo2 = transfer.getAccNoTo();
                    if(AccountLab.get(getActivity()).getAccount(accNo2)!=null)
                    AccountLab.get(getActivity()).getTransaction(AccountLab.get(getActivity()).getAccount(accNo2), new AccountLab.FirebaseCallbackTransferTo() {
                        @Override
                        public void onCallback(final Transaction transaction) {
                            AccountLab.get(getActivity()).getAccount(transfer.getAccNoTo()).getMemberIds(new Account.FirebaseCallbackMemberIds() {
                                @Override
                                public void onCallbackMemberIds(List<String> memberIdsList, final String admin) {
                                    AccountLab.get(getActivity()).getAccount(transfer.getAccNoTo()).getMembers(new Account.FirebaseCallbackMember() {
                                        @Override
                                        public void onCallback(final ArrayList<Member> memberList) {
                                            mMemberList.clear();
                                            mMemberList = new ArrayList<Member>(memberList);
                                            setSpinnerMembers(transaction.getMemberId());
                                        }
                                        }, memberIdsList);
                                }
                            });
                        }
                    }, transfer.getIncomeId());
                }
            }, mTransaction.getTid());

        } else//in case its a new account transaction{
            AccountLab.get(getActivity()).getAccount(accNo).getMemberIds(new Account.FirebaseCallbackMemberIds() {
                @Override
                public void onCallbackMemberIds(List<String> memberIdsList, final String admin) {
                    AccountLab.get(getActivity()).getAccount(accNo).getMembers(new Account.FirebaseCallbackMember() {
                        @Override
                        public void onCallback(final ArrayList<Member> memberList) {
                            mMemberList.clear();
                            mMemberList = new ArrayList<Member>(memberList);
                            setSpinnerMembers(admin);
                        }
                    }, memberIdsList);
                }
            });
    }

    private void setSpinnerMembers(final String memberId) {
        List mList = AccountLab.get(getActivity()).getAccount(accNo).extractMemberName(mMemberList);
        final List<String> aList = AccountLab.get(getActivity()).extractAccountName(AccountLab.get(getActivity()).getAccounts());
        if(getContext()!=null) {
            accountAdapterSpinner = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, aList);
            memberAdapterSpinner = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, mList);

            memberAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mMembersSpinner.setAdapter(memberAdapterSpinner);
            accountAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAccountsSpinner.setAdapter(accountAdapterSpinner);

            if (memberId != null) {
                mMembersSpinner.setSelection(getPositionInList(mMemberList, memberId));
                transferToName = (String) mMembersSpinner.getSelectedItem();
                transferToId = mMemberList.get(mMembersSpinner.getSelectedItemPosition()).getMemberId();
            } else if (mMemberList != null && mMemberList.size() > 0) {//default selection is the current account and the first member in list
                transferToName = (String) mMembersSpinner.getSelectedItem();
                transferToId = mMemberList.get(0).getMemberId();
            }
            mMembersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    transferToId = mMemberList.get(pos).getMemberId();
                    transferToName = (String) mMembersSpinner.getItemAtPosition(pos);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            if (accNo2 != null) //The user has selected to transfer to a different acc No
                mAccountsSpinner.setSelection(getPositionInList(accNo2));
            mAccountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    accNo2 = AccountLab.get(getActivity()).getAccounts().get(pos).getAccNo();
                    getMembersList(accNo2, memberId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    void getMembersList(final String aNo, final String memberId) {//if acc no is changed get new list of members in the account
        AccountLab.get(getActivity()).getAccount(aNo).getMemberIds(new Account.FirebaseCallbackMemberIds() {

            @Override
            public void onCallbackMemberIds(List<String> memberIdsList, final String adminId) {
                AccountLab.get(getActivity()).getAccount(aNo).getMembers(new Account.FirebaseCallbackMember() {
                    @Override
                    public void onCallback(final ArrayList<Member> memberList) {
                        mMemberList.clear();
                        mMemberList = new ArrayList<Member>(memberList);
                        memberAdapterSpinner.clear();
                        List<String> mList = AccountLab.get(getActivity()).getAccount(aNo).extractMemberName(mMemberList);
                        memberAdapterSpinner.addAll(mList);
                        memberAdapterSpinner.notifyDataSetChanged();
                        int pos;
                        if (memberId == null) {
                            mMembersSpinner.setSelection(getPositionInList(memberList, adminId));
                            pos = getPositionInList(memberList, adminId);
                        } else {
                            mMembersSpinner.setSelection(getPositionInList(memberList, memberId));
                            pos = getPositionInList(memberList, memberId);
                        }
                        transferToName =(String) mMembersSpinner.getItemAtPosition(pos);
                        transferToId = mMemberList.get(pos).getMemberId();
                    }
                }, memberIdsList);
            }
        });
    }

    private int getPositionInList(ArrayList<Member> memberList, String memberId) {
        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).getMemberId().equals(memberId)) {
                return i;
            }
        }
        return 0;
    }

    private int getPositionInList(String id) {
        List<Account> accountList = AccountLab.get(getActivity()).getAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getAccNo().equals(id)) {
                return i;
            }
        }
        return 0;
    }
}


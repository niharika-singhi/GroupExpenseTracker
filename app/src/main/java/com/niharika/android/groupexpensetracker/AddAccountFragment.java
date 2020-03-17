package com.niharika.android.groupexpensetracker;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.TextUtils;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import static androidx.navigation.fragment.NavHostFragment.findNavController;
import static java.lang.StrictMath.abs;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddAccountFragment extends Fragment {
    private static final String ARG_ACCOUNT_ID = "account_id", ARG_ACC_ID = "account_id", ARG_MEMBER_ID = "member_id";
    private TextView mAccountName, mDescription, mInitialBalance, mInitialBalanceLabel, mAddMembersLabel;
    private CheckBox mDefaultAccountCheckbox;
    private Button mOkButton, mCancelButton, mDeleteButton;
    private ImageButton mAddMember, mAddMemberContacts;
    private Account mAccount;
    private Spinner mCurrencySpinner;
    private boolean mNewAccountFlag = false, loadMembers = false, newMemberFlag = false;
    private ArrayAdapter<CharSequence> adapter;
    private static final int REQUEST_CONTACT = 0, noMembers = 0;
    private RecyclerView mMemberListRecyclerView;
    private int positionClicked = -1;
    private MemberAdapter mAdapter;
    private String mAdminId;
    private LinearLayout mLLProgress, mMemberLayout;
    private ArrayList<Member> mMemberList = new ArrayList<Member>();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((MainActivity) getActivity()).showDrawer(false);
        inflater.inflate(R.menu.fragment_add_account, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Navigation.findNavController(getView()).navigate(R.id.action_addAccountFragment_to_accountListFragment);
                return true;
            default:
                if (!saveAccountDetails(mNewAccountFlag))
                    return true;
                NavController navController = findNavController(this);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
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
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, queryFields, selection, new String[]{contactID}, null);
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
                        .setTitle(R.string.alert_dialog_set_member_delete_title)
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
        String msg=AccountLab.get(getActivity())
                .getNewMemberNotificationMsg(mAccount.getAccName(),member.getDisplayName());
        AccountLab.get(getActivity()).sendNotifications(mAccount,msg);

        if (newMemberFlag && member.getEmailId() != null) {
            new SendEmailTask().execute(new String[]{member.getEmailId(),
                    AccountLab.get(getActivity()).getNewMemberMsg()});
            newMemberFlag = false;
        }
        updateMemberUI();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    boolean saveAccountDetails(boolean newAccount) {
        String accountName = mAccountName.getText().toString().trim();
        if (TextUtils.isEmpty(accountName)) {
            mAccountName.setError("Please enter a Name for your account");
            mAccountName.requestFocus();
            return false;
        } else
            mAccount.setAccName(accountName);
        mAccount.setDescription(mDescription.getText().toString().trim());
        mAccount.setDefaultAccount(mDefaultAccountCheckbox.isChecked());
        mAccount.setCurrency(mCurrencySpinner.getSelectedItem().toString());
        if (newAccount) {
            mAccount.setCreatorId(AccountLab.get(getActivity()).getUser().getMemberId());
            String initialAmount = mInitialBalance.getText().toString().trim();
            if (!TextUtils.isEmpty(initialAmount))
                AccountLab.get(getActivity()).addInitialTransaction(mAccount, Double.parseDouble(mInitialBalance.getText().toString()));
            AccountLab.get(getActivity()).addAccount(mAccount);
        } else {
            AccountLab.get(getActivity()).updateAccount(mAccount);
        }
        return true;
    }

    void loadAccountDetails() {
        mAccountName.setText(mAccount.getAccName());
        mDescription.setText(mAccount.getDescription());
        mDefaultAccountCheckbox.setChecked(mAccount.isDefaultAccount());
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).equals(mAccount.getCurrency())) {
                mCurrencySpinner.setSelection(position);
            }
        }
    }

    public void onResume() {
        super.onResume();
        //if(loadMembers)
        //updateMemberUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_account, container, false);
        mOkButton = (Button) view.findViewById(R.id.ok_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_button);
        mDeleteButton = (Button) view.findViewById(R.id.del_button);
        mAccountName = (TextView) view.findViewById(R.id.account_name);
        mDescription = (TextView) view.findViewById(R.id.account_description);
        mInitialBalance = (TextView) view.findViewById(R.id.initial_balance);
        mInitialBalanceLabel = (TextView) view.findViewById(R.id.initial_balance_label);
        mDefaultAccountCheckbox = (CheckBox) view.findViewById(R.id.default_checkbox);
        mAddMemberContacts = (ImageButton) view.findViewById(R.id.add_member_imageButtonContacts);
        mAddMember = (ImageButton) view.findViewById(R.id.add_member_imageButton);
        mAddMembersLabel = (TextView) view.findViewById(R.id.add_members_label);
        mMemberListRecyclerView = (RecyclerView) view.findViewById(R.id.member_list_recycler_view);
        mMemberListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLLProgress = (LinearLayout) view.findViewById(R.id.llprogressbar);
        mMemberLayout = (LinearLayout) view.findViewById(R.id.memberLayout);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getArguments() != null && getArguments().containsKey(ARG_ACCOUNT_ID)) {
            String accNo = (String) getArguments().getSerializable(ARG_ACCOUNT_ID);
            //read account details
            mAccount = AccountLab.get(getActivity()).getAccount(accNo);
            mInitialBalance.setVisibility(View.GONE);
            mInitialBalanceLabel.setVisibility(View.GONE);
        } else {
            mAccount = new Account(AccountLab.get(getActivity()).getNewAcctId());
            mNewAccountFlag = true;
        }
        mDefaultAccountCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ((isChecked && mNewAccountFlag) || (isChecked && !mNewAccountFlag && !mAccount.isDefaultAccount())) {
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                            .setTitle(R.string.alert_dialog_set_default_title)
                            .setMessage(R.string.alert_dialog_default_permission_text)
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mDefaultAccountCheckbox.setChecked(false);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
        mCurrencySpinner = (Spinner) view.findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrencySpinner.setAdapter(adapter);
        mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        /*OnBackPressedCallback callback = new OnBackPressedCallback() {
            @Override
            public boolean handleOnBackPressed() {
                // Handle the back button event
                if (mNewAccountFlag)
                    Navigation.findNavController(getView()).navigate(R.id.action_addAccountFragment_to_accountTabFragment);
                else
                    Navigation.findNavController(getView()).navigate(R.id.action_addAccountFragment_to_accountListFragment);
                return true;
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);*/
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected()) {
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                } else {
                    InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(mAccountName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    Boolean success = saveAccountDetails(mNewAccountFlag);
                    if (success)
                        if (mNewAccountFlag) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                            Navigation.findNavController(view).navigate(R.id.action_addAccountFragment_to_accountTabFragment, bundle);
                        } else
                            Navigation.findNavController(view).navigate(R.id.action_addAccountFragment_to_accountListFragment);
                }
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                Navigation.findNavController(view).navigate(R.id.action_addAccountFragment_to_accountTabFragment, bundle);

            }
        });
        if (!mNewAccountFlag)
            if (AccountLab.get(getActivity()).checkDelAccountPermissions(mAccount)) {
                mDeleteButton.setVisibility(View.VISIBLE);
                mDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Boolean[] del = {true};
                        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                                .setTitle(R.string.alert_dialog_set_delete_title)
                                .setMessage(R.string.alert_dialog_delete_permission_text)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AccountLab.get(getActivity()).deleteAccount(mAccount);
                                        Navigation.findNavController(getView()).navigate(R.id.action_addAccountFragment_to_accountTabFragment);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Navigation.findNavController(getView()).navigate(R.id.action_addAccountFragment_to_accountTabFragment);
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }
        if (mNewAccountFlag) {
            mMemberLayout.setVisibility(View.GONE);
        }
        mAddMemberContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected()) {
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                } else {
                    if(AccountLab.get(getActivity()).checkPermissionRequired("Manifest.permission.READ_CONTACTS"))
                        callIntentImportContacts();
                    else
                        requestForSpecificPermission();

                }
            }
        });

        mAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected()) {
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                    Navigation.findNavController(view).navigate(R.id.action_addAccountFragment_to_editMemberFragment, bundle);

                }
            }
        });
        if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
            Navigation.findNavController(view).navigate(R.id.errFragment);
        else if (!mNewAccountFlag)
            loadAccountDetails();
        updateMemberUI();
        return view;
    }

    private void callIntentImportContacts() {
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContact, REQUEST_CONTACT);
    }

    public void updateMemberUI() {
        if (!mNewAccountFlag)
            mLLProgress.setVisibility(View.VISIBLE);
        if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
            Navigation.findNavController(getView()).navigate(R.id.errFragment);
        else
            mAccount.getMemberIds(new Account.FirebaseCallbackMemberIds() {

                @Override
                public void onCallbackMemberIds(List<String> account_memberList, String adminId) {
                    if (account_memberList.size() > noMembers) {
                        mAdminId = adminId;
                        mMemberList.clear();
                        mAccount.getMembers(new Account.FirebaseCallbackMember() {

                            public void onCallback(ArrayList<Member> memberList) {
                                mMemberList = memberList;
                                if (memberList.size() > noMembers) {

                                    mMemberListRecyclerView.setVisibility(View.VISIBLE);
                                    if (mAdapter == null) {
                                        mAdapter = new MemberAdapter(new ArrayList<Member>(memberList));
                                        mLLProgress.setVisibility(View.GONE);
                                        mMemberListRecyclerView.setAdapter(mAdapter);
                                    } else {
                                        loadMembers = true;
                                        mLLProgress.setVisibility(View.GONE);
                                        mAdapter.setMembers(new ArrayList<Member>(memberList));
                                        mMemberListRecyclerView.setAdapter(mAdapter);
                                    }
                                } else {
                                    mLLProgress.setVisibility(View.GONE);
                                    mMemberListRecyclerView.setVisibility(View.GONE);
                                }
                            }
                        }, account_memberList);
                    }
                }
            });
    }

    private class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mMemberName, mMemberRole;
        private ImageButton mDelMemberButton;
        private Member mMember;

        public MemberHolder(@NonNull View itemView) {
            super(itemView);
            mMemberName = (TextView) itemView.findViewById(R.id.member_name);
            mDelMemberButton = (ImageButton) itemView.findViewById(R.id.member_del_button);
            mMemberRole = (TextView) itemView.findViewById(R.id.member_role);
            itemView.setOnClickListener(this);
            mDelMemberButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            positionClicked = getAdapterPosition();
            if (view.getId() == R.id.member_del_button) {
                mAccount.delMember(mMember.getMemberId());
                updateMemberUI();
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                bundle.putSerializable(ARG_MEMBER_ID, mMember.getMemberId());
                Navigation.findNavController(view).navigate(R.id.action_addAccountFragment_to_editMemberFragment, bundle);
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
            if (mAdminId != null)
                if (mAdminId.equals(mMember.getMemberId())) {
                    mMemberRole.setVisibility(View.VISIBLE);
                    mMemberRole.setText(R.string.admin_label);
                    mDelMemberButton.setVisibility(View.INVISIBLE);
                }

        }
    }

    private class MemberAdapter extends RecyclerView.Adapter<AddAccountFragment.MemberHolder> {
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
            View view = getLayoutInflater().inflate(R.layout.list_item_member_add_account, parent, false);
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

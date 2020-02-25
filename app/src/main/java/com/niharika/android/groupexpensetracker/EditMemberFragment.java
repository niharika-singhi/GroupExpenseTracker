package com.niharika.android.groupexpensetracker;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static androidx.navigation.fragment.NavHostFragment.findNavController;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditMemberFragment extends Fragment {
    private static final String ARG_ACC_ID = "account_id", ARG_MEMBER_ID = "member_id";
    private Account mAccount;
    private Member mMember;
    private Button mOkButton, mCPasswordButton, mCancelButton;
    private TextView mRole, mName, mEmail, mMob, mAddress, mOldPassword, mNewPassword, mConfirmPassword, mCPasswordLabel;
    private String accNo, memberId;
    private int checkCounter = 0;
    private View view;
    private boolean mNewMemberFlag = false, viewMode = false;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_edit_member, menu);
        ((MainActivity) getActivity()).showDrawer(false);
    }

    public void setFragmentTitle() {
        if (mMember != null && mMember.getMemberName() != null)
            getActivity().setTitle("Hi! " + mMember.getMemberName());
        else
            getActivity().setTitle("Hi!");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Navigation.findNavController(getView()).navigate(R.id.action_editMemberFragment_to_accountTabFragment);
                return true;
            default:
                hideKeyboard(mName);
                saveMemberDetails();
                return true;
        }
    }

    public void loadMemberDetails() {
        if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected()) {
            Navigation.findNavController(view).navigate(R.id.errFragment);
            return;
        }
        if (!TextUtils.isEmpty(mMember.getEmailId()))
            mEmail.setEnabled(false);
        if (!TextUtils.isEmpty(mMember.getMobNo()))
            mMob.setEnabled(false);
        setFragmentTitle();
        mName.setText(mMember.getMemberName());
        mMob.setText(mMember.getMobNo());
        mEmail.setText(mMember.getEmailId());
        mAddress.setText(mMember.getAddress());
        if (viewMode) {
            mName.setEnabled(false);
            mEmail.setEnabled(false);
            mMob.setEnabled(false);
            mAddress.setEnabled(false);
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


    public void saveMemberDetails() {
        if (mNewMemberFlag) {
            if (!TextUtils.isEmpty(mName.getText()))
                mMember.setMemberName(mName.getText().toString());
            if (TextUtils.isEmpty(mEmail.getText()) && TextUtils.isEmpty(mMob.getText())) {
                mMob.setError("Please enter mob num");
                mMob.requestFocus();
                return;
            }
            if (!TextUtils.isEmpty(mAddress.getText()))
                mMember.setAddress(mAddress.getText().toString());
            String emailId = mEmail.getText().toString();
            if (!TextUtils.isEmpty(mEmail.getText()))
                if (!TextUtils.isEmpty(emailId) && !Patterns.EMAIL_ADDRESS.matcher(emailId).matches()) {
                    mEmail.setError("Email id is not valid");
                    mEmail.requestFocus();
                    return;
                } else
                    mMember.setEmailId(mEmail.getText().toString());
            if (!TextUtils.isEmpty(mMob.getText()))
                if (mMob.getText().length() < 10) {
                    mMob.setError("Phone num is not valid");
                    mMob.requestFocus();
                    return;
                } else
                    mMember.setMobNo(AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString())); //format it
            if (!TextUtils.isEmpty(mEmail.getText())) {//check for email id in database if exist update the member
                //if does not exist check for the ph num also
                AccountLab.get(getActivity()).checkDuplicateUsername(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member oldMember) {
                        if (oldMember == null) {
                            if (TextUtils.isEmpty(mMember.getMobNo())) {
                                addMemberToDb();
                                return;
                            } else if (!TextUtils.isEmpty(mMember.getMobNo())) {//check whether a member with this mob num exist,if exist update else add new member
                                final String mobNo = AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString());
                                AccountLab.get(getActivity()).checkDuplicateUsername(new AccountLab.FirebaseCallback() {
                                    @Override
                                    public void onCallback(Member oldMemberPhoneNum) {
                                        if (oldMemberPhoneNum == null) {
                                            addMemberToDb();
                                            return;
                                        } else {//mem exist jus update
                                            if (mAccount.getMember(oldMemberPhoneNum.getMemberId()) == null) {
                                                AccountLab.get(getActivity()).copyMembers(oldMemberPhoneNum, mMember);
                                                addMemberToDb();
                                                return;
                                            } else {
                                                displayMemberNotAdded();
                                            }
                                        }
                                    }
                                }, mobNo, mMember.getMemberId(), "P");
                            }
                        } else {//member exist jus update
                            if (mAccount.getMember(oldMember.getMemberId()) == null) {
                                AccountLab.get(getActivity()).copyMembers(oldMember, mMember);
                                addMemberToDb();
                                return;
                            } else {
                                displayMemberNotAdded();
                            }
                        }
                    }
                }, emailId, mMember.getMemberId(), "E");
            } else if (!TextUtils.isEmpty(mMob.getText()) && TextUtils.isEmpty(mEmail.getText())) {//if user has entered a ph num and no email id
                final String mobNo = AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString());
                AccountLab.get(getActivity()).checkDuplicateUsername(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member oldMember) {
                        if (oldMember == null) {
                            addMemberToDb();
                            return;
                        } else {//mem exist jus update
                            if (mAccount.getMember(oldMember.getUserId()) == null) {
                                AccountLab.get(getActivity()).copyMembers(oldMember, mMember);
                                addMemberToDb();
                                return;
                            } else {
                                displayMemberNotAdded();
                            }
                        }
                    }
                }, mobNo, mMember.getMemberId(), "P");

            }
        } else if (!viewMode) {//save current user details
            final String emailId = mEmail.getText().toString();
            String mob = mMob.getText().toString();
            if (mEmail.isEnabled() && !TextUtils.isEmpty(mEmail.getText()) && !emailId.equals(AccountLab.get(getActivity()).getUser().getEmailId()))
                if (!Patterns.EMAIL_ADDRESS.matcher(emailId).matches()) {
                    mEmail.setError("Email id is not valid");
                    mEmail.requestFocus();
                    return;
                } else {
                    //check whether email id exist if exist show err msg else update
                    AccountLab.get(getActivity()).checkDuplicateUsername(new AccountLab.FirebaseCallback() {
                        @Override
                        public void onCallback(Member oldMember) {
                            if (oldMember == null) {
                                if (!TextUtils.isEmpty(mName.getText()))
                                    AccountLab.get(getActivity()).getUser().setMemberName(mName.getText().toString());
                                if (!TextUtils.isEmpty(mEmail.getText()))
                                    AccountLab.get(getActivity()).getUser().setEmailId(mEmail.getText().toString());
                                if (!TextUtils.isEmpty(mMob.getText()))//format it
                                    AccountLab.get(getActivity()).getUser().setMobNo(AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString()));
                                if (!TextUtils.isEmpty(mAddress.getText()))
                                    AccountLab.get(getActivity()).getUser().setAddress(mAddress.getText().toString());
                                updateMemberDetails();

                                return;
                            } else {
                                mEmail.setError("Email id is already registered");
                                mMob.requestFocus();
                                return;
                            }
                        }
                    }, emailId, AccountLab.get(getActivity()).getUser().getMemberId(), "E");

                }
            else if (mMob.isEnabled() && !TextUtils.isEmpty(mob) && !AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString()).equals(AccountLab.get(getActivity()).getUser().getMobNo())) {
                if (mMob.getText().length() < 10) {
                    mMob.setError("Phone num is not valid");
                    mMob.requestFocus();
                    return;
                } else {
                    //check whether ph num exist if exist show err msg else update
                    final String mobNo = AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString());
                    AccountLab.get(getActivity()).checkDuplicateUsername(new AccountLab.FirebaseCallback() {
                        @Override
                        public void onCallback(Member oldMember) {
                            if (oldMember == null) {
                                if (!TextUtils.isEmpty(mName.getText()))
                                    AccountLab.get(getActivity()).getUser().setMemberName(mName.getText().toString());
                                if (!TextUtils.isEmpty(mEmail.getText()))
                                    AccountLab.get(getActivity()).getUser().setEmailId(mEmail.getText().toString());
                                if (!TextUtils.isEmpty(mMob.getText()))//format it
                                    AccountLab.get(getActivity()).getUser().setMobNo(AccountLab.get(getActivity()).formatPhNumber(mMob.getText().toString()));
                                if (!TextUtils.isEmpty(mAddress.getText()))
                                    AccountLab.get(getActivity()).getUser().setAddress(mAddress.getText().toString());
                                updateMemberDetails();
                                return;
                            } else {
                                mMob.setError("Phone num is already registered");
                                mMob.requestFocus();
                                return;
                            }
                        }
                    }, mobNo, AccountLab.get(getActivity()).getUser().getMemberId(), "P");
                }
            } else {
                if (!TextUtils.isEmpty(mName.getText()))
                    AccountLab.get(getActivity()).getUser().setMemberName(mName.getText().toString());

                if (!TextUtils.isEmpty(mAddress.getText()))
                    AccountLab.get(getActivity()).getUser().setAddress(mAddress.getText().toString());
                updateMemberDetails();
            }
        }
        if (mCPasswordLabel.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(mOldPassword.getText())) {
                mOldPassword.setError("Please enter password");
                mOldPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(mNewPassword.getText())) {
                mNewPassword.setError("Please enter password");
                mNewPassword.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(mConfirmPassword.getText())) {
                mConfirmPassword.setError("Please enter password");
                mConfirmPassword.requestFocus();
                return;
            }
            if (mNewPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                changePassword(null);
            }
        }
    }

    private void changePassword(String loginType) {//this is for only members with email id
        String userId = null;
        String userLoginType = null;
        if (loginType == null && mMember.getEmailId() != null) {
            userId = mMember.getEmailId();
            userLoginType = "E";
        } else {
            userId = mMember.getMobNo();
            userLoginType = "P";
        }
        AuthCredential credential = EmailAuthProvider
                .getCredential(userId, mOldPassword.getText().toString());
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String finalUserLoginType = userLoginType;
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(mConfirmPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        NavController navController = findNavController(EditMemberFragment.this);
                                        navController.navigate(R.id.action_editMemberFragment_to_accountTabFragment);
                                    } else {
                                        if (finalUserLoginType.equals("E") && mMember.getMobNo() != null)
                                            changePassword("P");
                                    }
                                }
                            });
                        } else {
                            mOldPassword.setError("Old password does not match with the records");
                            mOldPassword.requestFocus();
                            return;
                        }
                    }
                });
    }

    private void addMemberToDb() {
        AccountLab.get(getActivity()).updateMember(mMember);
        mAccount.addMemberToAccount(mMember.getMemberId(), "MEMBER");
        if (mNewMemberFlag && mMember.getEmailId() != null) {
            new SendEmailTask().execute(new String[]{mMember.getEmailId(), AccountLab.get(getActivity()).getNewMemberMsg()});
            mNewMemberFlag = false;
        }
        NavController navController = findNavController(this);
        if (mCPasswordLabel.getVisibility() != View.VISIBLE)
        navController.navigate(R.id.action_editMemberFragment_to_accountTabFragment);
    }

    private void updateMemberDetails() {//update the current user details

        AccountLab.get(getActivity()).updateMember(AccountLab.get(getActivity()).getUser());
        NavController navController = findNavController(this);
        if (mCPasswordLabel.getVisibility() != View.VISIBLE)
        navController.navigate(R.id.action_editMemberFragment_to_accountTabFragment);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add member details
        if (getArguments() != null && getArguments().containsKey(ARG_ACC_ID)) {
            accNo = (String) getArguments().getSerializable(ARG_ACC_ID);
            mAccount = AccountLab.get(getActivity()).getAccount(accNo);
        }
        //edit member details
        if (getArguments() != null && getArguments().containsKey(ARG_MEMBER_ID)) {
            memberId = (String) getArguments().getSerializable(ARG_MEMBER_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_member, container, false);

        mOkButton = (Button) view.findViewById(R.id.ok_member_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_member_button);
        mCPasswordButton = (Button) view.findViewById(R.id.change_passwd_button);
        mName = (TextView) view.findViewById(R.id.member_name);
        mMob = (TextView) view.findViewById(R.id.member_mob_num);
        mEmail = (TextView) view.findViewById(R.id.member_email);
        mAddress = (TextView) view.findViewById(R.id.member_address);
        mCPasswordLabel = (TextView) view.findViewById(R.id.change_passwd_label);
        mConfirmPassword = (TextView) view.findViewById(R.id.confirm_passwd);
        mNewPassword = (TextView) view.findViewById(R.id.new_passwd);
        mOldPassword = (TextView) view.findViewById(R.id.old_passwd);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mOkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected()) {
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                } else {
                    hideKeyboard(mName);
                    saveMemberDetails();
                }

            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_editMemberFragment_to_accountTabFragment);
            }
        });

        if (memberId == null && accNo == null)//profile display of current member
        {
            mMember = AccountLab.get(getActivity()).getUser();
            mCPasswordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCPasswordButton.setVisibility(View.GONE);
                    mOldPassword.setVisibility(View.VISIBLE);
                    mNewPassword.setVisibility(View.VISIBLE);
                    mConfirmPassword.setVisibility(View.VISIBLE);
                    mCPasswordLabel.setVisibility(View.VISIBLE);
                }
            });
            memberId = mMember.getMemberId();
            loadMemberDetails();
        } else if (accNo != null && memberId == null) {//new member
            mCPasswordButton.setVisibility(View.GONE);
            mNewMemberFlag = true;
            mMember = new Member(AccountLab.get(getActivity()).getNewMemberId());
        } else {
            mCPasswordButton.setVisibility(View.GONE);
            AccountLab.get(getActivity()).getMember(new AccountLab.FirebaseCallback() {
                @Override
                public void onCallback(Member member) {
                    viewMode = true;
                    mMember = member;
                    loadMemberDetails();
                }
            }, memberId);
        }
        return view;
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}

package com.niharika.android.groupexpensetracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    private static final String ARG_PAGE_DISPLAY = "page";
    private Button mSubmitButton, mLink, mVerifyButton;
    private TextView mEmailView, mPasswordView, mVerCode, mForgetPasswordLink, mTitleLabel;
    private FirebaseAuth mAuth;
    private LinearLayout mLLProgress, mCredentialLayout;
    Member mMember;
    private String mLogin, password, mTypeOfLogin, mVerificationId, mViewType = "R", mUsername;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private CallbackManager mCallbackManager;
    private FirebaseUser user;
    private LoginButton fbLoginButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1;
    private View view;
    private GridLayout mOtherLoginLayout;
    private ImageButton mEyeButton;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    private boolean mVerificationInProgress = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).disableActionBar();
        setFragmentTitle();
        //default view is Register if no arg is passed
        if (getArguments() != null && getArguments().containsKey(ARG_PAGE_DISPLAY)) {
            mViewType = (String) getArguments().getSerializable(ARG_PAGE_DISPLAY);
        }
        if (savedInstanceState != null)
            mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void setViewType() {
        if (mViewType.equals("L")) {//Login View
            mCredentialLayout.setVisibility(View.VISIBLE);
            mTitleLabel.setText(R.string.loginLabel);
            mLink.setText(R.string.RegisterLink);
            mSubmitButton.setText("Login");
        } else if (mViewType.equals("R")) {//Register View
            mTitleLabel.setText(R.string.RegisterLabel);
            mLink.setText(R.string.LoginLink);
            mSubmitButton.setText("Register");
        } else if (mViewType.equals("FP")) {//Forget Password View
            mTitleLabel.setText(R.string.forget_passwd);
            mPasswordView.setVisibility(View.GONE);
            mLLProgress.setVisibility(View.GONE);
            mEyeButton.setVisibility(View.GONE);
            mEmailView.requestFocus();
            mOtherLoginLayout.setVisibility(View.GONE);
            mLink.setVisibility(View.VISIBLE);
            mSubmitButton.setText("Send");
        } else if (mViewType.equals("V")) {//Phone Verification view
            mTitleLabel.setText(R.string.phone_ver);
            //mSubmitButton.setText("Send");
            mEyeButton.setVisibility(View.GONE);
            mEmailView.setVisibility(View.GONE);
            mPasswordView.setVisibility(View.GONE);
            mLLProgress.setVisibility(View.GONE);
            mVerCode.setVisibility(View.VISIBLE);
            mSubmitButton.setVisibility(View.GONE);
            mOtherLoginLayout.setVisibility(View.GONE);
            mVerCode.requestFocus();
            mVerifyButton.setVisibility(View.VISIBLE);
        }
    }

    public void setFragmentTitle() {
        getActivity().setTitle(R.string.app_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        mEmailView = (EditText) view.findViewById(R.id.editTextEmail);
        mPasswordView = (EditText) view.findViewById(R.id.editTextPassword);
        mLink = (Button) view.findViewById(R.id.link);
        mLink.setAllCaps(false);
        mForgetPasswordLink = (TextView) view.findViewById(R.id.fpasswdlink);
        mForgetPasswordLink.setAllCaps(false);
        mSubmitButton = (Button) view.findViewById(R.id.button_submit);
        mEyeButton = (ImageButton) view.findViewById(R.id.button_eye);
        mVerifyButton = (Button) view.findViewById(R.id.buttonVerify);
        mLLProgress = (LinearLayout) view.findViewById(R.id.llprogressbar);
        mVerCode = (EditText) view.findViewById(R.id.editTextVerificationCode);
        mCredentialLayout = (LinearLayout) view.findViewById(R.id.credential_layout);
        mAuth = FirebaseAuth.getInstance();
        mOtherLoginLayout = ((GridLayout) view.findViewById(R.id.other_login_layout));
        mTitleLabel = ((TextView) view.findViewById(R.id.title_text));
        if (mVerificationInProgress && validatePhoneNumber()) {
            sendSMS(mUsername);
        }
        setViewType();

        mEyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransformationMethod tMethod=mPasswordView.getTransformationMethod();
                if(tMethod.equals(HideReturnsTransformationMethod.getInstance())) {
                    mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else
                mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            private String newToken;

            @Override
            public void onClick(View view) {


                mEmailView.setError(null);
                mPasswordView.setError(null);
                if (mViewType != null && mViewType.equals("FP")) {
                    mLLProgress.setVisibility(View.VISIBLE);
                    forgetPassword();
                    return;
                }
                InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mEmailView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mLLProgress.setVisibility(View.VISIBLE);

                if (!AccountLab.get(getActivity()).isNetworkAvailableAndConnected())
                    Navigation.findNavController(view).navigate(R.id.errFragment);
                else
                    registerUser(view);

            }
        });
        mLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailView.setError(null);
                mPasswordView.setError(null);
                if (mViewType.equals("R"))
                    mViewType = "L";
                else
                    mViewType = "R";
                setViewType();
            }
        });
        mForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewType = "FP";
                setViewType();
            }
        });
        mCallbackManager = CallbackManager.Factory.create();
        fbLoginButton = view.findViewById(R.id.fb_login_button);
        fbLoginButton.setReadPermissions(Arrays.asList("email"));
        fbLoginButton.setFragment(this);
        fbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                mTypeOfLogin = "F";
                mLLProgress.setVisibility(View.VISIBLE);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                mLLProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException error) {
                mLLProgress.setVisibility(View.INVISIBLE);
                Log.d(MainFragment.TAG, "facebook:onError", error);
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        view.findViewById(R.id.google_log_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleSignInClient.signOut();
                mTypeOfLogin = "G";
                googleSignIn();
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback() {
            @Override
            public boolean handleOnBackPressed() {
                if (mViewType.equals("FP") || mViewType.equals("V"))
                    Navigation.findNavController(view).navigate(R.id.registerFragment);
                else
                    getActivity().finish();
                return true;
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return view;
    }

    private void forgetPassword() {
        mLLProgress.setVisibility(View.VISIBLE);
        mUsername = mEmailView.getText().toString().trim();
        if (TextUtils.isEmpty(mUsername)) {
            mEmailView.setError("Please enter email id or Phone num");
            mLLProgress.setVisibility(View.GONE);
            return;
        }
        mTypeOfLogin = AccountLab.get(getActivity()).getModeOfLogin(mUsername);
        if (mTypeOfLogin.equals("P")) {
            if (mUsername.length() < 10) {
                mEmailView.setError("Please enter valid email id or Phone num");
                mEmailView.requestFocus();
                mLLProgress.setVisibility(View.GONE);
                return;
            } else {
                mUsername = AccountLab.get(getActivity()).formatPhNumber(mUsername);
                AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member oldMember) {
                        if (oldMember != null) {
                            sendSMS(mUsername);
                        } else {

                            mLLProgress.setVisibility(View.GONE);
                            mEmailView.setError("The email id or ph number is not registered with us.");
                            mEmailView.requestFocus();
                        }
                    }
                }, mUsername, mTypeOfLogin);
            }
        } else
            FirebaseAuth.getInstance().sendPasswordResetEmail(mUsername)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mLLProgress.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogTheme))
                                        .setTitle(R.string.alert_title_mail)
                                        .setMessage(R.string.alert_dialog_msg_text_mail)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Navigation.findNavController(view).navigate(R.id.registerFragment);
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_info)
                                        .show();
                            } else {
                                mEmailView.setError("The email id or ph number is not registered with us.");
                                mEmailView.requestFocus();
                            }
                        }
                    });
    }

    private void registerUser(View view) {
        mUsername = mEmailView.getText().toString().trim();
        password = mPasswordView.getText().toString().trim();
        if (TextUtils.isEmpty(mUsername)) {
            mEmailView.setError("Please enter email id or Phone num");
            mEmailView.requestFocus();
            mLLProgress.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Please enter password");
            mPasswordView.requestFocus();
            mLLProgress.setVisibility(View.GONE);
            return;
        }
        mTypeOfLogin = AccountLab.get(getActivity()).getModeOfLogin(mUsername);
        if (mTypeOfLogin.equals("P")) {
            if (mUsername.length() < 10) {
                mEmailView.setError("Please enter valid email id or Phone num");
                mEmailView.requestFocus();
                mLLProgress.setVisibility(View.GONE);
                return;
            } else {
                mUsername = AccountLab.get(getActivity()).formatPhNumber(mUsername);
                sendSMS(mUsername);
            }
        } else if (mViewType.equals("R"))
            registerWithEmailCredential();
        else
            emailLogin();
    }

    private void showHome() {
        user = mAuth.getCurrentUser();
        if (mTypeOfLogin.equals("F")) {
            if (user.getEmail() != null)
                mUsername = mAuth.getCurrentUser().getEmail();
            else
                mUsername = AccountLab.get(getActivity()).formatPhNumber(mAuth.getCurrentUser().getPhoneNumber());
        }
        mMember = new Member(AccountLab.get(getActivity()).getNewMemberId(), mUsername, password);
        mMember.setLoggedIn(true);
        AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
            @Override
            public void onCallback(Member oldMember) {
                if (oldMember != null) {
                    if (oldMember.getUserId() == null) {
                        oldMember.setUserId(mAuth.getCurrentUser().getUid());
                        AccountLab.get(getActivity()).updateMember(oldMember);
                    }

                    AccountLab.get(getActivity()).setUser(oldMember);
                    AccountLab.get(getActivity()).isLoggedIn(true);
                } else {
                    mMember.setUserId(mAuth.getCurrentUser().getUid());
                    AccountLab.get(getActivity()).addMember(mMember);
                    AccountLab.get(getActivity()).setUser(mMember);
                    if (mMember.getEmailId() != null)
                        new SendEmailTask().execute(new String[]{mMember.getEmailId(), AccountLab.get(getActivity()).getRegistrationMsg()});
                }
                mLLProgress.setVisibility(View.GONE);
                Navigation.findNavController(getView()).navigate(R.id.accountTabFragment);
            }
        }, mUsername, mTypeOfLogin);
    }

    private void registerWithEmailCredential() {
        mAuth.createUserWithEmailAndPassword(mUsername, password)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showHome();
                        } else {
                            mLLProgress.setVisibility(View.INVISIBLE);
                            mEmailView.requestFocus();
                            if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                mEmailView.setError("The email id is already registered with us");
                            else
                                mEmailView.setError("Email id or password is incorrect");


                            return;
                        }
                    }
                });
    }


    void emailLogin() {
        mAuth.signInWithEmailAndPassword(mUsername, password)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showHome();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                mLLProgress.setVisibility(View.INVISIBLE);
                                mEmailView.requestFocus();
                                mEmailView.setError("The email id is not registered with us");
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                mLLProgress.setVisibility(View.INVISIBLE);
                                mPasswordView.requestFocus();
                                mPasswordView.setError("The user is already registered with social logins");
                            } else {
                                mLLProgress.setVisibility(View.INVISIBLE);
                                mPasswordView.requestFocus();
                                mPasswordView.setError("Username or Password is Incorrect");
                            }
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUsername = mAuth.getCurrentUser().getEmail();
                            showHome();
                        } else {
                            mLLProgress.setVisibility(View.INVISIBLE);
                            Log.w(MainFragment.TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void googleSignIn() {
        mLLProgress.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        mCredentialLayout.setVisibility(View.GONE);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showHome();
                        } else {
                            Log.d(MainFragment.TAG, "signInWithCredential:failure", task.getException());
                            mLLProgress.setVisibility(View.GONE);
                            mViewType = "L";
                            setViewType();
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                mLLProgress.setVisibility(View.INVISIBLE);

                                Toast.makeText(getActivity(), "The user is already registered with email/ph num", Toast.LENGTH_LONG).show();
                                Log.d(MainFragment.TAG, "The user is already registered with email/ph num");
                            } else {
                                mPasswordView.setError("The password is wrong");
                                mPasswordView.requestFocus();
                            }
                            LoginManager.getInstance().logOut();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                mLLProgress.setVisibility(View.INVISIBLE);
                Log.d(MainFragment.TAG, "Google sign in failed", e);
            }
        } else
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*@Override
    public void onStart() {
        super.onStart();
        if (mVerificationInProgress && validatePhoneNumber()) {
            Log.d(MainFragment.TAG, "mVer pass");
            sendSMS(mUsername);
        }
        else
            Log.d(MainFragment.TAG, "mVer fail on Start"+mVerificationInProgress);
    }*/


    private boolean validatePhoneNumber() {
        if (TextUtils.isEmpty(mUsername)) {
            mEmailView.setError("Please enter email id or Phone num");
            mEmailView.requestFocus();
            mLLProgress.setVisibility(View.GONE);
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Please enter password");
            mPasswordView.requestFocus();
            mLLProgress.setVisibility(View.GONE);
            return false;
        }
        mTypeOfLogin = AccountLab.get(getActivity()).getModeOfLogin(mUsername);
        if (mTypeOfLogin.equals("P")) {
            if (mUsername.length() < 10) {
                mEmailView.setError("Please enter valid email id or Phone num");
                mEmailView.requestFocus();
                mLLProgress.setVisibility(View.GONE);
                return false;
            } else {
                mUsername = AccountLab.get(getActivity()).formatPhNumber(mUsername);
                return true;
            }
        }
        return false;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }*/
    private void sendSMS(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);
        mVerificationInProgress = true;
    }

    private void showDialogForVerification() {
        mViewType = "V";
        setViewType();
        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = mVerCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    mVerCode.setError("Please enter valid code");
                    mVerCode.requestFocus();
                    return;
                }
                Log.d(MainFragment.TAG, " after verify code");
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,
                        mVerCode.getText().toString());
                if (!TextUtils.isEmpty(mVerCode.getText())) {
                    mVerCode.setEnabled(false);
                    mLLProgress.setVisibility(View.VISIBLE);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            showHome();
                            if (!TextUtils.isEmpty(mPasswordView.getText()))
                                user.updatePassword(mPasswordView.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.d(MainFragment.TAG, "Error password not updated"
                                                            + task.getException());
                                                }
                                            }
                                        });
                        } else {
                            mLLProgress.setVisibility(View.INVISIBLE);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mVerCode.setEnabled(true);
                                mVerCode.setError("The verification code entered was invalid");
                                mVerCode.requestFocus();
                                Log.d(MainFragment.TAG, "Ver code is wrong" + task.getException());
                                Toast.makeText(getActivity(), "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
   /* private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        Log.d(MainFragment.TAG, "Resend");
    }*/


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            mVerificationInProgress = false;
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }


        @Override
        public void onVerificationFailed(FirebaseException e) {
            mVerificationInProgress = false;
            mLLProgress.setVisibility(View.INVISIBLE);
            mVerCode.setError("Please enter correct code");
            mVerCode.requestFocus();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(getActivity(), "Invalid Request " + e.toString(), Toast.LENGTH_SHORT).show();
                Log.d(MainFragment.TAG, "invalid req" + e.toString());
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(getActivity(), "Too many requests from this device.Try after some time", Toast.LENGTH_SHORT).show();
                Log.d(MainFragment.TAG, "sms quota" + e.toString());
            }
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            mVerificationId = verificationId;
            PhoneAuthProvider.ForceResendingToken mResendToken = token;
            showDialogForVerification();


        }
    };
}


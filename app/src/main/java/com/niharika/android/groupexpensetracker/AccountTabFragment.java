package com.niharika.android.groupexpensetracker;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;

public class AccountTabFragment extends Fragment {
    private TabLayout mAccountTab;
    private List<Account> mAccounts=new ArrayList<Account>();
    private ViewPager mViewPager;
    private Account account;
    private Button mAddAccount;
    private LinearLayout mHomeLinearLayout;
    private static final int noAccounts = 0;
    private View view;
    private String mMemberId, lastTabId;
    private static final String Key = "MemberId",ARG_ACC_ID = "account_id";
    boolean drawingUI = false;
    private int count = 0, twice = 2;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_account_tab, menu);
        ((MainActivity) getActivity()).showDrawer(true);
        setFragmentTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!drawingUI) {
            updateUI();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    public void setFragmentTitle() {
        getActivity().setTitle(R.string.app_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                NavController navController = NavHostFragment.findNavController(this);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable(Key, AccountLab.get(getActivity()).getUser().getMemberId());
        Log.d(MainFragment.TAG,"in saving"+AccountLab.get(getActivity()).getUser().getMemberId());
    }

    protected void showAccounts() {
        int position = 0;
        if (AccountLab.get(getActivity()).getAccountsSize() > noAccounts) {
            mHomeLinearLayout.setVisibility(View.GONE);
            mAccountTab = (TabLayout) view.findViewById(R.id.account_tab_layout);
            if (!isAdded()) {
                return;
            }
            FragmentManager fragmentManager = getChildFragmentManager();
            mViewPager.setVisibility(View.VISIBLE);
            mAccountTab.setupWithViewPager(mViewPager);
            if (getArguments() != null) {
                if ((lastTabId = (String) getArguments().getSerializable(ARG_ACC_ID)) != null) {
                    for (int i = 0; i < mAccounts.size(); i++) {
                        if (lastTabId.equals(mAccounts.get(i).getAccNo())) {
                            position = i;
                        }
                    }
                }
            } else {
                for (int i = 0; i < mAccounts.size(); i++) {
                    if (mAccounts.get(i).isDefaultAccount()) {
                        position = i;
                    }
                }
            }
            mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

                @Override
                public Fragment getItem(int position) {
                    account = mAccounts.get(position);
                    return AccountFragment.newInstance(account.getAccNo());
                }
                @Override
                public String getPageTitle(int position) {
                    account = mAccounts.get(position);
                    String name=account.getAccName();
                    if(name.length()>10)
                    return account.getAccName().substring(0,10);
                    else
                        return account.getAccName();
                }
                @Override
                public int getCount() {
                    return mAccounts.size();
                }
            });
            mViewPager.setCurrentItem(position);
        } else {
            mViewPager.setVisibility(View.GONE);
            mHomeLinearLayout.setVisibility(View.VISIBLE);
            mAddAccount = view.findViewById(R.id.add_account_button);
            mAddAccount.setOnClickListener(Navigation.createNavigateOnClickListener(
                    R.id.action_accountTabFragment_to_addAccountFragment));
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( getActivity(),
                new OnSuccessListener<InstanceIdResult>() {


                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        AccountLab.get(getActivity()).addToken(newToken);
                        Log.d(MainFragment.TAG,"Here in reg token"+newToken);
                    }
                });

    }

    protected void updateUI() {
        drawingUI = true;
        if (AccountLab.get(getActivity()).isLoaded() == false) {
            mAccounts.clear();
            AccountLab.get(getActivity()).loadAccounts(new AccountLab.FirebaseCallbackAccounts() {
                @Override
                public void onCallback(ArrayList<Account> accountList) {
                    mAccounts = accountList;
                    showAccounts();
                }
            });
        } else {
            mAccounts = AccountLab.get(getActivity()).getAccounts();
            showAccounts();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account_tab, container, false);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.registerFragment);
        }
        mHomeLinearLayout = (LinearLayout) view.findViewById(R.id.home_linear_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.account_tab_view_pager);
        if (AccountLab.get(getActivity()).getUser() == null) {
            drawingUI = true;
            if (savedInstanceState != null) {
                mMemberId = (String) savedInstanceState.getSerializable(Key);
                AccountLab.get(getActivity()).setUser(new Member(mMemberId));
                Log.d(MainFragment.TAG,"user is"+mMemberId);
                updateUI();
            } else {
                String typeOfLogin;
                String username=null;
                if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                    username = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                }
                if (username != null && !username.equals("")) {
                    typeOfLogin = "E";
                } else {
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null)
                    username = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    typeOfLogin = "P";
                }
                AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member oldMember) {
                        if (oldMember != null) {
                            AccountLab.get(getActivity()).setUser(oldMember);
                            updateUI();
                        }
                    }
                }, username, typeOfLogin);
            }
        } else {
            drawingUI = true;
            updateUI();
        }
        OnBackPressedCallback callback = new OnBackPressedCallback() {
            @Override
            public boolean handleOnBackPressed() {
                // Handle the back button event
                count++;
                if (count == twice)
                    getActivity().finish();
                return true;
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return view;
    }
}

package com.niharika.android.groupexpensetracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

public class AccountListFragment extends Fragment {

    private FloatingActionButton mAddAccountButton;
    private RecyclerView mAccountListRecyclerView;
    private AccountAdapter mAdapter;
    private int positionClicked = -1;
    private final int noAccounts = 0;
    private Account mAccount;
    private List<Account> mAccountList = new ArrayList<Account>();
    private static final String ARG_ACC_ID = "account_id";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_account_list, menu);
        setFragmentTitle();
        ((MainActivity) getActivity()).showDrawer(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setFragmentTitle() {
        getActivity().setTitle(R.string.account_list_fragment_title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_list, container, false);
        mAddAccountButton = (FloatingActionButton) view.findViewById(R.id.add_account_floating_button);
        mAddAccountButton.setOnClickListener(
                Navigation.createNavigateOnClickListener(
                        R.id.action_accountListFragment_to_addAccountFragment));
        mAccountListRecyclerView = (RecyclerView) view.findViewById(R.id.account_list_recycler_view);
        mAccountListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        OnBackPressedCallback callback = new OnBackPressedCallback() {
            @Override
            public boolean handleOnBackPressed() {
                Navigation.findNavController(getView()).navigate(R.id.action_accountListFragment_to_accountTabFragment);
                return true;
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        updateUI();
        return view;
    }

    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        if (AccountLab.get(getActivity()).isLoaded()) {
            mAccountList = AccountLab.get(getActivity()).getAccounts();
            showAccounts();
        } else {
            AccountLab.get(getActivity()).loadAccounts(new AccountLab.FirebaseCallbackAccounts() {
                @Override
                public void onCallback(ArrayList<Account> accountList) {
                    mAccountList = accountList;
                    showAccounts();
                }
            });
        }
    }

    private void showAccounts() {
        if (mAccountList != null && mAccountList.size() > noAccounts) {
            if (mAdapter == null) {
                mAdapter = new AccountAdapter(mAccountList);
                mAccountListRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setAccounts(mAccountList);
                mAccountListRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    private class AccountHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mAccountName, mAccountDescription, mAccountCurrency, mAccountIncomeValue,
                mAccountExpenseValue, mAccountBalanceValue;
        private Account mAccount;
        Double totalExpense = 0.0, totalIncome = 0.0;

        public AccountHolder(@NonNull View itemView) {
            super(itemView);
            mAccountName = (TextView) itemView.findViewById(R.id.account_name);
            mAccountDescription = (TextView) itemView.findViewById(R.id.account_description);
            mAccountCurrency = (TextView) itemView.findViewById(R.id.account_currency);
            mAccountIncomeValue = (TextView) itemView.findViewById(R.id.account_income_value);
            mAccountExpenseValue = (TextView) itemView.findViewById(R.id.account_expense_value);
            mAccountBalanceValue = (TextView) itemView.findViewById(R.id.account_balance_value);
            mAccountIncomeValue = (TextView) itemView.findViewById(R.id.account_income_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            positionClicked = getAdapterPosition();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
            Navigation.findNavController(view).navigate(R.id.action_accountListFragment_to_addAccountFragment, bundle);
        }

        public void bindAccount(Account account) {
            mAccount = account;
            mAccountName.setText(mAccount.getAccName().toUpperCase());
            mAccountDescription.setText(mAccount.getDescription());
            mAccountCurrency.setText(mAccount.getCurrency());
            AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    totalIncome = value;
                    mAccountIncomeValue.setText(value.toString());
                }
            }, "A", null);
            AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    totalExpense = value;
                    mAccountExpenseValue.setText(value.toString());
                    Double bal = totalIncome - totalExpense;
                    if (bal >= 0)
                        mAccountBalanceValue.setTextColor(Color.GREEN);
                    else
                        mAccountBalanceValue.setTextColor(Color.RED);
                    Double absBal = abs(bal);
                    mAccountBalanceValue.setText(absBal.toString());
                }
            }, "A", null);
        }
    }

    private class AccountAdapter extends RecyclerView.Adapter<AccountHolder> {

        public AccountAdapter(List<Account> accounts) {
            mAccountList = accounts;
        }

        public void setAccounts(List<Account> accounts) {
            mAccountList = accounts;
        }

        @NonNull
        @Override
        public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_account, parent, false);
            return new AccountHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AccountHolder holder, int position) {
            Account account = mAccountList.get(position);
            holder.bindAccount(account);
        }

        @Override
        public int getItemCount() {
            return mAccountList.size();
        }
    }
}

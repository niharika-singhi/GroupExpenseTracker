package com.niharika.android.groupexpensetracker;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionListFragment extends Fragment {

    private RecyclerView mTransactionListRecyclerView;
    private TransactionAdapter mAdapter;
    private int positionClicked = -1,maxlengthChar = 25;
    private final int noTransactions = 0;
    private String accNo;
    private static final String ARG_ACC_ID = "account_id", ARG_TRANS_ID = "transaction_id";
    private String mFormatDatePattern = "dd-MM-yyyy hh:mm";
    private ArrayList<Transaction> mTransactionList;
    private List<Member> mMemberList;
    private SubMenu memberSubmenu;
    private SearchView searchView;
    private boolean memberWise=false;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((MainActivity)getActivity()).showDrawer(false);
        inflater.inflate(R.menu.fragment_transaction_list, menu);
        setFragmentTitle(AccountLab.get(getActivity()).getAccount(accNo).getAccName());
         memberSubmenu=(SubMenu) menu.findItem(R.id.member_transactions).getSubMenu();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Navigation.findNavController(getView()).navigate(R.id.action_transactionListFragment_to_accountTabFragment);
            case R.id.all_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("ALL");
                return true;
            case R.id.today_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("T");
                return true;
            case R.id.weekly_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("W");
                return true;
            case R.id.monthly_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("M");
                return true;
            case R.id.yearly_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("Y");
                return true;
            case R.id.income_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("I");
                return true;
            case R.id.expense_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("E");
                return true;
            case R.id.account_transfers_transactions:
                if(mTransactionList.size()>0)
                mAdapter.getFilter().filter("ET");
                return true;
            case R.id.action_search:
                return true;
            case R.id.member_transactions:

                        memberWise=true;
                      return true;
            default: if(mTransactionList.size()>0)
                mAdapter.getFilter().filter(item.getTitle());
                return true;
        }

    }

    public void setFragmentTitle(String title) {
        getActivity().setTitle(title);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ACC_ID)) {
            accNo = (String) getArguments().getSerializable(ARG_ACC_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        mTransactionListRecyclerView = (RecyclerView) view.findViewById(R.id.transaction_list_recycler_view);
        mTransactionListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTransactionList = new ArrayList<Transaction>();
        mTransactionList.clear();
        mMemberList = new ArrayList<Member>();
        OnBackPressedCallback callback = new OnBackPressedCallback() {
            @Override
            public boolean handleOnBackPressed() {
                // Handle the back button event
                Navigation.findNavController(getView()).navigate(R.id.accountTabFragment);
                return true;
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        updateUI();
        return view;
    }

    public void onResume() {
        super.onResume();
    }

    public void updateUI() {
        final Account account = AccountLab.get(getActivity()).getAccount(accNo);
        AccountLab.get(getActivity()).loadTransactionList(account, new AccountLab.FirebaseCallbackLoadTransaction() {
            @Override
            public void onCallback(final ArrayList<Transaction> transList) {
                mTransactionList = transList;
                Collections.reverse(mTransactionList);
                account.getMemberIds(new Account.FirebaseCallbackMemberIds() {
                    @Override
                    public void onCallbackMemberIds(List<String> memberIdsList, String adminId) {
                        account.getMembers(new Account.FirebaseCallbackMember() {
                                               @Override
                                               public void onCallback(ArrayList<Member> memberList) {
                                                   mMemberList.clear();
                                                   mMemberList = new ArrayList<>(memberList);
                                                   for(Member m:mMemberList){
                                                       String name="";
                                                       if(m.getMemberName()!=null)
                                                           name=m.getMemberName();
                                                       else if(m.getEmailId()!=null)
                                                           name=m.getEmailId();
                                                       else if(m.getMobNo()!=null)
                                                           name=m.getMobNo();
                                                       memberSubmenu.add(name);
                                                   }
                                                   if (transList.size() > noTransactions)
                                                   if (mAdapter == null) {
                                                       mAdapter = new TransactionAdapter(transList);
                                                       mTransactionListRecyclerView.setAdapter(mAdapter);
                                                   } else {
                                                       mAdapter.setTransactions(mTransactionList);
                                                       mAdapter.notifyDataSetChanged();
                                                   }
                                               }
                                           }
                                , memberIdsList);

                    }});

                     {
                    final List<String> memberIdsList = AccountLab.get(getActivity()).getAccount(accNo).extractMemberIds(transList);
                    Set<String> set = new HashSet<>(memberIdsList);
                    memberIdsList.clear();
                    memberIdsList.addAll(set);
                     }
            }
        });
    }

    private class TransactionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTransactionDescription, mAccountBalanceValue, mTransactionValue, mDateTime, mAddedBy, mCategory;
        private ImageButton mDelAccountButton;
        private Transaction mTransaction;

        public TransactionHolder(@NonNull View itemView) {
            super(itemView);
            mTransactionDescription = (TextView) itemView.findViewById(R.id.transaction_description);
            mTransactionValue = (TextView) itemView.findViewById(R.id.transaction_value);
            mAccountBalanceValue = (TextView) itemView.findViewById(R.id.account_balance_value);
            mDateTime = (TextView) itemView.findViewById(R.id.date_time_label);
            mAddedBy = (TextView) itemView.findViewById(R.id.transaction_addedBy);
            mCategory = (TextView) itemView.findViewById(R.id.transaction_category);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            positionClicked = getAdapterPosition();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ARG_ACC_ID, accNo);
            bundle.putSerializable(ARG_TRANS_ID, mTransaction.getTid());
            Navigation.findNavController(view).navigate(R.id.action_transactionListFragment_to_transactionFragment, bundle);
        }

        public void bindTransaction(Transaction transaction) {
            mTransaction = transaction;
            mDateTime.setText(DateFormat.format(mFormatDatePattern, mTransaction.getDate()));
            int n = mTransaction.getDescription().length();
            if (n > maxlengthChar) {
                n = maxlengthChar;
                mTransactionDescription.setText(mTransaction.getDescription().substring(0, n).concat(".."));
            } else
                mTransactionDescription.setText(mTransaction.getDescription());
            if (mTransaction.getType().startsWith("I"))
                mTransactionValue.setTextColor(Color.GREEN);
            else
                mTransactionValue.setTextColor(Color.RED);
            mTransactionValue.setText(mTransaction.getValue().toString());
            Member m = AccountLab.get(getActivity()).getAccount(accNo).getMember(mTransaction.getMemberId());
            if(m==null){
                AccountLab.get(getActivity()).getMember(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member m) {
                        if(m!=null)
                        if (m.getMemberName() != null)
                            mAddedBy.setText(m.getMemberName());
                        else
                        if (m.getEmailId() != null)
                            mAddedBy.setText(m.getEmailId());
                        else
                        if (m.getMobNo() != null)
                            mAddedBy.setText(m.getMobNo());

                    }
                },mTransaction.getMemberId());
            }
            else
            if (m.getMemberName() != null)
                mAddedBy.setText(m.getMemberName());
            else
                if (m.getEmailId() != null)
                mAddedBy.setText(m.getEmailId());
                else
                if (m.getMobNo() != null)
                    mAddedBy.setText(m.getMobNo());
            mCategory.setText(mTransaction.getCategory());
            mAccountBalanceValue.setText(AccountLab.get(getActivity()).getAccountBalance(mTransactionList,
                    new Date(mTransaction.getDate())).toString());
        }
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionListFragment.TransactionHolder> implements Filterable {
        private List<Transaction> mTransactions;
        private List<Transaction> transactionFilteredList;

        public TransactionAdapter(List<Transaction> transactions) {
            transactionFilteredList = new ArrayList<Transaction>();
            transactionFilteredList = transactions;
            mTransactions = transactions;
        }

        public void setTransactions(List<Transaction> transactions) {
            mTransactions = transactions;
            transactionFilteredList = transactions;
        }

        @NonNull
        @Override
        public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_transaction, parent, false);
            return new TransactionHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionHolder holder, int position) {
            Transaction transaction = transactionFilteredList.get(position);
            holder.bindTransaction(transaction);
        }

        @Override
        public int getItemCount() {
            return transactionFilteredList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        transactionFilteredList = mTransactions;
                    } else if (charString.equals("ALL")) {
                        transactionFilteredList = mTransactions;
                    } else if (charString.equals("M")) {
                        transactionFilteredList = filterByPeriod("M", mTransactions);
                    } else if (charString.equals("Y")) {
                        transactionFilteredList = filterByPeriod("Y", mTransactions);
                    } else if (charString.equals("T")) {
                        transactionFilteredList = filterByPeriod("T", mTransactions);
                    } else if (charString.equals("W")) {
                        transactionFilteredList = filterByPeriod("W", mTransactions);
                    } else if (charString.startsWith("I")) {
                        transactionFilteredList = filterByType(charString, mTransactions);
                    } else if (charString.equals("E")) {
                        transactionFilteredList = filterByType(charString, mTransactions);
                    } else if (charString.equals("ET")) {
                        transactionFilteredList = filterByType(charString, mTransactions);
                    }else if(memberWise) {
                        transactionFilteredList = filterByMember(charString, mTransactions);
                        memberWise=false;
                    }else
                        transactionFilteredList=search(charString,mTransactions);
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = transactionFilteredList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                    transactionFilteredList = (ArrayList<Transaction>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    private List<Transaction> search(String charString,List<Transaction> transactionList) {
        List resultList = new ArrayList<Transaction>();
        for (Transaction t : transactionList) {
            Member m = AccountLab.get(getActivity()).getAccount(accNo).getMember(t.getMemberId());
            String memberName=null;
            if(m.getMemberName()!=null)
                memberName=m.getMemberName();
            else if(m.getEmailId()!=null)
                memberName=m.getEmailId();
            else if(m.getMobNo()!=null)
                memberName=m.getMobNo();
            if ((t.getType() != null && t.getType().toLowerCase().contains(charString.toLowerCase()))
                    || (t.getDescription() != null && t.getDescription().toLowerCase().contains(charString.toLowerCase()))
                    || (t.getValue() != null && t.getValue().toString().toLowerCase().contains(charString.toLowerCase()))
                    || (t.getDate() != null && t.getDate().toString().toLowerCase().contains(charString.toLowerCase()))
                    || (t.getPaymentMethod() != null && t.getPaymentMethod().toLowerCase().contains(charString.toLowerCase()))
                    || (t.getCategory() != null && t.getCategory().toLowerCase().contains(charString.toLowerCase()))
                    || (t.getPayName() != null && t.getPayName().toLowerCase().contains(charString.toLowerCase()))
                    ||(memberName!= null && memberName.toLowerCase().contains(charString.toLowerCase()))
            ) {
                resultList.add(t);
            }
        }
            return resultList;
        }

        public List<Transaction> filterByType(String type, List<Transaction> transactionList) {
        List expList = new ArrayList<Transaction>();
        for (Transaction t : transactionList) {
            if (t.getType().equals(type)) {
                expList.add(t);
            }
        }
        return expList;
    }

    public List<Transaction> filterByMember(String mname, List<Transaction> transactionList) {
        List transList = new ArrayList<Transaction>();
        Member member=getMember(mname);
        for (Transaction t : transactionList) {
            if (t.getMemberId().equals(member.getMemberId())) {
                transList.add(t);
            }
        }
        return transList;
    }

    public Member getMember(String mname){
        for (Member m : mMemberList) {
            if ((m.getMemberName()!=null &&m.getMemberName().equals(mname)) || (m.getMobNo()!=null && m.getMobNo().equals(mname))
                    ||(m.getEmailId()!=null && m.getEmailId().equals(mname)) ) {
                return m;
            }
        }
        return null;
    }

    public List<Transaction> filterByPeriod(String period, List<Transaction> transactionList) {
        List expList = new ArrayList<Transaction>();
        for (Transaction t : transactionList) {
            Calendar c;
            Date date = new Date(t.getDate());
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
                        expList.add(t);
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
                        expList.add(t);
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
                        expList.add(t);
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
                    expList.add(t);
                }
            }
        }
        return expList;
    }
}
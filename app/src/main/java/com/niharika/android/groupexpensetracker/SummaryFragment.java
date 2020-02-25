package com.niharika.android.groupexpensetracker;


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static java.lang.StrictMath.abs;


/**
 * A simple {@link Fragment} subclass.
 */
public class SummaryFragment extends Fragment {
    class AccountSummary{
        private String mName;
        private Double mIncome,mExpense;
        public String getName() {
            return mName;
        }
        public void setName(String name) {
            mName = name;
        }
        public Double getIncome() {
            return mIncome;
        }

        public void setIncome(Double income) {
            mIncome = income;
        }

        public Double getExpense() {
            return mExpense;
        }

        public void setExpense(Double expense) {
            mExpense = expense;
        }

        public Double getBalance() {
            return mBalance;
        }

        public void setBalance(Double balance) {
            mBalance = balance;
        }

        private Double mBalance;

    }

    private RecyclerView mAccountListRecyclerView;
    private List<Account> mAccountList = new ArrayList<Account>();
    private AccountAdapter mAdapter;
    private int noAccounts = 0;
    private List<AccountSummary> mAcctSummaryList=new ArrayList<AccountSummary>();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((MainActivity) getActivity()).showDrawer(false);
        inflater.inflate(R.menu.fragment_summary, menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:
                Navigation.findNavController(getView()).navigate(R.id.accountTabFragment);
            case R.id.export_as_pdf:
                    if(requestPermission())
                    createandDisplayPdf();
                return true;
                default:
                return true;
        }

    }
    public void setFragmentTitle() {
        getActivity().setTitle(R.string.summary);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        mAccountListRecyclerView = (RecyclerView) view.findViewById(R.id.summary_list_recycler_view);
        mAccountListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    public void onResume() {
        super.onResume();
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
        if (AccountLab.get(getActivity()).getAccountsSize() > noAccounts) {
            if (mAdapter == null) {
                mAdapter = new AccountAdapter(mAccountList);
                mAccountListRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setAccounts(mAccountList);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class AccountHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mAccountName, mTotalIncome, mTotalExpense, mBalance,
                mAccountExpenseValue, mAccountBalanceValue;
        private Account mAccount;
        Double totalExpense = 0.0, totalIncome = 0.0;

        public AccountHolder(@NonNull View itemView) {
            super(itemView);
            mAccountName = (TextView) itemView.findViewById(R.id.acct_name);
            mTotalIncome = (TextView) itemView.findViewById(R.id.total_income);
            mTotalExpense = (TextView) itemView.findViewById(R.id.total_expense);
            mBalance = (TextView) itemView.findViewById(R.id.total_balance);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
        }

        public void bindAccount(Account account) {
            mAccount = account;
            final AccountSummary acc_summ=new AccountSummary();
            mAccountName.setText(mAccount.getAccName().toUpperCase());
            acc_summ.setName(mAccount.getAccName());
            AccountLab.get(getActivity()).getTotalIncome(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    totalIncome = value;
                    mTotalIncome.setText(value.toString());
                    acc_summ.setIncome(value);
                }
            }, "A", null);
            AccountLab.get(getActivity()).getTotalExpense(mAccount, new AccountLab.FirebaseCallbackCalculateTransaction() {
                @Override
                public void onCallback(Double value) {
                    totalExpense = value;
                    mTotalExpense.setText(value.toString());
                    acc_summ.setExpense(value);

                    Double bal = totalIncome - totalExpense;
                    acc_summ.setBalance(bal);
                    if (bal >= 0)
                        mBalance.setTextColor(Color.GREEN);
                    else
                        mBalance.setTextColor(Color.RED);
                    Double absBal = abs(bal);
                    mBalance.setText(absBal.toString());
                mAcctSummaryList.add(acc_summ);

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
            mAcctSummaryList.clear();
        }
        @NonNull
        @Override
        public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_summary, parent, false);
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

    public void createandDisplayPdf() {
        Document doc = new Document();
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Dir";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, getString(R.string.Summary_filename));
            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter.getInstance(doc, fOut);
            doc.open();
            Paragraph p1 = new Paragraph("Account Summary");
            p1.setAlignment(Paragraph.ALIGN_CENTER);
            PdfPTable table = addTableToDoc();
            doc.add(p1);
            doc.add(Chunk.NEWLINE);
            doc.add(table);
        } catch (IOException e) {
            Log.e("PDFCreator", "ioException:" + e);
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            doc.close();
        }
        viewPdf(getString(R.string.Summary_filename), "Dir");
    }

    private PdfPTable addTableToDoc() {
        float [] pointColumnWidths = {150F, 150F, 150F,150F};
        PdfPTable table = new PdfPTable(pointColumnWidths);
        PdfPCell headerCell= new PdfPCell(new Phrase("Name"));
        table.addCell(headerCell);
        headerCell.setPaddingLeft(10f);
        headerCell= new PdfPCell(new Phrase("Income"));
        table.addCell(headerCell);
        headerCell= new PdfPCell(new Phrase("Expense"));
        table.addCell(headerCell);
        headerCell= new PdfPCell(new Phrase("Balance"));
        table.addCell(headerCell);
        table.completeRow();
        table.setHeaderRows(1);
        PdfPCell cell;
        for(AccountSummary s:mAcctSummaryList) {
            cell=new PdfPCell(new Phrase(s.getName().toUpperCase()));
            cell.setPadding(2f);
            table.addCell(cell);
            cell=new PdfPCell(new Phrase(s.getIncome().toString()));
            cell.setPadding(2f);
            table.addCell(cell);
            cell=new PdfPCell(new Phrase(s.getExpense().toString()));
            cell.setPadding(2f);
            table.addCell(cell);
            cell=new PdfPCell(new Phrase(s.getBalance().toString()));
            cell.setPadding(2f);
            table.addCell(cell);
            table.completeRow();
        }
        return table;
    }

    private void viewPdf(String file, String directory) {

        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/" + directory + "/" + file);
        Uri path = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", pdfFile);
        // Setting the intent for pdf reader
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            Log.d(MainFragment.TAG,"error :"+e.toString());
        }
    }

    boolean requestPermission() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
                return false;
            }
        }
        return true;
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]
                {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createandDisplayPdf();
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


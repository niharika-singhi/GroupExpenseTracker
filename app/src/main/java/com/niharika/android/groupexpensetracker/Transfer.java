package com.niharika.android.groupexpensetracker;

public class Transfer {
    private String tId;
    private String incomeId;
    private String expenseId;
    private String accNoFrom,accNoTo;

    public String getAccNoFrom() {
        return accNoFrom;
    }

    public void setAccNoFrom(String accNoFrom) {
        this.accNoFrom = accNoFrom;
    }

    public String getAccNoTo() {
        return accNoTo;
    }

    public void setAccNoTo(String accNoTo) {
        this.accNoTo = accNoTo;
    }

    public Transfer(String tId, String incomeId, String expenseId, String accNo1, String accNo2) {
        this.tId = tId;
        this.incomeId = incomeId;
        this.expenseId = expenseId;
        accNoFrom = accNo1;
        accNoTo = accNo2;
    }

    public Transfer() {
    }


    public String gettId() {
        return tId;
    }

    public void settId(String tId) {
        this.tId = tId;
    }

    public String getIncomeId() {
        return incomeId;
    }

    public void setIncomeId(String incomeId) {
        this.incomeId = incomeId;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }


}

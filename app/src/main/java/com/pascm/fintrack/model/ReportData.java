package com.pascm.fintrack.model;

/**
 * Model class for report summary data
 */
public class ReportData {
    private double totalIncome;
    private double totalExpenses;
    private double balance;
    private int transactionCount;

    public ReportData() {
    }

    public ReportData(double totalIncome, double totalExpenses, double balance, int transactionCount) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.balance = balance;
        this.transactionCount = transactionCount;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}

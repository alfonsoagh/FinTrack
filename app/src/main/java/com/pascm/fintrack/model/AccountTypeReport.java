package com.pascm.fintrack.model;

/**
 * Model class for account type reports (Efectivo, Crédito, Débito)
 */
public class AccountTypeReport {
    private String accountType; // "CASH", "CREDIT", "DEBIT"
    private double amount;
    private int transactionCount;
    private double percentage;

    public AccountTypeReport() {
    }

    public AccountTypeReport(String accountType, double amount, int transactionCount) {
        this.accountType = accountType;
        this.amount = amount;
        this.transactionCount = transactionCount;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountTypeDisplay() {
        if (accountType == null) return "Sin especificar";

        switch (accountType) {
            case "CASH":
                return "Efectivo";
            case "CREDIT":
                return "Tarjeta de Crédito";
            case "DEBIT":
                return "Tarjeta de Débito";
            default:
                return accountType;
        }
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}

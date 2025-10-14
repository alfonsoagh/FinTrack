package com.pascm.fintrack.model;

public class DebitCard {
    private String bank;
    private String alias;
    private String brand; // "visa" o "mastercard"
    private String panLast4; // últimos 4 dígitos
    private String balance; // saldo (opcional)
    private CreditCard.CardGradient gradient;

    public DebitCard(String bank, String alias, String brand, String panLast4, CreditCard.CardGradient gradient) {
        this.bank = bank;
        this.alias = alias;
        this.brand = brand;
        this.panLast4 = panLast4;
        this.gradient = gradient;
        this.balance = "–";
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPanLast4() {
        return panLast4;
    }

    public void setPanLast4(String panLast4) {
        this.panLast4 = panLast4;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public CreditCard.CardGradient getGradient() {
        return gradient;
    }

    public void setGradient(CreditCard.CardGradient gradient) {
        this.gradient = gradient;
    }
}

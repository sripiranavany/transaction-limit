package com.sripiranavan.transactionlimit.model;

public class Transaction {
    private String appId;
    private String appName;
    private String cost;
    private String currency;
    private String msisdn;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", cost='" + cost + '\'' +
                ", currency='" + currency + '\'' +
                ", msisdn='" + msisdn + '\'' +
                '}';
    }
}

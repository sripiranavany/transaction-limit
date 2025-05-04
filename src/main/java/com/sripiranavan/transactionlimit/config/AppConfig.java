package com.sripiranavan.transactionlimit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Value("${transaction.limit.msisdn.delay.millis}")
    private long msisdnDelayMillis;
    @Value("${transaction.limit.tps}")
    private long tps;
    @Value("${transaction.limit.charging.gateway.baseurl}")
    private String chargingGatewayBaseurl;
    @Value("${transaction.limit.charging.gateway.path}")
    private String chargingGatewayPath;

    public long getMsisdnDelayMillis() {
        return msisdnDelayMillis;
    }

    public void setMsisdnDelayMillis(long msisdnDelayMillis) {
        this.msisdnDelayMillis = msisdnDelayMillis;
    }

    public long getTps() {
        return tps;
    }

    public void setTps(long tps) {
        this.tps = tps;
    }

    public String getChargingGatewayBaseurl() {
        return chargingGatewayBaseurl;
    }

    public void setChargingGatewayBaseurl(String chargingGatewayBaseurl) {
        this.chargingGatewayBaseurl = chargingGatewayBaseurl;
    }

    public String getChargingGatewayPath() {
        return chargingGatewayPath;
    }

    public void setChargingGatewayPath(String chargingGatewayPath) {
        this.chargingGatewayPath = chargingGatewayPath;
    }
}
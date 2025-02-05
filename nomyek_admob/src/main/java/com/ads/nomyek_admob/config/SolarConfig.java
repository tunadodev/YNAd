package com.ads.nomyek_admob.config;

public class SolarConfig {
    private boolean enableSolar;
    private String appTokenSolar;
    private boolean enableDebug = false;
    public boolean isEnable() {
        return enableSolar;
    }

    public void setEnable(boolean enableAirBridge) {
        this.enableSolar = enableAirBridge;
    }

    public String getToken() {
        return appTokenSolar;
    }

    public void setToken(String tokenAirBridge) {
        this.appTokenSolar = tokenAirBridge;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean debug) {
        this.enableDebug = debug;
    }
}

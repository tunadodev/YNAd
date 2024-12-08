package com.ads.nomyek.config;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;


public class YNAdConfig {

    //switch mediation use for app
    public static final int PROVIDER_ADMOB = 0;
    public static final int PROVIDER_MAX = 1;


    public static final String ENVIRONMENT_DEVELOP = "develop";
    public static final String ENVIRONMENT_PRODUCTION = "production";

    public static final String DEFAULT_TOKEN_FACEBOOK_SDK = "client_token";
    public static String ADJUST_TOKEN_TIKTOK = "client_token_adjust_tiktok";
    /**
     * config ad mediation using for app
     */
    private int mediationProvider = PROVIDER_ADMOB;
    private int backupMediationProvider = PROVIDER_ADMOB;

    private boolean isVariantDev = false;

    private AirBridgeConfig airBridgeConfig;


    /**
     * eventNamePurchase push event to adjust when user purchased
     */
    private String eventNamePurchase = "";
    private String idAdResume;
    private List<String> listDeviceTest = new ArrayList();

    private Application application;
    private boolean enableAdResume = false;
    private String facebookClientToken = DEFAULT_TOKEN_FACEBOOK_SDK;

    private String adjustTokenTiktok;
    /**
     * intervalInterstitialAd: time between two interstitial ad impressions
     * unit: seconds
     */
    private int intervalInterstitialAd = 0;

    public YNAdConfig(Application application) {
        this.application = application;
    }

    public YNAdConfig(Application application, int mediationProvider, int backupMediationProvider, String environment) {
        this.mediationProvider = mediationProvider;
        this.isVariantDev = environment.equals(ENVIRONMENT_DEVELOP);
        this.application = application;
        this.backupMediationProvider = backupMediationProvider;
    }


    public void setMediationProvider(int mediationProvider) {
        this.mediationProvider = mediationProvider;
    }

    /**
     * @deprecated As of release 5.5.0, replaced by {@link #setEnvironment(String)}
     */
    @Deprecated
    public void setVariant(Boolean isVariantDev) {
        this.isVariantDev = isVariantDev;
    }

    public void setEnvironment(String environment) {
        this.isVariantDev = environment.equals(ENVIRONMENT_DEVELOP);
    }

    public String getEventNamePurchase() {
        return eventNamePurchase;
    }

    public Application getApplication() {
        return application;
    }


    public int getMediationProvider() {
        return mediationProvider;
    }

    public int getBackupMediationProvider() {
        return backupMediationProvider;
    }

    public Boolean isVariantDev() {
        return isVariantDev;
    }


    public String getIdAdResume() {
        return idAdResume;
    }

    public List<String> getListDeviceTest() {
        return listDeviceTest;
    }

    public void setListDeviceTest(List<String> listDeviceTest) {
        this.listDeviceTest = listDeviceTest;
    }


    public void setIdAdResume(String idAdResume) {
        this.idAdResume = idAdResume;
        enableAdResume = true;
    }

    public Boolean isEnableAdResume() {
        return enableAdResume;
    }


    public int getIntervalInterstitialAd() {
        return intervalInterstitialAd;
    }

    public void setIntervalInterstitialAd(int intervalInterstitialAd) {
        this.intervalInterstitialAd = intervalInterstitialAd;
    }

    public void setFacebookClientToken(String token) {
        this.facebookClientToken = token;
    }

    public String getFacebookClientToken() {
        return this.facebookClientToken;
    }

    public String getAdjustTokenTiktok() {
        return adjustTokenTiktok;
    }

    public void setAdjustTokenTiktok(String adjustTokenTiktok) {
        ADJUST_TOKEN_TIKTOK = adjustTokenTiktok;
        this.adjustTokenTiktok = adjustTokenTiktok;
    }

    public AirBridgeConfig getAirBridgeConfig() {
        return airBridgeConfig;
    }

    public void setAirBridgeConfig(AirBridgeConfig airBridgeConfig) {
        this.airBridgeConfig = airBridgeConfig;
    }

    public boolean isEnableAirBridge() {
        if (airBridgeConfig == null)
            return false;
        return airBridgeConfig.isEnableAirBridge();
    }
}

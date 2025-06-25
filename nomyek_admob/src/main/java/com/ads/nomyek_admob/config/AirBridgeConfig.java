package com.ads.nomyek_admob.config;
/**
 * Created by tunado
 */
public class AirBridgeConfig {
    private boolean enableAirBridge;
    private boolean disableAirbridgeLog = false;
    private String appNameAirBridge;
    private String tokenAirBridge;
    private String eventAdClick;
    private String eventAdImpression;
    private String userState;
    private String tagTest;

    public boolean isEnableAirBridge() {
        return enableAirBridge;
    }

    public void setEnableAirBridge(boolean enableAirBridge) {
        this.enableAirBridge = enableAirBridge;
    }

    public void setDisableAirbridgeLog(boolean disableAirbridgeLog) {
        this.disableAirbridgeLog = disableAirbridgeLog;
    }

    public boolean isDisableAirbridgeLog() {
        return disableAirbridgeLog;
    }

    public String getTokenAirBridge() {
        return tokenAirBridge;
    }

    public void setTokenAirBridge(String tokenAirBridge) {
        this.tokenAirBridge = tokenAirBridge;
    }

    public String getEventAdImpression() {
        return eventAdImpression;
    }

    public void setEventAdImpression(String eventAdImpression) {
        this.eventAdImpression = eventAdImpression;
    }

    public String getEventAdClick() {
        return eventAdClick;
    }

    public void setEventAdClick(String eventAdClick) {
        this.eventAdClick = eventAdClick;
    }

    public String getAppNameAirBridge() {
        return appNameAirBridge;
    }

    public void setAppNameAirBridge(String appNameAirBridge) {
        this.appNameAirBridge = appNameAirBridge;
    }

    public String getUserState() {
        return userState;
    }

    public void setUserState(String state) {
        this.userState = state;
    }

    public String getTagTest() {
        return tagTest;
    }

    public void setTagTest(String state) {
        this.tagTest = state;
    }
}

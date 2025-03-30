package com.ads.yeknomadmob.ads_components;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;
import com.ads.yeknomadmob.ads_components.wrappers.AdsNative;
import com.ads.yeknomadmob.ads_components.wrappers.AdsReward;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.ads.yeknomadmob.event.YNMAirBridgeDefaultEvent;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class YNMAdsCallbacks {
    private String format = "";
    
    private YNMAirBridge.AppData appData;
    private double loadStartTime; // Field to track the start time

    public YNMAdsCallbacks() {
        appData = new YNMAirBridge.AppData();
    }

    public YNMAdsCallbacks(YNMAirBridge.AppData appData) {
        this.appData = appData;
    }

    public YNMAdsCallbacks(YNMAirBridge.AppData appData, String format) {
        this.format = format;
        this.appData = appData;
    }

    public YNMAirBridge.AppData getAppData() {
        return appData;
    }

    public void setAppData(YNMAirBridge.AppData appData) {
        this.appData = appData;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void onTimeOut() {
    }

    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdStartLoad() {
        YNMAirBridgeDefaultEvent.pushEventFormatRequestStart(appData, format);
        loadStartTime = System.currentTimeMillis(); // Record the start time
    }

    public void onAdFailedToLoad(@Nullable AdsError adError) {
        double loadTime = System.currentTimeMillis() - loadStartTime; // Calculate load time
        YNMAirBridgeDefaultEvent.pushEventFormatRequestFail(appData, format, Math.ceil(loadTime) / 1000);
    }

    public void onAdFailedToShow(@Nullable AdsError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {
        double loadTime = System.currentTimeMillis() - loadStartTime; // Calculate load time
        loadStartTime = System.currentTimeMillis();
        YNMAirBridgeDefaultEvent.pushEventFormatRequestSuccess(appData, format, Math.ceil(loadTime) / 1000);
    }

    // ad splash loaded when showSplashIfReady = false
    public void onAdSplashReady() {

    }

    public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {

    }

    public void onAdClicked() {
        YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClick(appData, format);
        switch (format) {
            case YNMAds.BANNER:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClickBanner(appData);
                break;
            case YNMAds.NATIVE:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClickNative(appData);
                break;
            case YNMAds.INTERSTITIAL:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClickInter(appData);
        }
    }

    public void onAdImpression() {
        YNMAirBridgeDefaultEvent.pushEventScreenAdFormatView(appData, format);
        switch (format) {
            case YNMAds.BANNER:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatViewBanner(appData);
                break;
            case YNMAds.NATIVE:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatViewNative(appData);
                break;
            case YNMAds.INTERSTITIAL:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatViewInter(appData);
        }
    }

    public void onNativeAdLoaded(@NonNull AdsNative nativeAd) {

    }

    public void onNativeAdLoaded() {

    }

    public void onInterstitialShow() {

    }

    public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {

    }

    public void onRewardAdLoaded(AdsReward rewardedAd) {
    }

    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
    }
    public void onCheckSkipInter(boolean isSkip) {

    }
}

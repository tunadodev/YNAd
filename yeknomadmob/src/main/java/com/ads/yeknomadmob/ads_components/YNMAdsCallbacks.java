package com.ads.yeknomadmob.ads_components;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;
import com.ads.yeknomadmob.ads_components.wrappers.AdsNative;
import com.ads.yeknomadmob.ads_components.wrappers.AdsReward;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.ads.yeknomadmob.event.YNMAirBridgeDefaultEvent;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class YNMAdsCallbacks {
    private String viewName, adsId, format;
    private double loadStartTime; // Field to track the start time

    public YNMAdsCallbacks() {
    }

    public YNMAdsCallbacks(String viewName) {
        this.viewName = viewName;
    }

    public YNMAdsCallbacks(String viewName, String adsId, String format) {
        this.viewName = viewName;
        this.adsId = adsId;
        this.format = format;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void onTimeOut() {
    }

    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdStartLoad() {
        YNMAirBridgeDefaultEvent.pushEventFormatRequestStart(adsId, viewName, format);
        loadStartTime = System.currentTimeMillis(); // Record the start time
    }

    public void onAdFailedToLoad(@Nullable AdsError adError) {
        double loadTime = System.currentTimeMillis() - loadStartTime; // Calculate load time
        YNMAirBridgeDefaultEvent.pushEventFormatRequestFail(adsId, viewName, format, Math.ceil(loadTime) / 1000);
    }

    public void onAdFailedToShow(@Nullable AdsError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {
        double loadTime = System.currentTimeMillis() - loadStartTime; // Calculate load time
        loadStartTime = System.currentTimeMillis();
        YNMAirBridgeDefaultEvent.pushEventFormatRequestSuccess(adsId, viewName, format, Math.ceil(loadTime) / 1000);
    }

    // ad splash loaded when showSplashIfReady = false
    public void onAdSplashReady() {

    }

    public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {

    }

    public void onAdClicked() {
        YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClick(adsId, viewName, format);
        switch (format) {
            case YNMAds.BANNER:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClickBanner(adsId, viewName);
                break;
            case YNMAds.NATIVE:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClickNative(adsId, viewName);
                break;
            case YNMAds.INTERSTITIAL:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatClickInter(adsId, viewName);
        }
    }

    public void onAdImpression() {
        YNMAirBridgeDefaultEvent.pushEventScreenAdFormatView(adsId, viewName, format);
        switch (format) {
            case YNMAds.BANNER:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatViewBanner(adsId, viewName);
                break;
            case YNMAds.NATIVE:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatViewNative(adsId, viewName);
                break;
            case YNMAds.INTERSTITIAL:
                YNMAirBridgeDefaultEvent.pushEventScreenAdFormatViewInter(adsId, viewName);
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
}

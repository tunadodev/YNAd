package com.ads.yeknomadmob.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.max.Max;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class MaxAdsCallback {
    public void onTimeOut() {
    }
    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedSplash() {
    }

    public void onAdFailedToLoad(@Nullable MaxError i) {
//        if (Max.getInstance().getDialog() != null) {
//            Max.getInstance().getDialog().dismiss();
//        }
    }

    public void onAdFailedToLoadHigh(@Nullable LoadAdError i) {
    }

    public void onAdFailedToLoadHighMedium(@Nullable LoadAdError i) {
    }

    public void onAdFailedToLoadAll(@Nullable LoadAdError i) {
    }

    public void onAdFailedToShow(@Nullable MaxError adError) {
    }

    public void onAdFailedToShowHigh(@Nullable AdError adError) {
    }

    public void onAdFailedToShowMedium(@Nullable AdError adError) {
    }

    public void onAdFailedToShowAll(@Nullable AdError adError) {
    }

    public void onAdLeftApplication() {
    }


    public void onAdLoaded() {
    }

    public void onAdLoadedHigh() {
    }

    public void onAdLoadedMedium() {
    }

    public void onAdLoadedAll() {
    }

    public void onAdSplashReady() {
    }

    public void onInterstitialLoad(@Nullable MaxInterstitialAd interstitialAd, @Nullable MaxAd ad) {

    }

    public void onAdClicked() {
    }

    public void onAdClickedHigh() {
    }

    public void onAdClickedMedium() {
    }

    public void onAdClickedAll() {
    }


    public void onAdImpression() {
    }

    public void onRewardAdLoaded(MaxRewardedAd rewardedAd) {
    }

    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
    }


    public void onUnifiedNativeAdLoaded(@NonNull MaxAd maxAd) {

    }

    public void onInterstitialShow() {

    }
}

package com.ads.yeknomadmob.ads_components;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;
import com.ads.yeknomadmob.ads_components.wrappers.AdsNative;
import com.ads.yeknomadmob.ads_components.wrappers.AdsReward;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class YNMAdsCallbacks {
    public void onTimeOut() {
    }
    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@Nullable AdsError adError) {
    }

    public void onAdFailedToShow(@Nullable AdsError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {

    }

    // ad splash loaded when showSplashIfReady = false
    public void onAdSplashReady() {

    }

    public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {

    }

    public void onAdClicked() {
    }

    public void onAdImpression() {
    }


    public void onNativeAdLoaded(@NonNull AdsNative nativeAd) {

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

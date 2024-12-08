package com.ads.nomyek.ads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.nomyek.ads.wrapper.ApAdError;
import com.ads.nomyek.ads.wrapper.ApInterstitialAd;
import com.ads.nomyek.ads.wrapper.ApNativeAd;
import com.ads.nomyek.ads.wrapper.ApRewardItem;

public class YNAdCallback {
    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@Nullable ApAdError adError) {
    }

    public void onAdFailedToShow(@Nullable ApAdError adError) {
    }

    public void onAdLeftApplication() {
    }

    public void onAdLoaded() {

    }

    // ad splash loaded when showSplashIfReady = false
    public void onAdSplashReady() {

    }

    public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {

    }

    public void onAdClicked() {
    }

    public void onAdImpression() {
    }


    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {

    }

    public void onUserEarnedReward(@NonNull ApRewardItem rewardItem) {

    }

    public void onInterstitialShow() {

    }

}

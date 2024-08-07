package com.ads.yeknomadmob.ads_components.wrappers;

import com.google.android.gms.ads.interstitial.InterstitialAd;

public class AdsInterstitial extends AdsBase{
    private InterstitialAd interstitialAd;

    public AdsInterstitial(AdsStatus status) {
        super(status);
    }

    public AdsInterstitial() {
    }

    public AdsInterstitial(InterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
        status = AdsStatus.AD_LOADED;
    }


    public void setInterstitialAd(InterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
        status = AdsStatus.AD_LOADED;
    }


    @Override
    public boolean isReady(){
        return interstitialAd != null;
    }


    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }
}

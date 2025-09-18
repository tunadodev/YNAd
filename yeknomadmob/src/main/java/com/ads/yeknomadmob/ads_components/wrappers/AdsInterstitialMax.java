package com.ads.yeknomadmob.ads_components.wrappers;

import com.applovin.mediation.ads.MaxInterstitialAd;

public class AdsInterstitialMax extends AdsBase{
    private MaxInterstitialAd interstitialAd;

    public AdsInterstitialMax(AdsStatus status) {
        super(status);
    }

    public AdsInterstitialMax() {
    }

    public AdsInterstitialMax(MaxInterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
        status = AdsStatus.AD_LOADED;
    }


    public void setInterstitialAd(MaxInterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
        status = AdsStatus.AD_LOADED;
    }


    @Override
    public boolean isReady(){
        return interstitialAd != null;
    }


    public MaxInterstitialAd getInterstitialAd() {
        return interstitialAd;
    }
}

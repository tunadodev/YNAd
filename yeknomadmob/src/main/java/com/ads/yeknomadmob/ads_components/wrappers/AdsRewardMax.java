package com.ads.yeknomadmob.ads_components.wrappers;

import com.applovin.mediation.ads.MaxRewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;


public class AdsRewardMax extends AdsBase {
    private MaxRewardedAd admobReward;
    private RewardedInterstitialAd admobRewardInter;
//    private MaxRewardedAd maxReward;

    public AdsRewardMax() {
    }

    public AdsRewardMax(AdsStatus status) {
        super(status);
    }

    public void setMaxReward(MaxRewardedAd admobReward) {
        this.admobReward = admobReward;
        status = AdsStatus.AD_LOADED;
    }

    public void setMaxReward(RewardedInterstitialAd admobRewardInter) {
        this.admobRewardInter = admobRewardInter;
    }

//    public void setMaxReward(MaxRewardedAd maxReward) {
//        this.maxReward = maxReward;
//        status = StatusAd.AD_LOADED;
//    }

//    public ApRewardAd(MaxRewardedAd maxReward) {
//        this.maxReward = maxReward;
//        status = StatusAd.AD_LOADED;
//    }

    public AdsRewardMax(RewardedInterstitialAd admobRewardInter) {
        this.admobRewardInter = admobRewardInter;
        status = AdsStatus.AD_LOADED;
    }

    public AdsRewardMax(MaxRewardedAd admobReward) {
        this.admobReward = admobReward;
        status = AdsStatus.AD_LOADED;
    }


    public MaxRewardedAd getMaxReward() {
        return admobReward;
    }

    public RewardedInterstitialAd getMaxRewardInter() {
        return admobRewardInter;
    }

//    public MaxRewardedAd getMaxReward() {
//        return maxReward;
//    }

    /**
     * Clean reward when shown
     */
    public void clean() {
//        maxReward = null;
        admobReward = null;
        admobRewardInter = null;
    }

    @Override
    public boolean isReady() {
        return admobReward != null ||admobRewardInter != null;
    }

    public boolean isRewardInterstitial(){
        return admobRewardInter != null;
    }
}
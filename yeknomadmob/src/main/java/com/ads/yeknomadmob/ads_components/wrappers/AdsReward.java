package com.ads.yeknomadmob.ads_components.wrappers;

import com.applovin.mediation.ads.MaxRewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;


public class AdsReward extends AdsBase {
    private RewardedAd admobReward;
    private RewardedInterstitialAd admobRewardInter;
//    private MaxRewardedAd maxReward;

    public AdsReward() {
    }

    public AdsReward(AdsStatus status) {
        super(status);
    }

    public void setAdmobReward(RewardedAd admobReward) {
        this.admobReward = admobReward;
        status = AdsStatus.AD_LOADED;
    }

    public void setAdmobReward(RewardedInterstitialAd admobRewardInter) {
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

    public AdsReward(RewardedInterstitialAd admobRewardInter) {
        this.admobRewardInter = admobRewardInter;
        status = AdsStatus.AD_LOADED;
    }

    public AdsReward(RewardedAd admobReward) {
        this.admobReward = admobReward;
        status = AdsStatus.AD_LOADED;
    }


    public RewardedAd getAdmobReward() {
        return admobReward;
    }

    public RewardedInterstitialAd getAdmobRewardInter() {
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
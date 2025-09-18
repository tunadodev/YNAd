package com.ads.yeknomadmob.ads_components.wrappers;

import com.applovin.mediation.MaxReward;

public class AdsRewardItemMax {

    private MaxReward admobRewardItem;
    //private MaxReward maxRewardItem;

    //public ApRewardItem(MaxReward maxRewardItem) {
//        this.maxRewardItem = maxRewardItem;
//    }

    public AdsRewardItemMax(MaxReward admobRewardItem) {
        this.admobRewardItem = admobRewardItem;
    }

    public MaxReward getAdmobRewardItem() {
        return admobRewardItem;
    }

    public void setAdmobRewardItem(MaxReward admobRewardItem) {
        this.admobRewardItem = admobRewardItem;
    }

//    public MaxReward getMaxRewardItem() {
//        return maxRewardItem;
//    }

//    public void setMaxRewardItem(MaxReward maxRewardItem) {
//        this.maxRewardItem = maxRewardItem;
//    }
}
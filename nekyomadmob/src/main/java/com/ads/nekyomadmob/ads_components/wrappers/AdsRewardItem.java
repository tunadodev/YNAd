package com.ads.nekyomadmob.ads_components.wrappers;

import com.google.android.gms.ads.rewarded.RewardItem;

public class AdsRewardItem {

    private RewardItem admobRewardItem;
    //private MaxReward maxRewardItem;

    //public ApRewardItem(MaxReward maxRewardItem) {
//        this.maxRewardItem = maxRewardItem;
//    }

    public AdsRewardItem(RewardItem admobRewardItem) {
        this.admobRewardItem = admobRewardItem;
    }

    public RewardItem getAdmobRewardItem() {
        return admobRewardItem;
    }

    public void setAdmobRewardItem(RewardItem admobRewardItem) {
        this.admobRewardItem = admobRewardItem;
    }

//    public MaxReward getMaxRewardItem() {
//        return maxRewardItem;
//    }

//    public void setMaxRewardItem(MaxReward maxRewardItem) {
//        this.maxRewardItem = maxRewardItem;
//    }
}
package com.ads.yeknomadmob.utils;


import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;

public interface RewardCallbackMax {
    void onUserEarnedReward(  MaxReward var1);
    void onRewardedAdClosed(  );
    void onRewardedAdFailedToShow(MaxError error);
    void onAdClicked();
}

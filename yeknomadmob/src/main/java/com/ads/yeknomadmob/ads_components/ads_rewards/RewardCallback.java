package com.ads.yeknomadmob.ads_components.ads_rewards;


import com.google.android.gms.ads.rewarded.RewardItem;

public interface   RewardCallback {
    void onUserEarnedReward(  RewardItem var1);
    void onRewardedAdClosed(  );
    void onRewardedAdFailedToShow(int codeError  );
    void onAdClicked();
}

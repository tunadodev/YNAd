package com.ads.demo;

import com.ads.demo.databinding.ActivityMainBinding;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;
import com.ads.nomyek_admob.ads_components.ads_native.YNMNativeAdView;
import com.ads.nomyek_admob.utils.AdsInterPreload;
import com.ads.nomyek_admob.utils.AdsNativePreload;
import com.ads.nomyek_admob.utils.AdsRewardPreload;

public class MainActivity extends BaseActivity<ActivityMainBinding>{
    private YNMNativeAdView ynmAdNative = null;
    @Override
    protected int getLayoutActivity() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        viewBinding.bannerView.loadBanner(this, BuildConfig.ad_banner);
        AdsInterPreload.preloadInterAds(this, BuildConfig.ad_interstitial_splash, "test");
        AdsRewardPreload.preloadRewardAds(this, BuildConfig.ad_reward, "test_reward");
        //AdsInterPreload.preloadInterAds(this, BuildConfig.ad_interstitial_splash, "test2");
//        YNMAds.getInstance().setInitCallback(() -> {
//            viewBinding.ykmNativeAds.loadNativeAd(this, BuildConfig.ad_native);
//        }) ;
        AdsNativePreload.flexPreloadedShowNativeAds(this, viewBinding.ykmNativeAds, "test", com.ads.nomyek_admob.R.layout.custom_native_admob_medium, com.ads.nomyek_admob.R.layout.custom_native_admob_large, BuildConfig.ad_interstitial_splash, BuildConfig.ad_native);


        viewBinding.showInter.setOnClickListener(view -> {
            AdsInterPreload.showPreloadInterAds(this, "test", "test2", BuildConfig.ad_interstitial_splash, 5000, new YNMAdsCallbacks() {
                @Override
                public void onNextAction() {
                    super.onNextAction();

                    AdsInterPreload.preloadInterAds(MainActivity.this, BuildConfig.ad_interstitial_splash, "test");
                    AdsInterPreload.preloadInterAds(MainActivity.this, BuildConfig.ad_interstitial_splash, "test2");
                }
            });
//            AdsRewardPreload.showRewardPreload(this,"test_reward", BuildConfig.ad_reward, 5000, new YNMAdsCallbacks() {
//                @Override
//                public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
//                    super.onUserEarnedReward(rewardItem);
//                }
//
//                @Override
//                public void onAdFailedToLoad(@Nullable AdsError adError) {
//                    super.onAdFailedToLoad(adError);
//                }
//            });
        });
    }

}
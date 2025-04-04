package com.ads.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.demo.databinding.ActivityMainBinding;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeAdView;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.ads.yeknomadmob.utils.AdsInterPreload;
import com.ads.yeknomadmob.utils.AdsNativePreload;
import com.ads.yeknomadmob.utils.AdsRewardPreload;
import com.ads.yeknomadmob.utils.AdsUnitItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding>{
    private YNMNativeAdView ynmAdNative = null;
    @Override
    protected int getLayoutActivity() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        viewBinding.bannerView.loadBanner(this, BuildConfig.ad_banner);
//        AdsInterPreload.preloadInterAds(this,new YNMAirBridge.AppData(), BuildConfig.ad_interstitial_splash, "test", 5000);
        AdsRewardPreload.preloadRewardAds(this,new YNMAirBridge.AppData(), BuildConfig.ad_reward, "test_reward", 5000);
        //AdsInterPreload.preloadInterAds(this, BuildConfig.ad_interstitial_splash, "test2");
//        YNMAds.getInstance().setInitCallback(() -> {
//            viewBinding.ykmNativeAds.loadNativeAd(this, BuildConfig.ad_native);
//        }) ;
//        AdsNativePreload.flexPreloadedShowNativeAds(this, viewBinding.ykmNativeAds, com.ads.yeknomadmob.R.layout.custom_native_admob_medium, com.ads.yeknomadmob.R.layout.custom_native_admob_large, BuildConfig.ad_interstitial_splash, BuildConfig.ad_native);


        viewBinding.showInter.setOnClickListener(view -> {
//            AdsInterPreload.showPreloadInterAds(this, "test", BuildConfig.ad_interstitial_splash, 5000, new YNMAdsCallbacks() {
//                @Override
//                public void onNextAction() {
//                    super.onNextAction();
//                    AdsInterPreload.preloadInterAds(MainActivity.this,new YNMAirBridge.AppData(), BuildConfig.ad_interstitial_splash, "test", 5000);
//                }
//            });
//            AdsRewardPreload.showRewardPreload(this,"test_reward", BuildConfig.ad_reward, 5000, new YNMAdsCallbacks() {
//                @Override
//                public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
//                    super.onUserEarnedReward(rewardItem);
//                    AdsRewardPreload.preloadRewardAds(MainActivity.this,new YNMAirBridge.AppData(), BuildConfig.ad_reward, "test_reward", 5000);
//
//                }
//
//                @Override
//                public void onAdFailedToLoad(@Nullable AdsError adError) {
//                    super.onAdFailedToLoad(adError);
//                }
//            });
            List<AdsUnitItem> adUnits = new ArrayList<>();
            adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key1"));
            adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
            AdsInterPreload.showPreloadMultipleInterAds(this, adUnits, 10000, new YNMAdsCallbacks() {
                @Override
                public void onAdClosed() {
                    // Xử lý khi quảng cáo đóng
                }

                @Override
                public void onNextAction() {
                    // Tiếp tục luồng
                }
            });
        });
        List<AdsUnitItem> adUnits = new ArrayList<>();
        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key1"));
        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
        AdsInterPreload.preloadMultipleInterAds(this, new YNMAirBridge.AppData("","list"), adUnits, 10000);

// Khi cần hiển thị quảng cáo

    }

}
package com.ads.demo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ads.demo.databinding.ActivityMainBinding;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMBannerCollapse;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMMultiFloorBannerAds;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMMultiFloorBannerLargeAds;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeAdView;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.ads.yeknomadmob.utils.AdsInterPreload;
import com.ads.yeknomadmob.utils.AdsRewardPreload;
import com.ads.yeknomadmob.utils.AdsUnitItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private static final String TAG = "MainActivityDemo";
    private YNMNativeAdView ynmAdNative = null;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshBannerRunnable;
    private boolean isBannerRefreshing = false;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected int getLayoutActivity() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        // --- Demo for Normal Banner (YNMBannerAdView) ---
        Log.d(TAG, "Loading Normal Banner Ad...");


        viewBinding.collapseBanner.loadBannerCollapse(this, () -> {
            viewBinding.bannerView.showAd(this, new YNMAdsCallbacks() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.d(TAG, "Normal Banner Ad loaded successfully.");
                }

                @Override
                public void onAdFailedToShow(AdsError error) {
                    super.onAdFailedToShow(error);
                    Log.e(TAG, "Normal Banner Ad failed to show: " + error.getMessage());
                }
            });
        });



        // --- Existing Interstitial and Reward Ad Logic ---
        AdsRewardPreload.preloadRewardAds(this, new YNMAirBridge.AppData(), BuildConfig.ad_reward, "test_reward", 6000);

        viewBinding.showInter.setOnClickListener(view -> {
            List<AdsUnitItem> adUnits = new ArrayList<>();
            adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key1"));
            adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
            AdsInterPreload.showPreloadMultipleInterAds(this, adUnits, 10000, new YNMAdsCallbacks() {
                @Override
                public void onAdClosed() {
                    // Handle ad closed
                }
            });
        });

        List<AdsUnitItem> adUnits = new ArrayList<>();
        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key1"));
        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
        AdsInterPreload.preloadMultipleInterAds(this, new YNMAirBridge.AppData("", "list"), adUnits, 10000);
    }

}

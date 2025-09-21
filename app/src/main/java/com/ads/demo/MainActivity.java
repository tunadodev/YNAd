package com.ads.demo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.demo.databinding.ActivityMainBinding;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMBannerAdView;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMBannerCollapse;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeAdView;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.ads.yeknomadmob.utils.AdsInterPreload;
import com.ads.yeknomadmob.utils.AdsRewardPreload;
import com.ads.yeknomadmob.utils.AdsUnitItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private YNMNativeAdView ynmAdNative = null;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshBannerRunnable;
    private boolean isBannerRefreshing = false;

    public void showBannerMultiId(String id, String unitName) {
        if (findViewById(R.id.bannerView) != null) {
            YNMAds.getInstance().setInitCallback(() -> {
                YNMBannerAdView bannerAdView = findViewById(R.id.bannerView);
                List<String> ids = new ArrayList<>();
                ids.add(id);
                ids.add(id);
//                bannerAdView.loadMultiIdBanner(this, ids, new YNMAdsCallbacks(new YNMAirBridge.AppData("nameView", unitName), YNMAds.BANNER));
            });
        }
    }

    public void startBannerRefresh(String id, String unitName) {
        if (isBannerRefreshing) {
            return; // Refresh loop is already running
        }

        refreshBannerRunnable = new Runnable() {
            @Override
            public void run() {
                // Load the banner
                showBannerMultiId(id, unitName);
                // Schedule the next refresh after 30 seconds
                refreshHandler.postDelayed(this, 30000); // 30 seconds
            }
        };

        // Start the refresh loop immediately
        refreshHandler.post(refreshBannerRunnable);
        isBannerRefreshing = true;
    }

    public void stopBannerRefresh() {
        if (refreshBannerRunnable != null) {
            refreshHandler.removeCallbacks(refreshBannerRunnable);
        }
        isBannerRefreshing = false;
    }

    @Override
    protected void onPause() {
        stopBannerRefresh();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopBannerRefresh();
        super.onDestroy();
    }


    @Override
    protected int getLayoutActivity() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
//        ArrayList<String> listAds = new ArrayList();
//        listAds.add(BuildConfig.ad_banner);
//        listAds.add(BuildConfig.ad_banner);
////        viewBinding.collapseBanner.loadBannerCollapse(this, listAds, new YNMBannerCollapse.OnCollapseListener() {
////            @Override
////            public void onBannerCollapsed() {
////                startBannerRefresh(BuildConfig.ad_banner, "test");
////            }
////        });
//
//        viewBinding.loadInter.setOnClickListener(view -> {
//            AdsRewardPreload.preloadRewardAds(this, new YNMAirBridge.AppData(), BuildConfig.ad_reward, "test_reward", 6000);
//            AdsRewardPreload.preloadRewardAdsMax(this, new YNMAirBridge.AppData(), BuildConfig.reward_max, "test_reward", 6000);
//        });
//
//        viewBinding.showInter.setOnClickListener(view -> {
//            AdsRewardPreload.showRewardPreload(this, "test_reward", new YNMAdsCallbacks() {
//
//            });
////            List<AdsUnitItem> adUnits = new ArrayList<>();
////            adUnits.add(new AdsUnitItem(BuildConfig.inter_max, "key4"));
////            adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
////
////            AdsInterPreload.showPreloadInterAds(this, adUnits, 10000, new YNMAdsCallbacks() {
////                @Override
////                public void onAdClosed() {
////                    // Xử lý khi quảng cáo đóng
////                }
////
////                @Override
////                public void onNextAction() {
////                    super.onNextAction();
////
////                }
////            });
//        });
////        List<AdsUnitItem> adUnits = new ArrayList<>();
////        adUnits.add(new AdsUnitItem(BuildConfig.ad_native, "key1"));
////        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
//        AdsUnitItem x = new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3");
//        AdsUnitItem y = new AdsUnitItem(BuildConfig.inter_max, "key4");
//        AdsInterPreload.preloadMax(this, new YNMAirBridge.AppData("", "list1"), y, 10000, null);
//        AdsInterPreload.preload(this, new YNMAirBridge.AppData("", "list"), x, 10000, null);
    }

}

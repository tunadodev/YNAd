package com.ads.demo;

import static android.view.View.VISIBLE;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.ads.demo.databinding.ActivityMainBinding;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMBannerAdView;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMBannerCollapse;
import com.ads.yeknomadmob.ads_components.ads_native.AdsNativePreload;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeAdView;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeCollapse;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.ads.yeknomadmob.ads_components.ads_inters.AdsInterPreload;
import com.ads.yeknomadmob.ads_components.ads_rewards.AdsRewardPreload;
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

        showFCollapsibleNative();
//        showFCollapsibleBanner();

        List<AdsUnitItem> adUnits = new ArrayList<>();
        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key1"));
        adUnits.add(new AdsUnitItem(BuildConfig.ad_interstitial_splash, "key3"));
        AdsInterPreload.preloadMultipleInterAds(this, new YNMAirBridge.AppData("", "list"), adUnits, 10000);
    }

    private void showFCollapsibleBanner() {
        if (findViewById(R.id.banner_small) != null) {
            findViewById(R.id.banner_small).setVisibility(View.INVISIBLE);
        }

        if (findViewById(R.id.banner_large) != null) {
            //preload banner small
            YNMBannerAdView adView2 = findViewById(R.id.banner_small);
            YNMBannerCollapse adView = findViewById(R.id.banner_large);
            adView.setVisibility(VISIBLE);
            YNMAds.getInstance().setInitCallback(() -> {
                adView.loadBannerCollapse(this, SplashActivity.banner_large_manager, new YNMBannerCollapse.OnCollapseListener() {
                    @Override
                    public void onBannerCollapsed() {
                        adView2.showAd(MainActivity.this, SplashActivity.banner_small_manager, new YNMAdsCallbacks(new YNMAirBridge.AppData("nameView", "banner"), YNMAds.BANNER));
                    }
                });
            });
        }
    }

    private void showFCollapsibleNative() {
        if (findViewById(R.id.native_banner) != null) {
            findViewById(R.id.native_banner).setVisibility(View.INVISIBLE);
        }
        if (findViewById(R.id.native_collapse) != null) {
            YNMNativeAdView nativeBanner = findViewById(R.id.native_banner);
            YNMNativeCollapse nativeCollapse= findViewById(R.id.native_collapse);
            nativeCollapse.loadNativeCollapse(this, SplashActivity.native_large_manager, () -> {
                nativeBanner.showAd(this,  SplashActivity.native_small_manager, new YNMAdsCallbacks(){
                    @Override
                    public void onAdFailedToShow(@Nullable AdsError adError) {
                        super.onAdFailedToShow(adError);
                        nativeBanner.setVisibility(View.GONE);
                    }
                });
            });
        }
    }
}

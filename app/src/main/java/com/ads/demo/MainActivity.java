package com.ads.demo;

import androidx.annotation.Nullable;

import com.ads.demo.databinding.ActivityMainBinding;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeAdView;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;

public class MainActivity extends BaseActivity<ActivityMainBinding>{
    private YNMNativeAdView ynmAdNative = null;
    @Override
    protected int getLayoutActivity() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        viewBinding.bannerView.loadBanner(this, BuildConfig.ad_banner);
        YNMAds.getInstance().setInitCallback(() -> {
            viewBinding.ykmNativeAds.loadNativeAd(this, BuildConfig.ad_native);
        }) ;

        viewBinding.showInter.setOnClickListener(view -> {
            YNMAds.getInstance().loadInterstitialAds(MainActivity.this, BuildConfig.ad_interstitial_splash, 5000, 0, true, new YNMAdsCallbacks(){
                @Override
                public void onNextAction() {

                }
            });
        });
    }

}
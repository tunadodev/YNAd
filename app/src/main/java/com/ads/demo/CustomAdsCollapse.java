package com.ads.demo;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.demo.databinding.LayoutAdsCollapseCompBinding;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;

public class CustomAdsCollapse extends RelativeLayout {
    private String TAG = "CustomAdsCollapse";
    private LayoutAdsCollapseCompBinding viewBinding;

    public CustomAdsCollapse(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CustomAdsCollapse(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomAdsCollapse(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        viewBinding = LayoutAdsCollapseCompBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void showTheBannerCollapseComp(Activity context){
        viewBinding.collapseBanner.loadBannerCollapse(context, SplashActivity.banner_large_manager, () -> {
            viewBinding.bannerView.showAd(context, SplashActivity.banner_small_manager, new YNMAdsCallbacks() {
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
    }

    public void showTheNativeCollapseComp(Activity context) {
        viewBinding.nativeLarge.loadNativeCollapse(context,SplashActivity.native_large_manager, () -> {
            viewBinding.nativeAdView.showAd(context, SplashActivity.native_small_manager, new YNMAdsCallbacks() {
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
    }
}

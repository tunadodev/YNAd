package com.ads.yeknomadmob.ads_components.ads_banner;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;

public class YNMBannerAdView extends RelativeLayout {

    private String TAG = "YNMBannerAdView";
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    public YNMBannerAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public YNMBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public YNMBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public YNMBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_banner_view, this);
    }

    public void loadAd(Activity activity) {
        loadAd(activity, new YNMAdsCallbacks());
    }

    public void loadAd(Activity activity, YNMAdsCallbacks ynmAdsCallbacks) {
        YNMMultiFloorBannerAds.getInstance().showMFBannerAd(this, ynmAdsCallbacks);
    }

    public void loadAd(Activity activity, int refreshInterval, YNMAdsCallbacks ynmAdsCallbacks) {
        loadAd(activity, ynmAdsCallbacks);

        if (refreshInterval > 0) {
            refreshRunnable = () -> {
                loadAd(activity, ynmAdsCallbacks);
                refreshHandler.postDelayed(refreshRunnable, refreshInterval);
            };
            refreshHandler.postDelayed(refreshRunnable, refreshInterval);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}

package com.ads.nomyek_admob.ads_components.ads_banner;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.nomyek_admob.R;
import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;

import java.util.List;

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

    public void loadBanner(Activity activity, String idBanner) {
        loadBanner(activity, idBanner, new YNMAdsCallbacks());
    }

    public void loadBanner(Activity activity, String idBanner, YNMAdsCallbacks ynmAdsCallbacks) {
        YNMAds.getInstance().loadBanner(activity, idBanner, ynmAdsCallbacks);
    }

    public void loadMultiIdBanner(Activity activity, List<String> idBanner, YNMAdsCallbacks ynmAdsCallbacks) {
        YNMAds.getInstance().loadMultiIdBanner(activity, idBanner, ynmAdsCallbacks);
    }

    public void loadMultiIdBanner(Activity activity, List<String> idBanner, int refreshInterval, YNMAdsCallbacks ynmAdsCallbacks) {
        YNMAds.getInstance().loadMultiIdBanner(activity, idBanner, ynmAdsCallbacks);

        if (refreshInterval > 0) {
            refreshRunnable = () -> {
                YNMAds.getInstance().loadMultiIdBanner(activity, idBanner, ynmAdsCallbacks);
                refreshHandler.postDelayed(refreshRunnable, refreshInterval);
            };
            refreshHandler.postDelayed(refreshRunnable, refreshInterval);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        refreshHandler.removeCallbacks(refreshRunnable);
    }
}
package com.ads.yeknomadmob.ads_components.ads_banner;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;

public class YNMBannerAdView extends RelativeLayout {

    private String TAG = "YNMBannerAdView";

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
}
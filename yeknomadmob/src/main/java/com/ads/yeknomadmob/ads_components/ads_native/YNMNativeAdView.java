package com.ads.yeknomadmob.ads_components.ads_native;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsNative;
import com.facebook.shimmer.ShimmerFrameLayout;

public class YNMNativeAdView extends RelativeLayout {

    private int layoutCustomNativeAd = 0;
    private ShimmerFrameLayout layoutLoading;
    private FrameLayout layoutPlaceHolder;
    private String TAG = "YNMNativeAdView";
    private boolean isContentMatchParent = false;

    public enum TYPE_NATIVE_ADS {
        DEFAULT,
        LARGER
    }

    public YNMNativeAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public YNMNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public YNMNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public YNMNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.YNMNativeAdView, 0, 0);
        // Get layout native view custom and  layout loading
        layoutCustomNativeAd = typedArray.getResourceId(R.styleable.YNMNativeAdView_layoutCustomNativeAd, 0);
        int idLayoutLoading = typedArray.getResourceId(R.styleable.YNMNativeAdView_layoutLoading, 0);
        isContentMatchParent = typedArray.getBoolean(R.styleable.YNMNativeAdView_is_content_match_parent, true);
        if (idLayoutLoading != 0)
            layoutLoading = (ShimmerFrameLayout) LayoutInflater.from(getContext()).inflate(idLayoutLoading, null);

        init();
    }

    private void init() {
        layoutPlaceHolder = new FrameLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, isContentMatchParent ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT);
        addView(layoutPlaceHolder, layoutParams);
        if (layoutLoading != null)
            addView(layoutLoading, layoutParams);
    }

    public void setLayoutCustomNativeAd(int layoutCustomNativeAd) {
        this.layoutCustomNativeAd = layoutCustomNativeAd;
    }

    public void setLayoutLoading(int idLayoutLoading) {
        if (layoutLoading != null) {
            removeView(layoutLoading);
        }
        this.layoutLoading = (ShimmerFrameLayout) LayoutInflater.from(getContext()).inflate(idLayoutLoading, null);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(layoutLoading, layoutParams);
    }

    public void populateNativeAdView(Activity activity, AdsNative nativeAd) {
        if (layoutLoading == null) {
            Log.e(TAG, "populateNativeAdView error : layoutLoading not set");
            return;
        }
        YNMAds.getInstance().populateNativeAdView(activity, nativeAd, layoutPlaceHolder, layoutLoading);
    }

    public void loadNativeAd(Activity activity, String idAd) {
        loadNativeAd(activity, idAd, new YNMAdsCallbacks() {
        });
    }

    public void loadNativeAd(Activity activity, String idAd, YNMAdsCallbacks YNMAdsCallbacks) {
        if (layoutLoading == null) {
            setLayoutLoading(R.layout.loading_native_medium);
        }
        if (layoutCustomNativeAd == 0) {
            layoutCustomNativeAd = R.layout.custom_native_admob_medium;
            setLayoutCustomNativeAd(layoutCustomNativeAd);
        }
        YNMAds.getInstance().loadNativeAd(activity, idAd, layoutCustomNativeAd, layoutPlaceHolder, layoutLoading, YNMAdsCallbacks);
    }
    public void loadNativeAdWithType(Activity activity, String idAd, YNMAdsCallbacks yNMAdsCallbacks, TYPE_NATIVE_ADS type) {
        switch (type) {
            case LARGER:
                layoutCustomNativeAd = R.layout.custom_native_admob_large;
                setLayoutCustomNativeAd(layoutCustomNativeAd);
                break;
            default:
                if (layoutLoading == null) {
                    setLayoutLoading(R.layout.loading_native_medium);
                }
                if (layoutCustomNativeAd == 0) {
                    layoutCustomNativeAd = R.layout.custom_native_admob_medium;
                    setLayoutCustomNativeAd(layoutCustomNativeAd);
                }
                break;
        }

        YNMAds.getInstance().loadNativeAd(activity, idAd, layoutCustomNativeAd, layoutPlaceHolder, layoutLoading, yNMAdsCallbacks);
    }

    public void loadNativeAd(Activity activity, String idAd, int layoutCustomNativeAd, int idLayoutLoading) {
        setLayoutLoading(idLayoutLoading);
        setLayoutCustomNativeAd(layoutCustomNativeAd);
        loadNativeAd(activity, idAd);
    }

    public void loadNativeAd(Activity activity, String idAd, int layoutCustomNativeAd, int idLayoutLoading, YNMAdsCallbacks yNMAdsCallbacks) {
        setLayoutLoading(idLayoutLoading);
        setLayoutCustomNativeAd(layoutCustomNativeAd);
        loadNativeAd(activity, idAd, yNMAdsCallbacks);
    }

    public void showAd(Activity activity, YNMMultiFloorNativeAds ads_operation, YNMAdsCallbacks ynmAdsCallbacks) {
        this.setVisibility(VISIBLE);
        if (layoutLoading == null) {
            setLayoutLoading(R.layout.loading_native_medium);
        }
        if (layoutCustomNativeAd == 0) {
            layoutCustomNativeAd = R.layout.custom_native_admob_medium;
            setLayoutCustomNativeAd(layoutCustomNativeAd);
        }
        ads_operation.showMFNativeAd(activity, layoutCustomNativeAd, this, ynmAdsCallbacks);
    }
}
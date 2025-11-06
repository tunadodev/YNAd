package com.ads.yeknomadmob.ads_components.ads_native;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.databinding.CollapsibleNativeAdsBinding;

public class YNMNativeCollapse extends RelativeLayout {

    private static final String TAG = "YNMNativeCollapse";
    private CollapsibleNativeAdsBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable collapseRunnable = this::collapseAd;
    private int showTime = 5000; // Default show time
    private boolean isAdShowing = false;
    private int layoutCustomNativeAd = 0;

    private OnCollapseListener onCollapseListener;

    public interface OnCollapseListener {
        void onNativeCollapsed();
    }

    public YNMNativeCollapse(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public YNMNativeCollapse(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public YNMNativeCollapse(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding = CollapsibleNativeAdsBinding.inflate(LayoutInflater.from(context), this, true);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AdsCollapse);
            showTime = a.getInt(R.styleable.AdsCollapse_show_time, 5000);

            a = getContext().obtainStyledAttributes(attrs, R.styleable.YNMNativeAdView);
            layoutCustomNativeAd = a.getResourceId(R.styleable.YNMNativeAdView_layoutCustomNativeAd, R.layout.custom_native_admob_medium);
            a.recycle();
        } else {
            layoutCustomNativeAd = R.layout.custom_native_admob_medium;
        }

        binding.collapseBtn.setOnClickListener(v -> collapseAd());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(collapseRunnable);
    }

    public void setOnCollapseListener(OnCollapseListener onCollapseListener) {
        this.onCollapseListener = onCollapseListener;
    }

    public void loadNativeCollapse(Activity activity, YNMMultiFloorNativeAds nativeAdsManager, OnCollapseListener onCollapseListener) {
        if (isAdShowing) {
            Log.d(TAG, "Ad is already showing. Refresh is ignored.");
            return;
        }

        handler.removeCallbacks(collapseRunnable);

        this.setVisibility(View.VISIBLE);
        binding.nativeCollapse.setVisibility(View.VISIBLE);
        binding.collapseBtn.setVisibility(View.GONE);

        this.onCollapseListener = onCollapseListener;

        nativeAdsManager.showMFNativeAd(activity, layoutCustomNativeAd, binding.nativeCollapse, new YNMAdsCallbacks() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                isAdShowing = true;
                binding.collapseBtn.setVisibility(View.VISIBLE);
                startCollapseTimer();
            }

            @Override
            public void onAdFailedToShow(AdsError error) {
                super.onAdFailedToShow(error);
                Log.e(TAG, "Failed to load collapse native from multi-floor manager: " + error.getMessage());
                collapseAd();
            }
        });
    }

    private void startCollapseTimer() {
        handler.postDelayed(collapseRunnable, showTime);
    }

    private void collapseAd() {
        isAdShowing = false;
        handler.removeCallbacks(collapseRunnable);
        binding.nativeCollapse.setVisibility(View.GONE);
        binding.collapseBtn.setVisibility(View.GONE);
        this.setVisibility(View.GONE);

        if (onCollapseListener != null) {
            onCollapseListener.onNativeCollapsed();
        }
    }
    
    public YNMNativeAdView getNativeAdView() {
        return binding.nativeCollapse;
    }
}

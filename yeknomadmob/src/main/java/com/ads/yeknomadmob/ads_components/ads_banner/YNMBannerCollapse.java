package com.ads.yeknomadmob.ads_components.ads_banner;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;

public class YNMBannerCollapse extends RelativeLayout {

    private String TAG = "YNMBannerCollapse";
    private FrameLayout largeBannerContainer;
    private MaterialCardView collapseButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable collapseRunnable = this::collapseBanner;
    private ShimmerFrameLayout containerShimmer;
    private int showTime = 5000; // Default show time in milliseconds
    private boolean isAdShowing = false;

    private OnCollapseListener onCollapseListener;

    public interface OnCollapseListener {
        void onBannerCollapsed();
    }

    public YNMBannerCollapse(@NonNull Context context) {
        super(context);
        init(null);
    }

    public YNMBannerCollapse(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public YNMBannerCollapse(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public YNMBannerCollapse(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.layout_banner_collapse, this);
        largeBannerContainer = findViewById(R.id.large_banner);
        collapseButton = findViewById(R.id.collapse_button);
        containerShimmer = findViewById(R.id.shimmer_container_banner_large);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.YNMBannerCollapse);
            showTime = a.getInt(R.styleable.YNMBannerCollapse_show_time, 5000);
            a.recycle();
        }

        collapseButton.setOnClickListener(v -> collapseBanner());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(collapseRunnable);
    }

    public void setOnCollapseListener(OnCollapseListener onCollapseListener) {
        this.onCollapseListener = onCollapseListener;
    }

    public void loadBannerCollapse(Activity activity, OnCollapseListener onCollapseListener) {
        if (isAdShowing) {
            Log.d(TAG, "Ad is already showing. Refresh is ignored.");
            return;
        }

        handler.removeCallbacks(collapseRunnable);

        this.setVisibility(View.VISIBLE);
        largeBannerContainer.setVisibility(View.VISIBLE);
        collapseButton.setVisibility(View.GONE);
        containerShimmer.startShimmer();
        containerShimmer.setVisibility(View.VISIBLE);


        this.onCollapseListener = onCollapseListener;

        YNMMultiFloorBannerLargeAds.getInstance().showMFBannerAd(largeBannerContainer, new YNMAdsCallbacks() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                isAdShowing = true;
                collapseButton.setVisibility(View.VISIBLE);
                startCollapseTimer();
            }

            @Override
            public void onAdFailedToShow(AdsError error) {
                super.onAdFailedToShow(error);
                Log.e(TAG, "Failed to load collapse banner from multi-floor manager: " + error.getMessage());
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                collapseBanner();
            }
        });
    }

    private void startCollapseTimer() {
        handler.postDelayed(collapseRunnable, showTime);
    }

    private void collapseBanner() {
        isAdShowing = false;
        handler.removeCallbacks(collapseRunnable);
        largeBannerContainer.setVisibility(View.GONE);
        collapseButton.setVisibility(View.GONE);
        this.setVisibility(View.GONE);

        if (onCollapseListener != null) {
            onCollapseListener.onBannerCollapsed();
        }
    }
}

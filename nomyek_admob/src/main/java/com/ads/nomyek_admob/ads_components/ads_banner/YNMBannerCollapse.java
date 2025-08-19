package com.ads.yeknomadmob.ads_components.ads_banner;

import static com.ads.yeknomadmob.admobs.Admob.BANNER_INLINE_LARGE_STYLE;

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
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.utils.AdsCallback;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public void loadBannerCollapse(Activity activity, List<String> adUnitIds, OnCollapseListener onCollapseListener) {
        if (isAdShowing) {
            Log.d(TAG, "Ad is already showing. Refresh is ignored.");
            return;
        }

        handler.removeCallbacks(collapseRunnable);

        this.setVisibility(View.VISIBLE);
        largeBannerContainer.setVisibility(View.VISIBLE);
        collapseButton.setVisibility(View.GONE);

        this.onCollapseListener = onCollapseListener;

        List<String> reversedAdUnitIds = new ArrayList<>(adUnitIds);
        Collections.reverse(reversedAdUnitIds);

        loadLargeBannerWithWaterfall(activity, reversedAdUnitIds);
    }

    private void loadLargeBannerWithWaterfall(Activity activity, List<String> adUnitIds) {
        if (adUnitIds.isEmpty()) {
            collapseBanner();
            return;
        }

        String adUnitId = adUnitIds.get(0);
        List<String> remainingAdUnitIds = adUnitIds.subList(1, adUnitIds.size());

        Admob.getInstance().loadBanner(activity, adUnitId, largeBannerContainer, containerShimmer, new AdsCallback() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                isAdShowing = true;
                collapseButton.setVisibility(View.VISIBLE);
                startCollapseTimer();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError errorCode) {
                super.onAdFailedToLoad(errorCode);
                if (!remainingAdUnitIds.isEmpty()) {
                    loadLargeBannerWithWaterfall(activity, remainingAdUnitIds);
                } else{
                    collapseBanner();
                }
            }
        }, true, BANNER_INLINE_LARGE_STYLE);
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
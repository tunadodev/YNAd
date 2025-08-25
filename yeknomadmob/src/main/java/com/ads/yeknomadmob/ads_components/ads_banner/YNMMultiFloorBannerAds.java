package com.ads.yeknomadmob.ads_components.ads_banner;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.utils.AdsCallback;
import com.ads.yeknomadmob.utils.AdsUnitItem;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YNMMultiFloorBannerAds {

    private static final String TAG = "YNMBannerAds";
    private static volatile YNMMultiFloorBannerAds instance;

    private Context applicationContext;
    private List<AdsUnitItem> highAdsIds;
    private static final Map<String, AdView> adCache = new ConcurrentHashMap<>();
    private volatile boolean isWaterfallLoading = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private YNMMultiFloorBannerAds() {
    }

    public static YNMMultiFloorBannerAds getInstance() {
        if (instance == null) {
            synchronized (YNMMultiFloorBannerAds.class) {
                if (instance == null) {
                    instance = new YNMMultiFloorBannerAds();
                }
            }
        }
        return instance;
    }

    public void init(@NonNull Context context, @NonNull List<AdsUnitItem> highAdsIds) {
        Log.d(TAG, "Initializing with " + highAdsIds.size() + " ad units.");
        this.applicationContext = context.getApplicationContext();
        this.highAdsIds = highAdsIds;
        Collections.reverse(this.highAdsIds);
        startWaterfallPreload();
    }

    private void startWaterfallPreload() {
        if (applicationContext == null || highAdsIds == null || highAdsIds.isEmpty()) {
            Log.w(TAG, "Preload skipped: Manager not initialized.");
            return;
        }
        if (isWaterfallLoading) {
            Log.d(TAG, "Preload skipped: A waterfall is already in progress.");
            return;
        }
        if (!adCache.isEmpty()) {
            Log.d(TAG, "Preload skipped: An ad is already in the cache.");
            return;
        }

        isWaterfallLoading = true;
        Log.i(TAG, "Starting STANDARD banner waterfall preload...");
        loadAdInWaterfall(0);
    }

    private void loadAdInWaterfall(final int index) {
        if (index >= highAdsIds.size()) {
            isWaterfallLoading = false;
            Log.w(TAG, "Waterfall finished for STANDARD banners. No ad was loaded.");
            return;
        }

        final AdsUnitItem adUnit = highAdsIds.get(index);
        Log.d(TAG, "Waterfall trying to load ad unit at index " + index + ": " + adUnit.getKey());
        Admob.getInstance().loadBannerAdView(applicationContext, adUnit.getAdUnitId(), Admob.BANNER_INLINE_SMALL_STYLE, new AdsCallback() {
            @Override
            public void onBannerAdLoaded(AdView adView) {
                super.onBannerAdLoaded(adView);
                if (adView != null) {
                    adCache.put(adUnit.getAdUnitId(), adView);
                    isWaterfallLoading = false;
                    Log.i(TAG, "Successfully preloaded and cached STANDARD banner ad: " + adUnit.getKey());
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                Log.e(TAG, "Failed to load STANDARD banner ad: " + adUnit.getKey() + ". Trying next.");
                loadAdInWaterfall(index + 1);
            }
        });
    }

    public void showMFBannerAd(@NonNull final ViewGroup bannerContainer, @NonNull final YNMAdsCallbacks callback) {
        Log.d(TAG, "Request to show a standard banner.");
        if (highAdsIds == null || highAdsIds.isEmpty()) {
            Log.e(TAG, "Cannot show ad: Ad IDs not configured for standard banners.");
            callback.onAdFailedToShow(new AdsError("Ad IDs not configured for standard banners."));
            return;
        }

        for (AdsUnitItem mAd : highAdsIds) {
            if (adCache.containsKey(mAd.getAdUnitId())) {
                Log.i(TAG, "Found cached STANDARD banner ad: " + mAd.getKey() + ". Preparing to show.");
                AdView adView = adCache.remove(mAd.getAdUnitId());
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    bannerContainer.removeAllViews();
                    bannerContainer.addView(adView);
                    Log.i(TAG, "Ad shown successfully. Removing from cache and starting preload for next ad.");
                    callback.onAdLoaded();
                    startWaterfallPreload(); // Start reloading
                    return;
                }
            }
        }
        Log.w(TAG, "No standard banner ad available in cache to show. Triggering new preload.");
        callback.onAdFailedToShow(new AdsError("No standard banner ad available."));
        startWaterfallPreload();
    }
}

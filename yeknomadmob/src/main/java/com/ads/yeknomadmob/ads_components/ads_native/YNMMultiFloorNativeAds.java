package com.ads.yeknomadmob.ads_components.ads_native;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsNative;
import com.ads.yeknomadmob.utils.AdsCallback;
import com.ads.yeknomadmob.utils.AdsUnitItem;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YNMMultiFloorNativeAds {

    private static final String TAG = "YNMNativeAds";

    private Context applicationContext;
    private List<AdsUnitItem> highAdsIds;
    private final Map<String, NativeAd> adCache = new ConcurrentHashMap<>();
    private volatile boolean isWaterfallLoading = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public YNMMultiFloorNativeAds() {
    }

    public void init(@NonNull Context context, @NonNull List<AdsUnitItem> highAdsIds) {
        Log.d(TAG, "Initializing with " + highAdsIds.size() + " ad units.");
        this.applicationContext = context.getApplicationContext();
        this.highAdsIds = highAdsIds;
        Collections.reverse(this.highAdsIds);
        startWaterfallPreload();
    }

    public void startWaterfallPreload() {
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
        Log.i(TAG, "Starting STANDARD native waterfall preload...");
        loadAdInWaterfall(0);
    }

    private void loadAdInWaterfall(final int index) {
        if (index >= highAdsIds.size()) {
            isWaterfallLoading = false;
            Log.w(TAG, "Waterfall finished for STANDARD natives. No ad was loaded.");
            return;
        }

        final AdsUnitItem adUnit = highAdsIds.get(index);
        Log.d(TAG, "Waterfall trying to load ad unit at index " + index + ": " + adUnit.getKey());
        Admob.getInstance().loadNativeAd(applicationContext, adUnit.getAdUnitId(), new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd nativeAd) {
                super.onUnifiedNativeAdLoaded(nativeAd);
                adCache.put(adUnit.getAdUnitId(), nativeAd);
                isWaterfallLoading = false;
                Log.i(TAG, "Successfully preloaded and cached STANDARD native ad: " + adUnit.getKey());
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                Log.e(TAG, "Failed to load STANDARD native ad: " + adUnit.getKey() + ". Trying next.");
                loadAdInWaterfall(index + 1);
            }
        });
    }

    public void showMFNativeAd(@NonNull final Activity activity,
                               @NonNull final int layoutResNative,
                               @NonNull final YNMNativeAdView nativeAdView,
                               @NonNull final YNMAdsCallbacks callback) {
        Log.d(TAG, "Request to show a standard native.");
        if (highAdsIds == null || highAdsIds.isEmpty()) {
            Log.e(TAG, "Cannot show ad: Ad IDs not configured for standard natives.");
            callback.onAdFailedToShow(new AdsError("Ad IDs not configured for standard natives."));
            return;
        }

        for (AdsUnitItem mAd : highAdsIds) {
            if (adCache.containsKey(mAd.getAdUnitId())) {
                Log.i(TAG, "Found cached STANDARD native ad: " + mAd.getKey() + ". Preparing to show.");
                NativeAd nativeAd = adCache.remove(mAd.getAdUnitId());
                if (nativeAd != null) {
                    nativeAdView.populateNativeAdView(activity, new AdsNative(layoutResNative, nativeAd));
                    Log.i(TAG, "Ad shown successfully. Removing from cache and starting preload for next ad.");
                    callback.onAdLoaded();
                    startWaterfallPreload(); // Start reloading
                    return;
                }
            }
        }
        Log.w(TAG, "No standard native ad available in cache to show. Triggering new preload.");
        callback.onAdFailedToShow(new AdsError("No standard native ad available."));
        startWaterfallPreload();
    }
}

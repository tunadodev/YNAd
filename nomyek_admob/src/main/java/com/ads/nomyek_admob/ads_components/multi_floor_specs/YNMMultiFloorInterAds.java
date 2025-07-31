package com.ads.nomyek_admob.ads_components.multi_floor_specs;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.nomyek_admob.admobs.Admob;
import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;
import com.ads.nomyek_admob.ads_components.wrappers.AdsError;
import com.ads.nomyek_admob.utils.AdsCallback;
import com.ads.nomyek_admob.utils.AdsInterPreload;
import com.ads.nomyek_admob.utils.AdsUnitItem;
import com.ads.nomyek_admob.utils.SharePreferenceUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a multi-floor interstitial ad waterfall to maximize ad revenue.
 * This class implements a sophisticated strategy to preload multiple interstitial ads with different
 * floor prices and serve the one with the highest floor that is available.
 * <p>
 * Key Features:
 * - Singleton Pattern: Ensures a single, consistent manager for the entire application.
 * - Waterfall Preloading: Preloads a list of ad units sequentially, starting from the highest floor. If an ad fails to load, it attempts to load the next one in the sequence.
 * - Highest-Floor-First Serving: When an ad is requested, it serves the available ad with the highest floor price.
 * - Pending Request Handling: If an ad is requested while all floors are loading, it queues the request and serves an ad as soon as one becomes available.
 * - Thread Safety: Uses thread-safe collections and synchronized blocks to manage state across multiple threads, ensuring safe execution.
 */
public class YNMMultiFloorInterAds {

    private static final String TAG = "YNMMultiFloorInterAds";

    // Singleton instance. `volatile` ensures that changes are visible across all threads immediately.
    private static volatile YNMMultiFloorInterAds instance;

    private Context applicationContext;
    private List<AdsUnitItem> highAdsIds;
    // A Handler to ensure that any operations that might interact with the UI are posted on the main thread.
    private final Handler handler = new Handler(Looper.getMainLooper());

    // --- State Management ---
    // A thread-safe cache for preloaded ads. Key is the Ad ID, value is the InterstitialAd object.
    private static final Map<String, InterstitialAd> adCache = new ConcurrentHashMap<>();
    // A thread-safe map to track ads that are currently in the process of being loaded to prevent duplicate requests.
    private static final Map<String, Boolean> loadingAds = new ConcurrentHashMap<>();

    // --- Pending Show Request State ---
    // These fields hold the details of a show request that is made while no ads are ready.
    private boolean isShowRequestPending = false;
    private Activity pendingActivity;
    private AdsUnitItem pendingBaseAd;
    private YNMAdsCallbacks pendingCallback;

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private YNMMultiFloorInterAds() {
    }

    /**
     * Provides the global singleton instance of the ad manager.
     * Uses double-checked locking for thread-safe lazy initialization.
     */
    public static YNMMultiFloorInterAds getInstance() {
        if (instance == null) {
            synchronized (YNMMultiFloorInterAds.class) {
                if (instance == null) {
                    instance = new YNMMultiFloorInterAds();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the multi-floor interstitial ads manager. This must be called once,
     * typically in the `onCreate` method of your Application class.
     *
     * @param context    The application context.
     * @param highAdsIds A list of AdMob Interstitial Ad IDs, ordered from the lowest floor price to the highest.
     */
    public void init(@NonNull Context context, @NonNull List<AdsUnitItem> highAdsIds) {
        this.applicationContext = context.getApplicationContext();
        this.highAdsIds = highAdsIds;
        // Reverse the list so that the highest floor is at index 0 for easier and more efficient processing.
        Collections.reverse(this.highAdsIds);
        startWaterfallPreload();
    }

    /**
     * Kicks off the waterfall preloading process.
     * It only starts if no ads are cached and none are currently loading.
     */
    private void startWaterfallPreload() {
        if (applicationContext == null || highAdsIds == null || highAdsIds.isEmpty()) {
            Log.w(TAG, "Cannot start waterfall preload: context or ad IDs are not initialized.");
            return;
        }
        // To prevent starting a new waterfall if one is already in progress or an ad is ready.
        if (!adCache.isEmpty() || !loadingAds.isEmpty()) {
            Log.d(TAG, "Waterfall preload skipped: ad cache not empty or an ad is already loading.");
            return;
        }
        Log.d(TAG, "Starting waterfall preload.");
        loadAdInWaterfall(0); // Start from the highest floor (index 0)
    }

    /**
     * Sequentially loads ads in a waterfall, from highest to lowest floor.
     * If an ad fails to load, it automatically tries the next one.
     *
     * @param index The current index in the highAdsIds list to attempt to load.
     */
    private void loadAdInWaterfall(final int index) {
        // Base case: If we've tried all ad IDs and none have loaded.
        if (index >= highAdsIds.size()) {
            Log.w(TAG, "Waterfall finished. No ad was loaded.");
            // If a show request was pending, this is the last point to trigger the fallback.
            handlePendingShowRequestIfLastAdFailed();
            return;
        }

        final AdsUnitItem adUnit = highAdsIds.get(index);

        if (adUnit == null || adUnit.getAdUnitId() == null || adUnit.getAdUnitId().isEmpty()) {
            Log.w(TAG, "Skipping invalid ad unit at index " + index + ". Proceeding to next.");
            // Immediately try the next ad in the waterfall.
            loadAdInWaterfall(index + 1);
            return;
        }

        // Safeguard: check if an ad is already cached or being loaded.
        if (adCache.containsKey(adUnit.getAdUnitId()) || loadingAds.containsKey(adUnit.getAdUnitId())) {
            Log.d(TAG, "Ad " + adUnit.getKey() + " is already cached or loading. Stopping waterfall.");
            return;
        }

        loadingAds.put(adUnit.getAdUnitId(), true);
        Log.d(TAG, "Waterfall loading ad at index " + index + ": " + adUnit.getKey());

        Admob.getInstance().getInterstitialAds(applicationContext, adUnit.getAdUnitId(), new AdsCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                loadingAds.remove(adUnit.getAdUnitId());

                if (interstitialAd != null) {
                    adCache.put(adUnit.getAdUnitId(), interstitialAd);
                    Log.d(TAG, "Successfully preloaded ad from waterfall: " + adUnit.getKey());
                    // Waterfall successful, stop here.
                    handlePendingShowRequest();
                } else {
                    // This case is unlikely but handled as a failure.
                    Log.e(TAG, "InterstitialAd was null for " + adUnit.getKey() + ". Proceeding to next in waterfall.");
                    loadAdInWaterfall(index + 1);
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                loadingAds.remove(adUnit.getAdUnitId());
                Log.e(TAG, "Failed to load ad: " + adUnit.getKey() + ". Error: " + (adError != null ? adError.getMessage() : "Unknown") + ". Proceeding to next in waterfall.");

                // --- Waterfall Logic ---
                // On failure, immediately try the next ad in the sequence.
                loadAdInWaterfall(index + 1);
            }
        });
    }


    /**
     * Checks for and fulfills a pending ad show request. This is called after an ad successfully loads.
     */
    private void handlePendingShowRequest() {
        synchronized (this) {
            if (isShowRequestPending) {
                Log.d(TAG, "Ad loaded, fulfilling pending show request.");
                // Post the show call to the main thread to ensure UI safety.
                handler.post(() -> {
                    if (pendingActivity != null && !pendingActivity.isDestroyed()) {
                        showMFInterAds(pendingActivity, pendingBaseAd, pendingCallback);
                    }
                    clearPendingShowRequest();
                });
            }
        }
    }

    /**
     * If a show request is pending and the last loading ad has just failed, this method
     * triggers the fallback mechanism.
     */
    private void handlePendingShowRequestIfLastAdFailed() {
        synchronized (this) {
            // Check if the show request is pending AND the waterfall is truly finished (no more ads loading).
            if (isShowRequestPending && loadingAds.isEmpty()) {
                Log.d(TAG, "Last loading ad failed, fulfilling pending show request with fallback.");
                handler.post(() -> {
                    if (pendingActivity != null && !pendingActivity.isDestroyed()) {
                        loadAndShowBaseAd(pendingActivity, pendingBaseAd, pendingCallback);
                    }
                    clearPendingShowRequest();
                });
            }
        }
    }

    /**
     * Resets the state of the pending show request. Executed within a synchronized block.
     */
    private void clearPendingShowRequest() {
        isShowRequestPending = false;
        pendingActivity = null;
        pendingBaseAd = null;
        pendingCallback = null;
    }

    /**
     * Attempts to show a multi-floor interstitial ad.
     * The logic is as follows:
     * 1. Iterate through the high-floor ads from highest to lowest.
     * 2. If a ready ad is found in the cache, show it immediately and start reloading a replacement.
     * 3. If no ads are ready but some are loading, queue the request to be shown when one loads.
     * 4. If no ads are ready and none are loading, proceed to load and show the fallback (base) ad.
     *
     * @param activity The activity context required to show the ad.
     * @param base_ad  A fallback Ad ID to use if no high-floor ads are available.
     * @param callback A callback to be invoked for ad lifecycle events.
     */
    public void showMFInterAds(@NonNull final Activity activity, @NonNull final AdsUnitItem base_ad, @NonNull final YNMAdsCallbacks callback) {
        // First, check if enough time has passed since the last interstitial ad was shown.
        long lastImpressionTime = SharePreferenceUtils.getLastImpressionInterstitialTime(activity);
        long interval = YNMAds.getInstance().getAdConfig().getIntervalInterstitialAd();

        if ((System.currentTimeMillis() - lastImpressionTime) / 1000 < interval) {
            Log.d(TAG, "Interstitial ad skipped due to interval constraint.");
            callback.onAdFailedToShow(new AdsError("Ad skipped due to frequency cap."));
            callback.onNextAction(false);
            return; // Exit without showing any ad.
        }

        if (highAdsIds == null || highAdsIds.isEmpty()) {
            Log.d(TAG, "High-floor ad IDs are null or empty. Proceeding with fallback.");
            loadAndShowBaseAd(activity, base_ad, callback);
            return;
        }

        // Iterate from highest to lowest floor.
        for (AdsUnitItem mAd : highAdsIds) {
            if (adCache.containsKey(mAd.getAdUnitId())) {
                // Log which floor is being shown.
                Log.d(TAG, "Showing high-floor ad: " + mAd.getKey());
                InterstitialAd ad = adCache.get(mAd.getAdUnitId());
                if (ad != null) {
                    destroyInterstitial(mAd.getAdUnitId()); // An ad can only be shown once. Remove it from the cache.
                    Admob.getInstance().forceShowInterstitial(activity, ad, new AdsCallback() {
                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.d(TAG, "High-floor ad shown: " + mAd.getKey());
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            callback.onAdClosed();
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(new AdsError(adError != null ? adError.getMessage() : "Unknown error"));
                        }

                        @Override
                        public void onNextAction(boolean isShown) {
                            super.onNextAction(isShown);
                            callback.onNextAction(isShown);
                        }
                    });
                    // Immediately start preloading a new ad to maintain a full cache.
                    startWaterfallPreload();
                    return; // Exit after successfully showing an ad.
                } else {
                    // Defensive cleanup: remove the key if the ad object is unexpectedly null.
                    destroyInterstitial(mAd.getAdUnitId());
                }
            }
        }

        // If no ads are ready, use the fallback.
        loadAndShowBaseAd(activity, base_ad, callback);
    }

    /**
     * Loads and shows the base (fallback) ad. This is the last resort if no high-floor ads are available.
     */
    private void loadAndShowBaseAd(@NonNull final Activity activity, @NonNull final AdsUnitItem base_ad, @NonNull final YNMAdsCallbacks callback) {
        Log.d(TAG, "No high ad ready, show base");
        AdsInterPreload.showPreloadInterAds(activity, base_ad.getKey(), base_ad.getAdUnitId(), 10000, callback);
    }

    /**
     * Removes an interstitial ad from the cache. This is called before showing an ad, as they are single-use.
     *
     * @param adId The Ad ID of the ad to remove.
     */
    public void destroyInterstitial(String adId) {
        if (adCache.remove(adId) != null) {
            Log.d(TAG, "Destroyed cached ad: " + adId);
        }
    }

    /**
     * Actively reloads all high-floor ads using the waterfall strategy.
     * This clears the cache and starts the loading process from the highest floor.
     */
    public void activelyReloadAllAds() {
        Log.d(TAG, "Actively reloading all high-floor ads via waterfall.");
        // Clear existing cache and loading flags to force a fresh waterfall load.
        adCache.clear();
        loadingAds.clear();
        startWaterfallPreload();
    }
}

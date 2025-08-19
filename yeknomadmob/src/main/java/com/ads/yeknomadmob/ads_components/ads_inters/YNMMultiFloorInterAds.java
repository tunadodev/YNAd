package com.ads.yeknomadmob.ads_components.ads_inters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.utils.AdsCallback;
import com.ads.yeknomadmob.utils.AdsUnitItem;
import com.ads.yeknomadmob.utils.SharePreferenceUtils;
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
 * - Timeout Safety: Includes a timeout to prevent the ad loading flow from blocking the application indefinitely.
 */
public class YNMMultiFloorInterAds {

    private static final String TAG = "YNMMultiFloorInterAds";
    // Defines the maximum time to wait for the entire ad waterfall to complete.
    // This prevents the ad loading process from blocking the application indefinitely if ad networks are unresponsive.
    private static final long WATERFALL_TIMEOUT_MS = 30000; // 30s

    // Singleton instance. `volatile` ensures that changes are visible across all threads immediately.
    private static volatile YNMMultiFloorInterAds instance;

    // Holds the application context, required for initializing the AdMob SDK and loading ads
    // without being tied to a specific Activity's lifecycle.
    private Context applicationContext;
    // Stores the list of ad units for the waterfall, ordered from highest to lowest floor price.
    // This list is central to the waterfall logic.
    private List<AdsUnitItem> highAdsIds;

    /**
     * handler: A Handler tied to the main UI thread (Looper.getMainLooper()).
     * Its purpose is to safely execute UI operations from background threads. For example, when an ad is
     * successfully loaded on a background thread, this handler is used to post the `show` operation
     * back onto the main thread, which is a requirement for any UI manipulation in Android.
     * <p>
     * timeoutHandler: While functionally identical to `handler`, this instance is dedicated solely to
     * managing the ad waterfall timeout. Using a separate handler for this specific task improves
     * code clarity and separates the timeout logic from general UI operations.
     * <p>
     * waterfallTimeoutRunnable: This is the task that gets executed by the `timeoutHandler`.
     * It contains the logic to reset the loading state (`isWaterfallLoading = false`) and handle
     * any pending ad requests if the waterfall process takes too long, preventing the app from getting stuck.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable waterfallTimeoutRunnable;


    // --- State Management ---
    /**
     * The adCache holds successfully preloaded interstitial ads.
     * It's a ConcurrentHashMap for thread safety, as loading and showing can happen on different threads.
     * Key: Ad Unit ID (String)
     * Value: The loaded InterstitialAd object.
     * This is essential for the core feature: having ads ready to be shown instantly.
     */
    private static final Map<String, InterstitialAd> adCache = new ConcurrentHashMap<>();

    /**
     * This flag prevents multiple waterfall preloading processes from running simultaneously.
     * It is marked as `volatile` to ensure that changes made by one thread are immediately visible to others.
     */
    private volatile boolean isWaterfallLoading = false;


    // --- Pending Show Request State ---
    // These fields manage a show request that arrives while the waterfall is still loading.
    // They work together to "queue" a request and fulfill it once an ad becomes available.

    /**
     * A flag that is set to `true` if `showMFInterAds` is called when no ad is ready, but
     * the waterfall is currently in progress (`isWaterfallLoading` is true).
     * It signals that there is a pending request to be fulfilled as soon as an ad loads.
     */
    private volatile boolean isShowRequestPending = false;

    /**
     * Stores the Activity context from a pending show request. This is necessary to show the
     * ad in the correct context once it's loaded. It is only non-null when `isShowRequestPending` is true.
     */
    private Activity pendingActivity;

    /**
     * Stores the callback from a pending show request. This ensures that the original caller is
     * notified of the ad's lifecycle events. It is only non-null when `isShowRequestPending` is true.
     */
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
     * It only starts if no ads are cached and a waterfall is not already in progress.
     */
    private void startWaterfallPreload() {
        if (applicationContext == null || highAdsIds == null || highAdsIds.isEmpty()) {
            Log.w(TAG, "Cannot start waterfall preload: context or ad IDs are not initialized.");
            return;
        }
        // To prevent starting a new waterfall if one is already in progress or an ad is ready.
        if (isWaterfallLoading) {
            Log.d(TAG, "Waterfall preload skipped: a waterfall is already in progress.");
            return;
        }
        if (!adCache.isEmpty()) {
            Log.d(TAG, "Waterfall preload skipped: ad cache is not empty.");
            return;
        }

        isWaterfallLoading = true;
        Log.d(TAG, "Starting waterfall preload with a " + WATERFALL_TIMEOUT_MS + "ms timeout.");

        // Define a timeout mechanism to prevent the app from getting stuck.
        waterfallTimeoutRunnable = () -> {
            if (isWaterfallLoading) {
                isWaterfallLoading = false;
                Log.e(TAG, "Waterfall loading timed out after " + WATERFALL_TIMEOUT_MS + "ms. Forcing state reset.");
                handlePendingShowRequestIfLastAdFailed();
            }
        };
        timeoutHandler.postDelayed(waterfallTimeoutRunnable, WATERFALL_TIMEOUT_MS);

        loadAdInWaterfall(0); // Start from the highest floor (index 0)
    }

    /**
     * Cancels the waterfall timeout handler.
     */
    private void cancelWaterfallTimeout() {
        if (waterfallTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(waterfallTimeoutRunnable);
            waterfallTimeoutRunnable = null;
        }
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
            isWaterfallLoading = false; // Mark waterfall as finished.
            cancelWaterfallTimeout();
            Log.w(TAG, "Waterfall finished. No ad was loaded.");
            handlePendingShowRequestIfLastAdFailed();
            return;
        }

        final AdsUnitItem adUnit = highAdsIds.get(index);

        if (adUnit == null || adUnit.getAdUnitId() == null || adUnit.getAdUnitId().isEmpty()) {
            Log.w(TAG, "Skipping invalid ad unit at index " + index + ". Proceeding to next.");
            loadAdInWaterfall(index + 1);
            return;
        }

        if (adCache.containsKey(adUnit.getAdUnitId())) {
            isWaterfallLoading = false; // Mark waterfall as finished.
            cancelWaterfallTimeout();
            Log.d(TAG, "Ad " + adUnit.getKey() + " is already cached. Stopping waterfall.");
            // An ad is ready, check for pending requests.
            handlePendingShowRequest();
            return;
        }

        Log.d(TAG, "Waterfall loading ad at index " + index + ": " + adUnit.getKey());

        Admob.getInstance().getInterstitialAds(applicationContext, adUnit.getAdUnitId(), new AdsCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                if (interstitialAd != null) {
                    isWaterfallLoading = false; // Mark waterfall as finished.
                    cancelWaterfallTimeout();
                    adCache.put(adUnit.getAdUnitId(), interstitialAd);
                    Log.d(TAG, "Successfully preloaded ad from waterfall: " + adUnit.getKey());
                    handlePendingShowRequest();
                } else {
                    // This case is unlikely but handled as a failure. Treat as a load failure.
                    Log.e(TAG, "InterstitialAd was null for " + adUnit.getKey() + ". Proceeding to next in waterfall.");
                    loadAdInWaterfall(index + 1);
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                Log.e(TAG, "Failed to load ad: " + adUnit.getKey() + ". Error: " + (adError != null ? adError.getMessage() : "Unknown") + ". Proceeding to next in waterfall.");
                // On failure, immediately try the next ad in the sequence. The isWaterfallLoading flag remains true.
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
                        // Check if there was a base ad in the pending request
                        if (pendingCallback != null) {
                            showMFInterAds(pendingActivity, pendingCallback);
                        }
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
            // Check if the show request is pending AND the waterfall is truly finished.
            if (isShowRequestPending && !isWaterfallLoading) {
                Log.d(TAG, "Last loading ad failed, fulfilling pending show request with fallback.");
                handler.post(() -> {
                    if (pendingActivity != null && !pendingActivity.isDestroyed()) {
                        Log.d(TAG, "No high ad ready, fallback to onNextAction");
                        if (pendingCallback != null) {
                            pendingCallback.onNextAction();
                        }
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
        pendingCallback = null;
    }

    /**
     * Attempts to show a multi-floor interstitial ad without a fallback ad unit.
     * If no high-floor ad is available, it will simply proceed via the callback.
     * The logic is as follows:
     * 1. Iterate through the high-floor ads from highest to lowest.
     * 2. If a ready ad is found in the cache, show it immediately and start reloading a replacement.
     * 3. If no ads are ready but a waterfall is loading, queue the request to be shown when one loads.
     * 4. If no ads are ready and none are loading, invoke the onNextAction callback.
     *
     * @param activity The activity context required to show the ad.
     * @param callback A callback to be invoked for ad lifecycle events.
     */
    public void showMFInterAds(@NonNull final Activity activity, @NonNull final YNMAdsCallbacks callback) {
        // First, check if enough time has passed since the last interstitial ad was shown.
        long lastImpressionTime = SharePreferenceUtils.getLastImpressionInterstitialTime(activity);
        long interval = YNMAds.getInstance().getAdConfig().getIntervalInterstitialAd();

        if ((System.currentTimeMillis() - lastImpressionTime) / 1000 < interval) {
            Log.d(TAG, "Interstitial ad skipped due to interval constraint.");
            callback.onAdFailedToShow(new AdsError("Ad skipped due to frequency cap."));
            callback.onNextAction();
            return; // Exit without showing any ad.
        }

        if (highAdsIds == null || highAdsIds.isEmpty()) {
            Log.d(TAG, "High-floor ad IDs are null or empty. Proceeding with fallback.");
            callback.onNextAction();
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
                        public void onNextAction() {
                            super.onNextAction();
                            callback.onNextAction();
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

        // If no ad is ready, check if a waterfall is in progress.
        if (isWaterfallLoading) {
            synchronized (this) {
                if (!isShowRequestPending) {
                    Log.d(TAG, "No ad ready, but waterfall is in progress. Queuing show request.");
                    isShowRequestPending = true;
                    pendingActivity = activity;
                    pendingCallback = callback;
                } else {
                    Log.w(TAG, "Another show request is already pending. Ignoring new request.");
                    callback.onAdFailedToShow(new AdsError("Another ad request is already in progress."));
                    callback.onNextAction();
                }
            }
            return;
        }

        callback.onNextAction();
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
     * If the ad already cached, it will not be reloaded
     */
    public void activelyReloadAllAds() {
        Log.d(TAG, "Actively reloading all high-floor ads via waterfall.");
        // This is acceptable, as the ongoing waterfall will eventually fill the (now clear) cache.
        startWaterfallPreload();
    }

    public boolean isWaterfallLoading() {
        return isWaterfallLoading;
    }
}

package com.ads.nomyek_admob.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;
import com.ads.nomyek_admob.ads_components.wrappers.AdsError;
import com.ads.nomyek_admob.ads_components.wrappers.AdsInterstitial;
import com.ads.nomyek_admob.dialogs.PrepareLoadingAdsDialog;
import com.ads.nomyek_admob.event.YNMAirBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdsInterPreload {
    public enum PreloadState {
        LOADING,    // Ad is currently being loaded
        SUCCESS,    // Ad has been loaded successfully
        FAIL        // Ad failed to load
    }

    public static class InterstitialModel {
        private AdsInterstitial interstitialAd;
        private PreloadState state;
        private YNMAdsCallbacks callback;
        private long lastUpdateTime;

        public InterstitialModel(AdsInterstitial ad, PreloadState state) {
            this.interstitialAd = ad;
            this.state = state;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public AdsInterstitial getInterstitialAd() {
            return interstitialAd;
        }

        public void setInterstitialAd(AdsInterstitial interstitialAd) {
            this.interstitialAd = interstitialAd;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public PreloadState getState() {
            return state;
        }

        public void setState(PreloadState state) {
            this.state = state;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public YNMAdsCallbacks getCallback() {
            return callback;
        }

        public void setCallback(YNMAdsCallbacks callback) {
            this.callback = callback;
        }

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }

        public boolean isReady() {
            return state == PreloadState.SUCCESS && interstitialAd != null && interstitialAd.isReady();
        }
    }

    private static final Map<String, InterstitialModel> mapCaches = new HashMap<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Helper method to check if context is invalid or destroyed
     */
    private static boolean isContextDestroyed(Context context) {
        if (context == null) {
            return true;
        }
        
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return activity.isFinishing() || activity.isDestroyed();
        }
        
        return false;
    }

    /**
     * Remove an interstitial ad from cache
     * @param key The key of the ad to remove
     */
    public static void destroyInterstitial(String key) {
        mapCaches.remove(key);
    }

    /**
     * Clear all interstitial ads from cache
     */
    public static void destroyAllInterstitials() {
        mapCaches.clear();
    }

    /**
     * Preload an interstitial ad with timeout
     */
    public static void preloadInterAds(Context context, YNMAirBridge.AppData appData, String id, String key, long timeout) {
        // Check if already loading or loaded
        InterstitialModel existingModel = mapCaches.get(key);
        if (existingModel != null) {
            // If already loading, just return
            if (existingModel.getState() == PreloadState.LOADING || existingModel.getState() == PreloadState.SUCCESS) {
                Log.d("AdsInterPreload", "Ad with key " + key + " is already loading");
                return;
            }
            // If loaded or failed, remove old model to start fresh
            destroyInterstitial(key);
        }

        // Create new model with LOADING state
        InterstitialModel model = new InterstitialModel(null, PreloadState.LOADING);
        mapCaches.put(key, model);

        // Set timeout handler
        mainHandler.postDelayed(() -> {
            InterstitialModel currentModel = mapCaches.get(key);
            if (currentModel != null && currentModel.getState() == PreloadState.LOADING) {
                currentModel.setState(PreloadState.FAIL);
                if (currentModel.getCallback() != null) {
                    currentModel.getCallback().onAdFailedToLoad(new AdsError("Preload timeout"));
                }
                // Remove on timeout
                destroyInterstitial(key);
            }
        }, timeout);

        // Start loading
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getInterstitialAds(context, id, new YNMAdsCallbacks(appData, YNMAds.INTERSTITIAL) {
                @Override
                public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                    InterstitialModel currentModel = mapCaches.get(key);
                    if (currentModel != null) {
                        currentModel.setInterstitialAd(interstitialAd);
                        currentModel.setState(PreloadState.SUCCESS);
                        if (currentModel.getCallback() != null) {
                            currentModel.getCallback().onAdLoaded();
                        }
                    }
                }

                @Override
                public void onAdFailedToLoad(@Nullable AdsError adError) {
                    InterstitialModel currentModel = mapCaches.get(key);
                    if (currentModel != null) {
                        currentModel.setState(PreloadState.FAIL);
                        if (currentModel.getCallback() != null) {
                            currentModel.getCallback().onAdFailedToLoad(adError);
                        }
                        // Remove on load fail
                        destroyInterstitial(key);
                    }
                }
            });
        });
    }

    /**
     * Load new interstitial ad with timeout
     */
    private static void loadNewInterstitial(Context context, String adId, String key, long timeOut, final YNMAdsCallbacks callback) {
        if (isContextDestroyed(context)) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }
        
        YNMAds.getInstance().loadInterstitialAds(context, adId, timeOut, 0, true, new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
            @Override
            public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                
                if (isContextDestroyed(context)) {
                    if (callback != null) {
                        callback.onNextAction();
                    }
                    destroyInterstitial(key);
                    return;
                }
                
                if (interstitialAd != null) {
                    YNMAds.getInstance().forceShowInterstitial(context, interstitialAd, new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if (callback != null) callback.onAdClosed();
                        }

                        @Override
                        public void onNextAction() {
                            super.onNextAction();
                            if (callback != null) callback.onNextAction();
                        }

                        @Override
                        public void onCheckSkipInter(boolean isSkip) {
                            super.onCheckSkipInter(isSkip);
                            if (!isSkip) {
                                // Remove from cache after showing
                                destroyInterstitial(key);
                            }
                        }
                    });
                }
            }

            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                super.onAdFailedToLoad(adError);
                if (callback != null) {
                    callback.onAdFailedToLoad(adError);
                    callback.onNextAction();
                }
                // Remove from cache on load fail
                destroyInterstitial(key);
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                if (callback != null) callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdsError adError) {
                super.onAdFailedToShow(adError);
                // Remove from cache on show fail
                destroyInterstitial(key);
            }

            @Override
            public void onTimeOut() {
                super.onTimeOut();
                if (callback != null) callback.onNextAction();
                // Remove from cache on timeout
                destroyInterstitial(key);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
            }
        });
    }

    /**
     * Show preloaded interstitial ad or load new one if needed
     */
    public static void showPreloadInterAds(Context context, String key, String adId, long timeOut, final YNMAdsCallbacks callback) {
        if (isContextDestroyed(context)) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }
        
        YNMAds.getInstance().setInitCallback(() -> {
            // Check impression interval
            if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                    < YNMAds.getInstance().getAdConfig().getIntervalInterstitialAd() * 1000L) {
                callback.onNextAction();
                return;
            }

            InterstitialModel model = mapCaches.get(key);
            
            if (model != null) {
                switch (model.getState()) {
                    case SUCCESS:
                        if (model.isReady()) {
                            // Show preloaded ad
                            YNMAds.getInstance().forceShowInterstitial(context, model.getInterstitialAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();
                                    if (callback != null) callback.onAdClosed();
                                }

                                @Override
                                public void onNextAction() {
                                    super.onNextAction();
                                    if (callback != null) callback.onNextAction();
                                }

                                @Override
                                public void onCheckSkipInter(boolean isSkip) {
                                    super.onCheckSkipInter(isSkip);
                                    if (!isSkip) {
                                        // Remove from cache after showing
                                        destroyInterstitial(key);
                                    }
                                }
                            });
                        } else {
                            // Preloaded ad is not ready, load new one
                            loadNewInterstitial(context, adId, key, timeOut, callback);
                        }
                        break;
                        
                    case LOADING:
                        // Wait for preload result
                        if (isContextDestroyed(context)) {
                            if (callback != null) {
                                callback.onNextAction();
                            }
                            return;
                        }
                        
                        PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
                        dialog.setCancelable(false);
                        dialog.show();
                        model.setCallback(new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                            @Override
                            public void onAdLoaded() {
                                if (dialog != null && dialog.isShowing() && !isContextDestroyed(context)) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e("AdsInterPreload", "Failed to dismiss dialog: " + e.getMessage());
                                    }
                                }
                                
                                if (isContextDestroyed(context)) {
                                    if (callback != null) {
                                        callback.onNextAction();
                                    }
                                    return;
                                }
                                
                                if (model.isReady()) {
                                    YNMAds.getInstance().forceShowInterstitial(context, model.getInterstitialAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                                        @Override
                                        public void onAdClosed() {
                                            super.onAdClosed();
                                            if (callback != null) callback.onAdClosed();
                                        }

                                        @Override
                                        public void onNextAction() {
                                            super.onNextAction();
                                            if (callback != null) callback.onNextAction();
                                        }

                                        @Override
                                        public void onCheckSkipInter(boolean isSkip) {
                                            super.onCheckSkipInter(isSkip);
                                            if (!isSkip) {
                                                // Remove from cache after showing
                                                destroyInterstitial(key);
                                            }
                                        }
                                    });
                                } else {
                                    loadNewInterstitial(context, adId, key, timeOut, callback);
                                }
                            }

                            @Override
                            public void onAdFailedToLoad(@Nullable AdsError adError) {
                                if (dialog != null && dialog.isShowing() && !isContextDestroyed(context)) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e("AdsInterPreload", "Failed to dismiss dialog: " + e.getMessage());
                                    }
                                }
                                
                                if (isContextDestroyed(context)) {
                                    if (callback != null) {
                                        callback.onNextAction();
                                    }
                                    return;
                                }
                                
                                loadNewInterstitial(context, adId, key, timeOut, callback);
                            }
                        });
                        break;
                        
                    case FAIL:
                        // Preload failed, load new one
                        loadNewInterstitial(context, adId, key, timeOut, callback);
                        break;
                }
            } else {
                // No preload exists, load new one
                loadNewInterstitial(context, adId, key, timeOut, callback);
            }
        });
    }

    /**
     * Get current preload state for a specific ad
     */
    public static PreloadState getPreloadState(String key) {
        InterstitialModel model = mapCaches.get(key);
        return model != null ? model.getState() : null;
    }

    /**
     * Check if an ad is ready to show
     */
    public static boolean isAdReady(String key) {
        InterstitialModel model = mapCaches.get(key);
        return model != null && model.isReady();
    }

    /**
     * Preload multiple interstitial ads with fallback mechanism.
     * Loads ads sequentially and stops when one ad loads successfully.
     * @param context Context
     * @param appData AppData
     * @param adUnits List of ad units to preload
     * @param timeout Timeout for each ad unit
     */
    public static void preloadMultipleInterAds(Context context, YNMAirBridge.AppData appData, List<AdsUnitItem> adUnits, long timeout) {
        preloadMultipleInterAds(context, appData, adUnits, timeout, null);
    }

    /**
     * Preload multiple interstitial ads with fallback mechanism.
     * Loads ads sequentially and stops when one ad loads successfully.
     * @param context Context
     * @param appData AppData
     * @param adUnits List of ad units to preload
     * @param timeout Timeout for each ad unit
     * @param callback Callback to be called when preload is complete (optional)
     */
    public static void preloadMultipleInterAds(Context context, YNMAirBridge.AppData appData, List<AdsUnitItem> adUnits, long timeout, final YNMAdsCallbacks callback) {
        if (adUnits == null || adUnits.isEmpty()) {
            Log.e("AdsInterPreload", "Ad units list is empty");
            if (callback != null) {
                callback.onAdFailedToLoad(new AdsError("Ad units list is empty"));
            }
            return;
        }

        // Start with first ad unit
        preloadSequentially(context, appData, adUnits, 0, timeout, callback);
    }

    private static void preloadSequentially(Context context, YNMAirBridge.AppData appData, List<AdsUnitItem> adUnits, int currentIndex, long timeout, final YNMAdsCallbacks callback) {
        // Check if we've tried all ad units
        if (currentIndex >= adUnits.size()) {
            Log.d("AdsInterPreload", "All ad units have been attempted without success");
            if (callback != null) {
                callback.onAdFailedToLoad(new AdsError("All ad units have been attempted without success"));
            }
            return;
        }

        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String adId = currentAdUnit.getAdUnitId();
        String key = currentAdUnit.getKey();
        
        // Check if already loading or loaded
        InterstitialModel existingModel = mapCaches.get(key);
        if (existingModel != null) {
            // If already SUCCESS, no need to continue
            if (existingModel.getState() == PreloadState.SUCCESS && existingModel.isReady()) {
                Log.d("AdsInterPreload", "Ad already loaded successfully for key: " + key);
                if (callback != null) {
                    callback.onAdLoaded();
                }
                return;
            }
            if (existingModel.getState() == PreloadState.LOADING) {
                Log.d("AdsInterPreload", "Ad with key " + key + " is already loading");
                return;
            }
            // If failed, remove old model
            destroyInterstitial(key);
        }

        // Create new model with LOADING state
        InterstitialModel model = new InterstitialModel(null, PreloadState.LOADING);
        mapCaches.put(key, model);
        
        // Set custom callback
        model.setCallback(new YNMAdsCallbacks(appData, YNMAds.INTERSTITIAL) {
            @Override
            public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                // Success! We loaded an ad, so no need to try next one
                InterstitialModel currentModel = mapCaches.get(key);
                if (currentModel != null) {
                    currentModel.setInterstitialAd(interstitialAd);
                    currentModel.setState(PreloadState.SUCCESS);
                }
                Log.d("AdsInterPreload", "Ad loaded successfully for key: " + key + ", stopping sequence");
                // Call the callback to notify success
                if (callback != null) {
                    callback.onAdLoaded();
                }
                // No need to continue with next ad unit
            }
            
            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                InterstitialModel currentModel = mapCaches.get(key);
                if (currentModel != null && currentModel.getState() == PreloadState.LOADING) {
                    currentModel.setState(PreloadState.FAIL);
                    destroyInterstitial(key);

                    // Try next ad unit on timeout
                    Log.d("AdsInterPreload", "Ad timeout for key: " + key + ", trying next one");
                    preloadSequentially(context, appData, adUnits, currentIndex + 1, timeout, callback);
                }
            }
        });

        // Set timeout handler
        mainHandler.postDelayed(() -> {
            InterstitialModel currentModel = mapCaches.get(key);
            if (currentModel != null && currentModel.getState() == PreloadState.LOADING) {
                currentModel.setState(PreloadState.FAIL);
                if (currentModel.getCallback() != null) {
                    currentModel.getCallback().onAdFailedToLoad(new AdsError("Preload timeout"));
                }
                destroyInterstitial(key);
                
                // Try next ad unit on timeout
                Log.d("AdsInterPreload", "Ad timeout for key: " + key + ", trying next one");
                preloadSequentially(context, appData, adUnits, currentIndex + 1, timeout, callback);
            }
        }, timeout);

        // Start loading
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getInterstitialAds(context, adId, model.getCallback());
        });
    }

    /**
     * Shows preloaded interstitial ads from a list with sequential checking:
     * 1. Checks each ad from start to end, shows first ready ad immediately
     * 2. If an ad is loading, waits for result and shows if successful
     * 3. If loading fails or times out, continues to next ad
     * 4. If no ads are ready in the list, loads a new ad using the last item
     * 
     * @param context Context
     * @param adUnits List of ad units to try showing
     * @param timeOut Timeout for loading operations
     * @param callback Callbacks for ad events
     */
    public static void showPreloadMultipleInterAds(Context context, List<AdsUnitItem> adUnits, long timeOut, final YNMAdsCallbacks callback) {
        if (adUnits == null || adUnits.isEmpty() || isContextDestroyed(context)) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }

        YNMAds.getInstance().setInitCallback(() -> {
            // Check impression interval
            if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                    < YNMAds.getInstance().getAdConfig().getIntervalInterstitialAd() * 1000L) {
                if (callback != null) {
                    callback.onNextAction();
                }
                return;
            }

            // Start checking from the first ad
            checkAndShowAdSequentially(context, adUnits, 0, timeOut, callback);
        });
    }
    
    /**
     * Helper method to check and show ads sequentially
     */
    private static void checkAndShowAdSequentially(Context context, List<AdsUnitItem> adUnits, int currentIndex,
                                                  long timeOut, final YNMAdsCallbacks callback) {
        if (isContextDestroyed(context)) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }
        
        // Check if we've reached the end of the list
        if (currentIndex >= adUnits.size()) {
            Log.d("AdsInterPreload", "No ready ads found in the list");
            // Load the last ad unit as a fallback
            if (!adUnits.isEmpty()) {
                AdsUnitItem lastAdUnit = adUnits.get(adUnits.size() - 1);
                Log.d("AdsInterPreload", "Loading new ad for the last key: " + lastAdUnit.getKey());
                loadNewInterstitial(context, lastAdUnit.getAdUnitId(), lastAdUnit.getKey(), timeOut, callback);
            } else {
                if (callback != null) {
                    callback.onNextAction();
                }
            }
            return;
        }

        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String key = currentAdUnit.getKey();
        String adId = currentAdUnit.getAdUnitId();
        
        Log.d("AdsInterPreload", "Checking ad with key: " + key + " (index: " + currentIndex + ")");
        
        // Get model from cache
        InterstitialModel model = mapCaches.get(key);
        
        if (model == null) {
            // No preload exists for this key, move to next
            Log.d("AdsInterPreload", "No preload exists for key: " + key + ", moving to next");
            checkAndShowAdSequentially(context, adUnits, currentIndex + 1, timeOut, callback);
            return;
        }
        
        // Check ad state
        switch (model.getState()) {
            case SUCCESS:
                if (model.isReady()) {
                    // Found ready ad, show it
                    Log.d("AdsInterPreload", "Found ready ad for key: " + key + ", showing it");
                    YNMAds.getInstance().forceShowInterstitial(context, model.getInterstitialAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if (callback != null) callback.onAdClosed();
                        }

                        @Override
                        public void onNextAction() {
                            super.onNextAction();
                            if (callback != null) callback.onNextAction();
                        }

                        @Override
                        public void onCheckSkipInter(boolean isSkip) {
                            super.onCheckSkipInter(isSkip);
                            if (!isSkip) {
                                // Remove from cache after showing
                                destroyInterstitial(key);
                            }
                        }
                    });
                } else {
                    // Ad not ready despite SUCCESS state, move to next
                    Log.d("AdsInterPreload", "Ad marked as SUCCESS but not ready for key: " + key + ", moving to next");
                    checkAndShowAdSequentially(context, adUnits, currentIndex + 1, timeOut, callback);
                }
                break;
                
            case LOADING:
                // Ad is still loading, wait for result
                Log.d("AdsInterPreload", "Ad is loading for key: " + key + ", waiting for result");
                
                if (isContextDestroyed(context)) {
                    if (callback != null) {
                        callback.onNextAction();
                    }
                    return;
                }
                
                PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
                dialog.setCancelable(false);
                dialog.show();
                
                // Set a timeout for waiting
                final Handler timeoutHandler = new Handler(Looper.getMainLooper());
                final Runnable timeoutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Timeout waiting for this ad
                        Log.d("AdsInterPreload", "Timeout waiting for loading ad with key: " + key);
                        if (dialog != null && dialog.isShowing() && !isContextDestroyed(context)) {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e("AdsInterPreload", "Failed to dismiss dialog: " + e.getMessage());
                            }
                        }
                        // Check next ad
                        checkAndShowAdSequentially(context, adUnits, currentIndex + 1, timeOut, callback);
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, timeOut);
                
                // Set callback to get load result
                model.setCallback(new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                    @Override
                    public void onAdLoaded() {
                        // Cancel timeout
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        if (dialog != null && dialog.isShowing() && !isContextDestroyed(context)) {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e("AdsInterPreload", "Failed to dismiss dialog: " + e.getMessage());
                            }
                        }
                        
                        if (isContextDestroyed(context)) {
                            if (callback != null) {
                                callback.onNextAction();
                            }
                            return;
                        }
                        
                        // Check if ad is ready
                        if (model.isReady()) {
                            Log.d("AdsInterPreload", "Ad finished loading and is ready for key: " + key + ", showing it");
                            YNMAds.getInstance().forceShowInterstitial(context, model.getInterstitialAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();
                                    if (callback != null) callback.onAdClosed();
                                }

                                @Override
                                public void onNextAction() {
                                    super.onNextAction();
                                    if (callback != null) callback.onNextAction();
                                }

                                @Override
                                public void onCheckSkipInter(boolean isSkip) {
                                    super.onCheckSkipInter(isSkip);
                                    if (!isSkip) {
                                        // Remove from cache after showing
                                        destroyInterstitial(key);
                                    }
                                }
                            });
                        } else {
                            // Ad finished loading but is not ready
                            Log.d("AdsInterPreload", "Ad finished loading but is not ready for key: " + key + ", moving to next");
                            checkAndShowAdSequentially(context, adUnits, currentIndex + 1, timeOut, callback);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable AdsError adError) {
                        // Cancel timeout
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        if (dialog != null && dialog.isShowing() && !isContextDestroyed(context)) {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e("AdsInterPreload", "Failed to dismiss dialog: " + e.getMessage());
                            }
                        }
                        
                        if (isContextDestroyed(context)) {
                            if (callback != null) {
                                callback.onNextAction();
                            }
                            return;
                        }
                        
                        // Ad failed to load, try next
                        Log.d("AdsInterPreload", "Ad failed to load for key: " + key + ", moving to next");
                        checkAndShowAdSequentially(context, adUnits, currentIndex + 1, timeOut, callback);
                    }
                });
                break;
                
            case FAIL:
                // Preload failed, move to next
                Log.d("AdsInterPreload", "Ad is in FAIL state for key: " + key + ", moving to next");
                checkAndShowAdSequentially(context, adUnits, currentIndex + 1, timeOut, callback);
                break;
        }
    }

    /**
     * Preload interstitial ads from a list with fallback mechanism
     * If one ad fails to load or times out, it automatically tries the next one
     * @param context Context
     * @param appData AppData
     * @param adUnits List of ad units to try
     * @param timeout Timeout for each ad unit
     */
    public static void preloadInterAdsWithFallback(Context context, YNMAirBridge.AppData appData, 
                                                  List<AdsUnitItem> adUnits, long timeout) {
        if (adUnits == null || adUnits.isEmpty()) {
            Log.e("AdsInterPreload", "Ad units list is empty");
            return;
        }
        
        preloadWithFallback(context, appData, adUnits, 0, timeout);
    }
    
    private static void preloadWithFallback(Context context, YNMAirBridge.AppData appData,
                                         List<AdsUnitItem> adUnits, int currentIndex, long timeout) {
        // Check if we've tried all ad units
        if (currentIndex >= adUnits.size()) {
            Log.d("AdsInterPreload", "All ad units have been attempted without success");
            return;
        }
        
        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String adId = currentAdUnit.getAdUnitId();
        String key = currentAdUnit.getKey();
        
        // Create proxy callbacks to detect success or failure
        YNMAdsCallbacks proxyCallbacks = new YNMAdsCallbacks(appData, YNMAds.INTERSTITIAL) {
            @Override
            public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                // Ad loaded successfully, no need to try next one
                Log.d("AdsInterPreload", "Ad loaded successfully for key: " + key);
                
                // We're using the existing system's state management, so no extra code needed here
                super.onInterstitialLoad(interstitialAd);
            }
            
            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                super.onAdFailedToLoad(adError);
                
                // Try next ad unit
                Log.d("AdsInterPreload", "Ad failed to load for key: " + key + ", trying next one");
                preloadWithFallback(context, appData, adUnits, currentIndex + 1, timeout);
            }
        };
        
        // Use existing timeout mechanism by adding callback
        mainHandler.postDelayed(() -> {
            InterstitialModel model = mapCaches.get(key);
            if (model != null && model.getState() == PreloadState.LOADING) {
                // If still loading after timeout, consider it failed and try next ad
                Log.d("AdsInterPreload", "Ad timeout for key: " + key + ", trying next one");
                preloadWithFallback(context, appData, adUnits, currentIndex + 1, timeout);
            }
        }, timeout);
        
        // Use existing preload function
        preloadInterAds(context, appData, adId, key, timeout);
    }

    /**
     * Show the first ready ad from the list or load new if none are ready
     * @param context Context
     * @param adUnits List of ad units to try
     * @param timeOut Timeout for each ad unit 
     * @param callback Callbacks for ad events
     */
    public static void showFirstReadyAd(Context context, List<AdsUnitItem> adUnits,
                                       long timeOut, final YNMAdsCallbacks callback) {
        if (adUnits == null || adUnits.isEmpty()) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }
        
        // Check impression interval
        if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                < YNMAds.getInstance().getAdConfig().getIntervalInterstitialAd() * 1000L) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }
        
        // Find first ready ad
        for (AdsUnitItem adUnit : adUnits) {
            if (isAdReady(adUnit.getKey())) {
                // Found ready ad, show it using existing function
                showPreloadInterAds(context, adUnit.getKey(), adUnit.getAdUnitId(), timeOut, callback);
                return;
            }
        }
        
        // If no ads ready, use the first one
        if (!adUnits.isEmpty()) {
            AdsUnitItem firstAdUnit = adUnits.get(0);
            showPreloadInterAds(context, firstAdUnit.getKey(), firstAdUnit.getAdUnitId(), timeOut, callback);
        } else {
            if (callback != null) {
                callback.onNextAction();
            }
        }
    }
}

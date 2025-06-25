package com.ads.nomyek_admob.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;
import com.ads.nomyek_admob.ads_components.wrappers.AdsError;
import com.ads.nomyek_admob.ads_components.wrappers.AdsReward;
import com.ads.nomyek_admob.ads_components.wrappers.AdsRewardItem;
import com.ads.nomyek_admob.dialogs.PrepareLoadingAdsDialog;
import com.ads.nomyek_admob.event.YNMAirBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdsRewardPreload {
    public enum PreloadState {
        LOADING,    // Ad is currently being loaded
        SUCCESS,    // Ad has been loaded successfully
        FAIL        // Ad failed to load
    }

    public static class RewardModel {
        private AdsReward rewardAd;
        private PreloadState state;
        private YNMAdsCallbacks callback;
        private long lastUpdateTime;

        public RewardModel(AdsReward ad, PreloadState state) {
            this.rewardAd = ad;
            this.state = state;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public AdsReward getRewardAd() {
            return rewardAd;
        }

        public void setRewardAd(AdsReward rewardAd) {
            this.rewardAd = rewardAd;
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
            return state == PreloadState.SUCCESS && rewardAd != null && rewardAd.isReady();
        }
    }

    private static final Map<String, RewardModel> mapCaches = new HashMap<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Remove a reward ad from cache
     */
    public static void destroyReward(String key) {
        mapCaches.remove(key);
    }

    /**
     * Clear all reward ads from cache
     */
    public static void destroyAllRewards() {
        mapCaches.clear();
    }

    /**
     * Preload a reward ad with timeout
     */
    public static void preloadRewardAds(Activity context, YNMAirBridge.AppData appData, String id, String key, long timeout) {
        // Check if already loading or loaded
        RewardModel existingModel = mapCaches.get(key);
        if (existingModel != null) {
            // If already loading, just return
            if (existingModel.getState() == PreloadState.LOADING || existingModel.getState() == PreloadState.SUCCESS) {
                Log.d("AdsRewardPreload", "Ad with key " + key + " is already loading");
                return;
            }
            // If loaded or failed, remove old model to start fresh
            mapCaches.remove(key);
        }

        // Create new model with LOADING state
        RewardModel model = new RewardModel(null, PreloadState.LOADING);
        mapCaches.put(key, model);

        // Set timeout handler
        mainHandler.postDelayed(() -> {
            RewardModel currentModel = mapCaches.get(key);
            if (currentModel != null && currentModel.getState() == PreloadState.LOADING) {
                currentModel.setState(PreloadState.FAIL);
                if (currentModel.getCallback() != null) {
                    currentModel.getCallback().onAdFailedToLoad(new AdsError("Preload timeout"));
                }
                // Remove on timeout
                destroyReward(key);
            }
        }, timeout);

        // Start loading
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getRewardAd(context, id, new YNMAdsCallbacks(appData, YNMAds.REWARD) {
                @Override
                public void onRewardAdLoaded(AdsReward rewardedAd) {
                    RewardModel currentModel = mapCaches.get(key);
                    if (currentModel != null) {
                        currentModel.setRewardAd(rewardedAd);
                        currentModel.setState(PreloadState.SUCCESS);
                        if (currentModel.getCallback() != null) {
                            currentModel.getCallback().onAdLoaded();
                        }
                    }
                }

                @Override
                public void onAdFailedToLoad(@Nullable AdsError adError) {
                    RewardModel currentModel = mapCaches.get(key);
                    if (currentModel != null) {
                        currentModel.setState(PreloadState.FAIL);
                        if (currentModel.getCallback() != null) {
                            currentModel.getCallback().onAdFailedToLoad(adError);
                        }
                        // Remove on load fail
                        destroyReward(key);
                    }
                }
            });
        });
    }

    /**
     * Load new reward ad with timeout
     */
    private static void loadNewReward(Activity context, String adId, String key, long timeOut, final YNMAdsCallbacks callback) {
        if (context == null || context.isFinishing() || context.isDestroyed()) {
            if (callback != null) {
                callback.onAdFailedToLoad(new AdsError("Activity is not available"));
            }
            return;
        }
        
        PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
        dialog.setCancelable(false);
        dialog.show();

        // Add timeout handler
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (dialog != null && dialog.isShowing() && !isActivityDestroyed(context)) {
                try {
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                }
                if (callback != null) {
                    callback.onAdFailedToLoad(new AdsError("Ad load timeout"));
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, timeOut);

        YNMAds.getInstance().getRewardAd(context, adId, new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
            @Override
            public void onRewardAdLoaded(AdsReward rewardedAd) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (dialog != null && dialog.isShowing() && !isActivityDestroyed(context)) {
                    try {
                        dialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                    }
                }
                
                if (isActivityDestroyed(context)) {
                    if (callback != null) {
                        callback.onAdFailedToLoad(new AdsError("Activity is not available"));
                    }
                    destroyReward(key);
                    return;
                }
                
                YNMAds.getInstance().forceShowRewardAd(context, rewardedAd, new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        if (callback != null) callback.onAdClosed();
                        // Remove from cache after showing
                    }

                    @Override
                    public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
                        super.onUserEarnedReward(rewardItem);
                        if (callback != null) callback.onUserEarnedReward(rewardItem);
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        if (callback != null) callback.onNextAction();
                    }
                });
                destroyReward(key);
            }

            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (dialog != null && dialog.isShowing() && !isActivityDestroyed(context)) {
                    try {
                        dialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                    }
                }
                if (callback != null) {
                    callback.onAdFailedToLoad(adError);
                }
                // Remove from cache on load fail
                destroyReward(key);
            }
        });
    }

    /**
     * Helper method to check if activity is destroyed or finishing
     */
    private static boolean isActivityDestroyed(Activity activity) {
        return activity == null || activity.isFinishing() || activity.isDestroyed();
    }

    /**
     * Show preloaded reward ad or load new one if needed
     */
    public static void showRewardPreload(Activity context, String key, String adId, long timeOut, final YNMAdsCallbacks callback) {
        if (isActivityDestroyed(context)) {
            if (callback != null) {
                callback.onAdFailedToLoad(new AdsError("Activity is not available"));
            }
            return;
        }
        
        YNMAds.getInstance().setInitCallback(() -> {
            RewardModel model = mapCaches.get(key);
            
            if (model != null) {
                switch (model.getState()) {
                    case SUCCESS:
                        if (model.isReady()) {
                            // Show preloaded ad
                            YNMAds.getInstance().forceShowRewardAd(context, model.getRewardAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();
                                    if (callback != null) callback.onAdClosed();
                                }

                                @Override
                                public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
                                    super.onUserEarnedReward(rewardItem);
                                    if (callback != null) callback.onUserEarnedReward(rewardItem);
                                }

                                @Override
                                public void onNextAction() {
                                    super.onNextAction();
                                    if (callback != null) callback.onNextAction();
                                }
                            });
                            // Remove from cache after showing
                            destroyReward(key);
                        } else {
                            // Preloaded ad is not ready, load new one
                            loadNewReward(context, adId, key, timeOut, callback);
                        }
                        break;
                        
                    case LOADING:
                        // Wait for preload result
                        if (isActivityDestroyed(context)) {
                            if (callback != null) {
                                callback.onAdFailedToLoad(new AdsError("Activity is not available"));
                            }
                            return;
                        }
                        
                        PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
                        dialog.setCancelable(false);
                        dialog.show();
                        
                        model.setCallback(new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                            @Override
                            public void onAdLoaded() {
                                if (dialog != null && dialog.isShowing() && !isActivityDestroyed(context)) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                                    }
                                }
                                
                                if (isActivityDestroyed(context)) {
                                    if (callback != null) {
                                        callback.onAdFailedToLoad(new AdsError("Activity is not available"));
                                    }
                                    destroyReward(key);
                                    return;
                                }
                                
                                if (model.isReady()) {
                                    YNMAds.getInstance().forceShowRewardAd(context, model.getRewardAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                                        @Override
                                        public void onAdClosed() {
                                            super.onAdClosed();
                                            if (callback != null) callback.onAdClosed();
                                        }

                                        @Override
                                        public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
                                            super.onUserEarnedReward(rewardItem);
                                            if (callback != null) callback.onUserEarnedReward(rewardItem);
                                        }

                                        @Override
                                        public void onNextAction() {
                                            super.onNextAction();
                                            if (callback != null) callback.onNextAction();
                                        }
                                    });
                                    // Remove from cache after showing
                                    destroyReward(key);
                                } else {
                                    loadNewReward(context, adId, key, timeOut, callback);
                                }
                            }

                            @Override
                            public void onAdFailedToLoad(@Nullable AdsError adError) {
                                if (dialog != null && dialog.isShowing() && !isActivityDestroyed(context)) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                                    }
                                }
                                loadNewReward(context, adId, key, timeOut, callback);
                            }
                        });
                        break;
                        
                    case FAIL:
                        // Preload failed, load new one
                        loadNewReward(context, adId, key, timeOut, callback);
                        break;
                }
            } else {
                // No preload exists, load new one
                loadNewReward(context, adId, key, timeOut, callback);
            }
        });
    }

    /**
     * Get current preload state for a specific ad
     */
    public static PreloadState getPreloadState(String key) {
        RewardModel model = mapCaches.get(key);
        return model != null ? model.getState() : null;
    }

    /**
     * Check if an ad is ready to show
     */
    public static boolean isAdReady(String key) {
        RewardModel model = mapCaches.get(key);
        return model != null && model.isReady();
    }

    /**
     * Preload multiple reward ads with fallback mechanism.
     * Loads ads sequentially and stops when one ad loads successfully.
     * @param activity Activity context
     * @param appData AppData
     * @param adUnits List of ad units to preload
     * @param timeout Timeout for each ad unit
     */
    public static void preloadMultipleRewardAds(Activity activity, YNMAirBridge.AppData appData, List<AdsUnitItem> adUnits, long timeout) {
        if (adUnits == null || adUnits.isEmpty()) {
            Log.e("AdsRewardPreload", "Ad units list is empty");
            return;
        }

        // Start with first ad unit
        preloadSequentially(activity, appData, adUnits, 0, timeout);
    }

    private static void preloadSequentially(Activity activity, YNMAirBridge.AppData appData, List<AdsUnitItem> adUnits, int currentIndex, long timeout) {
        // Check if we've tried all ad units
        if (currentIndex >= adUnits.size()) {
            Log.d("AdsRewardPreload", "All reward ad units have been attempted without success");
            return;
        }

        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String adId = currentAdUnit.getAdUnitId();
        String key = currentAdUnit.getKey();
        
        // Check if already loading or loaded
        RewardModel existingModel = mapCaches.get(key);
        if (existingModel != null) {
            // If already SUCCESS, no need to continue
            if (existingModel.getState() == PreloadState.SUCCESS && existingModel.isReady()) {
                Log.d("AdsRewardPreload", "Reward ad already loaded successfully for key: " + key);
                return;
            }
            if (existingModel.getState() == PreloadState.LOADING) {
                Log.d("AdsRewardPreload", "Reward ad with key " + key + " is already loading");
                return;
            }
            // If failed, remove old model
            destroyReward(key);
        }

        // Create new model with LOADING state
        RewardModel model = new RewardModel(null, PreloadState.LOADING);
        mapCaches.put(key, model);
        
        // Set custom callback
        model.setCallback(new YNMAdsCallbacks(appData, YNMAds.REWARD) {
            @Override
            public void onRewardAdLoaded(AdsReward rewardedAd) {
                // Success! We loaded an ad, so no need to try next one
                RewardModel currentModel = mapCaches.get(key);
                if (currentModel != null) {
                    currentModel.setRewardAd(rewardedAd);
                    currentModel.setState(PreloadState.SUCCESS);
                }
                Log.d("AdsRewardPreload", "Reward ad loaded successfully for key: " + key + ", stopping sequence");
                // No need to continue with next ad unit
            }
            
            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                RewardModel currentModel = mapCaches.get(key);
                if (currentModel != null) {
                    currentModel.setState(PreloadState.FAIL);
                }
                // Remove on load fail
                destroyReward(key);
                
                // Try next ad unit
                Log.d("AdsRewardPreload", "Reward ad failed to load for key: " + key + ", trying next one");
                preloadSequentially(activity, appData, adUnits, currentIndex + 1, timeout);
            }
        });

        // Set timeout handler
        mainHandler.postDelayed(() -> {
            RewardModel currentModel = mapCaches.get(key);
            if (currentModel != null && currentModel.getState() == PreloadState.LOADING) {
                currentModel.setState(PreloadState.FAIL);
                if (currentModel.getCallback() != null) {
                    currentModel.getCallback().onAdFailedToLoad(new AdsError("Preload timeout"));
                }
                destroyReward(key);
                
                // Try next ad unit on timeout
                Log.d("AdsRewardPreload", "Reward ad timeout for key: " + key + ", trying next one");
                preloadSequentially(activity, appData, adUnits, currentIndex + 1, timeout);
            }
        }, timeout);

        // Start loading
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getRewardAd(activity, adId, model.getCallback());
        });
    }

    /**
     * Shows preloaded reward ads from a list with sequential checking:
     * 1. Checks each ad from start to end, shows first ready ad immediately
     * 2. If an ad is loading, waits for result and shows if successful
     * 3. If loading fails or times out, continues to next ad
     * 4. If no ads are ready in the list, loads a new ad using the last item
     * 
     * @param activity Activity context
     * @param adUnits List of ad units to try showing
     * @param timeOut Timeout for loading operations
     * @param callback Callbacks for ad events
     */
    public static void showPreloadMultipleRewardAds(Activity activity, List<AdsUnitItem> adUnits, long timeOut, final YNMAdsCallbacks callback) {
        if (adUnits == null || adUnits.isEmpty()) {
            if (callback != null) {
                callback.onAdFailedToLoad(new AdsError("Ad units list is empty"));
            }
            return;
        }

        YNMAds.getInstance().setInitCallback(() -> {
            // Start checking from the first ad
            checkAndShowAdSequentially(activity, adUnits, 0, timeOut, callback);
        });
    }
    
    /**
     * Helper method to check and show reward ads sequentially
     */
    private static void checkAndShowAdSequentially(Activity activity, List<AdsUnitItem> adUnits, int currentIndex,
                                                  long timeOut, final YNMAdsCallbacks callback) {
        if (isActivityDestroyed(activity)) {
            if (callback != null) {
                callback.onAdFailedToLoad(new AdsError("Activity is not available"));
            }
            return;
        }
                                                  
        // Check if we've reached the end of the list
        if (currentIndex >= adUnits.size()) {
            Log.d("AdsRewardPreload", "No ready reward ads found in the list");
            // Load the last ad unit as a fallback
            if (!adUnits.isEmpty()) {
                AdsUnitItem lastAdUnit = adUnits.get(adUnits.size() - 1);
                Log.d("AdsRewardPreload", "Loading new reward ad for the last key: " + lastAdUnit.getKey());
                loadNewReward(activity, lastAdUnit.getAdUnitId(), lastAdUnit.getKey(), timeOut, callback);
            } else {
                if (callback != null) {
                    callback.onAdFailedToLoad(new AdsError("No ads to show"));
                }
            }
            return;
        }

        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String key = currentAdUnit.getKey();
        String adId = currentAdUnit.getAdUnitId();
        
        Log.d("AdsRewardPreload", "Checking reward ad with key: " + key + " (index: " + currentIndex + ")");
        
        // Get model from cache
        RewardModel model = mapCaches.get(key);
        
        if (model == null) {
            // No preload exists for this key, move to next
            Log.d("AdsRewardPreload", "No preload exists for key: " + key + ", moving to next");
            checkAndShowAdSequentially(activity, adUnits, currentIndex + 1, timeOut, callback);
            return;
        }
        
        // Check ad state
        switch (model.getState()) {
            case SUCCESS:
                if (model.isReady()) {
                    // Found ready ad, show it
                    Log.d("AdsRewardPreload", "Found ready reward ad for key: " + key + ", showing it");
                    YNMAds.getInstance().forceShowRewardAd(activity, model.getRewardAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if (callback != null) callback.onAdClosed();
                        }

                        @Override
                        public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
                            super.onUserEarnedReward(rewardItem);
                            if (callback != null) callback.onUserEarnedReward(rewardItem);
                        }

                        @Override
                        public void onNextAction() {
                            super.onNextAction();
                            if (callback != null) callback.onNextAction();
                        }
                    });
                    // Remove from cache after showing
                    destroyReward(key);
                } else {
                    // Ad not ready despite SUCCESS state, move to next
                    Log.d("AdsRewardPreload", "Reward ad marked as SUCCESS but not ready for key: " + key + ", moving to next");
                    checkAndShowAdSequentially(activity, adUnits, currentIndex + 1, timeOut, callback);
                }
                break;
                
            case LOADING:
                // Ad is still loading, wait for result
                Log.d("AdsRewardPreload", "Reward ad is loading for key: " + key + ", waiting for result");
                
                if (isActivityDestroyed(activity)) {
                    if (callback != null) {
                        callback.onAdFailedToLoad(new AdsError("Activity is not available"));
                    }
                    return;
                }
                
                PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(activity);
                dialog.setCancelable(false);
                dialog.show();
                
                // Set a timeout for waiting
                final Handler timeoutHandler = new Handler(Looper.getMainLooper());
                final Runnable timeoutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Timeout waiting for this ad
                        Log.d("AdsRewardPreload", "Timeout waiting for loading reward ad with key: " + key);
                        if (dialog != null && dialog.isShowing() && !isActivityDestroyed(activity)) {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                            }
                        }
                        // Check next ad
                        checkAndShowAdSequentially(activity, adUnits, currentIndex + 1, timeOut, callback);
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, timeOut);
                
                // Set callback to get load result
                model.setCallback(new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                    @Override
                    public void onAdLoaded() {
                        // Cancel timeout
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        if (dialog != null && dialog.isShowing() && !isActivityDestroyed(activity)) {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                            }
                        }
                        
                        if (isActivityDestroyed(activity)) {
                            if (callback != null) {
                                callback.onAdFailedToLoad(new AdsError("Activity is not available"));
                            }
                            return;
                        }
                        
                        // Check if ad is ready
                        if (model.isReady()) {
                            Log.d("AdsRewardPreload", "Reward ad finished loading and is ready for key: " + key + ", showing it");
                            YNMAds.getInstance().forceShowRewardAd(activity, model.getRewardAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();
                                    if (callback != null) callback.onAdClosed();
                                }

                                @Override
                                public void onUserEarnedReward(@NonNull AdsRewardItem rewardItem) {
                                    super.onUserEarnedReward(rewardItem);
                                    if (callback != null) callback.onUserEarnedReward(rewardItem);
                                }
                                @Override
                                public void onNextAction() {
                                    super.onNextAction();
                                    if (callback != null) callback.onNextAction();
                                }
                            });
                            // Remove from cache after showing
                            destroyReward(key);
                        } else {
                            // Ad finished loading but is not ready
                            Log.d("AdsRewardPreload", "Reward ad finished loading but is not ready for key: " + key + ", moving to next");
                            checkAndShowAdSequentially(activity, adUnits, currentIndex + 1, timeOut, callback);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable AdsError adError) {
                        // Cancel timeout
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        if (dialog != null && dialog.isShowing() && !isActivityDestroyed(activity)) {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e("AdsRewardPreload", "Failed to dismiss dialog: " + e.getMessage());
                            }
                        }
                        
                        // Ad failed to load, try next
                        Log.d("AdsRewardPreload", "Reward ad failed to load for key: " + key + ", moving to next");
                        checkAndShowAdSequentially(activity, adUnits, currentIndex + 1, timeOut, callback);
                    }
                });
                break;
                
            case FAIL:
                // Preload failed, move to next
                Log.d("AdsRewardPreload", "Reward ad is in FAIL state for key: " + key + ", moving to next");
                checkAndShowAdSequentially(activity, adUnits, currentIndex + 1, timeOut, callback);
                break;
        }
    }
}

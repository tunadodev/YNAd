package com.ads.yeknomadmob.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsReward;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.ads.yeknomadmob.dialogs.PrepareLoadingAdsDialog;
import com.ads.yeknomadmob.event.YNMAirBridge;

import java.util.HashMap;
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
        PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
        dialog.setCancelable(false);
        dialog.show();

        // Add timeout handler
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
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
                });
                destroyReward(key);
            }

            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (dialog.isShowing()) {
                    dialog.dismiss();
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
     * Show preloaded reward ad or load new one if needed
     */
    public static void showRewardPreload(Activity context, String key, String adId, long timeOut, final YNMAdsCallbacks callback) {
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
                        PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
                        dialog.setCancelable(false);
                        dialog.show();
                        model.setCallback(new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.REWARD) {
                            @Override
                            public void onAdLoaded() {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
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
                                    });
                                    // Remove from cache after showing
                                    destroyReward(key);
                                } else {
                                    loadNewReward(context, adId, key, timeOut, callback);
                                }
                            }

                            @Override
                            public void onAdFailedToLoad(@Nullable AdsError adError) {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
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
}

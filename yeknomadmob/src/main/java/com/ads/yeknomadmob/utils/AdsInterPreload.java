package com.ads.yeknomadmob.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;
import com.ads.yeknomadmob.dialogs.PrepareLoadingAdsDialog;
import com.ads.yeknomadmob.event.YNMAirBridge;

import java.util.HashMap;
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
            mapCaches.remove(key);
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
     * Show preloaded interstitial ad or load new one if needed
     */
    public static void showPreloadInterAds(Context context, String key, String adId, long timeOut, final YNMAdsCallbacks callback) {
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
                                    // Remove from cache after showing

                                }
                            });
                            destroyInterstitial(key);
                        } else {
                            // Preloaded ad is not ready, load new one
                            loadNewInterstitial(context, adId, timeOut, callback);
                        }
                        break;
                        
                    case LOADING:
                        // Wait for preload result
                        PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
                        dialog.setCancelable(false);
                        dialog.show();
                        model.setCallback(new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                            @Override
                            public void onAdLoaded() {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                if (model.isReady()) {
                                    YNMAds.getInstance().forceShowInterstitial(context, model.getInterstitialAd(), new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
                                        @Override
                                        public void onAdClosed() {
                                            super.onAdClosed();
                                            if (callback != null) callback.onAdClosed();
                                            // Remove from cache after showing
                                        }
                                    });
                                    destroyInterstitial(key);
                                } else {
                                    loadNewInterstitial(context, adId, timeOut, callback);
                                }
                            }

                            @Override
                            public void onAdFailedToLoad(@Nullable AdsError adError) {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                loadNewInterstitial(context, adId, timeOut, callback);
                            }
                        });
                        break;
                        
                    case FAIL:
                        // Preload failed, load new one
                        loadNewInterstitial(context, adId, timeOut, callback);
                        break;
                }
            } else {
                // No preload exists, load new one
                loadNewInterstitial(context, adId, timeOut, callback);
            }
        });
    }

    /**
     * Load new interstitial ad with timeout
     */
    private static void loadNewInterstitial(Context context, String adId, long timeOut, final YNMAdsCallbacks callback) {
        YNMAds.getInstance().loadInterstitialAds(context, adId, timeOut, 0, true, new YNMAdsCallbacks(new YNMAirBridge.AppData(), YNMAds.INTERSTITIAL) {
            @Override
            public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
            }

            @Override
            public void onAdFailedToLoad(@Nullable AdsError adError) {
                super.onAdFailedToLoad(adError);
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                if (callback != null) callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdsError adError) {
                super.onAdFailedToShow(adError);
            }

            @Override
            public void onTimeOut() {
                super.onTimeOut();
                if (callback != null) callback.onNextAction();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (callback != null) callback.onAdClosed();
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
}

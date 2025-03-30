package com.ads.yeknomadmob.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMAdsCallbacks;
import com.ads.yeknomadmob.ads_components.YNMInitCallback;
import com.ads.yeknomadmob.ads_components.ads_native.YNMNativeAdView;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class to manage preloading and showing of Native Ads
 * Handles the complete lifecycle of ads:
 * - Preloading ads for future use
 * - Tracking ad states (loading, loaded, failed, etc.)
 * - Showing preloaded ads
 * - Managing listeners for ad load/show events
 */
public class AdsNativePreload {
    /**
     * Possible states for a Native Ad
     */
    public enum State {
        LOAD,       // Ad is currently loading
        LOADED,     // Ad has been loaded successfully
        LOAD_FAIL,  // Ad failed to load
        SHOWED,     // Ad has been shown to the user
        SHOW_FAIL   // Ad failed to show
    }

    /**
     * Listener interface for Native Ad load events
     */
    public interface NativeAdLoadListener {
        void onNativeAdLoaded();
    }

    /**
     * Model class to hold Native Ad data and state
     */
    public static class NativeAdsModels {
        private NativeAd nativeAd;
        private NativeAdLoadListener nativeAdLoadListener;
        private State state;

        public NativeAdsModels(NativeAd nativeAd, NativeAdLoadListener nativeAdLoadListener, State state) {
            this.nativeAd = nativeAd;
            this.nativeAdLoadListener = nativeAdLoadListener;
            this.state = state;
        }

        public NativeAd getNativeAd() {
            return nativeAd;
        }

        public void setNativeAd(NativeAd nativeAd) {
            this.nativeAd = nativeAd;
            if (nativeAd != null) {
                this.state = State.LOADED;
            }
        }

        public NativeAdLoadListener getNativeAdLoadListener() {
            return nativeAdLoadListener;
        }

        public void setNativeAdLoadListener(NativeAdLoadListener nativeAdLoadListener) {
            this.nativeAdLoadListener = nativeAdLoadListener;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public boolean isLoading() {
            return state == State.LOAD;
        }
        
        public boolean isLoaded() {
            return state == State.LOADED && nativeAd != null;
        }
        
        public boolean isFailed() {
            return state == State.LOAD_FAIL;
        }
    }

    // Single map to store all Native Ads with their states and listeners
    private static final Map<String, NativeAdsModels> adsMap = new HashMap<>();

    /**
     * Check if an ad with the given key is currently loading
     */
    public static boolean isAdLoading(String key) {
        NativeAdsModels model = adsMap.get(key);
        return model != null && model.isLoading();
    }
    
    /**
     * Check if an ad with the given key is loaded and ready to show
     */
    public static boolean isAdLoaded(String key) {
        NativeAdsModels model = adsMap.get(key);
        return model != null && model.isLoaded();
    }

    /**
     * Store a Native Ad with a listener
     */
    public static void setNativeAd(NativeAd ad, String key, NativeAdLoadListener listener) {
        if (ad == null) return;
        
        NativeAdsModels model = new NativeAdsModels(ad, listener, State.LOADED);
        adsMap.put(key, model);

        // Notify the listener if provided
        if (listener != null) {
            listener.onNativeAdLoaded();
        }
    }

    /**
     * Store a Native Ad without a listener
     */
    public static void setNativeAd(NativeAd ad, String key) {
        if (ad == null) return;
        adsMap.put(key, new NativeAdsModels(ad, null, State.LOADED));
    }

    /**
     * Get a preloaded Native Ad by key
     */
    public static NativeAd getNativeAd(String key) {
        NativeAdsModels model = adsMap.get(key);
        return model != null ? model.getNativeAd() : null;
    }
    
    /**
     * Get the model containing the Native Ad and its state
     */
    public static NativeAdsModels getAdModel(String key) {
        return adsMap.get(key);
    }

    /**
     * Preload a Native Ad without listener
     */
    public static void PreLoadNative(Context context, String adId, String key, YNMAirBridge.AppData appData) {
        // Check if we need to create a new entry or update existing one
        NativeAdsModels model = adsMap.get(key);
        if (model == null) {
            model = new NativeAdsModels(null, null, State.LOAD);
            adsMap.put(key, model);
        } else if (!model.isLoading()) {
            // Only update state if not already loading
            model.setState(State.LOAD);
        } else {
            // Ad is already loading, no need to reload
            return;
        }
        
        // Start loading the ad
        loadNativeAdInternal(context, adId, key, appData, null);
    }

    /**
     * Preload a Native Ad with listener
     */
    public static void PreLoadNative(Context context, String adId, String key, YNMAirBridge.AppData appData, NativeAdLoadListener listener) {
        // Check if we need to create a new entry or update existing one
        NativeAdsModels model = adsMap.get(key);
        if (model == null) {
            model = new NativeAdsModels(null, listener, State.LOAD);
            adsMap.put(key, model);
        } else if (!model.isLoading()) {
            // Only update if not already loading
            model.setState(State.LOAD);
            model.setNativeAdLoadListener(listener);
        } else {
            // Ad is already loading, just update the listener
            model.setNativeAdLoadListener(listener);
            return;
        }
        
        // Start loading the ad
        loadNativeAdInternal(context, adId, key, appData, listener);
    }
    
    /**
     * Internal method to handle the actual ad loading logic
     */
    private static void loadNativeAdInternal(Context context, String adId, String key, 
                                           YNMAirBridge.AppData appData, NativeAdLoadListener listener) {
        YNMAdsCallbacks ynmCallbacks = new YNMAdsCallbacks(appData, YNMAds.NATIVE);
        
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd nativeAd) {
                // Update the model in map
                NativeAdsModels model = adsMap.get(key);
                if (model != null) {
                    model.setNativeAd(nativeAd);
                    model.setState(State.LOADED);
                    
                    // Notify listener if exists
                    NativeAdLoadListener modelListener = model.getNativeAdLoadListener();
                    if (modelListener != null) {
                        modelListener.onNativeAdLoaded();
                    }
                } else {
                    // Create new entry if model was removed
                    adsMap.put(key, new NativeAdsModels(nativeAd, listener, State.LOADED));
                    if (listener != null) {
                        listener.onNativeAdLoaded();
                    }
                }
                
                // Notify YNM callback
                ynmCallbacks.onAdLoaded();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                ynmCallbacks.onAdImpression();
                
                NativeAdsModels model = adsMap.get(key);
                if (model != null) {
                    model.setState(State.SHOWED);
                }
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                super.onAdFailedToLoad(error);
                ynmCallbacks.onAdFailedToLoad(new AdsError("Ad load error: " + error));
                
                NativeAdsModels model = adsMap.get(key);
                if (model != null) {
                    model.setState(State.LOAD_FAIL);
                }
            }
        });
    }

    /**
     * Load and show a Native Ad - handles different scenarios:
     * 1. If ad is already loaded, show it immediately
     * 2. If ad is loading, wait for load completion then show
     * 3. If ad is not loading/loaded or failed, start loading and show when ready
     */
    public static void flexPreloadedShowNativeAds(Context context, YNMNativeAdView adView, 
                                                String key, int mediumLayout, int largeLayout, 
                                                String adsId, YNMAirBridge.AppData appData) {
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                NativeAdsModels model = adsMap.get(key);
                
                // CASE 1: Ad is loaded and ready to show
                if (model != null && model.isLoaded()) {
                    showAdIfActivityActive(context, adView, model, mediumLayout, largeLayout, true);
                    return;
                }
                
                // CASE 2: Ad is currently loading - wait for it
                if (model != null && model.isLoading()) {
                    setupLoadListener(context, adView, key, mediumLayout, largeLayout);
                    return;
                }
                
                // CASE 3: Ad doesn't exist, failed to load, or is in an invalid state
                // Remove failed entry if exists
                if (model != null && model.isFailed()) {
                    adsMap.remove(key);
                }
                
                // Create new ad and start loading
                NativeAdLoadListener listener = createShowListener(context, adView, key, mediumLayout, largeLayout);
                
                // Create a new model and set to LOAD state
                adsMap.put(key, new NativeAdsModels(null, listener, State.LOAD));
                
                // Start loading the ad
                loadNativeAdInternal(context, adsId, key, appData, listener);
            }
        });
    }
    
    /**
     * Helper method to show ad if activity is still active
     */
    private static void showAdIfActivityActive(Context context, YNMNativeAdView adView, 
                                             NativeAdsModels model, int mediumLayout, 
                                             int largeLayout, boolean isPreloaded) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                AdsHelper.initAutoResizeAds(context, adView, model.getNativeAd(), 
                                          mediumLayout, largeLayout, isPreloaded);
                model.setState(State.SHOWED);
            }
        }
    }
    
    /**
     * Create a listener for showing an ad after it loads
     */
    private static NativeAdLoadListener createShowListener(Context context, YNMNativeAdView adView, 
                                                        String key, int mediumLayout, int largeLayout) {
        return new NativeAdLoadListener() {
            @Override
            public void onNativeAdLoaded() {
                NativeAdsModels loadedModel = adsMap.get(key);
                if (loadedModel != null && loadedModel.isLoaded()) {
                    showAdIfActivityActive(context, adView, loadedModel, mediumLayout, largeLayout, false);
                }
            }
        };
    }
    
    /**
     * Setup a listener for an already loading ad
     */
    private static void setupLoadListener(Context context, YNMNativeAdView adView, 
                                         String key, int mediumLayout, int largeLayout) {
        NativeAdsModels model = adsMap.get(key);
        if (model != null) {
            NativeAdLoadListener listener = createShowListener(context, adView, key, mediumLayout, largeLayout);
            model.setNativeAdLoadListener(listener);
        }
    }
    
    /**
     * Destroy a native ad and release its resources
     * Call this method when the ad is no longer needed or when the activity/fragment is destroyed
     * 
     * @param key The key identifier for the ad to destroy
     * @param removeFromMap Whether to also remove the entry from the map (true) or just destroy the ad (false)
     * @return true if ad was successfully destroyed, false otherwise
     */
    public static boolean destroyNativeAd(String key, boolean removeFromMap) {
        NativeAdsModels model = adsMap.get(key);
        if (model != null && model.getNativeAd() != null) {
            // Destroy the native ad to free up resources
            model.getNativeAd().destroy();
            
            if (removeFromMap) {
                // Remove entirely from the map
                adsMap.remove(key);
            } else {
                // Just set ad to null but keep the entry
                model.setNativeAd(null);
                model.setState(State.LOAD_FAIL); // Mark as failed so it can be reloaded if needed
            }
            return true;
        }
        return false;
    }
    
    /**
     * Destroy a native ad and remove it from the map
     * 
     * @param key The key identifier for the ad to destroy
     * @return true if ad was successfully destroyed, false otherwise
     */
    public static boolean destroyNativeAd(String key) {
        return destroyNativeAd(key, true);
    }
    
    /**
     * Destroy all native ads in the map and release resources
     * Call this method in your app's onDestroy or when ads are no longer needed
     * 
     * @param clearMap Whether to also clear the map after destroying all ads
     */
    public static void destroyAllNativeAds(boolean clearMap) {
        for (Map.Entry<String, NativeAdsModels> entry : adsMap.entrySet()) {
            NativeAdsModels model = entry.getValue();
            if (model != null && model.getNativeAd() != null) {
                model.getNativeAd().destroy();
            }
        }
        
        if (clearMap) {
            adsMap.clear();
        } else {
            // Just set all ads to null but keep entries
            for (Map.Entry<String, NativeAdsModels> entry : adsMap.entrySet()) {
                NativeAdsModels model = entry.getValue();
                if (model != null) {
                    model.setNativeAd(null);
                    model.setState(State.LOAD_FAIL);
                }
            }
        }
    }
    
    /**
     * Destroy all native ads in the map and clear the map completely
     * Use this method when you want to reset the entire ad system
     */
    public static void destroyAllNativeAds() {
        destroyAllNativeAds(true);
    }
}
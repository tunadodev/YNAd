package com.ads.yeknomadmob.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

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
import java.util.List;
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
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

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

    // ----------------- MULTIPLE ADS PRELOAD IMPLEMENTATION -----------------

    /**
     * Model class to hold ad unit ID and key information
     */
    public static class AdsUnitItem {
        private String adUnitId;
        private String key;

        public AdsUnitItem(String adUnitId, String key) {
            this.adUnitId = adUnitId;
            this.key = key;
        }

        public String getAdUnitId() {
            return adUnitId;
        }

        public String getKey() {
            return key;
        }
    }

    /**
     * Preload multiple native ads with fallback mechanism.
     * Loads ads sequentially and stops when one ad loads successfully.
     * 
     * @param context Context
     * @param appData AppData
     * @param adUnits List of ad units to preload
     * @param timeout Timeout for each ad unit in milliseconds
     */
    public static void preloadMultipleNativeAds(Context context, YNMAirBridge.AppData appData, 
                                                List<AdsUnitItem> adUnits, long timeout) {
        if (adUnits == null || adUnits.isEmpty()) {
            return;
        }

        // Start with first ad unit
        preloadSequentially(context, appData, adUnits, 0, timeout);
    }

    /**
     * Preload multiple native ads with a list of ad unit IDs
     * Uses a default timeout of 10 seconds per ad unit
     * 
     * @param context Context
     * @param appData AppData
     * @param adUnitIds List of ad unit IDs to preload
     */
    public static void preloadMultipleNativeAds(Context context, YNMAirBridge.AppData appData,
                                               List<String> adUnitIds) {
        if (adUnitIds == null || adUnitIds.isEmpty()) {
            return;
        }

        // Convert list of IDs to list of AdsUnitItem
        List<AdsUnitItem> adUnits = new java.util.ArrayList<>();
        for (int i = 0; i < adUnitIds.size(); i++) {
            String adId = adUnitIds.get(i);
            String key = "native_ad_" + i + "_" + adId;
            adUnits.add(new AdsUnitItem(adId, key));
        }

        // Call existing implementation with 10 seconds (10000ms) timeout
        preloadMultipleNativeAds(context, appData, adUnits, 10000);
    }

    /**
     * Internal method to preload ads sequentially with fallback
     */
    private static void preloadSequentially(Context context, YNMAirBridge.AppData appData, 
                                           List<AdsUnitItem> adUnits, int currentIndex, long timeout) {
        // Check if we've tried all ad units
        if (currentIndex >= adUnits.size()) {
            return;
        }

        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String adId = currentAdUnit.getAdUnitId();
        String key = currentAdUnit.getKey();
        
        // Check if already loading or loaded
        NativeAdsModels existingModel = adsMap.get(key);
        if (existingModel != null) {
            // If already SUCCESS, no need to continue
            if (existingModel.isLoaded()) {
                return;
            }
            if (existingModel.isLoading()) {
                return;
            }
            // If failed, remove old model
            destroyNativeAd(key, true);
        }

        // Create new model with LOAD state
        NativeAdsModels model = new NativeAdsModels(null, null, State.LOAD);
        adsMap.put(key, model);
        
        // Set custom load listener
        NativeAdLoadListener successListener = () -> {
            // Success! We loaded an ad, so no need to try next one
        };
        
        model.setNativeAdLoadListener(successListener);
        
        // Set timeout handler
        mainHandler.postDelayed(() -> {
            NativeAdsModels currentModel = adsMap.get(key);
            if (currentModel != null && currentModel.isLoading()) {
                // If still loading after timeout, consider it failed and try next ad unit
                destroyNativeAd(key, true);
                preloadSequentially(context, appData, adUnits, currentIndex + 1, timeout);
            }
        }, timeout);

        // Start loading the ad
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd nativeAd) {
                // Success! Store the ad
                NativeAdsModels currentModel = adsMap.get(key);
                if (currentModel != null) {
                    currentModel.setNativeAd(nativeAd);
                    currentModel.setState(State.LOADED);
                    
                    // Notify listener if exists
                    if (successListener != null) {
                        successListener.onNativeAdLoaded();
                    }
                }
                // No need to continue with next ad unit
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                // Failed to load, remove from map and try next
                destroyNativeAd(key, true);
                preloadSequentially(context, appData, adUnits, currentIndex + 1, timeout);
            }
        });
    }

    /**
     * Show the first available ad from a list of preloaded native ads
     * Checks each ad in the list in order and shows the first one that's ready
     * 
     * @param context Context
     * @param adView The native ad view to display the ad in
     * @param adUnits List of ad units to try
     * @param mediumLayout Layout resource for medium ads
     * @param largeLayout Layout resource for large ads
     * @param appData App data for callbacks
     * @param timeout Timeout for loading operations
     */
    public static void showPreloadMultipleNativeAds(Context context, YNMNativeAdView adView,
                                                  List<AdsUnitItem> adUnits, int mediumLayout, 
                                                  int largeLayout, YNMAirBridge.AppData appData, 
                                                  long timeout) {
        if (adUnits == null || adUnits.isEmpty()) {
            return;
        }

        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                // Start checking from the first ad
                checkAndShowAdSequentially(context, adView, adUnits, 0, mediumLayout, 
                                          largeLayout, appData, timeout);
            }
        });
    }

    /**
     * Helper method to check and show ads sequentially
     */
    private static void checkAndShowAdSequentially(Context context, YNMNativeAdView adView,
                                                List<AdsUnitItem> adUnits, int currentIndex,
                                                int mediumLayout, int largeLayout, 
                                                YNMAirBridge.AppData appData, long timeout) {
        // Check if we've reached the end of the list
        if (currentIndex >= adUnits.size()) {
            // If reached the end, try to load the last ad unit as fallback
            if (!adUnits.isEmpty()) {
                AdsUnitItem lastAdUnit = adUnits.get(adUnits.size() - 1);
                String lastKey = lastAdUnit.getKey();
                String lastAdId = lastAdUnit.getAdUnitId();
                
                // Create a new listener for loading and showing
                NativeAdLoadListener listener = createShowListener(context, adView, lastKey, mediumLayout, largeLayout);
                
                // Create a new model and set to LOAD state
                adsMap.put(lastKey, new NativeAdsModels(null, listener, State.LOAD));
                
                // Start loading the last ad
                loadNativeAdInternal(context, lastAdId, lastKey, appData, listener);
            }
            return;
        }

        // Get current ad unit
        AdsUnitItem currentAdUnit = adUnits.get(currentIndex);
        String key = currentAdUnit.getKey();
        String adId = currentAdUnit.getAdUnitId();
        
        // Get model from cache
        NativeAdsModels model = adsMap.get(key);
        
        // CASE 1: Ad is loaded and ready to show
        if (model != null && model.isLoaded()) {
            showAdIfActivityActive(context, adView, model, mediumLayout, largeLayout, true);
            return;
        }
        
        // CASE 2: Ad is currently loading - wait for it
        if (model != null && model.isLoading()) {
            // Set a timeout for waiting
            final Handler timeoutHandler = new Handler(Looper.getMainLooper());
            final Runnable timeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    // Timeout waiting for this ad, check next ad
                    checkAndShowAdSequentially(context, adView, adUnits, currentIndex + 1, 
                                             mediumLayout, largeLayout, appData, timeout);
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, timeout);
            
            // Set up load listener
            NativeAdLoadListener listener = new NativeAdLoadListener() {
                @Override
                public void onNativeAdLoaded() {
                    // Cancel timeout
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    // Show the loaded ad
                    NativeAdsModels loadedModel = adsMap.get(key);
                    if (loadedModel != null && loadedModel.isLoaded()) {
                        showAdIfActivityActive(context, adView, loadedModel, mediumLayout, largeLayout, true);
                    } else {
                        // Ad finished loading but is not ready, try next
                        checkAndShowAdSequentially(context, adView, adUnits, currentIndex + 1, 
                                                 mediumLayout, largeLayout, appData, timeout);
                    }
                }
            };
            model.setNativeAdLoadListener(listener);
            
            // Also handle case where loading fails
            mainHandler.postDelayed(() -> {
                NativeAdsModels currentModel = adsMap.get(key);
                if (currentModel != null && currentModel.isFailed()) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    checkAndShowAdSequentially(context, adView, adUnits, currentIndex + 1, 
                                             mediumLayout, largeLayout, appData, timeout);
                }
            }, 1000); // Check after 1 second
            
            return;
        }
        
        // CASE 3: Ad doesn't exist, failed to load, or is in an invalid state
        // Remove failed entry if exists
        if (model != null && model.isFailed()) {
            destroyNativeAd(key, true);
        }
        
        // Try next ad unit
        checkAndShowAdSequentially(context, adView, adUnits, currentIndex + 1, 
                                 mediumLayout, largeLayout, appData, timeout);
    }

    public static void showPreloadMultipleNativeAds(Context context, YNMNativeAdView adView,
                                                  List<String> adUnitIds, int mediumLayout, 
                                                  int largeLayout, YNMAirBridge.AppData appData) {
        if (adUnitIds == null || adUnitIds.isEmpty()) {
            return;
        }

        // Convert list of IDs to list of AdsUnitItem
        List<AdsUnitItem> adUnits = new java.util.ArrayList<>();
        for (int i = 0; i < adUnitIds.size(); i++) {
            String adId = adUnitIds.get(i);
            String key = "native_ad_" + i + "_" + adId;
            adUnits.add(new AdsUnitItem(adId, key));
        }

        // Call existing implementation with 10 seconds (10000ms) timeout
        showPreloadMultipleNativeAds(context, adView, adUnits, mediumLayout, 
                                    largeLayout, appData, 10000);
    }

    /**
     * Directly load and show native ads from a list of ad unit IDs.
     * Tries to load each ID for the specified timeout duration, if successful shows it, 
     * otherwise tries the next ID, continuing until the end of the list.
     *
     * @param context Context
     * @param adView The native ad view to display the ad in
     * @param adUnitIds List of ad unit IDs to try
     * @param mediumLayout Layout resource for medium ads
     * @param largeLayout Layout resource for large ads
     * @param appData App data for callbacks
     * @param timeout Timeout duration in milliseconds for each ad loading attempt
     */
    public static void loadAndShowNativeAds(Context context, YNMNativeAdView adView,
                                           List<String> adUnitIds, int mediumLayout, 
                                           int largeLayout, YNMAirBridge.AppData appData,
                                           long timeout) {
        if (adUnitIds == null || adUnitIds.isEmpty()) {
            return;
        }

        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                // Start trying from the first ad ID
                tryLoadAndShowAd(context, adView, adUnitIds, 0, mediumLayout, 
                                largeLayout, appData, timeout);
            }
        });
    }

    /**
     * Directly load and show native ads from a list of ad unit IDs with default timeout of 10 seconds.
     *
     * @param context Context
     * @param adView The native ad view to display the ad in
     * @param adUnitIds List of ad unit IDs to try
     * @param mediumLayout Layout resource for medium ads
     * @param largeLayout Layout resource for large ads
     * @param appData App data for callbacks
     */
    public static void loadAndShowNativeAds(Context context, YNMNativeAdView adView,
                                           List<String> adUnitIds, int mediumLayout, 
                                           int largeLayout, YNMAirBridge.AppData appData) {
        loadAndShowNativeAds(context, adView, adUnitIds, mediumLayout, largeLayout, appData, 10000);
    }

    /**
     * Helper method to try loading and showing ads sequentially
     */
    private static void tryLoadAndShowAd(Context context, YNMNativeAdView adView,
                                       List<String> adUnitIds, int currentIndex,
                                       int mediumLayout, int largeLayout, 
                                       YNMAirBridge.AppData appData, long timeout) {
        // Check if we've reached the end of the list
        if (currentIndex >= adUnitIds.size()) {
            return;
        }

        // Get current ad unit ID
        String adId = adUnitIds.get(currentIndex);
        String key = "direct_native_ad_" + currentIndex + "_" + adId;
        
        // Create callbacks for this ad attempt
        YNMAdsCallbacks ynmCallbacks = new YNMAdsCallbacks(appData, YNMAds.NATIVE);
        
        // Create a flag to track if this attempt has already moved to the next ID
        final boolean[] movedToNext = {false};
        
        // Set timeout handler
        final Handler timeoutHandler = new Handler(Looper.getMainLooper());
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (!movedToNext[0]) {
                    movedToNext[0] = true;
                    // Timeout reached, move to next ad
                    destroyNativeAd(key, true);
                    tryLoadAndShowAd(context, adView, adUnitIds, currentIndex + 1, 
                                   mediumLayout, largeLayout, appData, timeout);
                }
            }
        };
        
        // Set timeout with specified duration
        timeoutHandler.postDelayed(timeoutRunnable, timeout);
        
        // Start loading the ad
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd nativeAd) {
                // Cancel timeout
                timeoutHandler.removeCallbacks(timeoutRunnable);
                
                // Update callbacks
                ynmCallbacks.onAdLoaded();
                
                // Show the ad if activity is active
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        AdsHelper.initAutoResizeAds(context, adView, nativeAd, 
                                                 mediumLayout, largeLayout, false);
                    }
                }
                
                // Store the ad in map for reference
                setNativeAd(nativeAd, key);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                ynmCallbacks.onAdImpression();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                super.onAdFailedToLoad(error);
                ynmCallbacks.onAdFailedToLoad(new AdsError("Ad load error: " + error));
                
                // Only move to next if timeout hasn't already triggered a move
                if (!movedToNext[0]) {
                    movedToNext[0] = true;
                    
                    // Cancel timeout
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    // Try next ad unit immediately
                    tryLoadAndShowAd(context, adView, adUnitIds, currentIndex + 1, 
                                   mediumLayout, largeLayout, appData, timeout);
                }
            }
        });
    }
}
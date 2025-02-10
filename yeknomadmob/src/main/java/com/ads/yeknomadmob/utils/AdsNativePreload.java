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

public class AdsNativePreload {
    public enum State {
        LOAD,
        LOADED,
        LOAD_FAIL,
        SHOWED,
        SHOW_FAIL
    }

    public interface NativeAdLoadListener {
        void onNativeAdLoaded();
    }

    public static class NativeAdsModels {
        NativeAd nativeAd;
        NativeAdLoadListener nativeAdLoadListener;

        public NativeAdsModels(NativeAd nativeAd, NativeAdLoadListener nativeAdLoadListener) {
            this.nativeAd = nativeAd;
            this.nativeAdLoadListener = nativeAdLoadListener;
        }

        public NativeAd getNativeAd() {
            return nativeAd;
        }

        public void setNativeAd(NativeAd nativeAd) {
            this.nativeAd = nativeAd;
        }

        public NativeAdLoadListener getNativeAdLoadListener() {
            return nativeAdLoadListener;
        }

        public void setNativeAdLoadListener(NativeAdLoadListener nativeAdLoadListener) {
            this.nativeAdLoadListener = nativeAdLoadListener;
        }
    }

    public static NativeAd backupNativeAd;

    public static State backupState;
    public static State mainState;
    //map store
    public static Map<String, NativeAdsModels> adsMap = new HashMap<>();

    public static void setNativeAd(NativeAd ad, String id, NativeAdLoadListener listener) {
        adsMap.put(id, new NativeAdsModels(ad, listener));

        // Notify the listener if set
        if (Objects.requireNonNull(adsMap.get(id)).getNativeAdLoadListener() != null) {
            listener.onNativeAdLoaded();
        }
    }

    public static void setNativeAd(NativeAd ad, String key_identify) {
        adsMap.put(key_identify, new NativeAdsModels(ad, null));
    }

    public static NativeAd getNativeAd(String id) {
        if (adsMap != null && adsMap.get(id) != null) {
            return Objects.requireNonNull(adsMap.get(id)).getNativeAd();
        }
        return null;
    }

    public static void PreLoadNative(Context context, String adId, String identifyKey, YNMAirBridge.AppData appData) {
        YNMAdsCallbacks listener = new YNMAdsCallbacks(appData, YNMAds.NATIVE);
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                setNativeAd(unifiedNativeAd, identifyKey);
                listener.onAdLoaded();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                listener.onAdImpression();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                listener.onAdFailedToLoad(new AdsError("Ad load error" + i));
            }
        });
    }

    //with listener
    public static void PreLoadNative(Context context, String adId, String identifyKey, YNMAirBridge.AppData appData, NativeAdLoadListener listener) {
        YNMAdsCallbacks ynmAdsCallbacks = new YNMAdsCallbacks(appData, YNMAds.NATIVE);
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                setNativeAd(unifiedNativeAd, identifyKey, listener);
                ynmAdsCallbacks.onAdLoaded();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                ynmAdsCallbacks.onAdImpression();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                ynmAdsCallbacks.onAdFailedToLoad(new AdsError("Ad load error" + i));
            }
        });
    }

    public static void renderNativeAd(Context context, NativeAd nativeAd, YNMNativeAdView adView, int mediumLayout, int largeLayout, boolean preLoaded) {
        AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, preLoaded);
    }

    //load from preload native, if not available, reload, show an auto resized ads
    public static void flexPreloadedShowNativeAds(Context context, YNMNativeAdView adView, String key, int mediumLayout, int largeLayout, String adsId, YNMAirBridge.AppData appData) {
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                if (AdsNativePreload.getNativeAd(key) != null) {
                    // Native Ad đã được load xong, bạn có thể sử dụng nó ở đây
                    NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                    AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, true);
                } else {
                    //Khong thi load lai
                    AdsNativePreload.PreLoadNative(context, adsId, key, appData, () -> {
                        NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                        if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                            AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, false);
                        }
                    });
                }
            }
        });
    }

    //fixed size preloaded ads
    public static void fixedPreloadedShowNativeAds(Context context, String viewName, YNMNativeAdView adView, String key, int layout, String adsId, YNMAirBridge.AppData appData) {
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                if (AdsNativePreload.getNativeAd(key) != null) {
                    // Native Ad đã được load xong, bạn có thể sử dụng nó ở đây
                    NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                    AdsHelper.initFixedSizeAds(context, adView, nativeAd, layout, true);
                } else {
                    //Khong thi load lai
                    AdsNativePreload.PreLoadNative(context, viewName, adsId, appData, () -> {
                        NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                        if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                            AdsHelper.initFixedSizeAds(context, adView, nativeAd, layout, false);
                        }
                    });
                }
            }
        });
    }

    public static void flexPreloadedShowNativeAds(Context context, YNMNativeAdView adView, String key, int mediumLayout, int largeLayout, String adsId, String adsBackupId) {
        backupState = State.LOAD;
        mainState = State.LOAD;
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                if (AdsNativePreload.getNativeAd(key) != null) {
                    // Native Ad đã được load xong, bạn có thể sử dụng nó ở đây
                    NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                    AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, true);
                } else {

                    Admob.getInstance().loadNativeAd(context, adsId, new AdsCallback() {
                        @Override
                        public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                            if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                                AdsHelper.initAutoResizeAds(context, adView, unifiedNativeAd, mediumLayout, largeLayout, false);
                            }
                            mainState = State.LOADED;
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            mainState = State.SHOWED;
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            mainState = State.LOAD_FAIL;
                            if (backupState == State.LOADED) {
                                backupState = State.SHOWED;
                                if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                                    AdsHelper.initAutoResizeAds(context, adView, backupNativeAd, mediumLayout, largeLayout, false);
                                }
                            }
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            mainState = State.SHOW_FAIL;
                            if (backupState == State.LOADED) {
                                backupState = State.SHOWED;
                                if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                                    AdsHelper.initAutoResizeAds(context, adView, backupNativeAd, mediumLayout, largeLayout, false);
                                }
                            }
                        }
                    });
                    Admob.getInstance().loadNativeAd(context, adsBackupId, new AdsCallback() {
                        @Override
                        public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                            AdsNativePreload.backupNativeAd = unifiedNativeAd;
                            backupState = State.LOADED;
                            if (mainState == State.LOAD_FAIL || mainState == State.SHOW_FAIL) {
                                backupState = State.SHOWED;
                                if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                                    AdsHelper.initAutoResizeAds(context, adView, backupNativeAd, mediumLayout, largeLayout, false);
                                }
                            }
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }
                    });
                }
                ;
            }
        });
    }
}
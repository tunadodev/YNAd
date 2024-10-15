package com.ads.nekoadmob.utils;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.ads.nekoadmob.admobs.Admob;
import com.ads.nekoadmob.ads_components.YNMAds;
import com.ads.nekoadmob.ads_components.YNMInitCallback;
import com.ads.nekoadmob.ads_components.ads_native.YNMNativeAdView;
import com.google.android.gms.ads.nativead.NativeAd;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AdsNativePreload {
    public interface NativeAdLoadListener {
        void onNativeAdLoaded();
    }
    public static class NativeAdsModels{
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
        if(adsMap !=null && adsMap.get(id)!=null){
            return Objects.requireNonNull(adsMap.get(id)).getNativeAd();
        }
        return null;
    }

    public static void PreLoadNative(Context context, String adId, String identifyKey) {
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                setNativeAd(unifiedNativeAd, identifyKey);
            }
            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });
    }

    //with listener
    public static void PreLoadNative(Context context, String adId, String identifyKey, NativeAdLoadListener listener) {
        Admob.getInstance().loadNativeAd(context, adId, new AdsCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                setNativeAd(unifiedNativeAd, identifyKey, listener);
            }
            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });
    }

    public static void renderNativeAd(Context context, NativeAd nativeAd, YNMNativeAdView adView, int mediumLayout, int largeLayout, boolean preLoaded) {
        AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, preLoaded);
    }

    //load from preload native, if not available, reload, show an auto resized ads
    public static void flexPreloadedShowNativeAds(Context context, YNMNativeAdView adView, String key, int mediumLayout, int largeLayout, String adsId){
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                if (AdsNativePreload.getNativeAd(key) != null) {
                    // Native Ad đã được load xong, bạn có thể sử dụng nó ở đây
                    NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                    AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, true);
                } else {
                    //Khong thi load lai
                    AdsNativePreload.PreLoadNative(context, adsId, key,  () -> {
                        NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                        if (!((Activity)context).isFinishing() && !((Activity)context).isDestroyed()) {
                            AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, false);
                        }
                    });
                };
            }
        });
    }

    //fixed size preloaded ads
    public static void fixedPreloadedShowNativeAds(Context context, YNMNativeAdView adView, String key, int layout, String adsId){
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                if (AdsNativePreload.getNativeAd(key) != null) {
                    // Native Ad đã được load xong, bạn có thể sử dụng nó ở đây
                    NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                    AdsHelper.initFixedSizeAds(context, adView, nativeAd, layout, true);
                } else {
                    //Khong thi load lai
                    AdsNativePreload.PreLoadNative(context, adsId, key,  () -> {
                        NativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                        if (!((Activity)context).isFinishing() && !((Activity)context).isDestroyed()) {
                            AdsHelper.initFixedSizeAds(context, adView, nativeAd, layout, false);
                        }
                    });
                };
            }
        });
    }
}
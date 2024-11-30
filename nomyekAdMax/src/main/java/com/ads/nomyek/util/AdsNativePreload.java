package com.ads.nomyek.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.ads.nomyek.R;
import com.ads.nomyek.ads.YNAd;
import com.ads.nomyek.ads.YNAdCallback;
import com.ads.nomyek.ads.nativeAds.YNNativeAdView;
import com.ads.nomyek.ads.wrapper.ApNativeAd;
import com.google.android.gms.ads.nativead.NativeAd;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AdsNativePreload {
    public interface NativeAdLoadListener {
        void onNativeAdLoaded();
    }
    public static class NativeAdsModels{
        ApNativeAd nativeAd;
        NativeAdLoadListener nativeAdLoadListener;

        public NativeAdsModels(ApNativeAd nativeAd, NativeAdLoadListener nativeAdLoadListener) {
            this.nativeAd = nativeAd;
            this.nativeAdLoadListener = nativeAdLoadListener;
        }

        public ApNativeAd getNativeAd() {
            return nativeAd;
        }

        public void setNativeAd(ApNativeAd nativeAd) {
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

    public static void setNativeAd(ApNativeAd ad, String id, NativeAdLoadListener listener) {
        adsMap.put(id, new NativeAdsModels(ad, listener));

        // Notify the listener if set
        if (Objects.requireNonNull(adsMap.get(id)).getNativeAdLoadListener() != null) {
            listener.onNativeAdLoaded();
        }
    }

    public static void setNativeAd(ApNativeAd ad, String key_identify) {
        adsMap.put(key_identify, new NativeAdsModels(ad, null));
    }

    public static ApNativeAd getNativeAd(String id) {
        if(adsMap !=null && adsMap.get(id)!=null){
            return Objects.requireNonNull(adsMap.get(id)).getNativeAd();
        }
        return null;
    }

    public static void PreLoadNative(Context context, String adId, String identifyKey,int layoutId) {
        YNAd.getInstance().loadNativeAdResultCallback((Activity) context,adId, layoutId,new YNAdCallback(){
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                setNativeAd(nativeAd, identifyKey);
                //save or show native
            }
        });
    }

    //with listener
    public static void PreLoadNative(Context context, String adId, String identifyKey,int layoutId, NativeAdLoadListener listener) {
        YNAd.getInstance().loadNativeAdResultCallback((Activity) context,adId, layoutId,new YNAdCallback(){
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                setNativeAd(nativeAd, identifyKey, listener);
                //save or show native
            }
        });
    }

    public static void renderNativeAd(Context context, NativeAd nativeAd, YNNativeAdView adView, int mediumLayout, int largeLayout, boolean preLoaded) {
        AdsHelper.initAutoResizeAds(context, adView, nativeAd, mediumLayout, largeLayout, preLoaded);
    }

    //load from preload native, if not available, reload, show an auto resized ads
    public static void flexPreloadedShowNativeAds(Context context, YNNativeAdView adView, String key, String adsId, int layoutId){
        YNAd.getInstance().setInitCallback(() -> {
            if (AdsNativePreload.getNativeAd(key) != null) {
                // Native Ad đã được load xong, bạn có thể sử dụng nó ở đây
                ApNativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                YNAd.getInstance().populateNativeAdView((Activity) context, nativeAd,adView.layoutPlaceHolder,adView.layoutLoading);

            } else {
                //Khong thi load lai
                AdsNativePreload.PreLoadNative(context, adsId, key,layoutId,  () -> {
                    ApNativeAd nativeAd = AdsNativePreload.getNativeAd(key);
                    YNAd.getInstance().populateNativeAdView((Activity) context, nativeAd,adView.layoutPlaceHolder,adView.layoutLoading);
                });
            };
        });
    }

}
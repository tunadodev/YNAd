package com.ads.nomyek_admob.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;
import com.ads.nomyek_admob.ads_components.wrappers.AdsError;
import com.ads.nomyek_admob.ads_components.wrappers.AdsInterstitial;

import java.util.Map;

public class AdsInterPreload {
    public static Map<String, AdsInterstitial> mapCaches = new java.util.HashMap<String, AdsInterstitial>();
    public static void preloadInterAds(Context context, String id, String key, final YNMAdsCallbacks callback) {
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getInterstitialAds(context, id, new YNMAdsCallbacks() {
                @Override
                public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                    mapCaches.put(key, interstitialAd);
                }
                @Override
                public void onAdFailedToLoad(@Nullable AdsError adError) {
                    super.onAdFailedToLoad(adError);
                    if (callback != null) callback.onAdFailedToLoad(adError);
                }
            });
        });

    }

    public static void preloadInterAds(Context context, String id, String key) {
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getInterstitialAds(context, id, new YNMAdsCallbacks() {
                @Override
                public void onInterstitialLoad(@Nullable AdsInterstitial interstitialAd) {
                    mapCaches.put(key, interstitialAd);
                }
            });
        });

    }

    public static void showPreloadInterAds(Context context, String key, String keyBackup, String adId, long timeOut, final YNMAdsCallbacks callback) {
        YNMAds.getInstance().setInitCallback(() -> {
            AdsInterstitial adsInterstitial = mapCaches.containsKey(key) ? mapCaches.get(key) : null;
            if (adsInterstitial != null && adsInterstitial.isReady()) {
                Log.d("TAG", "showw 1: ");
                YNMAds.getInstance().forceShowInterstitial(context, adsInterstitial, callback);
            } else {
                if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                        < YNMAds.getInstance().getAdConfig().getIntervalInterstitialAd() * 1000L
                ) {
                    callback.onNextAction();
                    return;
                }
                AdsInterstitial adsInterstitialBackup = mapCaches.containsKey(keyBackup) ? mapCaches.get(keyBackup) : null;
                YNMAds.getInstance().loadInterstitialAds(context, adId, timeOut, 0, true, new YNMAdsCallbacks()
                {
                    @Override
                    public void onAdFailedToLoad(@Nullable AdsError adError) {
                        super.onAdFailedToLoad(adError);
                        if (adsInterstitialBackup != null && adsInterstitialBackup.isReady()) {
                            Log.d("TAG", "showw 2: ");
                            YNMAds.getInstance().forceShowInterstitial(context, adsInterstitialBackup, callback);
                        }
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        if (callback != null) callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdsError adError) {
                        super.onAdFailedToShow(adError);
                        if (adsInterstitialBackup != null && adsInterstitialBackup.isReady()) {
                            Log.d("TAG", "showw 2: ");
                            YNMAds.getInstance().forceShowInterstitial(context, adsInterstitialBackup, callback);
                        }
                    }

                    @Override
                    public void onTimeOut() {
                        super.onTimeOut();
                        if (adsInterstitialBackup != null && adsInterstitialBackup.isReady()) {
                            Log.d("TAG", "showw 2: ");
                            YNMAds.getInstance().forceShowInterstitial(context, adsInterstitialBackup, callback);
                        } else {
                            if (callback != null) callback.onNextAction();
                        }
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        SharePreferenceUtils.setLastImpressionInterstitialTime(context);
                    }

                });
            }
        });

        mapCaches.remove(key);
        mapCaches.remove(keyBackup);
    }
}

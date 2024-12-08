package com.ads.nomyek.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ads.nomyek.ads.YNAdBackup;
import com.ads.nomyek.ads.YNAdCallback;
import com.ads.nomyek.ads.wrapper.ApAdError;
import com.ads.nomyek.ads.wrapper.ApInterstitialAd;

import java.util.Map;

public class AdsInterPreload {
    public static Map<String, ApInterstitialAd> mapCaches = new java.util.HashMap<String, ApInterstitialAd>();
    public static void preloadInterAds(Context context, String id, String key) {
        YNAdBackup.getInstance().setInitCallback(() -> {
            YNAdBackup.getInstance().getInterstitialAds(context, id, new YNAdCallback() {
                @Override
                public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {
                    mapCaches.put(key, interstitialAd);
                }

                @Override
                public void onAdFailedToLoad(@Nullable ApAdError adError) {
                    super.onAdFailedToLoad(adError);
                }
            });
        });

    }

    public static void showPreloadInterAds(Context context, String key, String keyBackup, String adId, long timeOut, final YNAdCallback callback) {
        YNAdBackup.getInstance().setInitCallback(() -> {
            ApInterstitialAd adsInterstitial = mapCaches.containsKey(key) ? mapCaches.get(key) : null;
            if (adsInterstitial != null && adsInterstitial.isReady()) {
                Log.d("TAG", "showw 1: ");
                YNAdBackup.getInstance().forceShowInterstitial(context, adsInterstitial, callback);
            } else {
                ApInterstitialAd adsInterstitialBackup = mapCaches.containsKey(keyBackup) ? mapCaches.get(keyBackup) : null;
                YNAdBackup.getInstance().loadShowInterstitialAds(context, adId, timeOut, 0, true, new YNAdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {
//                        YNAdBackup.getInstance().forceShowInterstitial(context, interstitialAd, callback);
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable ApAdError adError) {
                        super.onAdFailedToLoad(adError);
                        if (adsInterstitialBackup != null && adsInterstitialBackup.isReady()) {
                            Log.d("TAG", "showw 2: ");
                            YNAdBackup.getInstance().forceShowInterstitial(context, adsInterstitialBackup, callback);
                        } else {
                            if (callback != null) callback.onNextAction();
                        }
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable ApAdError adError) {
                        super.onAdFailedToShow(adError);
                        if (adsInterstitialBackup != null && adsInterstitialBackup.isReady()) {
                            Log.d("TAG", "showw 2: ");
                            YNAdBackup.getInstance().forceShowInterstitial(context, adsInterstitialBackup, callback);
                        } else {
                            if (callback != null) callback.onNextAction();
                        }
                    }
                });
            }
        });

        mapCaches.remove(key);
        mapCaches.remove(keyBackup);
    }
}

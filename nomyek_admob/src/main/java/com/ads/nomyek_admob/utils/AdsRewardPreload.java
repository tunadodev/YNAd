package com.ads.nomyek_admob.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.ads_components.YNMAdsCallbacks;
import com.ads.nomyek_admob.ads_components.wrappers.AdsError;
import com.ads.nomyek_admob.ads_components.wrappers.AdsReward;
import com.ads.nomyek_admob.dialogs.PrepareLoadingAdsDialog;

import java.util.Map;

public class AdsRewardPreload {
    public static Map<String, AdsReward> mapCaches = new java.util.HashMap<String, AdsReward>();
    public static boolean isTimeOut = false;
    public static void preloadRewardAds(Activity context, String id, String key) {
        YNMAds.getInstance().setInitCallback(() -> {
            YNMAds.getInstance().getRewardAd(context, id, new YNMAdsCallbacks() {
                @Override
                public void onRewardAdLoaded(AdsReward rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        mapCaches.put(key, rewardedAd);
                }

                @Override
                public void onAdFailedToLoad(@Nullable AdsError adError) {
                    super.onAdFailedToLoad(adError);
                }
            });
        });
    }
    public static void showRewardPreload(Activity context, String key, String adId, long timeOut, final YNMAdsCallbacks callback) {
        YNMAds.getInstance().setInitCallback(() -> {
            AdsReward adsReward = mapCaches.containsKey(key) ? mapCaches.get(key) : null;

            if (adsReward != null && adsReward.isReady()) {
                YNMAds.getInstance().forceShowRewardAd(context, adsReward, callback);
                mapCaches.remove(key);
                return;
            }

            PrepareLoadingAdsDialog dialog = new PrepareLoadingAdsDialog(context);
            dialog.setCancelable(false);
            dialog.show();
            isTimeOut = false;
            // Add timeout handler
            Handler timeoutHandler = new Handler(Looper.getMainLooper());
            Runnable timeoutRunnable = () -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    if (callback != null) {
                        callback.onAdFailedToLoad(new AdsError("Ad load timeout"));
                    }
                }
                isTimeOut = true;
            };
            timeoutHandler.postDelayed(timeoutRunnable, timeOut);

            YNMAds.getInstance().getRewardAd(context, adId, new YNMAdsCallbacks() {
                @Override
                public void onRewardAdLoaded(AdsReward rewardedAd) {
                    if (isTimeOut) {
                        return;
                    }
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    super.onRewardAdLoaded(rewardedAd);
                    YNMAds.getInstance().forceShowRewardAd(context, rewardedAd, callback);
                }

                @Override
                public void onAdFailedToLoad(@Nullable AdsError adError) {
                    if (isTimeOut) {
                        return;
                    }
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    super.onAdFailedToLoad(adError);
                    if (callback != null) {
                        callback.onAdFailedToLoad(adError);
                    }
                }
            });
        });
    }
}

package com.ads.yeknomadmob.ads_components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.admobs.AppOpenManager;
import com.ads.yeknomadmob.ads_components.wrappers.AdsError;
import com.ads.yeknomadmob.ads_components.wrappers.AdsInterstitial;
import com.ads.yeknomadmob.ads_components.wrappers.AdsNative;
import com.ads.yeknomadmob.ads_components.wrappers.AdsReward;
import com.ads.yeknomadmob.ads_components.wrappers.AdsRewardItem;
import com.ads.yeknomadmob.config.YNMAdsConfig;
import com.ads.yeknomadmob.event.YNMAirBridge;
import com.ads.yeknomadmob.event.YNMSolar;
import com.ads.yeknomadmob.utils.AdsCallback;
import com.ads.yeknomadmob.utils.AppUtil;
import com.ads.yeknomadmob.utils.RewardCallback;
import com.ads.yeknomadmob.utils.SharePreferenceUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class YNMAds {
    public static final String TAG = "YKMAds";
    private static volatile YNMAds INSTANCE;
    private YNMAdsConfig adConfig;
    private YNMInitCallback initCallback;
    private Boolean initAdSuccess = false;

    //format
    public final static String BANNER = "Banner";
    public final static String NATIVE = "Native";
    public final static String INTERSTITIAL = "Interstitial";
    public final static String REWARD = "Reward";

    public static synchronized YNMAds getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new YNMAds();
        }
        return INSTANCE;
    }

    public YNMAdsConfig getAdConfig() {
        return adConfig;
    }

    public void init(Activity activity, Application context, YNMAdsConfig adConfig) {
        if (adConfig == null) {
            throw new RuntimeException("cant not set YNMAdConfig null");
        }
        this.adConfig = adConfig;
        if (adConfig.isEnableAirBridge()) {
            Log.i(TAG, "init airbridge");
            YNMAirBridge.enableAirBridge = true;
            YNMAirBridge.setTagTest(adConfig.getAirBridgeConfig().getTagTest());
            YNMAirBridge.setUserState(adConfig.getAirBridgeConfig().getUserState());
            YNMAirBridge.getInstance().init(context, adConfig.getAirBridgeConfig().getAppNameAirBridge(),adConfig.getAirBridgeConfig().getTokenAirBridge(), true);
        }
        if (adConfig.isEnableSolar()) {
            Log.i(TAG, "init solar");
            YNMSolar.enableSolar = true;
            YNMSolar.getInstance().init(context, adConfig.getSolarConfig().getToken(), adConfig.getSolarConfig().isEnableDebug());
        }
        AppUtil.VARIANT_DEV = adConfig.isVariantDev();
        Log.i(TAG, "Config variant dev: " + AppUtil.VARIANT_DEV);
        if (adConfig.getMediationProvider() == YNMAdsConfig.PROVIDER_ADMOB) {
            Admob.getInstance().init(activity, context, adConfig.getListDeviceTest());
            if (adConfig.isEnableAdResume())
                AppOpenManager.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());

            initAdSuccess = true;
            if (initCallback != null)
                initCallback.initAdsSuccess();
        }
    }

    public void loadBanner(final Activity mActivity, String id, final YNMAdsCallbacks adCallback) {
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                adCallback.onAdStartLoad();
                Admob.getInstance().loadBanner(mActivity, id, new AdsCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adCallback.onAdLoaded();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        adCallback.onAdClicked();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adCallback.onAdFailedToLoad(new AdsError(i));
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        adCallback.onAdImpression();
                    }
                });
                break;
        }
    }

    public void loadNativeAd(final Activity activity, String id,
                             int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout
                                     containerShimmerLoading, YNMAdsCallbacks callback) {
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                callback.onAdStartLoad();
                Admob.getInstance().loadNativeAd(((Context) activity), id, new AdsCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new AdsNative(layoutCustomNative, unifiedNativeAd));
                        populateNativeAdView(activity, new AdsNative(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        callback.onAdImpression();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new AdsError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new AdsError(adError));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                });
                break;
        }
    }

    public void populateNativeAdView(Activity activity, AdsNative apNativeAd, FrameLayout
            adPlaceHolder, ShimmerFrameLayout containerShimmerLoading) {
        if (apNativeAd.getAdmobNativeAd() == null && apNativeAd.getNativeView() == null) {
            containerShimmerLoading.setVisibility(View.GONE);
            Log.e(TAG, "populateNativeAdView failed : native is not loaded ");
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(apNativeAd.getLayoutCustomNative(), null);
                containerShimmerLoading.stopShimmer();
                containerShimmerLoading.setVisibility(View.GONE);
                adPlaceHolder.setVisibility(View.VISIBLE);
                Admob.getInstance().populateUnifiedNativeAdView(apNativeAd.getAdmobNativeAd(), adView);
                adPlaceHolder.removeAllViews();
                adPlaceHolder.addView(adView);
                break;
        }
    }

    public void loadCollapsibleBanner(final Activity activity, String id, String gravity, YNMAdsCallbacks adCallback) {
        adCallback.onAdStartLoad();
        Admob.getInstance().loadCollapsibleBanner(activity, id, gravity, new AdsCallback(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adCallback.onAdLoaded();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                adCallback.onAdImpression();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }
        });
    }

    public void setInitCallback(YNMInitCallback initCallback) {
        this.initCallback = initCallback;
        if (initAdSuccess)
            initCallback.initAdsSuccess();
    }


    public AdsInterstitial getInterstitialAds(Context context, String id, YNMAdsCallbacks adListener) {
        AdsInterstitial apInterstitialAd = new AdsInterstitial();
        adListener.onAdStartLoad();
        Admob.getInstance().getInterstitialAds(context, id, new AdsCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.d(TAG, "Admob onInterstitialLoad");
                apInterstitialAd.setInterstitialAd(interstitialAd);
                adListener.onInterstitialLoad(apInterstitialAd);
                adListener.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "Admob onAdFailedToLoad");
                adListener.onAdFailedToLoad(new AdsError(i));
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                Log.d(TAG, "Admob onAdFailedToShow");
                adListener.onAdFailedToShow(new AdsError(adError));
            }
        });
        return apInterstitialAd;
    }

    /**
     * Result a ApInterstitialAd in onInterstitialLoad
     */
    public AdsInterstitial getInterstitialAds(Context context, String id) {
        AdsInterstitial apInterstitialAd = new AdsInterstitial();
        Admob.getInstance().getInterstitialAds(context, id, new AdsCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.d(TAG, "Admob onInterstitialLoad: ");
                apInterstitialAd.setInterstitialAd(interstitialAd);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
            }
        });
        return apInterstitialAd;
    }

    public void loadInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, YNMAdsCallbacks adListener) {
        adListener.onAdStartLoad();
        Admob.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, showSplashIfReady, new AdsCallback() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                adListener.onAdClosed();
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                adListener.onNextAction();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                adListener.onAdFailedToLoad(new AdsError(i));

            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                adListener.onAdFailedToShow(new AdsError(adError));

            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adListener.onAdLoaded();
            }

            @Override
            public void onAdSplashReady() {
                super.onAdSplashReady();
                adListener.onAdSplashReady();
            }


            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (adListener != null) {
                    adListener.onAdClicked();
                }
            }

            @Override
            public void onTimeOut() {
                super.onTimeOut();
                if (adListener != null) {
                    adListener.onTimeOut();
                }
            }
        });
    }

    /**
     * Called force show AdsInterstitial when ready
     */
    public void forceShowInterstitial(Context context, AdsInterstitial mInterstitialAd,
                                      final YNMAdsCallbacks callback) {
        forceShowInterstitial(context, mInterstitialAd, callback, false);
    }

    /**
     * Called force show AdsInterstitial when ready
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReloadAds auto reload ad when ad close
     */
    public void forceShowInterstitial(@NonNull Context context, AdsInterstitial mInterstitialAd,
                                      @NonNull final YNMAdsCallbacks callback, boolean shouldReloadAds) {
        boolean isSkip = System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                < YNMAds.getInstance().adConfig.getIntervalInterstitialAd() * 1000L;
        callback.onCheckSkipInter(isSkip);
        if (isSkip) {
            Log.i(TAG, "forceShowInterstitial: ignore by interval impression interstitial time");
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null || mInterstitialAd.isNotReady()) {
            Log.e(TAG, "forceShowInterstitial: AdsInterstitial is not ready");
            callback.onNextAction();
            return;
        }
        AdsCallback adCallback = new AdsCallback() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed: ");
                callback.onAdClosed();
                if (shouldReloadAds) {
                    callback.onAdStartLoad();
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdsCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            Log.d(TAG, "Admob shouldReloadAds success");
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd);
                            callback.onAdLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            mInterstitialAd.setInterstitialAd(null);
                            callback.onAdFailedToLoad(new AdsError(i));
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            callback.onAdImpression();
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(new AdsError(adError));
                        }

                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                Log.d(TAG, "onNextAction: ");
                callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                Log.d(TAG, "onAdFailedToShow: ");
                callback.onAdFailedToShow(new AdsError(adError));
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdsCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            Log.d(TAG, "Admob shouldReloadAds success");
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd);
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            callback.onAdFailedToLoad(new AdsError(i));
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(new AdsError(adError));
                        }

                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
            }

            @Override
            public void onInterstitialShow() {
                super.onInterstitialShow();
                callback.onInterstitialShow();
                callback.onAdImpression();
            }
        };
        Admob.getInstance().forceShowInterstitial(context, mInterstitialAd.getInterstitialAd(), adCallback);
    }

    public void showInterstitialAdByTimes(Context context, AdsInterstitial mInterstitialAd, final YNMAdsCallbacks callback, boolean shouldReloadAds) {
        if (mInterstitialAd.isNotReady()) {
            Log.e(TAG, "forceShowInterstitial: AdsInterstitial is not ready");
            callback.onAdFailedToShow(new AdsError("AdsInterstitial is not ready"));
            return;
        }
        AdsCallback adCallback = new AdsCallback() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed: ");
                callback.onAdClosed();
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdsCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            Log.d(TAG, "Admob shouldReloadAds success");
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd);
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            mInterstitialAd.setInterstitialAd(null);
                            callback.onAdFailedToLoad(new AdsError(i));
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(new AdsError(adError));
                        }

                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                Log.d(TAG, "onNextAction: ");
                callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                Log.d(TAG, "onAdFailedToShow: ");
                callback.onAdFailedToShow(new AdsError(adError));
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdsCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            Log.d(TAG, "Admob shouldReloadAds success");
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd);
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            callback.onAdFailedToLoad(new AdsError(i));
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(new AdsError(adError));
                        }
                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (callback != null) {
                    callback.onAdClicked();
                }
            }

            @Override
            public void onInterstitialShow() {
                super.onInterstitialShow();
                if (callback != null) {
                    callback.onInterstitialShow();
                }
            }
        };
        Admob.getInstance().showInterstitialAdByTimes(context, mInterstitialAd.getInterstitialAd(), adCallback);
    }

    public AdsReward getRewardAd(Activity activity, String id) {
        AdsReward apRewardAd = new AdsReward();
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                Admob.getInstance().initRewardAds(activity, id, new AdsCallback() {

                    @Override
                    public void onRewardAdLoaded(RewardedAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        Log.i(TAG, "getRewardAd AdLoaded: ");
                        apRewardAd.setAdmobReward(rewardedAd);
                    }
                }, null);
                break;
//            case YNAdConfig.PROVIDER_MAX:
//                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
//                    @Override
//                    public void onAdLoaded() {
//                        super.onAdLoaded();
//                    }
//                });
//                apRewardAd.setMaxReward(maxRewardedAd);
        }
        return apRewardAd;
    }

    public AdsReward getRewardAdInterstitial(Activity activity, String id) {
        AdsReward apRewardAd = new AdsReward();
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                Admob.getInstance().getRewardInterstitial(activity, id, new AdsCallback() {

                    @Override
                    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        Log.i(TAG, "getRewardAdInterstitial AdLoaded: ");
                        apRewardAd.setAdmobReward(rewardedAd);
                    }
                }, null);
                break;
//            case YNAdConfig.PROVIDER_MAX:
//                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
//                    @Override
//                    public void onAdLoaded() {
//                        super.onAdLoaded();
//                    }
//                });
//                apRewardAd.setMaxReward(maxRewardedAd);
        }
        return apRewardAd;
    }

    public AdsReward getRewardAd(Activity activity, String id, YNMAdsCallbacks callback) {
        AdsReward apRewardAd = new AdsReward();
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                Admob.getInstance().initRewardAds(activity, id, new AdsCallback() {
                    @Override
                    public void onRewardAdLoaded(RewardedAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        apRewardAd.setAdmobReward(rewardedAd);
                        callback.onAdLoaded();
                        callback.onRewardAdLoaded(apRewardAd);
                    }
                }, null);
                return apRewardAd;
//            case YNAdConfig.PROVIDER_MAX:
//                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
//                    @Override
//                    public void onAdLoaded() {
//                        super.onAdLoaded();
//                        callback.onAdLoaded();
//                    }
//                });
//                apRewardAd.setMaxReward(maxRewardedAd);
//                return apRewardAd;
        }
        return apRewardAd;
    }

    public AdsReward getRewardInterstitialAd(Activity activity, String id, YNMAdsCallbacks callback) {
        AdsReward apRewardAd = new AdsReward();
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                Admob.getInstance().getRewardInterstitial(activity, id, new AdsCallback() {
                    @Override
                    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        apRewardAd.setAdmobReward(rewardedAd);
                        callback.onAdLoaded();
                    }
                }, null);
                return apRewardAd;
//            case YNAdConfig.PROVIDER_MAX:
//                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
//                    @Override
//                    public void onAdLoaded() {
//                        super.onAdLoaded();
//                        callback.onAdLoaded();
//                    }
//                });
//                apRewardAd.setMaxReward(maxRewardedAd);
//                return apRewardAd;
        }
        return apRewardAd;
    }

    public void forceShowRewardAd(Activity activity, AdsReward apRewardAd, YNMAdsCallbacks
            callback) {
        if (!apRewardAd.isReady()) {
            Log.e(TAG, "forceShowRewardAd fail: reward ad not ready");
            callback.onNextAction();
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case YNMAdsConfig.PROVIDER_ADMOB:
                if (apRewardAd.isRewardInterstitial()) {
                    Admob.getInstance().showRewardInterstitial(activity, apRewardAd.getAdmobRewardInter(), new RewardCallback() {

                        @Override
                        public void onUserEarnedReward(RewardItem var1) {
                            callback.onUserEarnedReward(new AdsRewardItem(var1));
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            apRewardAd.clean();
                            callback.onNextAction();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int codeError) {
                            apRewardAd.clean();
                            callback.onAdFailedToShow(new AdsError(new AdError(codeError, "note msg", "Reward")));
                        }

                        @Override
                        public void onAdClicked() {
                            if (callback != null) {
                                callback.onAdClicked();
                            }
                        }
                    }, null);
                } else {
                    Admob.getInstance().showRewardAds(activity, apRewardAd.getAdmobReward(), new RewardCallback() {

                        @Override
                        public void onUserEarnedReward(RewardItem var1) {
                            callback.onUserEarnedReward(new AdsRewardItem(var1));
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            apRewardAd.clean();
                            callback.onNextAction();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int codeError) {
                            apRewardAd.clean();
                            callback.onAdFailedToShow(new AdsError(new AdError(codeError, "note msg", "Reward")));
                        }

                        @Override
                        public void onAdClicked() {
                            if (callback != null) {
                                callback.onAdClicked();
                            }
                        }
                    }, null);
                }
                break;
//            case YNAdConfig.PROVIDER_MAX:
//                AppLovin.getInstance().showRewardAd(activity, apRewardAd.getMaxReward(), new AppLovinCallback() {
//                    @Override
//                    public void onUserRewarded(MaxReward reward) {
//                        super.onUserRewarded(reward);
//                        callback.onUserEarnedReward(new ApRewardItem(reward));
//                    }
//
//                    @Override
//                    public void onAdClosed() {
//                        super.onAdClosed();
//                        apRewardAd.clean();
//                        callback.onNextAction();
//                    }
//
//                    @Override
//                    public void onAdFailedToShow(@Nullable MaxError adError) {
//                        super.onAdFailedToShow(adError);
//                        apRewardAd.clean();
//                        callback.onAdFailedToShow(new ApAdError(adError));
//                    }
//
//                    @Override
//                    public void onAdClicked() {
//                        super.onAdClicked();
//                        if (callback != null) {
//                            callback.onAdClicked();
//                        }
//                    }
//                });
        }
    }
}

package com.ads.yeknomadmob.max;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.dialogs.PrepareLoadingAdsDialog;
import com.ads.yeknomadmob.event.YNMLogEventManager;
import com.ads.yeknomadmob.utils.InitAdsCallback;
import com.ads.yeknomadmob.utils.MaxAdsCallback;
import com.ads.yeknomadmob.utils.RewardCallbackMax;
import com.ads.yeknomadmob.utils.SharePreferenceUtils;
import com.ads.yeknomadmob.utils.TypeAds;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.facebook.ads.AdSettings;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MaxNew {

    private static final String TAG = "AppLovin";
    private static MaxNew instance;
    private int currentClicked = 0;
    private String nativeId;
    private int numShowAds = 3;

    private int maxClickAds = 100;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private PrepareLoadingAdsDialog dialog;
    private boolean isTimeout; // xử lý timeout show ads

    public boolean isShowLoadingSplash = false;  //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
    private Context context;

    private MaxRewardedAd rewardedAd;
//    private AppOpenAd appOpenAd = null;
//    private static final String SHARED_PREFERENCE_NAME = "ads_shared_preference";

//    private final Map<String, AppOpenAd> appOpenAdMap = new HashMap<>();

    private MaxInterstitialAd interstitialSplash;
    private MaxInterstitialAd interstitialAd;
    MaxNativeAdView nativeAdView;

    private boolean disableAdResumeWhenClickAds = false;

    public static MaxNew getInstance() {
        if (instance == null) {
            instance = new MaxNew();
            instance.isShowLoadingSplash = false;
        }
        return instance;
    }

    public void init(Context context, String key, InitAdsCallback callback) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            String processName = Application.getProcessName();
//            String packageName = context.getPackageName();
//            if (!packageName.equals(processName)) {
//                WebView.setDataDirectorySuffix(processName);
//            }
//        }
        List<String> gaids = new ArrayList<>();
        gaids = new ArrayList<>();
        String gaid = "";
        try {
            gaid = String.valueOf(AdvertisingIdClient.getAdvertisingIdInfo(context));
        } catch (Exception e) {
            gaid = "";
        }
        gaids.add(gaid);
        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(key)
                .setMediationProvider( AppLovinMediationProvider.MAX )
                .setTestDeviceAdvertisingIds(gaids)
                .build();
        AdSettings.setDataProcessingOptions( new String[] {} );
        AppLovinSdk.getInstance(context).initialize( initConfig, appLovinSdkConfiguration -> {
            Log.d(TAG, "init: applovin success");
            callback.initSuccess();
        });
        this.context = context;
    }

    public void init(Context context, String key, AppLovinCallback adCallback, Boolean enableDebug) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            String processName = Application.getProcessName();
//            String packageName = context.getPackageName();
//            if (!packageName.equals(processName)) {
//                WebView.setDataDirectorySuffix(processName);
//            }
//        }
//        if (enableDebug)
//            AppLovinSdk.getInstance(context).showMediationDebugger();
        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(key)
                .setMediationProvider( AppLovinMediationProvider.MAX )
                .build();
        AdSettings.setDataProcessingOptions( new String[] {} );
        AppLovinSdk.getInstance(context).initialize( initConfig, appLovinSdkConfiguration -> {
            Log.d(TAG, "init: applovin success");
            adCallback.initAppLovinSuccess();
        });
        this.context = context;
    }

    public void setNumShowAds(int numShowAds) {
        this.numShowAds = numShowAds;
    }

    public void setNumToShowAds(int numShowAds, int currentClicked) {
        this.numShowAds = numShowAds;
        this.currentClicked = currentClicked;
    }

    public MaxInterstitialAd getInterstitialSplash() {
        return interstitialSplash;
    }

    /**
     * Disable ad resume when user click ads and back to app
     *
     * @param disableAdResumeWhenClickAds
     */
    public void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds) {
        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds;
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     *
     * @param context
     * @param id
     * @param timeOut    : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
     * @param timeDelay  : thời gian chờ show ad từ lúc load ads
     * @param adListener
     */
    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, AppLovinCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        Log.i(TAG, "loadSplashInterstitialAds  start time loading:"
                + Calendar.getInstance().getTimeInMillis()
                + " ShowLoadingSplash:" + isShowLoadingSplash);

//        if (AppPurchase.getInstance().isPurchased(context)) {
//            if (adListener != null) {
//                adListener.onAdClosed();
//            }
//            return;
//        }

        getInterstitialAds(context, id, new MaxAdsCallback() {});
        new Handler().postDelayed(() -> {
            //check delay show ad splash
            if (interstitialSplash != null && interstitialSplash.isReady()) {
                Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ");
                onShowSplash((Activity) context, adListener);
                return;
            }
            Log.i(TAG, "loadSplashInterstitialAds: delay validate");
            isTimeDelay = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = () -> {
                Log.e(TAG, "loadSplashInterstitialAds: on timeout");
                isTimeout = true;
                if (interstitialSplash != null && interstitialSplash.isReady()) {
                    Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ");
                    onShowSplash((Activity) context, adListener);
                    return;
                }
                if (adListener != null) {
                    adListener.onAdClosed();
                    isShowLoadingSplash = false;
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        isShowLoadingSplash = true;

        interstitialSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.e(TAG, "loadSplashInterstitialAds end time loading success: "
                        + Calendar.getInstance().getTimeInMillis()
                        + " time limit:" + isTimeout);
                if (isTimeout)
                    return;
                if (isTimeDelay) {
                    onShowSplash((Activity) context, adListener);
                    Log.i(TAG, "loadSplashInterstitialAds: show ad on loaded ");
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
//                AppOpenMax.getInstance().setInterstitialShowing(true);
            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: " + error.getMessage());
                if (isTimeout)
                    return;
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    Log.e(TAG, "loadSplashInterstitialAds: load fail " + error.getMessage());
                    adListener.onAdFailedToLoad(error);
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     *
     * @param context
     * @param id
     * @param timeOut    : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
     * @param timeDelay  : thời gian chờ show ad từ lúc load ads
     * @param adListener
     */
    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, AppLovinCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        Log.i(TAG, "loadSplashInterstitialAds  start time loading:"
                + Calendar.getInstance().getTimeInMillis()
                + " ShowLoadingSplash:" + isShowLoadingSplash);

//        if (AppPurchase.getInstance().isPurchased(context)) {
//            if (adListener != null) {
//                adListener.onAdClosed();
//            }
//            return;
//        }

        getInterstitialAds(context, id, null);
        new Handler().postDelayed(() -> {
            //check delay show ad splash
            if (interstitialSplash != null && interstitialSplash.isReady()) {
                Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ");
                if (showSplashIfReady)
                    onShowSplash((Activity) context, adListener);
                else
                    adListener.onAdSplashReady();
                return;
            }
            Log.i(TAG, "loadSplashInterstitialAds: delay validate");
            isTimeDelay = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = () -> {
                Log.e(TAG, "loadSplashInterstitialAds: on timeout");
                isTimeout = true;
                if (interstitialSplash != null && interstitialSplash.isReady()) {
                    Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ");
                    if (showSplashIfReady)
                        onShowSplash((Activity) context, adListener);
                    else
                        adListener.onAdSplashReady();

                    return;
                }
                if (adListener != null) {
                    adListener.onAdClosed();
                    isShowLoadingSplash = false;
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        isShowLoadingSplash = true;

        interstitialSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.e(TAG, "loadSplashInterstitialAds end time loading success: "
                        + Calendar.getInstance().getTimeInMillis()
                        + " time limit:" + isTimeout);
                if (isTimeout)
                    return;
                if (isTimeDelay) {
                    if (showSplashIfReady)
                        onShowSplash((Activity) context, adListener);
                    else
                        adListener.onAdSplashReady();
                    Log.i(TAG, "loadSplashInterstitialAds: show ad on loaded ");
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
//                AppOpenMax.getInstance().setInterstitialShowing(true);
            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
                if (adListener != null) {
                    adListener.onAdClicked();
                }
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: " + error.getMessage());
                if (isTimeout)
                    return;
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    Log.e(TAG, "loadSplashInterstitialAds: load fail " + error.getMessage());
                    adListener.onAdFailedToLoad(error);
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
    }

    public void onShowSplash(Activity activity, AppLovinCallback adListener) {
        isShowLoadingSplash = true;
        Log.d(TAG, "onShowSplash: ");
        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }
        if (interstitialSplash == null) {
            adListener.onAdClosed();
            return;
        }
        interstitialSplash.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(context, ad, TypeAds.INTERSTITIAL));
        interstitialSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "onAdDisplayed: ");
//                AppOpenMax.getInstance().setInterstitialShowing(true);
                if (adListener != null) {
                    adListener.onAdImpression();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "onAdHidden: " + ((AppCompatActivity) activity).getLifecycle().getCurrentState());
//                AppOpenMax.getInstance().setInterstitialShowing(false);
                isShowLoadingSplash = false;
                if (adListener != null && ((AppCompatActivity) activity).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    adListener.onAdClosed();
                    interstitialSplash = null;
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                YNMLogEventManager.logClickAdsEvent(context, interstitialSplash.getAdUnitId());
                if (adListener != null) {
                    adListener.onAdClicked();
                }
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onAdDisplayFailed: " + error.getMessage());
                interstitialSplash = null;
                isShowLoadingSplash = false;
                if (adListener != null) {
                    adListener.onAdFailedToShow(error);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new PrepareLoadingAdsDialog(activity);
                if (activity != null && !activity.isDestroyed()) {
                    dialog.setCancelable(false);
                    dialog.show();
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
                adListener.onAdClosed();
                return;
            }
            new Handler().postDelayed(() -> {
                if (activity != null && !activity.isDestroyed())
                    interstitialSplash.showAd(activity);
            }, 800);
        } else {
            Log.e(TAG, "onShowSplash fail ");
            isShowLoadingSplash = false;
        }
    }

    public void onCheckShowSplashWhenFail(Activity activity, AppLovinCallback callback, int timeDelay) {
        if (MaxNew.getInstance().getInterstitialSplash() != null && !MaxNew.getInstance().isShowLoadingSplash) {
            new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (MaxNew.getInstance().getInterstitialSplash().isReady()) {
                        Log.i(TAG, "show ad splash when show fail in background");
                        MaxNew.getInstance().onShowSplash(activity, callback);
                    } else {
                        callback.onAdClosed();
                    }
                }
            }, timeDelay);
        }
    }


    /**
     * Trả về 1 InterstitialAd và request Ads
     *
     * @param context
     * @param id
     * @return
     */
    public void getInterstitialAds(Context context, String id, MaxAdsCallback callback) {
//        if (AppPurchase.getInstance().isPurchased(context) || AppLovinHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
//            Log.d(TAG, "getInterstitialAds: ignore");
//            return null;
//        }
        final MaxInterstitialAd interstitialAd = new MaxInterstitialAd(id, context);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: getInterstitialAds");
                if (callback != null)
                    callback.onInterstitialLoad(interstitialAd, ad);
                interstitialAd.setRevenueListener(maxAd -> {
                    Log.d(TAG, "OnPaidEvent getInterstitialAds:" + maxAd.getRevenue());
                    YNMLogEventManager.logPaidAdImpression(context, maxAd, TypeAds.INTERSTITIAL);
                });
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: getInterstitialAds " + error.getMessage());
                Log.i(TAG, error.getMessage());
                if (callback != null)
                    callback.onAdFailedToLoad(error);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        interstitialAd.loadAd();
    }

    private void requestInterstitialAds(MaxInterstitialAd maxInterstitialAd) {
        if (maxInterstitialAd != null && !maxInterstitialAd.isReady()) {
            maxInterstitialAd.loadAd();
        }
    }

    /**
     * Bắt buộc hiển thị  ads full và callback result
     *
     * @param context
     * @param interstitialAd
     * @param callback
     */
    public void forceShowInterstitial(Context context, MaxInterstitialAd interstitialAd, final MaxAdsCallback callback, boolean shouldReload) {
        currentClicked = numShowAds;
        showInterstitialAdByTimes(context, interstitialAd, callback, shouldReload);
    }

    /**
     * Hiển thị ads theo số lần được xác định trước và callback result
     * vd: click vào 3 lần thì show ads full.
     * AdmodHelper.setupAdmodData(context) -> kiểm tra xem app đc hoạt động đc 1 ngày chưa nếu YES thì reset lại số lần click vào ads
     *
     * @param context
     * @param interstitialAd
     * @param callback
     * @param shouldReloadAds
     */
    public void showInterstitialAdByTimes(final Context context, MaxInterstitialAd interstitialAd, final MaxAdsCallback callback, final boolean shouldReloadAds) {
        AppLovinHelper.setupAppLovinData(context);
//        if (AppPurchase.getInstance().isPurchased(context)) {
//            callback.onAdClosed();
//            return;
//        }
        if (interstitialAd == null || !interstitialAd.isReady()) {
            if (callback != null) {
                callback.onAdClosed();
            }
            return;
        }

        interstitialAd.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(context, ad, TypeAds.INTERSTITIAL));
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
//                AppOpenMax.getInstance().setInterstitialShowing(true);
                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
            }


            @Override
            public void onAdHidden(MaxAd ad) {
//                AppOpenMax.getInstance().setInterstitialShowing(false);
                if (callback != null && ((AppCompatActivity) context).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    callback.onAdClosed();
                    if (shouldReloadAds) {
                        requestInterstitialAds(interstitialAd);
                    }
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Log.d(TAG, "onAdHidden: " + ((AppCompatActivity) context).getLifecycle().getCurrentState());
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
                if (callback != null) {
                    callback.onAdClicked();
                }
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.e(TAG, "onAdDisplayFailed: " + error.getMessage());
                if (callback != null) {
                    callback.onAdClosed();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
        if (AppLovinHelper.getNumClickAdsPerDay(context, interstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, interstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onAdClosed();
        }
    }

    /**
     * Kiểm tra và hiện thị ads
     *
     * @param context
     * @param interstitialAd
     * @param callback
     */
    private void showInterstitialAd(Context context, MaxInterstitialAd interstitialAd, MaxAdsCallback callback) {
        currentClicked++;
        if (currentClicked >= numShowAds && interstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new PrepareLoadingAdsDialog(context);
                    try {
                        callback.onInterstitialShow();
                        dialog.setCancelable(false);
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(interstitialAd::showAd, 800);
            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity
     *
     * @param mActivity
     * @param id
     */
    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer);
    }

    public void loadBanner(final Activity mActivity, String id, final AdCallback adCallback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, adCallback);
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     *
     * @param mActivity
     * @param id
     * @param rootView
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer);
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback adCallback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, adCallback);
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {
//        if (AppPurchase.getInstance().isPurchased(mActivity)) {
//            containerShimmer.setVisibility(View.GONE);
//            return;
//        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        MaxAdView adView = new MaxAdView(id, mActivity);
        adView.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(mActivity, ad, TypeAds.BANNER));
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = mActivity.getResources().getDimensionPixelSize(R.dimen.banner_height);
        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
        adContainer.addView(adView);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: banner");
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                adContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: banner " + error.getMessage() + "   code:" + error.getCode());
                containerShimmer.stopShimmer();
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        adView.loadAd();
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final AdCallback adCallback) {
//        if (AppPurchase.getInstance().isPurchased(mActivity)) {
//            containerShimmer.setVisibility(View.GONE);
//            return;
//        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        MaxAdView adView = new MaxAdView(id, mActivity);
        adView.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(mActivity, ad, TypeAds.BANNER));
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = mActivity.getResources().getDimensionPixelSize(R.dimen.banner_height);
        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
        adContainer.addView(adView);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: banner");
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                adContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                if (adCallback != null) {
                    adCallback.onAdImpression();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
                if (adCallback != null) {
                    adCallback.onAdClicked();
                }
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: banner " + error.getMessage() + "   code:" + error.getCode());
                containerShimmer.stopShimmer();
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        adView.loadAd();
    }

    public void loadNative(final Activity mActivity, String adUnitId, int layout) {
        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, layout);
    }

    public void loadNativeSmall(final Activity mActivity, String adUnitId, int layout) {
        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, layout);
    }

    public void loadNativeFragment(final Activity mActivity, String adUnitId, View parent, int layout) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, layout);
    }

    public void loadNativeSmallFragment(final Activity mActivity, String adUnitId, View parent, int layout) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, layout);
    }


    public void loadNativeAd(Activity activity, ShimmerFrameLayout containerShimmer, FrameLayout nativeAdLayout, String id, int layoutCustomNative) {

//        if (AppPurchase.getInstance().isPurchased(context)) {
//            containerShimmer.setVisibility(View.GONE);
//            return;
//        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        nativeAdLayout.removeAllViews();
        nativeAdLayout.setVisibility(View.GONE);
        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
                .setTitleTextViewId(R.id.ad_headline)
                .setBodyTextViewId(R.id.ad_body)
                .setAdvertiserTextViewId(R.id.ad_advertiser)
                .setIconImageViewId(R.id.ad_app_icon)
                .setMediaContentViewGroupId(R.id.ad_media)
//                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.ad_call_to_action)
                .build();

        nativeAdView = new MaxNativeAdView(binder, activity);

        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(id, activity);
        nativeAdLoader.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(activity, ad, TypeAds.NATIVE));
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                Log.d(TAG, "onNativeAdLoaded ");
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                // Add ad view to view.
                nativeAdLayout.setVisibility(View.VISIBLE);
                nativeAdLayout.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                nativeAdLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                Log.e(TAG, "`onNativeAdClicked`: ");
                containerShimmer.setVisibility(View.VISIBLE);
                containerShimmer.startShimmer();
                nativeAdLayout.removeAllViews();
                nativeAdLayout.setVisibility(View.GONE);

                nativeAdView = new MaxNativeAdView(binder, activity);
                nativeAdLoader.loadAd(nativeAdView);
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }
        });
        nativeAdLoader.loadAd(nativeAdView);
    }

    public void loadNativeAd(Activity activity, String id, int layoutCustomNative, AppLovinCallback callback) {

//        if (AppPurchase.getInstance().isPurchased(context)) {
//            callback.onAdClosed();
//            return;
//        }

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
                .setTitleTextViewId(R.id.ad_headline)
                .setBodyTextViewId(R.id.ad_body)
                .setAdvertiserTextViewId(R.id.ad_advertiser)
                .setIconImageViewId(R.id.ad_app_icon)
                .setMediaContentViewGroupId(R.id.ad_media)
//                .setOptionsContentViewGroupId(R.id.options_view)
                .setCallToActionButtonId(R.id.ad_call_to_action)
                .build();

        nativeAdView = new MaxNativeAdView(binder, activity);

        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(id, activity);
        nativeAdLoader.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(activity, ad, TypeAds.NATIVE));
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                Log.d(TAG, "onNativeAdLoaded ");
                callback.onUnifiedNativeAdLoaded(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
                callback.onAdFailedToLoad(error);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                Log.e(TAG, "onNativeAdClicked: ");
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
                callback.onAdClicked();
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }
        });
        nativeAdLoader.loadAd(nativeAdView);
    }

//    public MaxRecyclerAdapter getNativeRepeatAdapter(Activity activity, String id, int layoutCustomNative, RecyclerView.Adapter originalAdapter,
//                                                     MaxAdPlacer.Listener listener, int repeatingInterval) {
//        //seting max
//        MaxAdPlacerSettings settings = new MaxAdPlacerSettings(id);
//        settings.setRepeatingInterval(repeatingInterval);
//
//        MaxRecyclerAdapter adAdapter = new MaxRecyclerAdapter(settings, originalAdapter, activity);
//        adAdapter.getAdPlacer().setAdSize(-1, -1);
//
//        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
//                .setTitleTextViewId(com.ads.control.R.id.ad_headline)
//                .setBodyTextViewId(com.ads.control.R.id.ad_body)
//                .setAdvertiserTextViewId(com.ads.control.R.id.ad_advertiser)
//                .setIconImageViewId(com.ads.control.R.id.ad_app_icon)
//                .setMediaContentViewGroupId(com.ads.control.R.id.ad_media)
//                .setOptionsContentViewGroupId(com.ads.control.R.id.ad_options_view)
//                .setCallToActionButtonId(com.ads.control.R.id.ad_call_to_action)
//                .build();
//
//        adAdapter.getAdPlacer().setNativeAdViewBinder(binder);
//        if (listener != null)
//            adAdapter.setListener(listener);
//        return adAdapter;
//    }
//
//    public MaxRecyclerAdapter getNativeFixedPositionAdapter(Activity activity, String id, int layoutCustomNative, RecyclerView.Adapter originalAdapter,
//                                                            MaxAdPlacer.Listener listener, int position) {
//        //seting max
//        MaxAdPlacerSettings settings = new MaxAdPlacerSettings(id);
//        settings.addFixedPosition(position);
//
//        MaxRecyclerAdapter adAdapter = new MaxRecyclerAdapter(settings, originalAdapter, activity);
//        adAdapter.getAdPlacer().setAdSize(-1, -1);
//
//        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
//                .setTitleTextViewId(com.ads.control.R.id.ad_headline)
//                .setBodyTextViewId(com.ads.control.R.id.ad_body)
//                .setAdvertiserTextViewId(com.ads.control.R.id.ad_advertiser)
//                .setIconImageViewId(com.ads.control.R.id.ad_app_icon)
//                .setMediaContentViewGroupId(com.ads.control.R.id.ad_media)
//                .setOptionsContentViewGroupId(com.ads.control.R.id.ad_options_view)
//                .setCallToActionButtonId(com.ads.control.R.id.ad_call_to_action)
//                .build();
//
//        adAdapter.getAdPlacer().setNativeAdViewBinder(binder);
//        if (listener != null)
//            adAdapter.setListener(listener);
//        return adAdapter;
//    }


    public MaxRewardedAd getRewardAd(Activity activity, String id, AppLovinCallback callback) {
        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(id, activity);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                callback.onUserRewarded(reward);
                Log.d(TAG, "onUserRewarded: ");
            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: ");
                callback.onAdLoaded();
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "onAdDisplayed: ");
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                callback.onAdClosed();
                Log.d(TAG, "onAdHidden: ");
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
                callback.onAdClicked();
//                if (disableAdResumeWhenClickAds)
//                    AppOpenMax.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.d(TAG, "onAdLoadFailed: " + error.getMessage());
                callback.onAdFailedToLoad(error);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onAdDisplayFailed: " + error.getMessage());
                callback.onAdFailedToShow(error);
            }
        });
        rewardedAd.loadAd();
        return rewardedAd;
    }

    public void getRewardAd(Activity activity, String id, MaxAdsCallback callback) {

        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(id, activity);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onUserRewarded(@NonNull MaxAd maxAd, @NonNull MaxReward maxReward) {

            }

            @Override
            public void onAdLoaded(@NonNull MaxAd maxAd) {
                callback.onRewardAdLoaded(rewardedAd);
                MaxNew.this.rewardedAd = rewardedAd;
                MaxNew.this.rewardedAd.setRevenueListener(maxAd1 -> {
                    Log.d(TAG, "OnPaidEvent Reward:" + maxAd1.getRevenue());
                    YNMLogEventManager.logPaidAdImpression(context,maxAd1, TypeAds.REWARDED);
                });
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdHidden(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdClicked(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                callback.onAdFailedToLoad(maxError);
                MaxNew.this.rewardedAd = null;
                Log.e(TAG, "RewardedAd onAdFailedToLoad: " + maxError.getMessage());
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {

            }
        });
        rewardedAd.loadAd();
    }

    public void showRewardAd(Activity activity, MaxRewardedAd maxRewardedAd, RewardCallbackMax callback) {
        if (maxRewardedAd.isReady()) {
            maxRewardedAd.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(activity, ad, TypeAds.REWARDED));
            maxRewardedAd.setListener(new MaxRewardedAdListener() {
                @Override
                public void onUserRewarded(MaxAd ad, MaxReward reward) {
                    callback.onUserEarnedReward(reward);
                    Log.d(TAG, "onUserRewarded: ");
                }

                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.d(TAG, "onAdLoaded: ");
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    Log.d(TAG, "onAdDisplayed: ");
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    callback.onRewardedAdClosed();
                    Log.d(TAG, "onAdHidden: ");
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                    YNMLogEventManager.logClickAdsEvent(context, ad.getAdUnitId());
                    callback.onAdClicked();
//                    if (disableAdResumeWhenClickAds)
//                        AppOpenMax.getInstance().disableAdResumeByClickAction();
                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.d(TAG, "onAdLoadFailed: " + error.getMessage());
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    Log.d(TAG, "onAdDisplayFailed: " + error.getMessage());
                    callback.onRewardedAdFailedToShow(error);
                }
            });
            maxRewardedAd.showAd(activity);
        } else {
            Log.e(TAG, "showRewardAd error -  reward ad not ready");
            callback.onRewardedAdFailedToShow(null);
        }
    }

    public void showRewardAd(Activity activity, MaxRewardedAd maxRewardedAd) {
        if (maxRewardedAd.isReady()) {
            maxRewardedAd.setRevenueListener(ad -> YNMLogEventManager.logPaidAdImpression(activity, ad, TypeAds.REWARDED));
            maxRewardedAd.showAd(activity);
        } else {
            Log.e(TAG, "showRewardAd error -  reward ad not ready");
        }
    }
}

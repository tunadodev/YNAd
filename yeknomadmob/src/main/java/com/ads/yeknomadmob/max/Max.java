package com.ads.yeknomadmob.max;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.yeknomadmob.R;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.admobs.AppOpenManager;
import com.ads.yeknomadmob.ads_components.ads_native.AdmobRecyclerAdapter;
import com.ads.yeknomadmob.ads_components.ads_native.YNMAdPlacer;
import com.ads.yeknomadmob.ads_components.ads_native.YNMAdPlacerSettings;
import com.ads.yeknomadmob.dialogs.PrepareLoadingAdsDialog;
import com.ads.yeknomadmob.event.YNMLogEventManager;
import com.ads.yeknomadmob.utils.AdmodHelper;
import com.ads.yeknomadmob.utils.AdsCallback;
import com.ads.yeknomadmob.utils.AppUtil;
import com.ads.yeknomadmob.utils.MaxAdsCallback;
import com.ads.yeknomadmob.utils.RewardCallback;
import com.ads.yeknomadmob.utils.SharePreferenceUtils;
import com.ads.yeknomadmob.utils.TypeAds;
import com.applovin.impl.mediation.MaxErrorImpl;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.ump.ConsentInformation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Max {

//    private static final String TAG = "YKMMax";
//    private static Max instance;
//    private int currentClicked = 0;
//    private int numShowAds = 3;
//    private int maxClickAds = 100;
//    private final int MAX_SMALL_INLINE_BANNER_HEIGHT = 50;
//    private Handler handlerTimeout;
//    private Runnable rdTimeout;
//    private PrepareLoadingAdsDialog dialog;
//    private boolean isTimeout; // xử lý timeout show ads
//    private boolean disableAdResumeWhenClickAds = false;
//    private boolean isShowLoadingSplash = false;  //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
//    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
//    private boolean openActivityAfterShowInterAds = false;
//    private Context context;
//    public static final String BANNER_INLINE_SMALL_STYLE = "BANNER_INLINE_SMALL_STYLE";
//    public static final String BANNER_INLINE_LARGE_STYLE = "BANNER_INLINE_LARGE_STYLE";
//
//    MaxInterstitialAd splashInterstitial;
//    MaxInterstitialAd interstitial;
//    MaxAd splashMaxAd;
//    MaxAd interMaxAd;
//    private ConsentInformation consentInformation;
//
//    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
//
//    public static Max getInstance() {
//        if (instance == null) {
//            instance = new Max();
//            instance.isShowLoadingSplash = false;
//        }
//        return instance;
//    }
//
//    private Max() {
//
//    }
//
//    public void setNumToShowAds(int numShowAds) {
//        this.numShowAds = numShowAds;
//    }
//
//    public void setNumToShowAds(int numShowAds, int currentClicked) {
//        this.numShowAds = numShowAds;
//        this.currentClicked = currentClicked;
//    }
//
//    /**
//     * Disable ad resume when user click ads and back to app
//     */
//    public void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds) {
//        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds;
//    }
//
//    /**
//     * khởi tạo admod
//     */
//    public void init(Activity activity, Context context, List<String> testDeviceList) {
//        this.context = context;
//        initializeMobileAdsSdk(testDeviceList);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            String processName = Application.getProcessName();
//            String packageName = context.getPackageName();
//            if (!packageName.equals(processName)) {
//                WebView.setDataDirectorySuffix(processName);
//            }
//        }
//    }
//
//    void initializeMobileAdsSdk(List<String> testDeviceList) {
//        if (isMobileAdsInitializeCalled.getAndSet(true)) {
//            return;
//        }
//        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder("", null)
//                .setMediationProvider( AppLovinMediationProvider.MAX )
//                .build();
//
//        AppLovinSdk.getInstance(context).initialize( initConfig, null);
//    }
//
//    public void init(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            String processName = Application.getProcessName();
//            String packageName = context.getPackageName();
//            if (!packageName.equals(processName)) {
//                WebView.setDataDirectorySuffix(processName);
//            }
//        }
//
//        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder("", null)
//                .setMediationProvider( AppLovinMediationProvider.MAX )
//                .build();
//
//        AppLovinSdk.getInstance(context).initialize( initConfig, null);
//        this.context = context;
//    }
//
//
//    public boolean isShowLoadingSplash() {
//        return isShowLoadingSplash;
//    }
//
//    private String getProcessName(Context context) {
//        if (context == null) return null;
//        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
//            if (processInfo.pid == android.os.Process.myPid()) {
//                return processInfo.processName;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * If true -> callback onNextAction() is called right after Ad Interstitial showed
//     * It help remove delay when user click close Ad and onAdClosed called
//     */
//    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
//        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
//    }
//
//    public AdRequest getAdRequest() {
//        AdRequest.Builder builder = new AdRequest.Builder();
//        return builder.build();
//    }
//
//    public boolean interstitialSplashLoaded() {
//        return splashMaxAd != null;
//    }
//
//    public MaxInterstitialAd getSplashMaxInterstitial() {
//        return splashInterstitial;
//    }
//
//    /**
//     * Multiple id inter splash call water fall
//     */
//
//    public void loadSplashInterstitialAds(final Context context, ArrayList<String> listID, long timeOut, long timeDelay, boolean showSplashIfReady, MaxAdsCallback adListener) {
//        isTimeDelay = false;
//        isTimeout = false;
//        Log.i(TAG, "loadSplashInterstitialAds  start time loading:" + Calendar.getInstance().getTimeInMillis() + "    ShowLoadingSplash:" + isShowLoadingSplash);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //check delay show ad splash
//                if (splashMaxAd != null) {
//                    Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ");
//                    if (showSplashIfReady)
//                        onShowSplash((AppCompatActivity) context, adListener);
//                    else
//                        adListener.onAdSplashReady();
//                    return;
//                }
//                Log.i(TAG, "loadSplashInterstitialAds: delay validate");
//                isTimeDelay = true;
//            }
//        }, timeDelay);
//
//        if (timeOut > 0) {
//            handlerTimeout = new Handler();
//            rdTimeout = new Runnable() {
//                @Override
//                public void run() {
//                    Log.e(TAG, "loadSplashInterstitialAds: on timeout");
//                    isTimeout = true;
//                    if (splashMaxAd != null) {
//                        Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ");
//                        if (showSplashIfReady)
//                            onShowSplash((AppCompatActivity) context, adListener);
//                        else
//                            adListener.onAdSplashReady();
//                        return;
//                    }
//                    if (adListener != null) {
//                        adListener.onNextAction();
//                        isShowLoadingSplash = false;
//                    }
//                }
//            };
//            handlerTimeout.postDelayed(rdTimeout, timeOut);
//        }
//
//        isShowLoadingSplash = true;
//        getInterstitialAds(context, listID, new MaxAdsCallback() {
//            @Override
//            public void onInterstitialLoad(MaxInterstitialAd interstitialAd, MaxAd ad) {
//                super.onInterstitialLoad(interstitialAd, ad);
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (interstitialAd != null) {
//                    interstitial = interstitialAd;
//                    splashMaxAd = ad;
//                    if (isTimeDelay) {
//                        if (showSplashIfReady)
//                            onShowSplash((AppCompatActivity) context, adListener);
//                        else
//                            adListener.onAdSplashReady();
//                        Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
//                    }
//                }
//            }
//
////            @Override
////            public void onAdFailedToShow(@Nullable AdError adError) {
////                super.onAdFailedToShow(adError);
////                if (adListener != null) {
////                    adListener.onAdFailedToShow(adError);
////                    adListener.onNextAction();
////                }
////            }
//
//            @Override
//            public void onAdFailedToLoad(MaxError i) {
//                super.onAdFailedToLoad(i);
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (adListener != null) {
//                    adListener.onNextAction();
//                    if (handlerTimeout != null && rdTimeout != null) {
//                        handlerTimeout.removeCallbacks(rdTimeout);
//                    }
//                    if (i != null)
//                        Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
//                    adListener.onAdFailedToLoad(i);
//                }
//            }
//        });
//
//    }
//
//    /**
//     * get multiple id inter splash call water fall
//     */
//
//    public void getInterstitialAds(Context context, ArrayList<String> listID, MaxAdsCallback adCallback) {
//        for (String id : listID) {
//            if (AdmodHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
//                adCallback.onInterstitialLoad(null, null);
//                return;
//            }
//        }
//        if (listID.isEmpty()) {
//            adCallback.onInterstitialLoad(null, null);
//            return;
//        }
//
//        MaxInterstitialAd inter = new MaxInterstitialAd(listID.get(0), context);
////        inter.setAdReviewListener();
//        inter.setListener(new MaxAdListener() {
//            @Override
//            public void onAdLoaded(@NonNull MaxAd maxAd) {
//                if (adCallback != null)
//                    adCallback.onInterstitialLoad(inter, maxAd);
//                Log.d(TAG, "OnPaidEvent getInterstitialAds:" + maxAd.getRevenue());
//                Log.i(TAG, "InterstitialAds onAdLoaded");
//                Log.i(TAG + "CheckID", "InterstitialAds onAdLoaded: " + interMaxAd.getAdUnitId());
//            }
//
//            @Override
//            public void onAdDisplayed(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdHidden(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdClicked(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//                // Handle the error
//                Log.i(TAG, maxError.getMessage());
//                if (listID.isEmpty()) {
//                    if (adCallback != null)
//                        adCallback.onAdFailedToLoad(maxError);
//                } else {
//                    Log.i(TAG + "CheckID", "InterstitialAds onAdLoaded Fail: " + listID.get(0));
//                    listID.remove(0);
//                    Log.i(TAG, "InterstitialAds onAdLoaded");
//                    getInterstitialAds(context, listID, adCallback);
//                }
//            }
//
//            @Override
//            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
//
//            }
//        });
//        inter.loadAd();
//    }
//
//    /**
//     * Load quảng cáo Full tại màn SplashActivity
//     * Sau khoảng thời gian timeout thì load ads và callback về cho View
//     *
//     * @param timeOut    : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
//     * @param timeDelay  : thời gian chờ show ad từ lúc load ads
//     */
//    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, MaxAdsCallback adListener) {
//        isTimeDelay = false;
//        isTimeout = false;
//        Log.i(TAG, "loadSplashInterstitalAds  start time loading:" + Calendar.getInstance().getTimeInMillis() + "    ShowLoadingSplash:" + isShowLoadingSplash);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //check delay show ad splash
//                if (splashMaxAd != null) {
//                    Log.i(TAG, "loadSplashInterstitalAds:show ad on delay ");
//                    onShowSplash((AppCompatActivity) context, adListener);
//                    return;
//                }
//                Log.i(TAG, "loadSplashInterstitalAds: delay validate");
//                isTimeDelay = true;
//            }
//        }, timeDelay);
//
//        if (timeOut > 0) {
//            handlerTimeout = new Handler();
//            rdTimeout = new Runnable() {
//                @Override
//                public void run() {
//                    Log.e(TAG, "loadSplashInterstitalAds: on timeout");
//                    isTimeout = true;
//                    if (splashMaxAd != null) {
//                        Log.i(TAG, "loadSplashInterstitalAds:show ad on timeout ");
//                        onShowSplash((AppCompatActivity) context, adListener);
//                        return;
//                    }
//                    if (adListener != null) {
//                        adListener.onNextAction();
//                        isShowLoadingSplash = false;
//                    }
//                }
//            };
//            handlerTimeout.postDelayed(rdTimeout, timeOut);
//        }
//
//
//        isShowLoadingSplash = true;
//        getInterstitialAds(context, id, new MaxAdsCallback() {
//            @Override
//            public void onInterstitialLoad(MaxInterstitialAd interstitialAd,MaxAd ad) {
//                super.onInterstitialLoad(interstitialAd, ad);
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (interstitialAd != null) {
//                    splashInterstitial = interstitialAd;
//                    splashMaxAd = ad;
//                    if (isTimeDelay) {
//                        onShowSplash((AppCompatActivity) context, adListener);
//                        Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
//                    }
//                }
//            }
//
//            @Override
//            public void onAdFailedToLoad(MaxError i) {
//                super.onAdFailedToLoad(i);
//                isShowLoadingSplash = false;
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (adListener != null) {
//                    if (handlerTimeout != null && rdTimeout != null) {
//                        handlerTimeout.removeCallbacks(rdTimeout);
//                    }
//                    if (i != null)
//                        Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
//                    adListener.onAdFailedToLoad(i);
//                    adListener.onNextAction();
//                }
//            }
//
////            @Override
////            public void onAdFailedToShow(@Nullable AdError adError) {
////                super.onAdFailedToShow(adError);
////                if (adListener != null) {
////                    adListener.onAdFailedToShow(adError);
////                    adListener.onNextAction();
////                }
////            }
//        });
//
//    }
//
//    /**
//     * Load quảng cáo Full tại màn SplashActivity
//     * Sau khoảng thời gian timeout thì load ads và callback về cho View
//     *
//     * @param context
//     * @param id
//     * @param timeOut           : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
//     * @param timeDelay         : thời gian chờ show ad từ lúc load ads
//     * @param showSplashIfReady : auto show ad splash if ready
//     * @param adListener
//     */
//    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, MaxAdsCallback adListener) {
//        isTimeDelay = false;
//        isTimeout = false;
//        dialog = new PrepareLoadingAdsDialog(context);
//        dialog.setCancelable(false);
//        dialog.show();
//        Log.i(TAG, "loadSplashInterstitialAds  start time loading:" + Calendar.getInstance().getTimeInMillis() + "    ShowLoadingSplash:" + isShowLoadingSplash);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //check delay show ad splash
//                if (splashMaxAd != null) {
//                    Log.i(TAG, "loadSplashInterstitalAds:show ad on delay ");
//                    if (showSplashIfReady)
//                        onShowSplash((AppCompatActivity) context, adListener);
//                    else
//                        adListener.onAdSplashReady();
//                    return;
//                }
//                Log.i(TAG, "loadSplashInterstitalAds: delay validate");
//                isTimeDelay = true;
//            }
//        }, timeDelay);
//
//        if (timeOut > 0) {
//            handlerTimeout = new Handler();
//            rdTimeout = new Runnable() {
//                @Override
//                public void run() {
//                    Log.e(TAG, "loadSplashInterstitalAds: on timeout");
//                    isTimeout = true;
//                    if (splashMaxAd != null) {
//                        Log.i(TAG, "loadSplashInterstitalAds:show ad on timeout ");
//                        if (showSplashIfReady)
//                            onShowSplash((AppCompatActivity) context, adListener);
//                        else
//                            adListener.onAdSplashReady();
//                        return;
//                    }
//                    if (adListener != null) {
//                        adListener.onTimeOut();
//                        if (dialog != null && dialog.isShowing())
//                            dialog.dismiss();
//                        isShowLoadingSplash = false;
//                    }
//                }
//            };
//            handlerTimeout.postDelayed(rdTimeout, timeOut);
//        }
//
//        isShowLoadingSplash = true;
//        getInterstitialAds(context, id, new MaxAdsCallback() {
//            @Override
//            public void onInterstitialLoad(MaxInterstitialAd interstitialAd, MaxAd ad) {
//                super.onInterstitialLoad(interstitialAd, ad);
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (interstitialAd != null) {
//                    splashInterstitial = interstitialAd;
//                    splashMaxAd = ad;
//                    if (isTimeDelay) {
//                        if (showSplashIfReady)
//                            onShowSplash((AppCompatActivity) context, adListener);
//                        else
//                            adListener.onAdSplashReady();
//                        Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
//                    }
//                }
//            }
//
////            @Override
////            public void onAdFailedToShow(@Nullable AdError adError) {
////                super.onAdFailedToShow(adError);
////                if (adListener != null) {
////                    adListener.onAdFailedToShow(adError);
////                    adListener.onNextAction();
////                }
////            }
//
//            @Override
//            public void onAdFailedToLoad(MaxError i) {
//                super.onAdFailedToLoad(i);
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (adListener != null) {
//                    adListener.onNextAction();
//                    if (handlerTimeout != null && rdTimeout != null) {
//                        handlerTimeout.removeCallbacks(rdTimeout);
//                    }
//                    if (i != null)
//                        Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
//                    adListener.onAdFailedToLoad(i);
//                }
//            }
//        });
//
//    }
//
//    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean isShow, boolean showSplashIfReady, MaxAdsCallback adListener) {
//        isTimeDelay = false;
//        isTimeout = false;
//        Log.i(TAG, "loadSplashInterstitialAds  start time loading:" + Calendar.getInstance().getTimeInMillis() + "    ShowLoadingSplash:" + isShowLoadingSplash);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //check delay show ad splash
//                if (splashMaxAd != null) {
//                    Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ");
//                    if (showSplashIfReady && isShow)
//                        onShowSplash((AppCompatActivity) context, adListener);
//                    else
//                        adListener.onAdSplashReady();
//                    return;
//                }
//                Log.i(TAG, "loadSplashInterstitialAds: delay validate");
//                isTimeDelay = true;
//            }
//        }, timeDelay);
//
//        if (timeOut > 0) {
//            handlerTimeout = new Handler();
//            rdTimeout = new Runnable() {
//                @Override
//                public void run() {
//                    Log.e(TAG, "loadSplashInterstitialAds: on timeout");
//                    isTimeout = true;
//                    if (splashMaxAd != null) {
//                        Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ");
//                        if (showSplashIfReady && isShow)
//                            onShowSplash((AppCompatActivity) context, adListener);
//                        else
//                            adListener.onAdSplashReady();
//                        return;
//                    }
//                    if (adListener != null) {
//                        adListener.onNextAction();
//                        isShowLoadingSplash = false;
//                    }
//                }
//            };
//            handlerTimeout.postDelayed(rdTimeout, timeOut);
//        }
//
//        isShowLoadingSplash = true;
//        getInterstitialAds(context, id, new MaxAdsCallback() {
//            @Override
//            public void onInterstitialLoad(MaxInterstitialAd interstitialAd, MaxAd ad) {
//                super.onInterstitialLoad(interstitialAd, ad);
//                Log.e(TAG, "loadSplashInterstitialAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (interstitialAd != null) {
//                    splashMaxAd = ad;
//                    splashInterstitial = interstitialAd;
//                    if (isTimeDelay) {
//                        if (showSplashIfReady && isShow)
//                            onShowSplash((AppCompatActivity) context, adListener);
//                        else
//                            adListener.onAdSplashReady();
//                        Log.i(TAG, "loadSplashInterstitialAds:show ad on loaded ");
//                    }
//                }
//            }
//
////            @Override
////            public void onAdFailedToShow(@Nullable AdError adError) {
////                super.onAdFailedToShow(adError);
////                if (adListener != null) {
////                    adListener.onAdFailedToShow(adError);
////                    adListener.onNextAction();
////                }
////            }
//
//            @Override
//            public void onAdFailedToLoad(MaxError i) {
//                super.onAdFailedToLoad(i);
//                Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
//                if (isTimeout)
//                    return;
//                if (adListener != null) {
//                    adListener.onNextAction();
//                    if (handlerTimeout != null && rdTimeout != null) {
//                        handlerTimeout.removeCallbacks(rdTimeout);
//                    }
//                    if (i != null)
//                        Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
//                    adListener.onAdFailedToLoad(i);
//                }
//            }
//        });
//
//    }
//
//    public void onShowSplash(AppCompatActivity activity, MaxAdsCallback adListener) {
//        isShowLoadingSplash = true;
//        Log.d(TAG, "onShowSplash: ");
//
//        if (splashMaxAd == null) {
//            adListener.onNextAction();
//            return;
//        }
//
//        splashInterstitial.setRevenueListener(maxAd -> {
//            Log.d(TAG, "OnPaidEvent splash:" + maxAd.getRevenue());
//            YNMLogEventManager.logPaidAdImpression(context, maxAd, TypeAds.INTERSTITIAL);
//        });
//
//        if (handlerTimeout != null && rdTimeout != null) {
//            handlerTimeout.removeCallbacks(rdTimeout);
//        }
//
//        if (adListener != null) {
//            adListener.onAdLoaded();
//        }
//
//        splashInterstitial.setListener(new MaxAdListener() {
//            @Override
//            public void onAdLoaded(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdDisplayed(@NonNull MaxAd maxAd) {
//                Log.d(TAG, " Splash:onAdShowedFullScreenContent ");
//                AppOpenManager.getInstance().setInterstitialShowing(true);
//                isShowLoadingSplash = false;
//                if (adListener != null) {
//                    adListener.onAdImpression();
//                }
//            }
//
//            @Override
//            public void onAdHidden(@NonNull MaxAd maxAd) {
//                Log.d(TAG, " Splash:onAdDismissedFullScreenContent ");
//                AppOpenManager.getInstance().setInterstitialShowing(false);
//                splashMaxAd = null;
//                if (adListener != null) {
//                    if (!openActivityAfterShowInterAds) {
//                        adListener.onNextAction();
//                    }
//                    adListener.onAdClosed();
//
//                    if (dialog != null) {
//                        dialog.dismiss();
//                    }
//                }
//                isShowLoadingSplash = false;
//            }
//
//            @Override
//            public void onAdClicked(@NonNull MaxAd maxAd) {
//                if (disableAdResumeWhenClickAds)
//                    AppOpenManager.getInstance().disableAdResumeByClickAction();
//                YNMLogEventManager.logClickAdsEvent(context, splashMaxAd.getAdUnitId());
//            }
//
//            @Override
//            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//
//            }
//
//            @Override
//            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
//                Log.e(TAG, "Splash onAdFailedToShowFullScreenContent: " + maxError.getMessage());
//                splashMaxAd = null;
//                isShowLoadingSplash = false;
//                if (adListener != null) {
//                    adListener.onAdFailedToShow(maxError);
//                    if (!openActivityAfterShowInterAds) {
//                        adListener.onNextAction();
//                    }
//
//                    if (dialog != null) {
//                        dialog.dismiss();
//                    }
//                }
//            }
//        });
//
//        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//            try {
//                try {
//                    AppOpenManager.getInstance().setInterstitialShowing(true);
//                } catch (Exception e) {
//                    if (adListener != null)
//                        adListener.onNextAction();
//                    return;
//                }
//            } catch (Exception e) {
//                dialog = null;
//                e.printStackTrace();
//            }
//            new Handler().postDelayed(() -> {
//                if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//                    if (openActivityAfterShowInterAds && adListener != null) {
//                        adListener.onNextAction();
//                        new Handler().postDelayed(() -> {
//                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
//                                dialog.dismiss();
//                        }, 1500);
//                    }
//                    if (activity != null && splashMaxAd != null) {
//                        if (splashInterstitial.isReady()) {
//                            Log.i(TAG, "start show InterstitialAd " + activity.getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
//                            splashInterstitial.showAd(activity);
//                            isShowLoadingSplash = false;
//                        }
//                    } else if (adListener != null) {
//                        if (dialog != null) {
//                            dialog.dismiss();
//                        }
//                        adListener.onNextAction();
//                        isShowLoadingSplash = false;
//                    }
//                } else {
//                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
//                        dialog.dismiss();
//                    isShowLoadingSplash = false;
//                    Log.e(TAG, "onShowSplash:   show fail in background after show loading ad");
//                    adListener.onAdFailedToShow(new MaxErrorImpl(0, " show fail in background after show loading ad YNMAds"));
//                }
//            }, 800);
//
//        } else {
//            isShowLoadingSplash = false;
//            Log.e(TAG, "onShowSplash: fail on background");
//        }
//    }
//
//    public void onShowSplash(AppCompatActivity activity, MaxAdsCallback adListener, MaxInterstitialAd mInter) {
//        splashInterstitial = mInter;
//        isShowLoadingSplash = true;
//        Log.d(TAG, "onShowSplash: ");
//
//        if (mInter == null) {
//            adListener.onNextAction();
//            return;
//        }
//
//        splashInterstitial.setRevenueListener(maxAd -> {
//            Log.d(TAG, "OnPaidEvent splash:" + maxAd.getRevenue());
//            YNMLogEventManager.logPaidAdImpression(context, maxAd, TypeAds.INTERSTITIAL);
//        });
//
//        if (handlerTimeout != null && rdTimeout != null) {
//            handlerTimeout.removeCallbacks(rdTimeout);
//        }
//
//        if (adListener != null) {
//            adListener.onAdLoaded();
//        }
//
//        splashInterstitial.setListener(new MaxAdListener() {
//            @Override
//            public void onAdLoaded(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdDisplayed(@NonNull MaxAd maxAd) {
//                Log.d(TAG, " Splash:onAdShowedFullScreenContent ");
//                AppOpenManager.getInstance().setInterstitialShowing(true);
//                isShowLoadingSplash = false;
//                if (adListener != null) {
//                    adListener.onAdImpression();
//                }
//            }
//
//            @Override
//            public void onAdHidden(@NonNull MaxAd maxAd) {
//                Log.d(TAG, " Splash:onAdDismissedFullScreenContent ");
//                AppOpenManager.getInstance().setInterstitialShowing(false);
//                splashMaxAd = null;
//                if (adListener != null) {
//                    if (!openActivityAfterShowInterAds) {
//                        adListener.onNextAction();
//                    }
//                    adListener.onAdClosed();
//
//                    if (dialog != null) {
//                        dialog.dismiss();
//                    }
//                }
//                isShowLoadingSplash = false;
//            }
//
//            @Override
//            public void onAdClicked(@NonNull MaxAd maxAd) {
//                if (disableAdResumeWhenClickAds)
//                    AppOpenManager.getInstance().disableAdResumeByClickAction();
//                YNMLogEventManager.logClickAdsEvent(context, splashMaxAd.getAdUnitId());
//            }
//
//            @Override
//            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//
//            }
//
//            @Override
//            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
//                Log.e(TAG, "Splash onAdFailedToShowFullScreenContent: " + maxError.getMessage());
//                splashMaxAd = null;
//                isShowLoadingSplash = false;
//                if (adListener != null) {
//                    adListener.onAdFailedToShow(maxError);
//                    if (!openActivityAfterShowInterAds) {
//                        adListener.onNextAction();
//                    }
//
//                    if (dialog != null) {
//                        dialog.dismiss();
//                    }
//                }
//            }
//        });
//
//        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//            try {
//                if (dialog != null && dialog.isShowing())
//                    dialog.dismiss();
//                dialog = new PrepareLoadingAdsDialog(activity);
//                try {
//                    AppOpenManager.getInstance().setInterstitialShowing(true);
//                } catch (Exception e) {
//                    adListener.onNextAction();
//                    return;
//                }
//            } catch (Exception e) {
//                dialog = null;
//                e.printStackTrace();
//            }
//            new Handler().postDelayed(() -> {
//                if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//                    if (openActivityAfterShowInterAds && adListener != null) {
//                        adListener.onNextAction();
//                        new Handler().postDelayed(() -> {
//                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
//                                dialog.dismiss();
//                        }, 1500);
//                    }
//                    if (activity != null && splashMaxAd != null) {
//                        Log.i(TAG, "start show InterstitialAd " + activity.getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
//                        splashInterstitial.showAd(activity);
//                        isShowLoadingSplash = false;
//                    } else if (adListener != null) {
//                        if (dialog != null) {
//                            dialog.dismiss();
//                        }
//                        adListener.onNextAction();
//                        isShowLoadingSplash = false;
//                    }
//                } else {
//                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
//                        dialog.dismiss();
//                    isShowLoadingSplash = false;
//                    Log.e(TAG, "onShowSplash:   show fail in background after show loading ad");
//                    if (adListener != null)
//                        adListener.onAdFailedToShow(new MaxErrorImpl(0, " show fail in background after show loading ad YNMAds"));
//                }
//            }, 800);
//
//        } else {
//            isShowLoadingSplash = false;
//            Log.e(TAG, "onShowSplash: fail on background");
//        }
//    }
//
//    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AdsCallback callback, int timeDelay) {
//        new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (interstitialSplashLoaded() && !isShowLoadingSplash()) {
//                    Log.i(TAG, "show ad splash when show fail in background");
//                    Admob.getInstance().onShowSplash(activity, callback);
//                }
//            }
//        }, timeDelay);
//    }
//
//    public void loadInterstitialAds(Context context, String id, long timeOut, MaxAdsCallback adListener) {
//        isTimeout = false;
//        interMaxAd = null;
//        getInterstitialAds(context, id, new MaxAdsCallback() {
//            @Override
//            public void onInterstitialLoad(MaxInterstitialAd interstitialAd, MaxAd ad) {
//                Max.this.interstitial = interstitialAd;
//
//                if (interstitialAd == null) {
//                    if (adListener != null) {
//                        adListener.onAdFailedToLoad(null);
//                    }
//                    return;
//                }
//                if (handlerTimeout != null && rdTimeout != null) {
//                    handlerTimeout.removeCallbacks(rdTimeout);
//                }
//                if (isTimeout) {
//                    return;
//                }
//                if (adListener != null) {
//                    if (handlerTimeout != null && rdTimeout != null) {
//                        handlerTimeout.removeCallbacks(rdTimeout);
//                    }
//                    adListener.onInterstitialLoad(interstitialAd, ad);
//                }
//
//                if (interstitialAd != null) {
//                    interstitialAd.setRevenueListener(maxAd -> {
//                        Log.d(TAG, "OnPaidEvent loadInterstitialAds:" + maxAd.getRevenue());
//                        YNMLogEventManager.logPaidAdImpression(context, maxAd, TypeAds.INTERSTITIAL);
//                    });
//                }
//            }
//
//            @Override
//            public void onAdFailedToLoad(MaxError i) {
//
//                if (adListener != null) {
//                    if (handlerTimeout != null && rdTimeout != null) {
//                        handlerTimeout.removeCallbacks(rdTimeout);
//                    }
//                    adListener.onAdFailedToLoad(i);
//                }
//            }
//        });
//
//
//        if (timeOut > 0) {
//            handlerTimeout = new Handler();
//            rdTimeout = () -> {
//                isTimeout = true;
//                if (interstitial != null) {
//                    adListener.onInterstitialLoad(interstitial, interMaxAd);
//                    return;
//                }
//                if (adListener != null) {
//
//                    adListener.onNextAction();
//                }
//            };
//            handlerTimeout.postDelayed(rdTimeout, timeOut);
//        }
//    }
//
//
//    /**
//     * Trả về 1 InterstitialAd và request Ads
//     *
//     * @param context
//     * @param id
//     * @return
//     */
//    public void getInterstitialAds(Context context, String id, MaxAdsCallback adCallback) {
//        if (AdmodHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
//            adCallback.onInterstitialLoad(null, null);
//            return;
//        }
//
//        MaxInterstitialAd inter = new MaxInterstitialAd(id, context);
////        inter.setAdReviewListener();
//        inter.setListener(new MaxAdListener() {
//            @Override
//            public void onAdLoaded(@NonNull MaxAd maxAd) {
//                if (adCallback != null)
//                    adCallback.onInterstitialLoad(inter, maxAd);
//
//                //tracking adjust
//                inter.setRevenueListener(maxAd1 -> {
//                    Log.d(TAG, "OnPaidEvent getInterstitialAds:" + maxAd1.getRevenue());
//
//                    YNMLogEventManager.logPaidAdImpression(context, maxAd1, TypeAds.INTERSTITIAL);
//                });
//                Log.i(TAG, "InterstitialAds onAdLoaded");
//            }
//
//            @Override
//            public void onAdDisplayed(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdHidden(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdClicked(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//                Log.i(TAG, maxError.getMessage());
//                if (adCallback != null)
//                    adCallback.onAdFailedToLoad(maxError);
//            }
//
//            @Override
//            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
//
//            }
//        });
//        inter.loadAd();
//    }
//
//
//    /**
//     * Hiển thị ads  timeout
//     * Sử dụng khi reopen app in splash
//     */
//    public void showInterstitialAdByTimes(final Context context, final MaxInterstitialAd mInterstitialAd, final MaxAdsCallback callback, long timeDelay) {
//        if (timeDelay > 0) {
//            handlerTimeout = new Handler();
//            rdTimeout = new Runnable() {
//                @Override
//                public void run() {
//                    forceShowInterstitial(context, mInterstitialAd, callback);
//                }
//            };
//            handlerTimeout.postDelayed(rdTimeout, timeDelay);
//        } else {
//            forceShowInterstitial(context, mInterstitialAd, callback);
//        }
//    }
//
//
//    /**
//     * Hiển thị ads theo số lần được xác định trước và callback result
//     * vd: click vào 3 lần thì show ads full.
//     * AdmodHelper.setupAdmodData(context) -> kiểm tra xem app đc hoạt động đc 1 ngày chưa nếu YES thì reset lại số lần click vào ads
//     */
//    public void showInterstitialAdByTimes(final Context context, MaxInterstitialAd mInterstitialAd, final MaxAdsCallback callback) {
//        AdmodHelper.setupAdmodData(context);
//
//        if (mInterstitialAd == null) {
//            if (callback != null) {
//                callback.onNextAction();
//            }
//            return;
//        }
//
//        mInterstitialAd.setListener(new MaxAdListener() {
//            @Override
//            public void onAdLoaded(@NonNull MaxAd maxAd) {
//
//            }
//
//            @Override
//            public void onAdDisplayed(@NonNull MaxAd maxAd) {
//                Log.e(TAG, "onAdShowedFullScreenContent ");
//                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
//                AppOpenManager.getInstance().setInterstitialShowing(true);
//            }
//
//            @Override
//            public void onAdHidden(@NonNull MaxAd maxAd) {
//                AppOpenManager.getInstance().setInterstitialShowing(false);
//                if (callback != null) {
//                    if (!openActivityAfterShowInterAds) {
//                        callback.onNextAction();
//                    }
//                    callback.onAdClosed();
//                }
//                if (dialog != null) {
//                    dialog.dismiss();
//                }
//                Log.e(TAG, "onAdDismissedFullScreenContent");
//            }
//
//            @Override
//            public void onAdClicked(@NonNull MaxAd maxAd) {
//                if (disableAdResumeWhenClickAds)
//                    AppOpenManager.getInstance().disableAdResumeByClickAction();
//                if (callback != null) {
//                    callback.onAdClicked();
//                }
//                YNMLogEventManager.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
//            }
//
//            @Override
//            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//
//            }
//
//            @Override
//            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
//                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + maxError.getMessage());
//                // Called when fullscreen content failed to show.
//                if (callback != null) {
//                    callback.onAdFailedToShow(maxError);
//                    if (!openActivityAfterShowInterAds) {
//                        callback.onNextAction();
//                    }
//
//                    if (dialog != null) {
//                        dialog.dismiss();
//                    }
//                }
//            }
//        });
//
//        if (AdmodHelper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
//            if (dialog != null && !dialog.isShowing()) {
//                dialog.dismiss();
//            }
//            dialog = new PrepareLoadingAdsDialog(context);
//            dialog.setCancelable(false);
//            dialog.show();
//            showInterstitialAd(context, mInterstitialAd, callback);// 1000ms = 1 second
//            return;
//        }
//        if (callback != null) {
//            callback.onNextAction();
//        }
//    }
//
//
//    /**
//     * Bắt buộc hiển thị  ads full và callback result
//     *
//     * @param context
//     * @param mInterstitialAd
//     * @param callback
//     */
//    public void forceShowInterstitial(Context context, MaxInterstitialAd mInterstitialAd, final MaxAdsCallback callback) {
//        currentClicked = numShowAds;
//        showInterstitialAdByTimes(context, mInterstitialAd, callback);
//    }
//
//    /**
//     * Kiểm tra và hiện thị ads
//     *
//     * @param context
//     * @param mInterstitialAd
//     * @param callback
//     */
//    private void showInterstitialAd(Context context, MaxInterstitialAd mInterstitialAd, MaxAdsCallback callback) {
//        currentClicked++;
//        if (currentClicked >= numShowAds && mInterstitialAd != null) {
//            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//                try {
////                    if (dialog != null && dialog.isShowing())
////                        dialog.dismiss();
//                    try {
//                        callback.onInterstitialShow();
//                        AppOpenManager.getInstance().setInterstitialShowing(true);
//                    } catch (Exception e) {
//                        callback.onNextAction();
//                        return;
//                    }
//                } catch (Exception e) {
//                    dialog = null;
//                    e.printStackTrace();
//                }
//                new Handler().postDelayed(() -> {
//                    if (((AppCompatActivity) context).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//                        if (openActivityAfterShowInterAds && callback != null) {
//                            callback.onNextAction();
//                            new Handler().postDelayed(() -> {
//                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
//                                    dialog.dismiss();
//                            }, 1500);
//                        }
//                        Log.i(TAG, "start show InterstitialAd " + ((AppCompatActivity) context).getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
//                        mInterstitialAd.showAd((Activity) context);
//                    } else {
//                        if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
//                            dialog.dismiss();
//                        Log.e(TAG, "showInterstitialAd:   show fail in background after show loading ad");
//                        callback.onAdFailedToShow(new MaxErrorImpl(0, " show fail in background after show loading ad YNMAds"));
//                    }
//                }, 800);
//            }
//            currentClicked = 0;
//        } else if (callback != null) {
//            if (dialog != null) {
//                dialog.dismiss();
//            }
//            callback.onNextAction();
//        }
//    }
//
//    /**
//     * Load quảng cáo Banner Trong Activity
//     *
//     * @param mActivity
//     * @param id
//     */
//    public void loadBanner(final Activity mActivity, String id) {
//        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load quảng cáo Banner Trong Activity
//     *
//     * @param mActivity
//     * @param id
//     */
//    public void loadBanner(final Activity mActivity, String id, MaxAdsCallback callback) {
//        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
//    }
//
//
//    /**
//     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
//     *
//     * @param mActivity
//     * @param id
//     * @deprecated Using loadInlineBanner()
//     */
//    @Deprecated
//    public void loadBanner(final Activity mActivity, String id, Boolean useInlineAdaptive) {
//        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
//     *
//     * @param activity
//     * @param id
//     * @param inlineStyle
//     */
//    public void loadInlineBanner(final Activity activity, String id, String inlineStyle) {
//        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
//        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
//    }
//
//    /**
//     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
//     *
//     * @param mActivity
//     * @param id
//     * @param callback
//     * @param useInlineAdaptive
//     * @deprecated Using loadInlineBanner() with callback
//     */
//    @Deprecated
//    public void loadBanner(final Activity mActivity, String id, final MaxAdsCallback callback, Boolean useInlineAdaptive) {
//        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
//     *
//     * @param activity
//     * @param id
//     * @param inlineStyle
//     * @param callback
//     */
//    public void loadInlineBanner(final Activity activity, String id, String inlineStyle, final MaxAdsCallback callback) {
//        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
//        loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
//    }
//
//    /**
//     * Load Quảng Cáo Banner Trong Fragment
//     *
//     * @param mActivity
//     * @param id
//     * @param rootView
//     */
//    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
//        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load Quảng Cáo Banner Trong Fragment
//     */
//    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final MaxAdsCallback callback) {
//        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
//     *
//     * @deprecated Using loadInlineBannerFragment()
//     */
//    @Deprecated
//    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, Boolean useInlineAdaptive) {
//        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
//     */
//    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle) {
//        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
//        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
//    }
//
//    /**
//     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
//     *
//     * @deprecated Using loadInlineBannerFragment() with callback
//     */
//    @Deprecated
//    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final MaxAdsCallback callback, Boolean useInlineAdaptive) {
//        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
//        loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
//    }
//
//    /**
//     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
//     */
//    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle, final MaxAdsCallback callback) {
//        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
//        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
//        loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
//    }
//
//    public void loadBanner(final Activity mActivity, String id,
//                           final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer,
//                           final MaxAdsCallback callback, Boolean useInlineAdaptive, String inlineStyle) {
//
//        if (adContainer.getChildCount() > 0) {
//            for (int i = 0; i < adContainer.getChildCount(); i++) {
//                View child = adContainer.getChildAt(i);
//                if (child instanceof MaxAdView) {
//                    ((MaxAdView) child).destroy();
//                }
//            }
//            adContainer.removeAllViews();
//        }
//
//        adContainer.setVisibility(View.GONE);
//        containerShimmer.setVisibility(View.VISIBLE);
//        containerShimmer.startShimmer();
//        try {
//
//            MaxAdView adView = new MaxAdView(id, context);
//            adView.setListener(new MaxAdViewAdListener() {
//                @Override
//                public void onAdExpanded(@NonNull MaxAd maxAd) {
//
//                }
//
//                @Override
//                public void onAdCollapsed(@NonNull MaxAd maxAd) {
//
//                }
//
//                @Override
//                public void onAdLoaded(@NonNull MaxAd maxAd) {
//                    Log.d(TAG, "Banner adapter class name: " + adView.getClass().getName());
//                    containerShimmer.stopShimmer();
//                    containerShimmer.setVisibility(View.GONE);
//                    adContainer.setVisibility(View.VISIBLE);
//                    if (adView != null) {
//                        adView.setRevenueListener(maxAd1 -> {
//                            Log.d(TAG, "OnPaidEvent banner:" + maxAd1.getRevenue());
//
//                            YNMLogEventManager.logPaidAdImpression(context, maxAd1, TypeAds.BANNER);
//                        });
//                    }
//
//                    if (callback != null) {
//                        callback.onAdLoaded();
//                    }
//                }
//
//                @Override
//                public void onAdDisplayed(@NonNull MaxAd maxAd) {
//                    if (callback != null) {
//                        callback.onAdImpression();
//                    }
//                }
//
//                @Override
//                public void onAdHidden(@NonNull MaxAd maxAd) {
//
//                }
//
//                @Override
//                public void onAdClicked(@NonNull MaxAd maxAd) {
//                    if (disableAdResumeWhenClickAds)
//                        AppOpenManager.getInstance().disableAdResumeByClickAction();
//                    if (callback != null) {
//                        callback.onAdClicked();
//                        Log.d(TAG, "onAdClicked");
//                    }
//                    YNMLogEventManager.logClickAdsEvent(context, id);
//                }
//
//                @Override
//                public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//                    if (callback != null) {
//                        callback.onAdFailedToLoad(maxError);
//                    }
//                }
//
//                @Override
//                public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
//
//                }
//            });
//
//            boolean isTablet = AppLovinSdkUtils.isTablet(context);
//            int height;
//            if (isTablet){
//                height = 90;
//            } else {
//                height = 50;
//            }
//            int heightPx = AppLovinSdkUtils.dpToPx(context, height);
//            adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
//            adView.setBackgroundColor(Color.WHITE);
//            adContainer.addView(adView);
//
//            if (heightPx > 0) {
////                int heightInPixels = (int) (heightPx * Resources.getSystem().getDisplayMetrics().density + 0.5f);
//                containerShimmer.getLayoutParams().height = heightPx;
//                containerShimmer.requestLayout();
//            }
////            adView.setAdSize(adSize);
//            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            adView.loadAd();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    /**
//     * load quảng cáo big native
//     *
//     * @param mActivity
//     * @param id
//     */
//    public void loadNative(final Activity mActivity, String id) {
//        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
//        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
//        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.custom_native_admob_free_size);
//    }
//
//    public void loadNativeAd(Context context, String id, final MaxAdsCallback callback) {
////        VideoOptions videoOptions = new VideoOptions.Builder()
////                .setStartMuted(true)
////                .build();
//
////        NativeAdOptions adOptions = new NativeAdOptions.Builder()
////                .setVideoOptions(videoOptions)
////                .build();
//
//        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(id, context);
//        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
//            @Override
//            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, @NonNull MaxAd maxAd) {
//                super.onNativeAdLoaded(maxNativeAdView, maxAd);
//                callback.onUnifiedNativeAdLoaded(maxAd);
//            }
//
//            @Override
//            public void onNativeAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//                super.onNativeAdLoadFailed(s, maxError);
//                Log.e(TAG, "NativeAd onAdFailedToLoad: " + maxError.getMessage());
//                callback.onAdFailedToLoad(maxError);
//            }
//
//            @Override
//            public void onNativeAdClicked(@NonNull MaxAd maxAd) {
//                super.onNativeAdClicked(maxAd);
//                if (disableAdResumeWhenClickAds)
//                    AppOpenManager.getInstance().disableAdResumeByClickAction();
//                if (callback != null) {
//                    callback.onAdClicked();
//                    Log.d(TAG, "onAdClicked");
//                }
//                YNMLogEventManager.logClickAdsEvent(context, id);
//            }
//
//            @Override
//            public void onNativeAdExpired(@NonNull MaxAd maxAd) {
//                super.onNativeAdExpired(maxAd);
//            }
//        });
//        nativeAdLoader.setRevenueListener(maxAd1 -> {
//            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + maxAd1.getRevenue());
//            YNMLogEventManager.logPaidAdImpression(context, maxAd1, TypeAds.NATIVE);
//            Log.d(TAG, "native onAdImpression");
//            if (callback != null) {
//                callback.onAdImpression();
//            }
//        });
//        nativeAdLoader.loadAd();
//    }
//
//    private void loadNative(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout) {
//        frameLayout.removeAllViews();
//        frameLayout.setVisibility(View.GONE);
//        containerShimmer.setVisibility(View.VISIBLE);
//        containerShimmer.startShimmer();
//
////        VideoOptions videoOptions = new VideoOptions.Builder()
////                .setStartMuted(true)
////                .build();
////
////        NativeAdOptions adOptions = new NativeAdOptions.Builder()
////                .setVideoOptions(videoOptions)
////                .build();
//        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(id, context);
//        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
//            @Override
//            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, @NonNull MaxAd maxAd) {
//                super.onNativeAdLoaded(maxNativeAdView, maxAd);
//                containerShimmer.stopShimmer();
//                containerShimmer.setVisibility(View.GONE);
//                frameLayout.setVisibility(View.VISIBLE);
//                View adView = LayoutInflater.from(context).inflate(layout, null);
////                @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
////                        .inflate(layout, null);
//                nativeAdLoader.loadAd(maxNativeAdView);
//                frameLayout.removeAllViews();
//                frameLayout.addView(adView);
//            }
//
//            @Override
//            public void onNativeAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
//                super.onNativeAdLoadFailed(s, maxError);
//                Log.e(TAG, "NativeAd onAdFailedToLoad: " + maxError.getMessage());
//                callback.onAdFailedToLoad(maxError);
//            }
//
//            @Override
//            public void onNativeAdClicked(@NonNull MaxAd maxAd) {
//                super.onNativeAdClicked(maxAd);
//                if (disableAdResumeWhenClickAds)
//                    AppOpenManager.getInstance().disableAdResumeByClickAction();
//                if (callback != null) {
//                    callback.onAdClicked();
//                    Log.d(TAG, "onAdClicked");
//                }
//                YNMLogEventManager.logClickAdsEvent(context, id);
//            }
//
//            @Override
//            public void onNativeAdExpired(@NonNull MaxAd maxAd) {
//                super.onNativeAdExpired(maxAd);
//            }
//        });
//        nativeAdLoader.setRevenueListener(maxAd1 -> {
//            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + maxAd1.getRevenue());
//            YNMLogEventManager.logPaidAdImpression(context, maxAd1, TypeAds.NATIVE);
//            Log.d(TAG, "native onAdImpression");
//            if (callback != null) {
//                callback.onAdImpression();
//            }
//        });
//        nativeAdLoader.loadAd();
//
//        AdLoader adLoader = new AdLoader.Builder(context, id)
//                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
//
//                    @Override
//                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
//
//                    }
//
//
//                })
//                .withAdListener(new AdListener() {
//                    @Override
//                    public void onAdFailedToLoad(LoadAdError error) {
//                        Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
//                        containerShimmer.stopShimmer();
//                        containerShimmer.setVisibility(View.GONE);
//                        frameLayout.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onAdClicked() {
//                        super.onAdClicked();
//                        if (disableAdResumeWhenClickAds)
//                            AppOpenManager.getInstance().disableAdResumeByClickAction();
//                        YNMLogEventManager.logClickAdsEvent(context, id);
//                    }
//                })
//                .withNativeAdOptions(adOptions)
//                .build();
//
//        adLoader.loadAd(getAdRequest());
//    }
//
//    private View createNativeAdView(Context context, @Nullable MaxNativeAdView maxNativeAdView, int layout)
//    {
//        View viewOutput = LayoutInflater.from(context).inflate(layout, null);
//        try {
//            if (maxNativeAdView != null)
//                ((ImageView) viewOutput
//                        .findViewById(R.id.iconImageView))
//                        .setImageDrawable(maxNativeAdView.getIconImageView().getDrawable());
//        } catch (Exception ignored) {}
//
//        try {
//            if (maxNativeAdView != null)
//                ((TextView) viewOutput
//                        .findViewById(R.id.titleTextView))
//                        .setText(maxNativeAdView.getTitleTextView().getText());
//        } catch (Exception ignored) {}
//
//        try {
//            if (maxNativeAdView != null)
//                ((TextView) viewOutput
//                        .findViewById(R.id.titleTextView))
//                        .setText(maxNativeAdView.getTitleTextView().getText());
//        } catch (Exception ignored) {}
//
//        try {
//            if (maxNativeAdView != null)
//                ((TextView) viewOutput
//                        .findViewById(R.id.advertiserTextView))
//                        .setText(maxNativeAdView.getAdvertiserTextView().getText());
//        } catch (Exception ignored) {}
//
//        try {
//            if (maxNativeAdView != null)
//                ((TextView) viewOutput
//                        .findViewById(R.id.bodyTextView))
//                        .setText(maxNativeAdView.getBodyTextView().getText());
//        } catch (Exception ignored) {}
//
//        try {
//            if (maxNativeAdView != null)
//                ((Button) viewOutput
//                        .findViewById(R.id.ctaButton))
//                        .setText(maxNativeAdView.getCallToActionButton().getText());
//        } catch (Exception ignored) {}
//        return viewOutput;
//    }
//
//    private void loadNative(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout, final AdsCallback callback) {
//        frameLayout.removeAllViews();
//        frameLayout.setVisibility(View.GONE);
//        containerShimmer.setVisibility(View.VISIBLE);
//        containerShimmer.startShimmer();
//
////        VideoOptions videoOptions = new VideoOptions.Builder()
////                .setStartMuted(true)
////                .build();
////
////        NativeAdOptions adOptions = new NativeAdOptions.Builder()
////                .setVideoOptions(videoOptions)
////                .build();
//
//
//        AdLoader adLoader = new AdLoader.Builder(context, id)
//                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
//
//                    @Override
//                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
//                        containerShimmer.stopShimmer();
//                        containerShimmer.setVisibility(View.GONE);
//                        frameLayout.setVisibility(View.VISIBLE);
//                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
//                                .inflate(layout, null);
//                        nativeAd.setOnPaidEventListener(adValue -> {
//                            Log.d(TAG, "OnPaidEvent Native:" + adValue.getValueMicros());
//
//                            YNMLogEventManager.logPaidAdImpression(context,
//                                    adValue,
//                                    id,
//                                    nativeAd.getResponseInfo(), TypeAds.NATIVE);
//                        });
//                        populateUnifiedNativeAdView(nativeAd, adView);
//                        frameLayout.removeAllViews();
//                        frameLayout.addView(adView);
//                    }
//
//                })
//                .withAdListener(new AdListener() {
//                    @Override
//                    public void onAdFailedToLoad(LoadAdError error) {
//                        Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
//                        containerShimmer.stopShimmer();
//                        containerShimmer.setVisibility(View.GONE);
//                        frameLayout.setVisibility(View.GONE);
//                    }
//
//
//                    @Override
//                    public void onAdClicked() {
//                        super.onAdClicked();
//                        if (disableAdResumeWhenClickAds)
//                            AppOpenManager.getInstance().disableAdResumeByClickAction();
//                        if (callback != null) {
//                            callback.onAdClicked();
//                            Log.d(TAG, "onAdClicked");
//                        }
//                        YNMLogEventManager.logClickAdsEvent(context, id);
//                    }
//                })
//                .withNativeAdOptions(adOptions)
//                .build();
//
//
//        adLoader.loadAd(getAdRequest());
//    }
//
//    public AdmobRecyclerAdapter getNativeRepeatAdapter(Activity activity, String id, int layoutCustomNative, int layoutAdPlaceHolder, RecyclerView.Adapter originalAdapter,
//                                                       YNMAdPlacer.Listener listener, int repeatingInterval) {
//        YNMAdPlacerSettings settings = new YNMAdPlacerSettings(layoutCustomNative, layoutAdPlaceHolder);
//        settings.setAdUnitId(id);
//        settings.setListener(listener);
//        settings.setRepeatingInterval(repeatingInterval);
//        AdmobRecyclerAdapter adAdapter = new AdmobRecyclerAdapter(settings, originalAdapter, activity);
//        return adAdapter;
//    }
//
//    public AdmobRecyclerAdapter getNativeFixedPositionAdapter(Activity activity, String id, int layoutCustomNative, int layoutAdPlaceHolder, RecyclerView.Adapter originalAdapter,
//                                                              YNMAdPlacer.Listener listener, int position) {
//        YNMAdPlacerSettings settings = new YNMAdPlacerSettings(layoutCustomNative, layoutAdPlaceHolder);
//        settings.setAdUnitId(id);
//        settings.setListener(listener);
//        settings.setFixedPosition(position);
//        AdmobRecyclerAdapter adAdapter = new AdmobRecyclerAdapter(settings, originalAdapter, activity);
//        return adAdapter;
//    }
//
//
//    @SuppressLint("HardwareIds")
//    public String getDeviceId(Activity activity) {
//        String android_id = Settings.Secure.getString(activity.getContentResolver(),
//                Settings.Secure.ANDROID_ID);
//        return md5(android_id).toUpperCase();
//    }
//
//    private String md5(final String s) {
//        try {
//            // Create MD5 Hash
//            MessageDigest digest = MessageDigest
//                    .getInstance("MD5");
//            digest.update(s.getBytes());
//            byte messageDigest[] = digest.digest();
//
//            // Create Hex String
//            StringBuffer hexString = new StringBuffer();
//            for (int i = 0; i < messageDigest.length; i++) {
//                String h = Integer.toHexString(0xFF & messageDigest[i]);
//                while (h.length() < 2)
//                    h = "0" + h;
//                hexString.append(h);
//            }
//            return hexString.toString();
//
//        } catch (NoSuchAlgorithmException e) {
//        }
//        return "";
//    }
//
//    public PrepareLoadingAdsDialog getDialog() {
//        return dialog;
//    }
//
//
//
//    private RewardedAd rewardedAd;
//    private String rewardId;
//    /**
//     * Khởi tạo quảng cáo reward
//     *
//     * @param context
//     * @param id
//     */
//    public void initRewardAds(Context context, String id, String tokenAdjust) {
////        if (Arrays.asList(context.getResources().getStringArray(R.array.list_id_test)).contains(id)) {
////            showTestIdAlert(context, REWARD_ADS, id);
////        }
////        if (AppPurchase.getInstance().isPurchased(context)) {
////            return;
////        }
//        this.rewardId = id;
//        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                Max.this.rewardedAd = rewardedAd;
//                Max.this.rewardedAd.setOnPaidEventListener(adValue -> {
//
//                    Log.d(TAG, "OnPaidEvent Reward:" + adValue.getValueMicros());
//
//                    YNMLogEventManager.logPaidAdImpression(context,
//                            adValue,
//                            rewardedAd.getAdUnitId(), Max.this.rewardedAd.getResponseInfo()
//                            , TypeAds.REWARDED);
//
////                    if (tokenAdjust != null) {
////                        YNMLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
////                    }
//                });
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                super.onAdFailedToLoad(loadAdError);
//                Log.e(TAG, "RewardedAd onAdFailedToLoad: " + loadAdError.getMessage());
//            }
//        });
//    }
//
//    /**
//     * Load ad Reward
//     *
//     * @param context
//     * @param id
//     */
//    public void initRewardAds(Context context, String id, AdsCallback callback, String tokenAdjust) {
////        if (Arrays.asList(context.getResources().getStringArray(R.array.list_id_test)).contains(id)) {
////            showTestIdAlert(context, REWARD_ADS, id);
////        }
////        if (AppPurchase.getInstance().isPurchased(context)) {
////            return;
////        }
//        this.rewardId = id;
//        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                callback.onRewardAdLoaded(rewardedAd);
//                Max.this.rewardedAd = rewardedAd;
//                Max.this.rewardedAd.setOnPaidEventListener(adValue -> {
//                    Log.d(TAG, "OnPaidEvent Reward:" + adValue.getValueMicros());
//
//                    YNMLogEventManager.logPaidAdImpression(context,
//                            adValue,
//                            rewardedAd.getAdUnitId(),
//                            Max.this.rewardedAd.getResponseInfo()
//                            , TypeAds.REWARDED);
//
////                    if (tokenAdjust != null) {
////                        YNLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
////                    }
//                });
//
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                callback.onAdFailedToLoad(loadAdError);
//                Max.this.rewardedAd = null;
//                Log.e(TAG, "RewardedAd onAdFailedToLoad: " + loadAdError.getMessage());
//            }
//        });
//    }
//
//    /**
//     * Load ad Reward Interstitial
//     *
//     * @param context
//     * @param id
//     */
//    public void getRewardInterstitial(Context context, String id, AdsCallback callback, String tokenAdjust) {
////        if (Arrays.asList(context.getResources().getStringArray(R.array.list_id_test)).contains(id)) {
////            showTestIdAlert(context, REWARD_ADS, id);
////        }
////        if (AppPurchase.getInstance().isPurchased(context)) {
////            return;
////        }
//        this.rewardId = id;
//        RewardedInterstitialAd.load(context, id, getAdRequest(), new RewardedInterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedAd) {
//                callback.onRewardAdLoaded(rewardedAd);
//                Log.i(TAG, "RewardInterstitial onAdLoaded ");
//                rewardedAd.setOnPaidEventListener(adValue -> {
//                    Log.d(TAG, "OnPaidEvent Reward:" + adValue.getValueMicros());
//                    YNMLogEventManager.logPaidAdImpression(context,
//                            adValue,
//                            rewardedAd.getAdUnitId(),
//                            rewardedAd.getResponseInfo()
//                            , TypeAds.REWARDED);
//
////                    if (tokenAdjust != null) {
////                        YNLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
////                    }
//                });
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                callback.onAdFailedToLoad(loadAdError);
//                Log.e(TAG, "RewardInterstitial onAdFailedToLoad: " + loadAdError.getMessage());
//            }
//        });
//    }
//
//    public RewardedAd getRewardedAd() {
//
//        return rewardedAd;
//    }
//
//    /**
//     * Show Reward and callback
//     *
//     * @param context
//     * @param adCallback
//     */
//    public void showRewardAds(final Activity context, final RewardCallback adCallback, String tokenAdjust) {
////        if (AppPurchase.getInstance().isPurchased(context)) {
////            adCallback.onUserEarnedReward(null);
////            return;
////        }
//        if (rewardedAd == null) {
//            initRewardAds(context, this.rewardId, tokenAdjust);
//
//            adCallback.onRewardedAdFailedToShow(0);
//            return;
//        } else {
//            Max.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent();
//                    if (adCallback != null)
//                        adCallback.onRewardedAdClosed();
//
//                    AppOpenManager.getInstance().setInterstitialShowing(false);
//
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                    super.onAdFailedToShowFullScreenContent(adError);
//                    if (adCallback != null)
//                        adCallback.onRewardedAdFailedToShow(adError.getCode());
//                }
//
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    super.onAdShowedFullScreenContent();
//
//                    AppOpenManager.getInstance().setInterstitialShowing(true);
//                    rewardedAd = null;
//                }
//
//                public void onAdClicked() {
//                    super.onAdClicked();
//                    if (disableAdResumeWhenClickAds)
//                        AppOpenManager.getInstance().disableAdResumeByClickAction();
//                    YNMLogEventManager.logClickAdsEvent(context, rewardedAd.getAdUnitId());
//                }
//            });
//            rewardedAd.show(context, new OnUserEarnedRewardListener() {
//                @Override
//                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//                    if (adCallback != null) {
//                        adCallback.onUserEarnedReward(rewardItem);
//
//                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * Show Reward Interstitial and callback
//     *
//     * @param activity
//     * @param rewardedInterstitialAd
//     * @param adCallback
//     */
//    public void showRewardInterstitial(final Activity activity, RewardedInterstitialAd rewardedInterstitialAd, final RewardCallback adCallback, String tokenAdjust) {
////        if (AppPurchase.getInstance().isPurchased(activity)) {
////            adCallback.onUserEarnedReward(null);
////            return;
////        }
//        if (rewardedInterstitialAd == null) {
//            initRewardAds(activity, this.rewardId, tokenAdjust);
//
//            adCallback.onRewardedAdFailedToShow(0);
//            return;
//        } else {
//            rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent();
//                    if (adCallback != null)
//                        adCallback.onRewardedAdClosed();
//
//                    AppOpenManager.getInstance().setInterstitialShowing(false);
//
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                    super.onAdFailedToShowFullScreenContent(adError);
//                    if (adCallback != null)
//                        adCallback.onRewardedAdFailedToShow(adError.getCode());
//                }
//
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    super.onAdShowedFullScreenContent();
//
//                    AppOpenManager.getInstance().setInterstitialShowing(true);
//
//                }
//
//                public void onAdClicked() {
//                    super.onAdClicked();
//                    YNMLogEventManager.logClickAdsEvent(activity, rewardedAd.getAdUnitId());
//                    if (disableAdResumeWhenClickAds)
//                        AppOpenManager.getInstance().disableAdResumeByClickAction();
//                }
//            });
//            rewardedInterstitialAd.show(activity, new OnUserEarnedRewardListener() {
//                @Override
//                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//                    if (adCallback != null) {
//                        adCallback.onUserEarnedReward(rewardItem);
//                    }
//                }
//            });
//        }
//    }
//
//
//    /**
//     * Show quảng cáo reward và nhận kết quả trả về
//     *
//     * @param context
//     * @param adCallback
//     */
//    public void showRewardAds(final Activity context, RewardedAd rewardedAd, final RewardCallback adCallback, String tokenAdjust) {
////        if (AppPurchase.getInstance().isPurchased(context)) {
////            adCallback.onUserEarnedReward(null);
////            return;
////        }
//        if (rewardedAd == null) {
//            initRewardAds(context, this.rewardId, tokenAdjust);
//
//            adCallback.onRewardedAdFailedToShow(0);
//            return;
//        } else {
//            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent();
//                    if (adCallback != null)
//                        adCallback.onRewardedAdClosed();
//
//
//                    AppOpenManager.getInstance().setInterstitialShowing(false);
//
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                    super.onAdFailedToShowFullScreenContent(adError);
//                    if (adCallback != null)
//                        adCallback.onRewardedAdFailedToShow(adError.getCode());
//                }
//
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    super.onAdShowedFullScreenContent();
//
//                    AppOpenManager.getInstance().setInterstitialShowing(true);
//                    initRewardAds(context, rewardId, tokenAdjust);
//                }
//
//                public void onAdClicked() {
//                    super.onAdClicked();
//                    if (disableAdResumeWhenClickAds)
//                        AppOpenManager.getInstance().disableAdResumeByClickAction();
//                    if (adCallback != null) {
//                        adCallback.onAdClicked();
//                    }
//                    YNMLogEventManager.logClickAdsEvent(context, rewardedAd.getAdUnitId());
//                }
//            });
//            rewardedAd.show(context, new OnUserEarnedRewardListener() {
//                @Override
//                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//                    if (adCallback != null) {
//                        adCallback.onUserEarnedReward(rewardItem);
//
//                    }
//                }
//            });
//        }
//    }
//
//    public final static int SPLASH_ADS = 0;
//    public final static int RESUME_ADS = 1;
//    private final static int BANNER_ADS = 2;
//    private final static int INTERS_ADS = 3;
//    private final static int REWARD_ADS = 4;
//    private final static int NATIVE_ADS = 5;

}

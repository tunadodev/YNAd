package com.example.andmoduleads.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ads.nomyek.admob.Admob;
import com.ads.nomyek.admob.AppOpenManager;
import com.ads.nomyek.ads.YNAd;
import com.ads.nomyek.ads.YNAdCallback;
import com.ads.nomyek.ads.nativeAds.YNNativeAdView;
import com.ads.nomyek.config.YNAdConfig;
import com.ads.nomyek.ads.bannerAds.YNBannerAdView;
import com.ads.nomyek.ads.wrapper.ApAdError;
import com.ads.nomyek.ads.wrapper.ApInterstitialAd;
import com.ads.nomyek.ads.wrapper.ApRewardAd;
import com.ads.nomyek.billing.AppPurchase;
import com.ads.nomyek.dialog.DialogExitApp1;
import com.ads.nomyek.dialog.InAppDialog;
import com.ads.nomyek.funtion.AdCallback;
import com.ads.nomyek.funtion.DialogExitListener;
import com.ads.nomyek.funtion.PurchaseListener;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.ads.nomyek.util.AdsNativePreload;
import com.example.andmoduleads.BuildConfig;
import com.example.andmoduleads.R;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.nativead.NativeAd;

public class MainActivity extends AppCompatActivity {
    public static final String PRODUCT_ID = "android.test.purchased";
    private static final String TAG = "MAIN_TEST";
    //adjust
    private static final String EVENT_TOKEN_SIMPLE = "g3mfiw";
    private static final String EVENT_TOKEN_REVENUE = "a4fd35";


    private FrameLayout frAds;
    private NativeAd unifiedNativeAd;
    private ApInterstitialAd mInterstitialAd;
    private ApRewardAd rewardAd;

    private boolean isShowDialogExit = false;

    private String idBanner = "";
    private String idNative = "";
    private String idInter = "";

    private int layoutNativeCustom;
    private YNNativeAdView YNNativeAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        YNNativeAdView = findViewById(R.id.YNNativeAds);


        configMediationProvider();
        YNAd.getInstance().setCountClickToShowAds(3);

        AppOpenManager.getInstance().setEnableScreenContentCallback(true);
        AppOpenManager.getInstance().setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                Log.e("AppOpenManager", "onAdShowedFullScreenContent: ");

            }
        });
        /**
         * Sample integration native ads
         */

        AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                Log.e("PurchaseListioner", "ProductPurchased:" + productId);
                Log.e("PurchaseListioner", "transactionDetails:" + transactionDetails);
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Log.e("PurchaseListioner", "displayErrorMessage:" + errorMsg);
            }

            @Override
            public void onUserCancelBilling() {

            }
        });

        YNBannerAdView bannerAdView = findViewById(R.id.bannerView);
        bannerAdView.loadBanner(this, idBanner, new YNAdCallback() {
            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });
        loadAdInterstitial();

        findViewById(R.id.btShowAds).setOnClickListener(v -> {
            YNAd.getInstance().loadSplashInterstitialAds(this, idInter, 5000, 0, true, new YNAdCallback() {
                @Override
                public void onNextAction() {
                    startActivity(new Intent(MainActivity.this, ContentActivity.class));
                }
            });

        });

        findViewById(R.id.btForceShowAds).setOnClickListener(v -> {
            if (mInterstitialAd.isReady()) {
                YNAd.getInstance().forceShowInterstitial(this, mInterstitialAd, new YNAdCallback() {
                    @Override
                    public void onNextAction() {
                        Log.i(TAG, "onAdClosed: start content and finish main");
                        startActivity(new Intent(MainActivity.this, SimpleListActivity.class));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable ApAdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.i(TAG, "onAdFailedToShow:" + adError.getMessage());
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        Log.d(TAG, "onInterstitialShow");
                    }
                }, true);
            } else {
                loadAdInterstitial();
            }

        });
        AdsNativePreload.flexPreloadedShowNativeAds(this, YNNativeAdView, "test", idNative, R.layout.custom_native_max_medium2);

        findViewById(R.id.btnShowReward).setOnClickListener(v -> {

//            if (rewardAd != null && rewardAd.isReady()) {
//                YNAd.getInstance().forceShowRewardAd(this, rewardAd, new YNAdCallback());
//                return;
//            }
//            rewardAd = YNAd.getInstance().getRewardAd(this,  BuildConfig.ad_reward, null);
        });

        Button btnIAP = findViewById(R.id.btIap);
        if (AppPurchase.getInstance().isPurchased()) {
            btnIAP.setText("Consume Purchase");
        } else {
            btnIAP.setText("Purchase");
        }
        btnIAP.setOnClickListener(v -> {
            if (AppPurchase.getInstance().isPurchased()) {
                AppPurchase.getInstance().consumePurchase(AppPurchase.PRODUCT_ID_TEST);
            } else {
                InAppDialog dialog = new InAppDialog(this);
                dialog.setCallback(() -> {
                    AppPurchase.getInstance().purchase(this, PRODUCT_ID);
                    dialog.dismiss();
                });
                dialog.show();
            }
        });

    }

    private void configMediationProvider() {
        if (YNAd.getInstance().getMediationProvider() == YNAdConfig.PROVIDER_ADMOB) {
            idBanner = BuildConfig.ad_banner;
            idNative = BuildConfig.ad_native;
            idInter = BuildConfig.ad_interstitial_splash;
            layoutNativeCustom = com.ads.nomyek.R.layout.custom_native_admod_medium_rate;
        } else {
            idBanner = getString(R.string.applovin_test_banner);
            idNative = getString(R.string.applovin_test_native);
            idInter = getString(R.string.applovin_test_inter);
            layoutNativeCustom = com.ads.nomyek.R.layout.custom_native_max_medium2;
        }
    }

    private void loadAdInterstitial() {

        mInterstitialAd = YNAd.getInstance().getInterstitialAds(this, idInter, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNativeExit();
    }

    private void loadNativeExit() {

        if (unifiedNativeAd != null)
            return;
        Admob.getInstance().loadNativeAd(this, BuildConfig.ad_native, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(NativeAd unifiedNativeAd) {
                MainActivity.this.unifiedNativeAd = unifiedNativeAd;
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        }, null);
    }

    @Override
    public void onBackPressed() {
        if (unifiedNativeAd == null)
            return;

        DialogExitApp1 dialogExitApp1 = new DialogExitApp1(this, unifiedNativeAd, 1);
        dialogExitApp1.setDialogExitListener(new DialogExitListener() {
            @Override
            public void onExit(boolean exit) {
                MainActivity.super.onBackPressed();
            }
        });
        dialogExitApp1.setCancelable(false);
        dialogExitApp1.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult", "ProductPurchased:" + data.toString());
        if (AppPurchase.getInstance().isPurchased(this)) {
            findViewById(R.id.btIap).setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private MaxNativeAdLoader nativeAdLoader;
    private MaxAd nativeAd;
    private void loadNativeAd() {
        nativeAdLoader = new MaxNativeAdLoader(idNative, this);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd maxNativeAdView)
            {
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd);
                }

                nativeAd = maxNativeAdView;

                // Lấy thông tin quảng cáo
                MaxNativeAd nativeAdData = maxNativeAdView.getNativeAd();
                if (nativeAdData != null) {
//                    FrameLayout adContainer = findViewById(R.id.native_ad_container);
//                    adContainer.removeAllViews();
//                    adContainer.addView(nativeAdView);
                    bindNativeAd(nativeAdData);
                }
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error)
            {
                Toast.makeText(MainActivity.this, "Load Ad Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                // Native ad load failed.
                // AppLovin recommends retrying with exponentially higher delays up to a maximum delay.
            }

            @Override
            public void onNativeAdClicked(final MaxAd nativeAd) {
                Toast.makeText(MainActivity.this, "Ad Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        nativeAdLoader.loadAd();
    }

    private void bindNativeAd(MaxNativeAd nativeAd) {
        // Tìm các view trong layout quảng cáo
    //        FrameLayout adContainer = findViewById(R.id.native_ad_container);
    //        adContainer.removeAllViews();
    //
//            View nativeAdView = getLayoutInflater().inflate(R.layout.custom_native_max_medium2, null);
//
//            TextView title = nativeAdView.findViewById(R.id.native_ad_title);
//            TextView body = nativeAdView.findViewById(R.id.native_ad_body);
//            ImageView icon = nativeAdView.findViewById(R.id.native_ad_icon);
//            ImageView mediaImage = nativeAdView.findViewById(R.id.native_ad_media_image);
//
//            // Hiển thị dữ liệu quảng cáo
//            title.setText(nativeAd.getTitle());
//            body.setText(nativeAd.getBody());
//
//            // Sử dụng thư viện Glide để load ảnh từ URL
//            Glide.with(this).load(nativeAd.getIcon().getUri()).into(icon);
//            Glide.with(this).load(nativeAd.getMainImage().getUri()).into(mediaImage);
//
//            // Thêm quảng cáo vào container
//            adContainer.addView(nativeAdView);
    }
}
package com.example.andmoduleads;

import com.ads.nomyek.ads.YNAd;
import com.ads.nomyek.config.AirBridgeConfig;
import com.ads.nomyek.config.YNAdConfig;
import com.ads.nomyek.application.AdsMultiDexApplication;
import com.ads.nomyek.applovin.AppLovin;
import com.ads.nomyek.applovin.AppOpenMax;
import com.ads.nomyek.billing.AppPurchase;
import com.ads.nomyek.admob.Admob;
import com.ads.nomyek.admob.AppOpenManager;
import com.example.andmoduleads.activity.MainActivity;
import com.example.andmoduleads.activity.SplashActivity;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends AdsMultiDexApplication {

    protected StorageCommon storageCommon;
    private static MyApplication context;

    public static MyApplication getApplication() {
        return context;
    }

    public StorageCommon getStorageCommon() {
        return storageCommon;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Admob.getInstance().setNumToShowAds(0);

        storageCommon = new StorageCommon();
        initBilling();
        initAds();

    }

    private void initAds() {
        String environment = BuildConfig.env_dev ? YNAdConfig.ENVIRONMENT_DEVELOP : YNAdConfig.ENVIRONMENT_PRODUCTION;
        YNAdConfig = new YNAdConfig(this, YNAdConfig.PROVIDER_ADMOB, com.ads.nomyek.config.YNAdConfig.PROVIDER_MAX, environment);

        // Optional: setup Adjust event
//        YNAdConfig.setAdjustTokenTiktok("123456");
        // Optional: setup Airbridge
        AirBridgeConfig airBridgeConfig = new AirBridgeConfig();
        airBridgeConfig.setEnableAirBridge(true);
        airBridgeConfig.setAppNameAirBridge("calculator");
        airBridgeConfig.setTokenAirBridge("22e9f842075b4fb3a0412debe07f6cdd");
        YNAdConfig.setAirBridgeConfig(airBridgeConfig);
        // Optional: enable ads resume
        YNAdConfig.setIdAdResume(BuildConfig.ads_open_app);

        // Optional: setup list device test - recommended to use
        //listTestDevice.add("2635738a-f29d-43f5-95bf-10ca1ad1abe1");
        YNAdConfig.setListDeviceTest(listTestDevice);
        YNAdConfig.setIntervalInterstitialAd(15);

        YNAd.getInstance().init(this, YNAdConfig, true);

        // Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true);
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);

        if (YNAd.getInstance().getMediationProvider() == YNAdConfig.PROVIDER_ADMOB) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        } else {
            AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        }
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(MainActivity.PRODUCT_ID);
        List<String> listSubsId = new ArrayList<>();

        AppPurchase.getInstance().initBilling(getApplication(), listINAPId, listSubsId);
    }

}

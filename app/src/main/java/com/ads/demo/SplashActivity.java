package com.ads.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import com.ads.demo.databinding.ActivitySplashScreenBinding;
import com.ads.yeknomadmob.admobs.Admob;
import com.ads.yeknomadmob.admobs.AppOpenManager;
import com.ads.yeknomadmob.ads_components.YNMAds;
import com.ads.yeknomadmob.ads_components.YNMInitCallback;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMMultiFloorBannerAds;
import com.ads.yeknomadmob.ads_components.ads_banner.YNMMultiFloorBannerLargeAds;
import com.ads.yeknomadmob.config.AirBridgeConfig;
import com.ads.yeknomadmob.config.SolarConfig;
import com.ads.yeknomadmob.config.YNMAdsConfig;
import com.ads.yeknomadmob.utils.AdsNativePreload;
import com.ads.yeknomadmob.utils.AdsUnitItem;
import com.google.android.gms.ads.FullScreenContentCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashScreenBinding> {
    boolean isFirstTimeToGoToApp = false, isAdShown = false, isStartNextActivityCalled = false;
    Timer timer;

    @Override
    protected int getLayoutActivity() {
        return R.layout.activity_splash_screen;
    }

    @Override
    protected void initViews() {
        initDefine();
        initAction();
    }

    private void initDefine(){
        timer = new Timer();
    }

    private void initAction(){
        initAds();
        showInterstialAds();
        timer.schedule(new AfterLoading(), 25000); // Schedule to run after 8 seconds (8000 milliseconds)

        List<AdsUnitItem> ids = new ArrayList<>();
        ids.add(new AdsUnitItem(BuildConfig.ad_banner, "banner_base"));
        ids.add(new AdsUnitItem(BuildConfig.ad_banner, "banner_high_1"));
        ids.add(new AdsUnitItem(BuildConfig.ad_banner, "banner_high_2"));

        YNMMultiFloorBannerAds.getInstance().init(this, ids);
        YNMMultiFloorBannerLargeAds.getInstance().init(this, ids);

    }
    class AfterLoading extends TimerTask {
        public void run() {
            // Your code here
            if(!isAdShown){
                startNextActivity(0);
            }
            // Terminate the timer thread
            cancel();
        }
    }

    private void startNextActivity(long time) {
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void showInterstialAds() {
        YNMAds.getInstance().setInitCallback(new YNMInitCallback() {
            @Override
            public void initAdsSuccess() {
                AppOpenManager.getInstance().setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        if (!isStartNextActivityCalled) {
                            startNextActivity(200);
                            isStartNextActivityCalled = true;
                        }
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        isAdShown = true;
                    }
                });
                AppOpenManager.getInstance().setSplashActivity(SplashActivity.class, BuildConfig.ad_open_splash, 25000);
                AppOpenManager.getInstance().loadAndShowSplashAds(BuildConfig.ad_open_splash, 1000);
//                AdsNativePreload.PreLoadNative(SplashActivity.this, BuildConfig.ad_native, "test");
            }
        });
    }

    private void initAds(){
        Application app = Application.getGlobalApp();
        String environment = BuildConfig.env_dev ? YNMAdsConfig.ENVIRONMENT_DEVELOP : YNMAdsConfig.ENVIRONMENT_PRODUCTION;
        app.ynmAdsConfig = new YNMAdsConfig(app, YNMAdsConfig.PROVIDER_ADMOB, environment);

        // Optional: setup Airbridge
        AirBridgeConfig airBridgeConfig = new AirBridgeConfig();
        airBridgeConfig.setEnableAirBridge(true);
        airBridgeConfig.setAppNameAirBridge("calculator");
        airBridgeConfig.setTokenAirBridge("22e9f842075b4fb3a0412debe07f6cdd");
        app.ynmAdsConfig.setAirBridgeConfig(airBridgeConfig);

        SolarConfig solarConfig = new SolarConfig();
        solarConfig.setEnable(true);
        solarConfig.setToken("1e25374deef8ab86");
        solarConfig.setEnableDebug(false);
        app.ynmAdsConfig.setSolarConfig(solarConfig);
        // Optional: enable ads resume
        app.ynmAdsConfig.setIdAdResume(BuildConfig.ads_open_app);

        // Optional: setup list device test - recommended to use
        app.listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        app.ynmAdsConfig.setListDeviceTest(app.listTestDevice);
        app.ynmAdsConfig.setIntervalInterstitialAd(15);

        YNMAds.getInstance().init(SplashActivity.this, app, app.ynmAdsConfig);

        // Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);

        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
    }
}
package com.ads.nekyomadmob.event;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ads.nekyomadmob.utils.TypeAds;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.ResponseInfo;
import com.reyun.solar.engine.OnAttributionListener;
import com.reyun.solar.engine.OnInitializationCallback;
import com.reyun.solar.engine.SolarEngineConfig;
import com.reyun.solar.engine.SolarEngineManager;
import com.reyun.solar.engine.infos.SEAdImpEventModel;

import org.json.JSONObject;


public class YNMSolar {
    private static final String TAG = "YNMSolar";
    private Context context;
    private static YNMSolar instance;
    public static boolean enableSolar = false;


    public YNMSolar() {
    }

    public static YNMSolar getInstance(){
        if (instance ==null)
            instance = new YNMSolar();
        return instance;
    }

    public void init(Application context, String appkey) {
        init(context, appkey, false);
    }

    public void init(Application context, String appKey, boolean enableDebugLog) {
        this.context = context;
        SolarEngineManager.getInstance().preInit(context, appKey);
        SolarEngineConfig config = new SolarEngineConfig.Builder()
                .isDebugModel(enableDebugLog)
                .isGDPRArea(true)
                .adPersonalizationEnabled(true)
                .adUserDataEnabled(true)
                .build();
        //SolarEngineConfig config = new SolarEngineConfig.Builder().isDebugModel(enableDebugLog).build();
        config.setOnAttributionListener(new OnAttributionListener() {
            @Override
            public void onAttributionSuccess(JSONObject attribution) {
                Log.d(TAG, "onAttributionSuccess: " + attribution.toString());
                //Performed when the attribution results are successfully obtained
            }

            @Override
            public void onAttributionFail(int errorCode) {
                //Performed when the attribution results are not obtained
            }
        });
        SolarEngineManager.getInstance().initialize(context, appKey,config, new OnInitializationCallback() {
            @Override
            public void onInitializationCompleted(int code) {
                if(code == 0) {
                    //Initialization success
                } else {
                    //Initialization failed, please check the code table below for specific failure reason
                }
            }
        });
    }

    public static void onTrackEvent(String eventName) {

    }

    public static void logAdClicked() {
        if (enableSolar) {

        }
    }

    public static void logPaidAdImpressionValue(Context context, AdValue adValue, String adUnitId, ResponseInfo responseInfo, TypeAds adType) {
        if (enableSolar) {
            // TODO: Send the impression-level ad revenue information to your
            //preferred analytics server directly within this callback.

            // Extract the impression-level ad revenue data.
            double valueMicros = adValue.getValueMicros();
            String currencyCode = adValue.getCurrencyCode();
            int precision = adValue.getPrecisionType();


            AdapterResponseInfo loadedAdapterResponseInfo = responseInfo.getLoadedAdapterResponseInfo();
            String adSourceName = loadedAdapterResponseInfo.getAdSourceName();
            String adSourceId = loadedAdapterResponseInfo.getAdSourceId();
            String adSourceInstanceName = loadedAdapterResponseInfo.getAdSourceInstanceName();
            String adSourceInstanceId = loadedAdapterResponseInfo.getAdSourceInstanceId();

            Bundle extras = responseInfo.getResponseExtras();
            String mediationGroupName = extras.getString("mediation_group_name");
            String mediationABTestName = extras.getString("mediation_ab_test_name");
            String mediationABTestVariant = extras.getString("mediation_ab_test_variant");
            //SE SDK processing logic
            SEAdImpEventModel seAdImpEventModel =  new SEAdImpEventModel();
            //Monetization Platform Name
            seAdImpEventModel.setAdNetworkPlatform(adSourceName);
            //Mediation Platform Name (e.g. admob SDK as "admob")
            seAdImpEventModel.setMediationPlatform("admob");
            //Displayed Ad Type (Taking Rewarded Ad as an example, adType = 1)
            seAdImpEventModel.setAdType(adType.ordinal());
            //Monetization Platform App ID
            seAdImpEventModel.setAdNetworkAppID(adSourceId);
            //Monetization Platform Ad Unit ID
            seAdImpEventModel.setAdNetworkADID(adUnitId);
            //Ad eCPM
            seAdImpEventModel.setEcpm(valueMicros/1000);
            //Monetization Platform Currency Type
            seAdImpEventModel.setCurrencyType(currencyCode);
            //True: rendered success
            seAdImpEventModel.setRenderSuccess(true);
            //You can add custom properties as needed. Here we do not give examples.
            SolarEngineManager.getInstance().trackAdImpression(seAdImpEventModel);
        }
    }

    public static void logCustomEvent(String eventName) {
//        Event event = new Event(eventName);
//        Airbridge.trackEvent(event);
    }
}

package com.ads.nomyek_admob.event;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ads.nomyek_admob.utils.TypeAds;
import com.google.android.gms.ads.AdValue;

import java.util.HashMap;
import java.util.Map;

import co.ab180.airbridge.Airbridge;
import co.ab180.airbridge.AirbridgeConfig;
import co.ab180.airbridge.event.Event;


public class YNMAirBridge {
    private static final String TAG = "YNMAirBridge";
    private Context context;
    private static YNMAirBridge tnmAirBridge;
    public static boolean enableAirBridge = false;

    public YNMAirBridge() {
    }

    public static YNMAirBridge getInstance(){
        if (tnmAirBridge ==null)
            tnmAirBridge = new YNMAirBridge();
        return tnmAirBridge;
    }

    public void init(Application context, String appName, String tokenApp) {
        init(context, appName, tokenApp, false);
    }

    public void init(Application context, String appName, String tokenApp, boolean enableDebugLog) {
        this.context = context;
        AirbridgeConfig config = new AirbridgeConfig.Builder(appName, tokenApp)
                .setLogLevel(enableDebugLog? Log.DEBUG:Log.INFO)
                .build();
        Airbridge.init(context, config);
    }

    public static void onTrackEvent(String eventName) {
        Airbridge.trackEvent(eventName);
    }

    public static void logAdClicked() {
        if (enableAirBridge) {
            Airbridge.trackEvent("airbridge.adClick");
        }
    }

    public static void logPaidAdImpressionValue(Context context, AdValue adValue, String adUnitId, String mediationAdapterClassName, TypeAds adType) {
        if (enableAirBridge) {
            // Extract the impression-level ad revenue data.
            long valueMicros = adValue.getValueMicros();
            String currencyCode = adValue.getCurrencyCode();
            int precision = adValue.getPrecisionType();

            // Get the ad unit ID.
            String adNetworkAdapter = mediationAdapterClassName;

            Event event = new Event("airbridge.adImpression");
            Map<String, Object> admob = new HashMap<>();
            admob.put("value_micros", valueMicros);
            admob.put("currency_code", currencyCode);
            admob.put("precision", precision);

            admob.put("ad_unit_id", adUnitId);
            admob.put("ad_network_adapter", adNetworkAdapter);

            Map<String, Object> adPartners = new HashMap<>();
            adPartners.put("admob", admob);
            Map<String, Object> semanticAttributes = new HashMap<>();
            semanticAttributes.put("adPartners", adPartners);
            semanticAttributes.put("currency", currencyCode);

            event.setAction(adUnitId);
            event.setLabel(adNetworkAdapter);
            event.setValue(valueMicros / 1000000.0);
            event.setSemanticAttributes(semanticAttributes);

            Airbridge.trackEvent(event);
        }
    }

    public static void logCustomEvent(String eventName) {
        Event event = new Event(eventName);
        Airbridge.trackEvent(event);
    }
}

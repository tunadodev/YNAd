package com.ads.nomyek_admob.event;

import com.ads.nomyek_admob.ads_components.YNMAds;
import com.ads.nomyek_admob.event.YNMAirBridge;

import java.util.HashMap;
import java.util.Map;

import co.ab180.airbridge.event.Event;

public class YNMAirBridgeDefaultEvent {
    private static String getViewName(YNMAirBridge.AppData appData) {
        if (appData == null) return "";
        String viewName = appData.getViewName();
        return viewName != null ? viewName : "";
    }

    private static String getAdUnit(YNMAirBridge.AppData appData) {
        if (appData == null) return "";
        String adUnit = appData.getAdUnit();
        return adUnit != null ? adUnit : "";
    }

    private static String getAdIdentifier(YNMAirBridge.AppData appData) {
        if (appData == null) return "";
        return getAdUnit(appData) + "_" + getViewName(appData);
    }
    //screen_view
    public static void pushEventScreenView(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_view", null, getViewName(appData)));
    }

    //screen_ad_format_view
    public static void pushEventScreenAdFormatView(YNMAirBridge.AppData appData, String format) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format != null ? format : "");
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventScreenAdFormatClick(YNMAirBridge.AppData appData, String format) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format != null ? format : "");
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventFormatRequestStart(YNMAirBridge.AppData appData, String format) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format != null ? format : "");
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_start", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventFormatRequestSuccess(YNMAirBridge.AppData appData, String format, double timeRequest) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format != null ? format : "");
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_success", null, getAdIdentifier(appData), timeRequest, custom));
    }

    public static void pushEventFormatRequestFail(YNMAirBridge.AppData appData, String format, double timeRequest) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format != null ? format : "");
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_fail", null, getAdIdentifier(appData), timeRequest, custom));
    }

    public static void pushEventScreenAdFormatViewBanner(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.BANNER);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_banner", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventScreenAdFormatViewNative(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.NATIVE);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_native", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventScreenAdFormatViewInter(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.INTERSTITIAL);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_inter", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventScreenAdFormatClickBanner(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.BANNER);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_banner", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventScreenAdFormatClickNative(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.NATIVE);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_native", null, getAdIdentifier(appData), 0, custom));
    }

    public static void pushEventScreenAdFormatClickInter(YNMAirBridge.AppData appData) {
        if (appData == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.INTERSTITIAL);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_inter", null, getAdIdentifier(appData), 0, custom));
    }
}

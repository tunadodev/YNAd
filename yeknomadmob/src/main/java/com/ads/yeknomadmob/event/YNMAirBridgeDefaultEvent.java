package com.ads.yeknomadmob.event;

import com.ads.yeknomadmob.ads_components.YNMAds;
import co.ab180.airbridge.event.Event;
import java.util.HashMap;
import java.util.Map;

import co.ab180.airbridge.event.Event;

public class YNMAirBridgeDefaultEvent {
    //screen_view
    public static void pushEventScreenView(YNMAirBridge.AppData appData) {
        if (appData == null || appData.getViewName() == null) return;
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_view", null, appData.getViewName()));
    }

    //screen_ad_format_view
    public static void pushEventScreenAdFormatView(YNMAirBridge.AppData appData, String format) {
        if (appData == null || appData.getViewName() == null || appData.getAdUnit() == null || format == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        custom.put("tag_screen", appData.getViewName());
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatClick(YNMAirBridge.AppData appData, String format) {
        if (appData == null || appData.getViewName() == null || appData.getAdUnit() == null || format == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        custom.put("tag_screen", appData.getViewName());
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventFormatRequestStart(YNMAirBridge.AppData appData, String format) {
        if (appData == null || appData.getViewName() == null || appData.getAdUnit() == null || format == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        custom.put("tag_screen", appData.getViewName());
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_start", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventFormatRequestSuccess(YNMAirBridge.AppData appData, String format, double timeRequest) {
        if (appData == null || appData.getViewName() == null || appData.getAdUnit() == null || format == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        custom.put("tag_screen", appData.getViewName());
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_success", null, appData.getAdUnit() + "_" + appData.getViewName(), timeRequest, custom));
    }

    public static void pushEventFormatRequestFail(YNMAirBridge.AppData appData, String format, double timeRequest) {
        if (appData == null || appData.getViewName() == null || appData.getAdUnit() == null || format == null) return;
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        custom.put("tag_screen", appData.getViewName());
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_fail", null, appData.getAdUnit() + "_" + appData.getViewName(), timeRequest, custom));
    }
}
package com.ads.yeknomadmob.event;

import com.ads.yeknomadmob.ads_components.YNMAds;

import java.util.HashMap;
import java.util.Map;

import co.ab180.airbridge.event.Event;

public class YNMAirBridgeDefaultEvent {
    //screen_view
    public static void pushEventScreenView(YNMAirBridge.AppData appData) {
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_view", null, appData.getViewName()));
    }

    //screen_ad_format_view
    public static void pushEventScreenAdFormatView(YNMAirBridge.AppData appData, String format) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatClick(YNMAirBridge.AppData appData, String format) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventFormatRequestStart(YNMAirBridge.AppData appData, String format) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_start", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventFormatRequestSuccess(YNMAirBridge.AppData appData, String format, double timeRequest) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_success", null, appData.getAdUnit() + "_" + appData.getViewName(), timeRequest, custom));
    }

    public static void pushEventFormatRequestFail(YNMAirBridge.AppData appData, String format, double timeRequest) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_fail", null, appData.getAdUnit() + "_" + appData.getViewName(), timeRequest, custom));
    }

    public static void pushEventScreenAdFormatViewBanner(YNMAirBridge.AppData appData) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.BANNER);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_banner", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatViewNative(YNMAirBridge.AppData appData) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.NATIVE);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_native", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatViewInter(YNMAirBridge.AppData appData) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.INTERSTITIAL);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_inter", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatClickBanner(YNMAirBridge.AppData appData) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.BANNER);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_banner", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatClickNative(YNMAirBridge.AppData appData) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.NATIVE);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_native", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }

    public static void pushEventScreenAdFormatClickInter(YNMAirBridge.AppData appData) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.INTERSTITIAL);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_inter", null, appData.getAdUnit() + "_" + appData.getViewName(), 0, custom));
    }
}

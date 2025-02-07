package com.ads.yeknomadmob.event;

import com.ads.yeknomadmob.ads_components.YNMAds;

import java.util.HashMap;
import java.util.Map;

import co.ab180.airbridge.event.Event;

public class YNMAirBridgeDefaultEvent {
    //screen_view
    public static void pushEventScreenView(String viewName) {
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_view", null, viewName));
    }

    //screen_ad_format_view
    public static void pushEventScreenAdFormatView(String adUnit, String viewName, String format) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventScreenAdFormatClick(String adUnit, String viewName, String format) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventFormatRequestStart(String adUnit, String viewName, String format) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_start", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventFormatRequestSuccess(String adUnit, String viewName, String format, double timeRequest) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_success", null, adUnit + "_" + viewName, timeRequest, custom));
    }

    public static void pushEventFormatRequestFail(String adUnit, String viewName, String format, double timeRequest) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", format);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_request_fail", null, adUnit + "_" + viewName, timeRequest, custom));
    }

    public static void pushEventScreenAdFormatViewBanner(String adUnit, String viewName) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.BANNER);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_banner", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventScreenAdFormatViewNative(String adUnit, String viewName) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.NATIVE);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_native", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventScreenAdFormatViewInter(String adUnit, String viewName) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.INTERSTITIAL);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_view_inter", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventScreenAdFormatClickBanner(String adUnit, String viewName) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.BANNER);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_banner", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventScreenAdFormatClickNative(String adUnit, String viewName) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.NATIVE);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_native", null, adUnit + "_" + viewName, 0, custom));
    }

    public static void pushEventScreenAdFormatClickInter(String adUnit, String viewName) {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tag_format", YNMAds.INTERSTITIAL);
        YNMAirBridge.getInstance().logCustomEvent(new Event("screen_ad_format_click_inter", null, adUnit + "_" + viewName, 0, custom));
    }
}

package com.ads.yeknomadmob.ads_components.wrappers;

import android.view.View;

import com.google.android.gms.ads.nativead.NativeAd;

public class AdsNative extends AdsBase {
    private int layoutCustomNative;
    private View nativeView;
    private NativeAd admobNativeAd;

    public AdsNative(AdsStatus status) {
        super(status);
    }

    public AdsNative(int layoutCustomNative, View nativeView) {
        this.layoutCustomNative = layoutCustomNative;
        this.nativeView = nativeView;
        status = AdsStatus.AD_LOADED;
    }

    public AdsNative(int layoutCustomNative, NativeAd admobNativeAd) {
        this.layoutCustomNative = layoutCustomNative;
        this.admobNativeAd = admobNativeAd;
        status = AdsStatus.AD_LOADED;
    }

    public NativeAd getAdmobNativeAd() {
        return admobNativeAd;
    }

    public void setAdmobNativeAd(NativeAd admobNativeAd) {
        this.admobNativeAd = admobNativeAd;
        if (admobNativeAd != null)
            status = AdsStatus.AD_LOADED;
    }

    public AdsNative() {
    }


    @Override
    boolean isReady() {
        return nativeView != null || admobNativeAd != null;
    }


    public int getLayoutCustomNative() {
        return layoutCustomNative;
    }

    public void setLayoutCustomNative(int layoutCustomNative) {
        this.layoutCustomNative = layoutCustomNative;
    }

    public View getNativeView() {
        return nativeView;
    }

    public void setNativeView(View nativeView) {
        this.nativeView = nativeView;
    }

    public String toString(){
        return "Status:"+ status + " == nativeView:"+nativeView + " == admobNativeAd:"+admobNativeAd;
    }

}

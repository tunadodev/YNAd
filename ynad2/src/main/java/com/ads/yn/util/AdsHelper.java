package com.ads.yn.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.ads.yn.ads.nativeAds.YNNativeAdView;
import com.ads.yn.ads.wrapper.ApNativeAd;
import com.google.android.gms.ads.nativead.NativeAd;

//check if a view is below or above a ratio with the full height
public class AdsHelper {
    public interface ResizeCallback {
        void onBelowThreshold(int viewHeight);
        void onAboveThreshold(int viewHeight);
    }
    public static void resizeViewIfNeeded(Context context, double thresholdRatio,double comparedHeight,  ResizeCallback callback) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;

        // Calculate the desired height based on the custom threshold ratio
        int desiredHeight = (int) (thresholdRatio * screenHeight);

        // Notify the callback based on the comparison
        if (comparedHeight > desiredHeight) {
            if (callback != null) {
                callback.onAboveThreshold(desiredHeight);
            }
        } else {
            if (callback != null) {
                callback.onBelowThreshold(screenHeight);
            }
        }
    }

    //if the ads size take more than half screen, down size it
    public static void initAutoResizeAds(Context context, YNNativeAdView nativeAdView, NativeAd nativeAd, int medium, int large, boolean preLoaded){
        //AdsNative apNativeAd = new AdsNative(R.layout.custom_native_admob_large, nativeAd);
        AdsHelper.resizeViewIfNeeded(context,0.5, context.getResources().getDimension(com.intuit.sdp.R.dimen._270sdp), new ResizeCallback() {
            @Override
            public void onBelowThreshold(int viewHeight) {

                ApNativeAd apNativeAd = new ApNativeAd(large, nativeAd);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Your code here
                        nativeAdView.populateNativeAdView((Activity) context, apNativeAd);
                    }
                }, preLoaded ? 1000 : 0);
            }

            @Override
            public void onAboveThreshold(int viewHeight) {
                // Update the view's height
                ViewGroup.LayoutParams layoutParams = nativeAdView.getLayoutParams();
                layoutParams.height = (int) context.getResources().getDimension(com.intuit.sdp.R.dimen._160sdp);
                nativeAdView.setLayoutParams(layoutParams);

                ApNativeAd apNativeAd = new ApNativeAd(medium, nativeAd);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Your code here
                        nativeAdView.populateNativeAdView((Activity) context, apNativeAd);
                    }
                }, preLoaded ? 1000 : 0);
            }
        });
        //set delay so it will have load effect even though the ads is preloaded
    }

    //if the ads size take more than half screen, down size it
    public static void initFixedSizeAds(Context context, YNNativeAdView nativeAdView,NativeAd nativeAd, int layout, boolean preLoaded){
        ApNativeAd apNativeAd = new ApNativeAd(layout, nativeAd);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your code here
                nativeAdView.populateNativeAdView((Activity) context, apNativeAd);
            }
        }, preLoaded ? 1000 : 0);
    }
}

package com.ads.yeknomadmob.ads_components.wrappers;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;

public class AdsError {
    private LoadAdError loadAdError;
    private AdError adError;
    private String message = "";

    public AdsError(AdError adError) {
        this.adError = adError;
    }

    public AdsError(LoadAdError loadAdError) {
        this.loadAdError = loadAdError;
    }

    public AdsError(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage(){
        if (loadAdError!=null)
            return loadAdError.getMessage();
        if (adError!=null)
            return adError.getMessage();
        if (!message.isEmpty())
            return message;
        return "unknown error";
    }
}

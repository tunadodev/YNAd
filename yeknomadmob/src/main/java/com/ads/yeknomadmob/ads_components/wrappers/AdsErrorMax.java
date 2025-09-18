package com.ads.yeknomadmob.ads_components.wrappers;

import com.applovin.mediation.MaxError;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;

public class AdsErrorMax {
    private MaxError loadAdError;
    private AdError adError;
    private String message = "";

    public AdsErrorMax(AdError adError) {
        this.adError = adError;
    }

    public AdsErrorMax(MaxError loadAdError) {
        this.loadAdError = loadAdError;
    }

    public AdsErrorMax(String message) {
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

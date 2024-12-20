package com.ads.nekyomadmob.ads_components.wrappers;

import com.google.android.gms.ads.AdValue;

public class AdsValue {
    private AdValue admobAdValue;
    public AdsValue(AdValue admobAdValue) {
        this.admobAdValue = admobAdValue;
    }
    public AdValue getAdmobAdValue() {
        return admobAdValue;
    }
}

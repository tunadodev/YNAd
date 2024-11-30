package com.ads.nomyek.application;

import androidx.multidex.MultiDexApplication;

import com.ads.nomyek.config.YNAdConfig;
import com.ads.nomyek.util.AppUtil;
import com.ads.nomyek.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {

    public YNAdConfig YNAdConfig;
    public List<String> listTestDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        listTestDevice = new ArrayList<String>();
        YNAdConfig = new YNAdConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }


}

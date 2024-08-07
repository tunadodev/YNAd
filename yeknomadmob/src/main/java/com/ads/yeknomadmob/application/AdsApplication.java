package com.ads.yeknomadmob.application;

import android.app.Application;
import com.ads.yeknomadmob.config.YNMAdsConfig;
import com.ads.yeknomadmob.utils.AppUtil;
import com.ads.yeknomadmob.utils.SharePreferenceUtils;
import java.util.ArrayList;
import java.util.List;

public abstract class AdsApplication extends Application {

    public YNMAdsConfig ynmAdsConfig;
    public List<String> listTestDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        listTestDevice = new ArrayList<String>();
        ynmAdsConfig = new YNMAdsConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }


}

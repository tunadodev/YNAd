package com.ads.demo;

import com.ads.yeknomadmob.application.AdsApplication;

public class Application extends AdsApplication{
    private static Application globalApp;
    @Override
    public void onCreate() {
        super.onCreate();
        setGlobalApp(this);
    }

    public static Application getGlobalApp() {
        return globalApp;
    }

    public void setGlobalApp(Application globalApp) {
        Application.globalApp = globalApp;
    }
}


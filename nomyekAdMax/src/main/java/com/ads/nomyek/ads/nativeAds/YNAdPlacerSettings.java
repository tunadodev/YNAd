package com.ads.nomyek.ads.nativeAds;

public class YNAdPlacerSettings {

    private String adUnitId;
    private int positionFixAd = -1;
    private boolean isRepeatingAd = false;
    private int layoutCustomAd = -1;
    private int layoutAdPlaceHolder = -1;
    private YNAdPlacer.Listener listener;

    public YNAdPlacerSettings(String adUnitId, int layoutCustomAd, int layoutPlaceHolderAd) {
        this.adUnitId = adUnitId;
        this.layoutCustomAd = layoutCustomAd;
        this.layoutAdPlaceHolder = layoutPlaceHolderAd;
    }

    public YNAdPlacerSettings(int layoutCustomAd, int layoutPlaceHolderAd) {
        this.adUnitId = adUnitId;
        this.layoutCustomAd = layoutCustomAd;
        this.layoutAdPlaceHolder = layoutPlaceHolderAd;
    }

    public void setFixedPosition(int positionAd) {
        positionFixAd = positionAd;
        isRepeatingAd = false;
    }

    public YNAdPlacer.Listener getListener() {
        return listener;
    }

    public void setListener(YNAdPlacer.Listener listener) {
        this.listener = listener;
    }


    public int getLayoutCustomAd() {
        return layoutCustomAd;
    }

    public int getLayoutAdPlaceHolder() {
        return layoutAdPlaceHolder;
    }

    public void setLayoutAdPlaceHolder(int layoutAdPlaceHolder) {
        this.layoutAdPlaceHolder = layoutAdPlaceHolder;
    }

    public void setLayoutCustomAd(int layoutCustomAd) {
        this.layoutCustomAd = layoutCustomAd;
    }

    public void setRepeatingInterval(int positionAd) {
        positionFixAd = positionAd - 1;
        isRepeatingAd = true;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public int getPositionFixAd() {
        return positionFixAd;
    }


    public boolean isRepeatingAd() {
        return isRepeatingAd;
    }


}

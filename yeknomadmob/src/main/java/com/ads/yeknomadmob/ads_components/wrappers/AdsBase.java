package com.ads.yeknomadmob.ads_components.wrappers;

public abstract class AdsBase {
    protected AdsStatus status = AdsStatus.AD_INIT;

    public AdsBase(AdsStatus status) {
        this.status = status;
    }

    public AdsBase() {
    }

    public AdsStatus getStatus() {
        return status;
    }

    public void setStatus(AdsStatus status) {
        this.status = status;
    }


    abstract boolean isReady();

    public boolean isNotReady(){
        return !isReady();
    }

    public boolean isLoading(){
        return status == AdsStatus.AD_LOADING;
    }
    public boolean isLoadFail(){
        return status == AdsStatus.AD_LOAD_FAIL;
    }
}

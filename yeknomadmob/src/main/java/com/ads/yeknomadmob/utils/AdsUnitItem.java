package com.ads.yeknomadmob.utils;

/**
 * A shared class for ad unit configurations.
 * Can be used for different ad formats like Interstitial, Reward, etc.
 */
public class AdsUnitItem {
    private String adUnitId;
    private String key;

    /**
     * Create a new ad unit item
     * @param adUnitId The ad unit ID from ad network
     * @param key The key used to store and retrieve the ad in cache
     */
    public AdsUnitItem(String adUnitId, String key) {
        this.adUnitId = adUnitId;
        this.key = key;
    }

    /**
     * Get the ad unit ID
     * @return Ad unit ID string
     */
    public String getAdUnitId() {
        return adUnitId;
    }

    /**
     * Get the key for this ad unit
     * @return Key string used for cache
     */
    public String getKey() {
        return key;
    }
} 
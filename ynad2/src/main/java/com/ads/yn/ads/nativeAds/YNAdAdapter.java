package com.ads.yn.ads.nativeAds;

import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;

public class YNAdAdapter {
    private AdmobRecyclerAdapter admobRecyclerAdapter;
    private MaxRecyclerAdapter maxRecyclerAdapter;

    public YNAdAdapter(AdmobRecyclerAdapter admobRecyclerAdapter) {
        this.admobRecyclerAdapter = admobRecyclerAdapter;
    }

    public YNAdAdapter(MaxRecyclerAdapter maxRecyclerAdapter) {
        this.maxRecyclerAdapter = maxRecyclerAdapter;
    }

    public RecyclerView.Adapter getAdapter() {
        if (admobRecyclerAdapter != null) return admobRecyclerAdapter;
        return maxRecyclerAdapter;
    }

    public void notifyItemRemoved(int pos) {
        if (maxRecyclerAdapter != null) {
            maxRecyclerAdapter.notifyItemRemoved(pos);
        }
    }

    public int getOriginalPosition(int pos) {
        if (maxRecyclerAdapter != null) {
           return maxRecyclerAdapter.getOriginalPosition(pos);
        }
        return 0;
    }

    public void loadAds() {
        if (maxRecyclerAdapter != null)
            maxRecyclerAdapter.loadAds();
    }

    public void destroy() {
        if (maxRecyclerAdapter != null)
            maxRecyclerAdapter.destroy();
    }
}

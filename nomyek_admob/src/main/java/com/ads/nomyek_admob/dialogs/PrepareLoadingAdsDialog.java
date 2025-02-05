package com.ads.nomyek_admob.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.ads.nomyek_admob.R;

public class PrepareLoadingAdsDialog extends Dialog {
    public PrepareLoadingAdsDialog(Context context) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading_ads);

        // Set the dialog to full screen
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

}
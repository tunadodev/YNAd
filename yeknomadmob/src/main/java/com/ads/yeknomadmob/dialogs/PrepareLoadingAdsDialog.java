package com.ads.yeknomadmob.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.ads.yeknomadmob.R;

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

    @Override
    public void dismiss() {
        try {
            Context context = getContext();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    // Activity still exists, safe to dismiss
                    super.dismiss();
                }
                // Otherwise, the activity is no longer valid, no need to dismiss
            } else {
                // Context is not an activity, still try to dismiss normally
                super.dismiss();
            }
        } catch (Exception e) {
            // Log exception for debugging
            Log.e("PrepareLoadingAdsDialog", "Error dismissing dialog: " + e.getMessage());
            // Attempt to dismiss anyway as a last resort
            try {
                super.dismiss();
            } catch (Exception ignored) {
                // Nothing more we can do
            }
        }
    }
}
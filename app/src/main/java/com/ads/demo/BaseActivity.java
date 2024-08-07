package com.ads.demo;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseActivity<VB extends ViewDataBinding> extends AppCompatActivity {
    protected VB viewBinding;
    protected Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initWindow(window);
        }
        fullScreenCall(window);
        super.onCreate(savedInstanceState);
        // Initialize the view binding
        viewBinding = DataBindingUtil.setContentView(this, getLayoutActivity());
        setContentView(viewBinding.getRoot());
        initViews();

        // Add padding to the top to avoid status bar hiding content
        View contentView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, new androidx.core.view.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int paddingTop = Math.max(topInset, 50); // Ensure padding top is at least 50 or the status bar height
                v.setPadding(0, paddingTop, 0, 0);
                return insets;
            }
        });
    }

    protected abstract int getLayoutActivity();

    protected abstract void initViews();

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void initWindow(Window window) {
        Drawable background = new ColorDrawable(Color.parseColor("#FFFFFF"));
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(window.getContext().getResources().getColor(android.R.color.black));
        window.setBackgroundDrawable(background);
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public static void fullScreenCall(Window window) {
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

}
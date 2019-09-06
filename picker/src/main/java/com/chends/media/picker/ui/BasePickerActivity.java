package com.chends.media.picker.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.chends.media.picker.R;
import com.chends.media.picker.utils.PickerUtil;
import com.chends.media.picker.utils.statusbar.FlyMeOSStatusBarFontUtils;
import com.chends.media.picker.utils.statusbar.StatusBarUtils;

/**
 * base Activity
 * @author chends create on 2019/9/6.
 */
public abstract class BasePickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
    }

    /**
     * 初始化状态栏
     */
    private void initStatus() {
        StatusBarUtils.Builder builder = new StatusBarUtils.Builder(this);
        boolean black = FlyMeOSStatusBarFontUtils.isBlackColor(ContextCompat.getColor(this, colorPrimaryDark()), 50);
        builder.setDarkFont(!black);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 添加布局
            View mStatusBarTintView = new View(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    PickerUtil.getStatusHeight(this));
            params.gravity = Gravity.TOP;
            mStatusBarTintView.setLayoutParams(params);
            mStatusBarTintView.setVisibility(View.VISIBLE);
            ((ViewGroup) ((ViewGroup) getWindow().getDecorView()).getChildAt(0)).addView(mStatusBarTintView);
            builder.setStatusBarColor(ContextCompat.getColor(this, colorPrimaryDark()))
                    .setStatusBarView(mStatusBarTintView);
        }
        builder.isTranslucentStatus(isTranslucentStatus()).builder().init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 21以上使用setStatusBarColor
            if (!isTranslucentStatus()) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(Integer.MIN_VALUE);

            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        }
    }

    /**
     * 是否透明状态栏
     * @return <ul>是否透明状态栏<li>true：布局中不需要android:fitsSystemWindows="true",显示效果为全屏透明，键盘模式无效</li>
     * <Li>false：布局中需要android:fitsSystemWindows="true"</Li></ul>
     */
    public boolean isTranslucentStatus() {
        return true;
    }

    @ColorRes
    protected int colorPrimaryDark() {
        return R.color.color_media_picker_title_bg;
    }

    protected void xmlClick(View view) {

    }
}

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
import android.widget.FrameLayout;

import com.chends.media.picker.R;
import com.chends.media.picker.utils.PickerUtil;
import com.chends.media.picker.utils.statusbar.FlyMeStatusBarUtils;
import com.chends.media.picker.utils.statusbar.StatusBarUtils;

/**
 * base Activity
 * @author chends create on 2019/9/6.
 */
public abstract class BasePickerActivity extends AppCompatActivity {
    //private View topBar; // 使用fitsSystemWindows的view
    private boolean needFitsSystem = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        trySetFitsSystem();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        trySetFitsSystem();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        trySetFitsSystem();
    }

    /**
     * 设置fitsSystemWindows
     */
    private void trySetFitsSystem() {
        if (needFitsSystem) {
            // 使用fitsSystemWindows的view
            View topBar = findViewById(R.id.topBar);
            if (topBar != null) {
                ((View) topBar.getParent()).setFitsSystemWindows(true);
            }
        }
    }

    /**
     * 初始化状态栏<br/>
     * <ol><li>如果颜色为深色：只使用fitsSystemWindows</li>
     * <li>如果颜色为浅色：
     * <ol><li>如果状态栏颜色可变黑那么使用fitsSystemWindows</li>
     * <li>如果不能变黑，则使用自定义布局</li></ol>
     * </li></ol>
     */
    private void initStatus() {
        StatusBarUtils.Builder builder = new StatusBarUtils.Builder(this);
        boolean black = FlyMeStatusBarUtils.isBlackColor(ContextCompat.getColor(this, colorPrimaryDark()), 50);
        if (black) {
            needFitsSystem = true;
        } else {
            if (StatusBarUtils.isSupportStatusBarDarkFont()) {
                needFitsSystem = true;
            } else {
                needFitsSystem = false;
                // 添加自定义布局
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
            }
        }
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
        builder.builder().init();
    }

    /**
     * 是否透明状态栏
     * @return <ul>是否透明状态栏<li>true：布局中不需要android:fitsSystemWindows="true",显示效果为全屏透明，键盘模式无效</li>
     * <Li>false：布局中需要android:fitsSystemWindows="true"</Li></ul>
     */
   /* public boolean isTranslucentStatus() {
        return true;
    }*/
    @ColorRes
    protected int colorPrimaryDark() {
        return R.color.color_media_picker_title_bg;
    }

}

package com.chends.media.picker.utils.statusbar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * 状态栏工具
 * @author chends create on 2019/9/6.
 */
public class StatusBarUtils {

    private Activity mActivity;
    private Window mWindow;

    /**
     * 判断手机支不支持状态栏字体变色
     * Is support status bar dark font boolean.
     * @return the boolean
     */
    public static boolean isSupportStatusBarDarkFont() {
        return OSUtils.isMiUi6Later() || OSUtils.isFlyMeOS4Later() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    private Builder builder;

    public StatusBarUtils(Builder builder) {
        this.builder = builder;
        WeakReference<Activity> activityWeakReference = new WeakReference<>(builder.activity);
        mActivity = activityWeakReference.get();
        mWindow = mActivity.getWindow();
    }

    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiFlags = mWindow.getDecorView().getSystemUiVisibility();
            uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;  //防止系统栏隐藏时内容区域大小发生变化
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !OSUtils.isEmUi3_1()) {
                uiFlags = initBarAboveLOLLIPOP(uiFlags); //初始化5.0以上，包含5.0
                uiFlags = setStatusBarDarkFont(uiFlags); //android 6.0以上设置状态栏字体为暗色
            } else {
                initBarBelowLOLLIPOP(); //初始化5.0以下，4.4以上沉浸式
            }
            setupStatusBarView();
            mWindow.addFlags(Integer.MIN_VALUE);
            mWindow.getDecorView().setSystemUiVisibility(uiFlags);
        }
        if (OSUtils.isMiUi6Later()) {
            setMiUiStatusBarDark(mWindow, builder.darkFont ? 2 : 3); // 修改miui状态栏字体颜色
        }
        if (OSUtils.isFlyMeOS4Later()) { // 修改FlyMe OS状态栏字体颜色
            if (builder.flyMeOSStatusBarFontColor != 0) {
                FlyMeStatusBarUtils.setStatusBarDarkIcon(mActivity, builder.flyMeOSStatusBarFontColor);
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    FlyMeStatusBarUtils.setStatusBarDarkIcon(mActivity, builder.darkFont);
                }
            }
        }
    }

    /**
     * 初始化android 5.0以上状态栏和导航栏
     * @param uiFlags the ui flags
     * @return the int
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int initBarAboveLOLLIPOP(int uiFlags) {
        uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;  //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态栏遮住。
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  //需要设置这个才能设置状态栏颜色
        mWindow.setStatusBarColor(Color.TRANSPARENT);
        return uiFlags;
    }

    /**
     * 初始化android 4.4和emUi3.1状态栏和导航栏
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initBarBelowLOLLIPOP() {
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
    }

    /**
     * 设置状态栏布局的颜色
     */
    private void setupStatusBarView() {
        if (builder.statusBarView != null) {
            builder.statusBarView.setBackgroundColor(ColorUtils.blendARGB(builder.statusBarColor,
                    Color.BLACK, builder.statusBarAlpha));
        }
    }

    /**
     * 设置状态栏字体颜色，android6.0以上
     * @param uiFlags flag
     */
    private int setStatusBarDarkFont(int uiFlags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (builder.darkFont) {
                return uiFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                return uiFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else {
            return uiFlags;
        }
    }

    /**
     * 设置MiUi状态栏
     * @param window window
     * @param type   0--只需要状态栏透明 1-状态栏透明且黑色字体 2-黑色字体 3-清除黑色字体
     */
    private static void setMiUiStatusBarDark(Window window, int type) {
        try {
            int tranceFlag;
            int darkModeFlag;
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            tranceFlag = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT").getInt(layoutParams);
            darkModeFlag = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE").getInt(layoutParams);
            Method extraFlagField = window.getClass().getMethod("setExtraFlags", int.class, int.class);
            if (type == 0) { // 只需要状态栏透明
                extraFlagField.invoke(window, tranceFlag, tranceFlag);
            } else if (type == 1) {// 状态栏透明且黑色字体
                extraFlagField.invoke(window, tranceFlag | darkModeFlag, tranceFlag | darkModeFlag);
            } else if (type == 2) { // 黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else { // 清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
            //LogUtil.d("setMiUiStatusBarDark success");
        } catch (Exception e) {
            //LogUtil.e("Exception while setMiUiStatusBarDark:" + e.getMessage());
        }
    }

    public static class Builder {
        private Activity activity;
        /**
         * 状态栏透明度
         */
        @FloatRange(from = 0f, to = 1f)
        private float statusBarAlpha = 0.0f;
        /**
         * 状态栏颜色
         */
        @ColorInt
        private int statusBarColor = Color.TRANSPARENT;
        /**
         * 状态栏字体深色与亮色标志位
         */
        private boolean darkFont = false;
        /**
         * flyMeOS状态栏字体变色
         */
        @ColorInt
        private int flyMeOSStatusBarFontColor = Color.BLACK;

        private View statusBarView;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setStatusBarAlpha(float statusBarAlpha) {
            return statusBarDarkFont(darkFont, statusBarAlpha);
        }

        public Builder setStatusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
            return this;
        }

        public Builder setDarkFont(boolean darkFont) {
            return statusBarDarkFont(darkFont, 0f);
        }

        public Builder statusBarDarkFont(boolean isDarkFont, @FloatRange(from = 0f, to = 1f) float statusAlpha) {
            this.darkFont = isDarkFont;
            if (!isDarkFont) {
                this.flyMeOSStatusBarFontColor = 0;
            }
            if (isSupportStatusBarDarkFont()) {
                this.statusBarAlpha = 0;
            } else {
                this.statusBarAlpha = statusAlpha;
            }
            return this;
        }

        public Builder setStatusBarView(View statusBarView) {
            this.statusBarView = statusBarView;
            return this;
        }


        public StatusBarUtils builder() {
            return new StatusBarUtils(this);
        }
    }
}

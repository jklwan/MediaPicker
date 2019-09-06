package com.chends.media.picker.utils.statusbar;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * FlyMe OS 修改状态栏字体颜色工具类
 * @author chends create on 2019/9/6.
 */
public class FlyMeOSStatusBarFontUtils {
    private static Method mSetStatusBarColorIcon;
    private static Method mSetStatusBarDarkIcon;
    private static Field mStatusBarColorFiled;
    private static int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 0;

    static {
        try {
            mSetStatusBarColorIcon = Activity.class.getMethod("setStatusBarDarkIcon", int.class);
        } catch (NoSuchMethodException e) {
            //LogUtil.e("error:" + e.getMessage());
        }
        try {
            mSetStatusBarDarkIcon = Activity.class.getMethod("setStatusBarDarkIcon", boolean.class);
        } catch (NoSuchMethodException e) {
            //LogUtil.e("error:" + e.getMessage());
        }
        try {
            mStatusBarColorFiled = WindowManager.LayoutParams.class.getField("statusBarColor");
        } catch (NoSuchFieldException e) {
            //LogUtil.e("error:" + e.getMessage());
        }
        try {
            Field field = View.class.getField("SYSTEM_UI_FLAG_LIGHT_STATUS_BAR");
            SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = field.getInt(null);
        } catch (Exception e) {
            //LogUtil.e("error:" + e.getMessage());
        }
    }

    /**
     * 判断颜色是否偏黑色
     * @param color 颜色
     * @param level 级别
     * @return boolean
     */
    public static boolean isBlackColor(int color, int level) {
        int grey = toGrey(color);
        return grey < level;
    }

    /**
     * 颜色转换成灰度值
     * @param rgb 颜色
     * @return the int 灰度值
     */
    public static int toGrey(int rgb) {
        int blue = rgb & 0x000000FF;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    /**
     * 设置状态栏字体图标颜色
     * @param activity 当前activity
     * @param color    颜色
     */
    public static void setStatusBarDarkIcon(Activity activity, int color) {
        if (mSetStatusBarColorIcon != null) {
            try {
                mSetStatusBarColorIcon.invoke(activity, color);
            } catch (Exception e) {
                //LogUtil.e("error:" + e.getMessage());
            }
        } else {
            boolean whiteColor = isBlackColor(color, 50);
            if (mStatusBarColorFiled != null) {
                setStatusBarDarkIcon(activity, whiteColor, whiteColor);
                setStatusBarDarkIcon(activity.getWindow(), color);
            } else {
                setStatusBarDarkIcon(activity, whiteColor);
            }
        }
    }

    /**
     * 设置状态栏字体图标颜色(只限全屏非activity情况)
     * @param window 当前窗口
     * @param color  颜色
     */
    public static void setStatusBarDarkIcon(Window window, int color) {
        try {
            setStatusBarColor(window, color);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                setStatusBarDarkIcon(window.getDecorView(), true);
            }
        } catch (Exception e) {
            //LogUtil.e("error:" + e.getMessage());
        }
    }

    /**
     * 设置状态栏字体图标颜色
     * @param activity 当前activity
     * @param dark     是否深色 true为深色 false 为白色
     */
    public static void setStatusBarDarkIcon(Activity activity, boolean dark) {
        setStatusBarDarkIcon(activity, dark, true);
    }

    private static boolean changeMeiZuFlag(WindowManager.LayoutParams winParams, String flagName, boolean on) {
        try {
            Field f = winParams.getClass().getDeclaredField(flagName);
            f.setAccessible(true);
            int bits = f.getInt(winParams);
            Field f2 = winParams.getClass().getDeclaredField("meizuFlags");
            f2.setAccessible(true);
            int meiZuFlags = f2.getInt(winParams);
            int oldFlags = meiZuFlags;
            if (on) {
                meiZuFlags |= bits;
            } else {
                meiZuFlags &= ~bits;
            }
            if (oldFlags != meiZuFlags) {
                f2.setInt(winParams, meiZuFlags);
                return true;
            }
        } catch (Exception e) {
            //LogUtil.e("error:" + e.getMessage());
        }
        return false;
    }

    /**
     * 设置状态栏颜色
     * @param view view
     * @param dark dark
     */
    private static void setStatusBarDarkIcon(View view, boolean dark) {
        int oldVis = view.getSystemUiVisibility();
        int newVis = oldVis;
        if (dark) {
            newVis |= SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            newVis &= ~SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (newVis != oldVis) {
            view.setSystemUiVisibility(newVis);
        }
    }

    /**
     * 设置状态栏颜色
     * @param window window
     * @param color color
     */
    private static void setStatusBarColor(Window window, int color) {
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (mStatusBarColorFiled != null) {
            try {
                int oldColor = mStatusBarColorFiled.getInt(winParams);
                if (oldColor != color) {
                    mStatusBarColorFiled.set(winParams, color);
                    window.setAttributes(winParams);
                }
            } catch (IllegalAccessException e) {
                //LogUtil.e("error:" + e.getMessage());
            }
        }
    }

    /**
     * 设置状态栏字体图标颜色(只限全屏非activity情况)
     * @param window 当前窗口
     * @param dark   是否深色 true为深色 false 为白色
     */
    public static void setStatusBarDarkIcon(Window window, boolean dark) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            changeMeiZuFlag(window.getAttributes(), "MEIZU_FLAG_DARK_STATUS_BAR_ICON", dark);
        } else {
            View decorView = window.getDecorView();
            if (decorView != null) {
                setStatusBarDarkIcon(decorView, dark);
                setStatusBarColor(window, 0);
            }
        }
    }

    private static void setStatusBarDarkIcon(Activity activity, boolean dark, boolean flag) {
        if (mSetStatusBarDarkIcon != null) {
            try {
                mSetStatusBarDarkIcon.invoke(activity, dark);
            } catch (Exception e) {
                //LogUtil.e("error:" + e.getMessage());
            }
        } else {
            if (flag) {
                setStatusBarDarkIcon(activity.getWindow(), dark);
            }
        }
    }
}

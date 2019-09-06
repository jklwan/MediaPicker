package com.chends.media.picker.utils.statusbar;

import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * 手机系统判断
 * @author chends create on 2019/9/6.
 */
public class OSUtils {
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_EMUI_VERSION_NAME = "ro.build.version.EmUi";
    private static final String KEY_DISPLAY = "ro.build.display.id";
    private static final boolean init, isMiUi, isMiUi6Later, isEmUi, isEmUi3_1, isEmUi3_0, isFlyMeOS,
            isFlyMeOS4Later, isFlyMeOS5;
    static {
        isMiUi = isMiUi();
        isMiUi6Later = isMiUi6Later();
        isEmUi = isEmUi();
        isEmUi3_0 = isEmUi3_0();
        isEmUi3_1 = isEmUi3_1();
        isFlyMeOS = isFlyMeOS();
        isFlyMeOS4Later = isFlyMeOS4Later();
        isFlyMeOS5 = isFlyMeOS5();
        init = true;
    }

    /**
     * 判断是否为MiUi
     * Is MiUi boolean.
     * @return the boolean
     */
    public static boolean isMiUi() {
        if(init) return isMiUi;
        String property = getSystemProperty(KEY_MIUI_VERSION_NAME, "");
        return !TextUtils.isEmpty(property);
    }

    /**
     * 判断MiUi版本是否大于等于6
     * Is MiUi 6 later boolean.
     * @return the boolean
     */
    public static boolean isMiUi6Later() {
        if(init) return isMiUi6Later;
        String version = getMIUIVersion();
        int num;
        if ((!version.isEmpty())) {
            try {
                num = Integer.valueOf(version.substring(1));
                return num >= 6;
            } catch (NumberFormatException e) {
                return false;
            }
        } else
            return false;
    }

    /**
     * 获得MiUi的版本
     * Gets MiUi version.
     * @return the MiUi version
     */
    private static String getMIUIVersion() {
        return isMiUi() ? getSystemProperty(KEY_MIUI_VERSION_NAME, "") : "";
    }

    /**
     * 判断是否为EmUi
     * Is EmUi boolean.
     * @return the boolean
     */
    public static boolean isEmUi() {
        if(init) return isEmUi;
        String property = getSystemProperty(KEY_EMUI_VERSION_NAME, "");
        return !TextUtils.isEmpty(property);
    }

    /**
     * 得到EmUi的版本
     * Gets EmUi version.
     * @return the EmUi version
     */
    private static String getEmUiVersion() {
        return isEmUi() ? getSystemProperty(KEY_EMUI_VERSION_NAME, "") : "";
    }

    /**
     * 判断是否为EmUi3.1版本（EmUi3.1使用android4.4的方法）
     * Is EmUi 3 1 boolean.
     * @return the boolean
     */
    public static boolean isEmUi3_1() {
        if(init) return isEmUi3_1;
        String property = getEmUiVersion();
        return TextUtils.equals("EmotionUI 3", property) || property.contains("EmotionUI_3.1");
    }

    /**
     * 判断是否为EmUi3.0版本
     * Is EmUi 3 1 boolean.
     * @return the boolean
     */
    public static boolean isEmUi3_0() {
        if(init) return isEmUi3_0;
        String property = getEmUiVersion();
        return property.contains("EmotionUI_3.0");
    }

    /**
     * 判断是否为FlyMeOS
     * Is FlyMe os boolean.
     * @return the boolean
     */
    public static boolean isFlyMeOS() {
        if(init) return isFlyMeOS;
        return getFlyMeOSFlag().toLowerCase().contains("flyme");
    }

    /**
     * 判断FlyMeOS的版本是否大于等于4
     * Is FlyMe os 4 later boolean.
     * @return the boolean
     */
    public static boolean isFlyMeOS4Later() {
        if(init) return isFlyMeOS4Later;
        String version = getFlyMeOSVersion();
        int num;
        if (!version.isEmpty()) {
            try {
                if (version.toLowerCase().contains("os")) {
                    num = Integer.valueOf(version.substring(9, 10));
                } else {
                    num = Integer.valueOf(version.substring(6, 7));
                }
                return num >= 4;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 判断FlyMeOS的版本是否等于5
     * Is FlyMe os 5 boolean.
     * @return the boolean
     */
    public static boolean isFlyMeOS5() {
        if(init) return isFlyMeOS5;
        String version = getFlyMeOSVersion();
        int num;
        if (!version.isEmpty()) {
            try {
                if (version.toLowerCase().contains("os")) {
                    num = Integer.valueOf(version.substring(9, 10));
                } else {
                    num = Integer.valueOf(version.substring(6, 7));
                }
                return num == 5;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }


    /**
     * 得到FlyMeOS的版本
     * Gets FlyMe os version.
     * @return the FlyMe os version
     */
    private static String getFlyMeOSVersion() {
        return isFlyMeOS() ? getSystemProperty(KEY_DISPLAY, "") : "";
    }

    private static String getFlyMeOSFlag() {
        return getSystemProperty(KEY_DISPLAY, "");
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}

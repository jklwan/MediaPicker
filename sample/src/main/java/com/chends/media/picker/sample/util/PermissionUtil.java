package com.chends.media.picker.sample.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

/**
 * @author chends create on 2019/9/6.
 */
public class PermissionUtil {

    /**
     * 权限检查
     * @param context context
     * @param permission 权限
     * @return true or false
     */
    public static boolean checkPermission(Context context, String permission) {
        int targetSdkVersion = getTargetVersion(context);
        boolean result = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (targetSdkVersion >= Build.VERSION_CODES.M) {
                    result = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                } else {
                    result = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static int version = 0;

    private static int getTargetVersion(Context context) {
        if (version == 0) {
            try {
                final PackageInfo info = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                version = info.applicationInfo.targetSdkVersion;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return version;
    }

    /**
     * 验证请求结果
     * @param grantResults result
     * @return true为通过
     */
    public static boolean verifyPermission(int... grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}

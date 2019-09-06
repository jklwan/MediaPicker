package com.chends.media.picker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.chends.media.picker.utils.PermissionUtil;

/**
 * 权限判断
 * @author chends create on 2019/9/6.
 */
public class PermissionActivity extends AppCompatActivity {
    private static final String BUNDLE_PERMISSION = "permission";
    private String permission;
    private final int RequestCode = 1;

    /**
     * 权限检查，在{@link #onActivityResult(int, int, Intent)}中获取结果
     * @param activity    activity
     * @param permission  权限
     * @param requestCode requestCode
     */
    public static void checkPermission(Activity activity, String permission, int requestCode) {
        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.putExtra(BUNDLE_PERMISSION, permission);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permission = getIntent().getStringExtra(BUNDLE_PERMISSION);
        if (TextUtils.isEmpty(permission)) {
            setResult(false);
            return;
        }
        if (!PermissionUtil.checkPermission(this, permission)) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, RequestCode);
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // 询问
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, RequestCode);
            }*/
        } else {
            setResult(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                setResult(true);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // 不再询问，无法成功获取权限
                    setResult(false);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, RequestCode);
                }
            }
        }
    }

    /**
     * 返回成功还是失败
     * @param success success
     */
    private void setResult(boolean success) {
        Intent intent = new Intent();
        setResult(success ? RESULT_OK : RESULT_CANCELED, intent);
        finish();
    }
}

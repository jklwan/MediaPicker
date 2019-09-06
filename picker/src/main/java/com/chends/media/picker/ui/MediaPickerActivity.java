package com.chends.media.picker.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.chends.media.picker.R;
import com.chends.media.picker.utils.ControlUtil;
import com.chends.media.picker.utils.ToastUtils;

/**
 * 选择页面
 * @author chends create on 2019/9/5.
 */
public class MediaPickerActivity extends BasePickerActivity {

    private ControlUtil util;
    private final int permissionCode = 1, previewCode = permissionCode + 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);
        PermissionActivity.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, permissionCode);
    }

    /**
     * 初始化
     */
    private void init() {

    }

    @Override
    protected void xmlClick(View view) {
        switch (view.getId()){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case permissionCode:
                if (resultCode == RESULT_OK){
                    // 成功
                    init();
                } else {
                    ToastUtils.showLong(this, R.string.string_media_picker_no_storage_permission);
                    finish();
                    return;
                }
                break;
            case previewCode:
                break;
        }
    }
}

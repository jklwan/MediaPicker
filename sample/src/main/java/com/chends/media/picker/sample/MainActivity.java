package com.chends.media.picker.sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chends.media.picker.MediaPicker;
import com.chends.media.picker.MimeType;
import com.chends.media.picker.utils.ToastUtils;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final int requestCode = 1, chooseCode = 2;
    private View clickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoaderManager.enableDebugLogging(BuildConfig.DEBUG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                setResult(true);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // 不再询问，无法成功获取权限
                    setResult(false);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, this.requestCode);
                }
            }
        }
    }

    /**
     * 返回成功还是失败
     * @param success success
     */
    private void setResult(boolean success) {
        if (!success){
            ToastUtils.showLong(this,"无法获取存储卡权限");
            return;
        }

        switch (clickView.getId()) {
            case R.id.chooseImage:
                MediaPicker.with(this)
                        .addTypes(MimeType.allImage())
                        .maxNum(8)
                        .start(chooseCode);
                break;
            case R.id.chooseVideo:
                MediaPicker.with(this)
                        .addTypes(MimeType.allVideo())
                        .maxNum(3)
                        .start(chooseCode);
                break;
            case R.id.chooseIVideo:
                MediaPicker.with(this)
                        .addTypes(MimeType.allImage())
                        .addTypes(MimeType.allVideo())
                        .maxNum(5)
                        .start(chooseCode);
                break;
            case R.id.chooseIAudio:
                MediaPicker.with(this)
                        .addTypes(MimeType.allImage())
                        .addTypes(allAudio())
                        .maxNum(8)
                        .start(chooseCode);
                break;
            case R.id.chooseAVideo:
                MediaPicker.with(this)
                        .addTypes(MimeType.allVideo())
                        .addTypes(allAudio())
                        .maxNum(3)
                        .start(chooseCode);
                break;
            case R.id.chooseAll:
                MediaPicker.with(this)
                        .addTypes(MimeType.all())
                        .addTypes(allAudio())
                        .maxNum(9)
                        .start(chooseCode);
                break;
        }
    }

    public void xmlClick(View view) {
        clickView = view;
        if (!PermissionUtil.checkPermission(this, permission)) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            setResult(true);
        }
    }

    private Set<String> allAudio(){
        return new HashSet<String>(){{
            add("audio/mpeg");
            add("audio/ogg");
            add("audio/aac");
        }};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == chooseCode) {

        }
    }
}

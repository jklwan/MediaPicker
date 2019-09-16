package com.chends.media.picker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chends.media.picker.R;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.utils.ControlUtil;
import com.chends.media.picker.utils.PickerUtil;

import java.util.ArrayList;

/**
 * 选择页面
 * @author chends create on 2019/9/5.
 */
public class MediaPickerActivity extends BasePickerActivity {

    private ControlUtil util;
    public static final int PREVIEW_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);
        if (!PickerBean.getInstance().reset) {
            finish();
            return;
        }
        util = new ControlUtil(this);
        util.onRestoreInstanceState(savedInstanceState);
        util.startLoader();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.gc();
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PREVIEW_CODE) {
            sendResult(this);
        }
    }

    /**
     * send result
     * @param activity activity
     */
    public static void sendResult(Activity activity) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Constant.EXTRA_CHOOSE_DATA, new ArrayList<>(PickerBean.getInstance().chooseList));
        activity.setResult(RESULT_OK, intent);
        activity.finish();
    }

    /**
     * 打开预览页
     * @param activity activity
     */
    public static boolean startPreview(Activity activity) {
        if (PickerBean.getInstance().showPreview) {
            Class cls = PickerUtil.getPreview();
            if (cls != null) {
                Intent intent = new Intent(activity, cls);
                intent.putParcelableArrayListExtra(Constant.EXTRA_CHOOSE_DATA, new ArrayList<>(PickerBean.getInstance().chooseItem));
                try {
                    activity.startActivityForResult(intent, PREVIEW_CODE);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 打开预览页
     * @param activity activity
     * @param position position
     * @param folderId 文件夹id
     */
    public static boolean startPreview(Activity activity, int position, String folderId) {
        if (PickerBean.getInstance().showPreview) {
            Class cls = PickerUtil.getPreview();
            if (cls != null) {
                Intent intent = new Intent(activity, cls);
                intent.putExtra(Constant.EXTRA_POSITION, position);
                intent.putExtra(Constant.EXTRA_FOLDER_ID, folderId);
                try {
                    activity.startActivityForResult(intent, PREVIEW_CODE);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (util != null) {
            util.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (util != null) {
            util.onRestoreInstanceState(savedInstanceState);
        }
    }
}

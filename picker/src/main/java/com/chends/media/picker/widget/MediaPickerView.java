package com.chends.media.picker.widget;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.chends.media.picker.R;
import com.chends.media.picker.model.FolderBean;
import com.chends.media.picker.model.PickerBean;

import java.util.List;

/**
 * MediaPickerView
 * @author chends create on 2019/9/7.
 */
public class MediaPickerView {
    private Activity activity;
    private final TextView title, finish, folderName, preview;
    private final View back, folderArrow, bottom, folder;

    public MediaPickerView(Activity activity) {
        this.activity = activity;
        back = activity.findViewById(R.id.topBar_back);
        title = activity.findViewById(R.id.topBar_title);
        finish = activity.findViewById(R.id.topBar_finish);
        bottom = activity.findViewById(R.id.picker_bottom);
        folder = activity.findViewById(R.id.picker_folder);
        folderName = activity.findViewById(R.id.picker_folder_name);
        folderArrow = activity.findViewById(R.id.picker_folder_arrow);
        preview = activity.findViewById(R.id.picker_preview);
        finish.setEnabled(false);
        bottom.setVisibility(View.GONE);
        preview.setEnabled(false);
    }

    public void setClickListener(View.OnClickListener listener) {
        back.setOnClickListener(listener);
        finish.setOnClickListener(listener);
        folder.setOnClickListener(listener);
        preview.setOnClickListener(listener);
    }

    public void onFolderLoad(List<FolderBean> list) {
        bottom.setVisibility(View.VISIBLE);
        finish.setEnabled(!PickerBean.getInstance().chooseList.isEmpty());
    }

    public void onFolderLoaderReset() {

    }
}

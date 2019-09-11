package com.chends.media.picker.utils;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chends.media.picker.R;
import com.chends.media.picker.adapter.ItemAdapter;
import com.chends.media.picker.listener.FolderLoaderCallback;
import com.chends.media.picker.listener.FolderPopupListener;
import com.chends.media.picker.listener.FolderSelectedListener;
import com.chends.media.picker.listener.ItemClickListener;
import com.chends.media.picker.listener.ItemLoaderCallback;
import com.chends.media.picker.model.FolderBean;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.ui.MediaPickerActivity;
import com.chends.media.picker.widget.FolderPopupWindow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理
 * @author chends create on 2019/9/6.
 */
public class ControlUtil implements LifecycleObserver {
    private WeakReference<AppCompatActivity> reference;
    private FolderLoaderUtil folderUtil;
    private ItemLoaderUtil itemUtil;
    private TextView title, finish, folderName, preview;
    private View topBar, back, folderArrow, bottom, folder;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private FolderPopupWindow popupWindow;
    private LoaderCallBack callBack;

    public ControlUtil(AppCompatActivity activity) {
        this.reference = new WeakReference<>(activity);
        activity.getLifecycle().addObserver(this);
        callBack = new LoaderCallBack();
        folderUtil = new FolderLoaderUtil(activity, callBack);
        itemUtil = new ItemLoaderUtil(activity, callBack);
        initView(activity, callBack);
    }

    private void initView(Activity activity, FolderSelectedListener listener) {
        topBar = activity.findViewById(R.id.topBar);
        back = activity.findViewById(R.id.topBar_back);
        title = activity.findViewById(R.id.topBar_title);
        finish = activity.findViewById(R.id.topBar_finish);
        recyclerView = activity.findViewById(R.id.recyclerView);
        bottom = activity.findViewById(R.id.picker_bottom);
        folder = activity.findViewById(R.id.picker_folder);
        folderName = activity.findViewById(R.id.picker_folder_name);
        folderArrow = activity.findViewById(R.id.picker_folder_arrow);
        preview = activity.findViewById(R.id.picker_preview);
        finish.setEnabled(false);
        bottom.setVisibility(View.GONE);
        preview.setVisibility(PickerBean.getInstance().showPreview ? View.VISIBLE : View.GONE);
        preview.setEnabled(false);
        int height = Resources.getSystem().getDisplayMetrics().heightPixels - PickerUtil.getStatusHeight(activity) -
                activity.getResources().getDimensionPixelSize(R.dimen.dimen_media_picker_top_bar) -
                activity.getResources().getDimensionPixelSize(R.dimen.dimen_media_picker_bottom_bar);
        popupWindow = FolderPopupWindow.create(activity, ViewGroup.LayoutParams.MATCH_PARENT, height);
        popupWindow.setSelectListener(listener);
        popupWindow.setPopupListener(callBack);
        setClickListener(new PickerClick());
        recyclerView.setLayoutManager(new GridLayoutManager(activity, PickerBean.getInstance().spanCount));
        recyclerView.addItemDecoration(new CommonItemDecoration
                .Builder(PickerBean.getInstance().spanCount)
                .setDecoration(PickerUtil.dp2px(1.5f))
                .setIncludeLR(true)
                .setIncludeTB(true)
                .build());
    }

    private void setClickListener(View.OnClickListener listener) {
        back.setOnClickListener(listener);
        finish.setOnClickListener(listener);
        folder.setOnClickListener(listener);
        preview.setOnClickListener(listener);
    }

    private class PickerClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.topBar_back) {
                reference.get().onBackPressed();
            } else if (v.getId() == R.id.topBar_finish) {
                if (!PickerBean.getInstance().chooseList.isEmpty()) {
                    MediaPickerActivity.sendResult(reference.get());
                }
            } else if (v.getId() == R.id.picker_folder) {
                popupWindow.show(topBar);
                onShowDismissPopup(true);
            } else if (v.getId() == R.id.picker_preview) {
                if (!PickerBean.getInstance().chooseList.isEmpty()) {
                    if (!MediaPickerActivity.startPreview(reference.get())) {
                        ToastUtils.showLong(reference.get(), R.string.string_media_picker_no_preview);
                    }
                }
            }
        }
    }

    /**
     * 开始获取数据前提是必须有存储卡读写权限
     */
    public void startLoader() {
        if (PickerBean.getInstance().chooseList.isEmpty()) {
            startLoadFolder();
        } else {
            // 初始化
            if (checkItemUtil()) {
                itemUtil.search();
            } else {
                startLoadFolder();
            }
        }
    }

    /**
     * 开始加载文件夹列表
     */
    private void startLoadFolder() {
        if (checkFolderUtil()) {
            folderUtil.startLoader();
        }
    }

    private class LoaderCallBack implements FolderSelectedListener, FolderLoaderCallback, FolderPopupListener,
            ItemLoaderCallback, ItemClickListener {
        @Override
        public void onDismiss() {
            onShowDismissPopup(false);
        }

        @Override
        public void onSelected(FolderBean bean) {
            if (checkActivity()) {
                title.setText(bean.getDisplayName());
                folderName.setText(bean.getDisplayName());
                if (checkItemUtil()) {
                    itemUtil.startLoader(bean.getId());
                }
                if (checkFolderUtil()) {
                    folderUtil.setStateCurrentSelection(popupWindow.getSelection());
                }
                //popupWindow.dismiss();
            }
        }

        @Override
        public void onFolderLoaderFinish(@NonNull List<FolderBean> list) {
            if (checkActivity()) {
                if (list.isEmpty()) {
                    ToastUtils.showLong(reference.get(), R.string.string_media_picker_no_media);
                } else {
                    bottom.setVisibility(View.VISIBLE);
                    popupWindow.setData(list);
                    popupWindow.setSelect(folderUtil.getCurrentSelection());
                    title.setText(list.get(folderUtil.getCurrentSelection()).getDisplayName());
                    updateFinish();
                    folderName.setText(title.getText());
                }
            }
        }

        @Override
        public void onFolderLoaderReset() {
            if (checkActivity()) {
                popupWindow.setData(null);
            }
        }

        @Override
        public void onItemLoaderFinish(Cursor cursor, boolean isSearch) {
            if (isSearch) {
                Map<String, ItemBean> maps = new HashMap<>();
                while (cursor.moveToNext()) {
                    ItemBean bean = ItemBean.valueOf(cursor);
                    maps.put(bean.getPath(), bean);
                }
                List<String> choose = PickerBean.getInstance().chooseList;
                List<String> delList = new ArrayList<>();
                for (String path : choose) {
                    if (maps.containsKey(path)) {
                        PickerBean.getInstance().chooseItem.add(maps.get(path));
                    } else {
                        delList.add(path);
                    }
                }
                if (!delList.isEmpty()) {
                    PickerBean.getInstance().chooseList.removeAll(delList);
                }
                startLoadFolder();
            } else {
                if (itemAdapter == null) {
                    itemAdapter = new ItemAdapter(cursor, MediaStore.MediaColumns._ID);
                    itemAdapter.setClickListener(callBack);
                    recyclerView.setAdapter(itemAdapter);
                } else {
                    recyclerView.scrollToPosition(0);
                    itemAdapter.swapCursor(cursor);
                }
                // after refresh finish
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                    }
                });
            }
        }

        @Override
        public void onItemLoaderReset(boolean isSearch) {
            if (!isSearch) {
                if (itemAdapter != null) {
                    itemAdapter.swapCursor(null);
                }
            }
        }

        @Override
        public void onItemClick(ItemBean bean, int position) {
            if (!PickerUtil.isFileExist(bean.getPath())) {
                ToastUtils.showShort(reference.get(), reference.get().getString(R.string.string_media_picker_fileNoExist));
                return;
            }
            if (PickerUtil.checkFile(reference.get(), bean)) {
                if (!MediaPickerActivity.startPreview(reference.get(), position, bean.getId())) {
                    onItemSelectClick(bean, position);
                }
            } else {
                ToastUtils.showShort(reference.get(), reference.get().getString(R.string.string_media_picker_fileError));
            }
        }

        @Override
        public void onItemSelectClick(ItemBean bean, int position) {
            if (!PickerUtil.isFileExist(bean.getPath())) {
                ToastUtils.showShort(reference.get(), reference.get().getString(R.string.string_media_picker_fileNoExist));
                return;
            }
            if (PickerUtil.checkFile(reference.get(), bean)) {
                // start select
                if (PickerUtil.selectPath(reference.get(), bean)) {
                    itemAdapter.setChoose(position, bean.getPath());
                    updateFinish();
                }
            } else {
                ToastUtils.showShort(reference.get(), reference.get().getString(R.string.string_media_picker_fileError));
            }
        }
    }

    /**
     * 更新finish和preview文字
     */
    private void updateFinish() {
        if (checkActivity()) {
            if (PickerBean.getInstance().chooseList.isEmpty()) {
                finish.setText(R.string.string_media_picker_finish);
                preview.setText(R.string.string_media_picker_preview);
            } else {
                finish.setText(reference.get().getString(R.string.string_media_picker_finish_format,
                        PickerBean.getInstance().chooseList.size(), PickerBean.getInstance().maxNum));
                preview.setText(reference.get().getString(R.string.string_media_picker_preview_format,
                        PickerBean.getInstance().chooseList.size(), PickerBean.getInstance().maxNum));
            }
            finish.setEnabled(!PickerBean.getInstance().chooseList.isEmpty());
            preview.setEnabled(!PickerBean.getInstance().chooseList.isEmpty());
        }
    }

    /**
     * onSaveInstanceState
     * @param outState outState
     */
    public void onSaveInstanceState(Bundle outState) {
        if (checkFolderUtil()) {
            folderUtil.onSaveInstanceState(outState);
        }
    }

    /**
     * onRestoreInstanceState
     * @param outState outState
     */
    public void onRestoreInstanceState(Bundle outState) {
        if (checkFolderUtil()) {
            folderUtil.onRestoreInstanceState(outState);
        }
    }

    private void onShowDismissPopup(boolean show) {
        folderArrow.animate().cancel();
        if (show) {
            folderArrow.animate().rotation(180).setDuration(200).start();
        } else {
            folderArrow.animate().rotation(0).setDuration(200).start();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        popupWindow.dismiss();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (checkFolderUtil()) {
            folderUtil.onDestroy();
        }
        if (checkItemUtil()) {
            itemUtil.onDestroy();
        }
    }

    private boolean checkActivity() {
        Activity activity;
        if (reference != null && (activity = reference.get()) != null && !activity.isFinishing()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return !activity.isDestroyed();
            }
            return true;
        }
        return false;
    }

    private boolean checkFolderUtil() {
        return folderUtil != null;
    }

    private boolean checkItemUtil() {
        return itemUtil != null;
    }
}

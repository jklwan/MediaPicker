package com.chends.media.picker.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chends.media.picker.R;
import com.chends.media.picker.adapter.PreviewCursorPagerAdapter;
import com.chends.media.picker.adapter.PreviewPagerAdapter;
import com.chends.media.picker.listener.ItemLoaderCallback;
import com.chends.media.picker.listener.PickerCallback;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.utils.ItemLoaderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 预览选择页
 * @author chends create on 2019/9/6.
 */
public class PreviewActivity extends BasePickerActivity {
    private TextView title, finish;
    private int selectPosition;
    private ViewPager viewPager;
    private PagerAdapter mAdapter;
    private ItemLoaderUtil util;
    private boolean useCursor = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker_preview);
        String folderId = getIntent().getStringExtra(Constant.EXTRA_FOLDER_ID);
        List<ItemBean> list = new ArrayList<>();
        if (TextUtils.isEmpty(folderId)) {
            list = getIntent().getParcelableArrayListExtra(Constant.EXTRA_CHOOSE_DATA);
            if (list == null || list.isEmpty()) {
                finish();
                return;
            }
            useCursor = false;
        } else {
            useCursor = true;
        }

        if (savedInstanceState != null) {
            selectPosition = savedInstanceState.getInt(Constant.STATE_CURRENT_SELECTION, 0);
        } else {
            selectPosition = getIntent().getIntExtra(Constant.EXTRA_POSITION, 0);
        }
        View back = findViewById(R.id.topBar_back);
        title = findViewById(R.id.topBar_title);
        finish = findViewById(R.id.topBar_finish);
        PreviewClick click = new PreviewClick();
        back.setOnClickListener(click);
        finish.setOnClickListener(click);
        updateFinish();
        viewPager = findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectPosition = position;
                updateTitle();
            }
        });
        if (useCursor){
            mAdapter = new PreviewCursorPagerAdapter(getSupportFragmentManager(), MediaStore.MediaColumns._ID);
            ((PreviewCursorPagerAdapter)mAdapter).setCallback(new PickerCallback() {
                @Override
                public void onChooseChange(boolean choose, String path) {
                    updateFinish();
                }
            });
        } else {
            mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), list);
            ((PreviewPagerAdapter)mAdapter).setCallback(new PickerCallback() {
                @Override
                public void onChooseChange(boolean choose, String path) {
                    updateFinish();
                }
            });
        }

        viewPager.setAdapter(mAdapter);
        if (useCursor) {
            util = new ItemLoaderUtil(this, new ItemLoaderCallback() {
                @Override
                public void onItemLoaderFinish(Cursor cursor) {
                    ((PreviewCursorPagerAdapter)mAdapter).swapCursor(cursor);
                    if (selectPosition < 0 || selectPosition >= mAdapter.getCount()) {
                        selectPosition = 0;
                    }
                    viewPager.setCurrentItem(selectPosition, false);
                    updateTitle();
                }

                @Override
                public void onItemLoaderReset() {
                    ((PreviewCursorPagerAdapter)mAdapter).swapCursor(null);
                }
            });
            util.startLoader(folderId);
        }
    }

    private class PreviewClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.topBar_back) {
                onBackPressed();
            } else if (v.getId() == R.id.topBar_finish) {
                if (!PickerBean.getInstance().chooseList.isEmpty()) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        }
    }

    /**
     * 更新标题
     */
    private void updateTitle() {
        title.setText(String.format(Locale.getDefault(), "%1$d/%2$d", selectPosition, mAdapter.getCount()));
    }

    private void updateFinish() {
        if (PickerBean.getInstance().chooseList.isEmpty()) {
            finish.setText(getString(R.string.string_media_picker_finish));
        } else {
            finish.setText(getString(R.string.string_media_picker_finish_format,
                    PickerBean.getInstance().maxNum, PickerBean.getInstance().chooseList.size()));
        }
        finish.setEnabled(!PickerBean.getInstance().chooseList.isEmpty());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putInt(Constant.STATE_CURRENT_SELECTION, selectPosition);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            selectPosition = savedInstanceState.getInt(Constant.STATE_CURRENT_SELECTION, 0);
            if (selectPosition < 0 || selectPosition >= mAdapter.getCount()) {
                selectPosition = 0;
            }
            viewPager.setCurrentItem(selectPosition, false);
            updateTitle();
        }
    }

    @Override
    protected void onDestroy() {
        if (useCursor && util != null) {
            util.onDestroy();
        }
        super.onDestroy();
    }
}
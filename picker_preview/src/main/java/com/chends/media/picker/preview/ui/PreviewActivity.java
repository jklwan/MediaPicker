package com.chends.media.picker.preview.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chends.media.picker.listener.ItemLoaderCallback;
import com.chends.media.picker.listener.SimpleAnimationListener;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.preview.R;
import com.chends.media.picker.preview.adapter.PreviewCursorPagerAdapter;
import com.chends.media.picker.preview.adapter.PreviewPagerAdapter;
import com.chends.media.picker.ui.BasePickerActivity;
import com.chends.media.picker.utils.ItemLoaderUtil;
import com.chends.media.picker.utils.PickerUtil;
import com.chends.media.picker.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * 预览选择页
 * @author chends create on 2019/9/6.
 */
public class PreviewActivity extends BasePickerActivity {
    private View topLayout, bottom;
    private TextView title, finish;
    private ImageView select;
    private int selectPosition;
    private ViewPager viewPager;
    private PagerAdapter mAdapter;
    private ItemLoaderUtil util;
    private boolean useCursor = false;
    private boolean isFull = false;
    private Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PickerBean.getInstance().reset) {
            finish();
            return;
        }
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
        initTopView();

        //SubsamplingScaleImageView.setPreferredBitmapConfig(Bitmap.Config.RGB_565);
        if (savedInstanceState != null) {
            selectPosition = savedInstanceState.getInt(Constant.STATE_CURRENT_SELECTION, 0);
        } else {
            selectPosition = getIntent().getIntExtra(Constant.EXTRA_POSITION, 0);
        }
        View back = findViewById(R.id.topBar_back);
        title = findViewById(R.id.topBar_title);
        finish = findViewById(R.id.topBar_finish);
        select = findViewById(R.id.select);
        View selectText = findViewById(R.id.select_text);
        viewPager = findViewById(R.id.viewPager);
        PreviewClick click = new PreviewClick();
        back.setOnClickListener(click);
        finish.setOnClickListener(click);
        select.setOnClickListener(click);
        selectText.setOnClickListener(click);
        if (useCursor) {
            mAdapter = new PreviewCursorPagerAdapter(getSupportFragmentManager(), MediaStore.MediaColumns._ID)
                    .setListener(new ChangeFull());
        } else {
            mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), list)
                    .setListener(new ChangeFull());
        }
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectPosition = position;
                updateTitle();
            }
        });
        viewPager.setAdapter(mAdapter);
        if (useCursor) {
            util = new ItemLoaderUtil(this, new ItemLoaderCallback() {
                @Override
                public void onItemLoaderFinish(Cursor cursor, boolean isSearch) {
                    if (!isSearch) {
                        ((PreviewCursorPagerAdapter) mAdapter).swapCursor(cursor);
                        if (selectPosition < 0 || selectPosition >= mAdapter.getCount()) {
                            selectPosition = 0;
                        }
                        viewPager.setCurrentItem(selectPosition, false);
                        updateTitle();
                        updateFinish();
                    }
                }

                @Override
                public void onItemLoaderReset(boolean isSearch) {
                    if (!isSearch) {
                        ((PreviewCursorPagerAdapter) mAdapter).swapCursor(null);
                    }
                }
            });
            util.startLoader(folderId);
        } else {
            updateFinish();
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
            } else if (v.getId() == R.id.select || v.getId() == R.id.select_text) {
                ItemBean item;
                if (useCursor) {
                    item = ((PreviewCursorPagerAdapter) mAdapter).getMediaItem(selectPosition);
                } else {
                    item = ((PreviewPagerAdapter) mAdapter).getMediaItem(selectPosition);
                }
                if (item != null) {
                    if (!PickerUtil.isFileExist(PreviewActivity.this, item.getPath())) {
                        ToastUtils.showShort(PreviewActivity.this,
                                getString(R.string.string_media_picker_fileNoExist));
                        return;
                    }
                    if (PickerUtil.checkFile(PreviewActivity.this, item)) {
                        if (PickerUtil.selectPath(PreviewActivity.this, item)) {
                            updateFinish();
                        }
                    } else {
                        ToastUtils.showShort(PreviewActivity.this,
                                getString(R.string.string_media_picker_fileError));
                    }
                }
            }
        }
    }

    /**
     * 更新标题
     */
    private void updateTitle() {
        title.setText(String.format(Locale.getDefault(), "%1$d/%2$d", selectPosition + 1, mAdapter.getCount()));
        updateChoose();
    }

    /**
     * 更新选中状态
     */
    private void updateChoose() {
        ItemBean item;
        if (useCursor) {
            item = ((PreviewCursorPagerAdapter) mAdapter).getMediaItem(selectPosition);
        } else {
            item = ((PreviewPagerAdapter) mAdapter).getMediaItem(selectPosition);
        }
        if (item == null) return;
        boolean choose = PickerBean.getInstance().chooseList.contains(item.getPath());
        if (choose) {
            select.setImageResource(R.drawable.ic_media_picker_checked);
        } else {
            select.setImageResource(R.drawable.ic_media_picker_uncheck);
        }
    }

    /**
     * 更新完成
     */
    private void updateFinish() {
        if (PickerBean.getInstance().chooseList.isEmpty()) {
            finish.setText(getString(R.string.string_media_picker_finish));
        } else {
            finish.setText(getString(R.string.string_media_picker_finish_format,
                    PickerBean.getInstance().chooseList.size(), PickerBean.getInstance().maxNum));
        }
        finish.setEnabled(!PickerBean.getInstance().chooseList.isEmpty());
        updateChoose();
    }

    /**
     * 初始化
     */
    private void initTopView() {
        topLayout = findViewById(R.id.topLayout);
        bottom = findViewById(R.id.bottom);
        topLayout.setOnClickListener(null);
        bottom.setOnClickListener(null);
    }

    private class ChangeFull implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            changeFull();
        }
    }

    /**
     * 改变全屏状态
     */
    private void changeFull() {
        isFull = !isFull;
        if (isFull) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (topAnim != null && !topAnim.hasEnded()) {
            topAnim.cancel();
        }
        topAnim = AnimationUtils.loadAnimation(this, isFull ? R.anim.top_slide_out :
                R.anim.top_slide_in);
        topAnim.setAnimationListener(topListener);
        if (bottomAnim != null && !bottomAnim.hasEnded()) {
            bottomAnim.cancel();
        }
        bottomAnim = AnimationUtils.loadAnimation(this, isFull ? R.anim.folder_slide_out :
                R.anim.folder_slide_in);
        bottomAnim.setAnimationListener(bottomListener);
        topLayout.startAnimation(topAnim);
        bottom.startAnimation(bottomAnim);
    }

    private SimpleAnimationListener topListener = new SimpleAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            topLayout.setVisibility(isFull ? View.GONE : View.VISIBLE);
        }
    };

    private SimpleAnimationListener bottomListener = new SimpleAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            bottom.setVisibility(isFull ? View.GONE : View.VISIBLE);
        }
    };

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
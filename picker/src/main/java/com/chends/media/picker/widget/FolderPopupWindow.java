package com.chends.media.picker.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.math.MathUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;

import com.chends.media.picker.R;
import com.chends.media.picker.adapter.FolderAdapter;
import com.chends.media.picker.listener.FolderPopupListener;
import com.chends.media.picker.listener.FolderSelectedListener;
import com.chends.media.picker.listener.SimpleAnimationListener;
import com.chends.media.picker.model.FolderBean;

import java.util.List;

/**
 * 文件夹
 * @author cds created on 2019/9/7.
 */
public class FolderPopupWindow extends PopupWindow {

    private View mContentView;
    private Context context;
    private Animation animation;

    private FolderAdapter mAdapter;
    private FolderPopupListener listener;

    public static FolderPopupWindow create(Activity activity, int width, int height) {
        ViewGroup group = (ViewGroup) activity.getWindow().getDecorView();
        View contentView = LayoutInflater.from(activity).inflate(R.layout.layout_media_picker_folder, group, false);
        return new FolderPopupWindow(width, height, contentView);
    }

    private FolderPopupWindow(int width, int height, View contentView) {
        super(contentView, width, height, true);
        this.mContentView = contentView;
        if (height > 0) {
            int padding = height / 6;
            contentView.setPadding(0, padding, 0, 0);
        }
        context = contentView.getContext();
        setAnimationStyle(R.style.anim_popup_folder);
        setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.color_media_picker_folder_bg)));
        setTouchable(true);
        setOutsideTouchable(true);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        initViews();
        update();
    }

    /**
     * init view
     */
    private void initViews() {
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RecyclerView recyclerView = mContentView.findViewById(R.id.recyclerView);
        recyclerView.setOnClickListener(null);
        mAdapter = new FolderAdapter(context);
        recyclerView.setAdapter(mAdapter);
    }

    public void setSelectListener(FolderSelectedListener listener) {
        mAdapter.setListener(listener);
    }

    public void setPopupListener(FolderPopupListener listener) {
        this.listener = listener;
    }

    /**
     * setData
     * @param list list
     */
    public void setData(List<FolderBean> list) {
        mAdapter.setList(list);
    }

    public void setSelect(int position) {
        mAdapter.setSelect(position);
    }

    public int getSelection() {
        return MathUtils.clamp(mAdapter.getSelection(),
                0, mAdapter.getItemCount() - 1);
    }

    /**
     * cancel
     */
    private void cancelAnim() {
        if (animation != null && !animation.hasEnded()) {
            animation.cancel();
        }
    }

    /**
     * 显示
     * @param view view
     */
    public void show(View view) {
        cancelAnim();
        showAsDropDown(view);
        animation = AnimationUtils.loadAnimation(context, R.anim.folder_slide_in);
        mContentView.startAnimation(animation);
    }

    @Override
    public void dismiss() {
        cancelAnim();
        animation = AnimationUtils.loadAnimation(context, R.anim.folder_slide_out);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                FolderPopupWindow.super.dismiss();
            }
        });
        mContentView.startAnimation(animation);
        if (listener != null) {
            listener.onDismiss();
        }
    }

}

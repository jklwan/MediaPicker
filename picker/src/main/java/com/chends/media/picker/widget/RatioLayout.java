package com.chends.media.picker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 等比例FrameLayout
 * @author chends create on 2019/9/6.
 */
public class RatioLayout extends FrameLayout {

    private RatioHelper ratioHelper;
    private boolean fullScreen = false;

    /**
     * 是否全屏
     * @param fullScreen fullScreen
     */
    public void setFullScreen(boolean fullScreen) {
        if (this.fullScreen != fullScreen) {
            this.fullScreen = fullScreen;
            postInvalidate();
        }
    }

    public RatioLayout(@NonNull Context context) {
        this(context, null);
    }

    public RatioLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ratioHelper = new RatioHelper();
        ratioHelper.loadFromAttributes(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (fullScreen) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int[] wh = ratioHelper.measureWH(widthMeasureSpec, heightMeasureSpec);
            super.onMeasure(wh[0], wh[1]);
        }
    }

    /**
     * 设置宽高比
     * @param ratio 宽高比
     */
    public void setRatio(float ratio) {
        ratioHelper.setRatio(ratio);
    }

    /**
     * 设置是否以宽度为基准
     * @param widthStandard 是否以宽度为基准<ul><li>true：表示高度通过宽度和比例计算</li>
     *                      <li>false：表示宽度通过高度和比例计算</li></ul>
     */
    public void setWidthStandard(boolean widthStandard) {
        ratioHelper.setWidthStandard(widthStandard);
    }

}

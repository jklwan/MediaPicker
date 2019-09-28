package com.chends.media.picker.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * 等比例ImageView
 */
public class RatioImageView extends AppCompatImageView {

    private RatioHelper ratioHelper;

    public RatioImageView(Context context) {
        this(context, null);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ratioHelper = new RatioHelper();
        ratioHelper.loadFromAttributes(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] wh = ratioHelper.measureWH(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(wh[0], wh[1]);
    }

    /**
     * 设置宽高比
     * @param ratio 宽高比
     */
    public void setRatio(float ratio) {
        ratioHelper.setRatio(ratio);
    }

    /**
     * 设置宽高比
     * @param width_ratio  width_ratio
     * @param height_ratio height_ratio
     */
    public void setRatio(float width_ratio, float height_ratio) {
        ratioHelper.setRatio(width_ratio, height_ratio);
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
package com.chends.media.picker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.chends.media.picker.R;

/**
 * 等比例布局工具类（默认16:9）
 * @author chends create on 2019/9/6.
 */
class RatioHelper {

    /**
     * 是否以宽度为基准
     */
    private boolean isWidthStandard = true;
    private float width_ratio = 16.0f;
    private float height_ratio = 9.0f;


    RatioHelper() {
    }

    void loadFromAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ratio);
            width_ratio = a.getFloat(R.styleable.Ratio_width_ratio, 16.0f);
            height_ratio = a.getFloat(R.styleable.Ratio_height_ratio, 9.0f);
            isWidthStandard = a.getBoolean(R.styleable.Ratio_widthStandard, true);
            a.recycle();
        }
    }

    /**
     * onMeasure
     * @param widthMeasureSpec  widthMeasureSpec
     * @param heightMeasureSpec heightMeasureSpec
     */
    int[] measureWH(int widthMeasureSpec, int heightMeasureSpec) {
        if (isWidthStandard) {
            //得到heightSize
            int heightSize = (int) (View.MeasureSpec.getSize(widthMeasureSpec) * height_ratio / width_ratio);
            //通过heightSize  和指定的EXACTLY模式得到heightMeasureSpec
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightSize, View.MeasureSpec.EXACTLY);
        } else {
            int widthSize = (int) (View.MeasureSpec.getSize(heightMeasureSpec) * width_ratio / height_ratio);
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY);
        }
        return new int[]{widthMeasureSpec, heightMeasureSpec};
    }

    /**
     * 设置宽高比
     * @param ratio 宽高比
     */
    void setRatio(float ratio) {
        width_ratio = 100.0f;
        height_ratio = width_ratio / ratio;
    }

    /**
     * 设置宽高比
     * @param width_ratio  width_ratio
     * @param height_ratio height_ratio
     */
    void setRatio(float width_ratio, float height_ratio) {
        this.width_ratio = width_ratio;
        this.height_ratio = height_ratio;
    }

    /**
     * 设置是否以宽度为基准
     * @param widthStandard 是否以宽度为基准<ul><li>true：表示高度通过宽度和比例计算</li>
     *                      <li>false：表示宽度通过高度和比例计算</li></ul>
     */
    void setWidthStandard(boolean widthStandard) {
        isWidthStandard = widthStandard;
    }

}

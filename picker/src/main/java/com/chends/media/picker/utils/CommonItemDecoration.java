package com.chends.media.picker.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * recycler item 间隔
 * @author chends create on 2019/9/6.
 */
public class CommonItemDecoration extends RecyclerView.ItemDecoration {
    private int[] left; // left 数组
    private int[] right; // right 数组
    private Builder builder; // builder

    public static class Builder {
        private int spanCount; // 每行个数
        private int decoration; // 间距
        private boolean includeTB = false; // 是否需要上下分割线
        private boolean includeLR = false; // 是否需要左右分割线
        private int positionOffset; // positionOffset
        private boolean isVertical = true; // 默认纵向

        public Builder(int spanCount) {
            this.spanCount = spanCount;
        }

        public Builder setDecoration(int decoration) {
            this.decoration = decoration;
            return this;
        }

        /**
         * 左右分割线
         * @param includeTB 是否需要左右分割线
         */
        public Builder setIncludeTB(boolean includeTB) {
            this.includeTB = includeTB;
            return this;
        }

        /**
         * 上下分割线
         * @param includeLR 是否需要上下分割线
         */
        public Builder setIncludeLR(boolean includeLR) {
            this.includeLR = includeLR;
            return this;
        }

        /**
         * positionOffset
         * @param positionOffset 第几个item开始需要分割线（相当于头部有几个item）
         */
        public Builder setPositionOffset(int positionOffset) {
            this.positionOffset = positionOffset;
            return this;
        }

        public Builder setVertical(boolean vertical) {
            isVertical = vertical;
            return this;
        }

        public CommonItemDecoration build() {
            return new CommonItemDecoration(this);
        }
    }

    private CommonItemDecoration(Builder builder) {
        this.builder = builder;
        int total; // item总共的间隔
        if (builder.includeLR) {
            total = builder.decoration + Math.round(builder.decoration * 1f / builder.spanCount);
        } else {
            total = Math.round(builder.decoration * (builder.spanCount - 1) * 1f / builder.spanCount);
        }
        if (builder.spanCount <= 0) {
            builder.spanCount = 1;
        }
        left = new int[builder.spanCount];
        right = new int[builder.spanCount];
        for (int i = 0; i < builder.spanCount; i++) {
            if (i == 0) {
                if (builder.includeLR) {
                    left[i] = builder.decoration;
                } else {
                    left[i] = 0;
                }
            } else {
                left[i] = builder.decoration - (total - left[i - 1]); // 后一列的left = decoration - (total - 上一列的left)
            }
            right[i] = total - left[i];
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view) - builder.positionOffset;
        if (position < 0) {
            return;
        }
        int column = position % builder.spanCount;// 计算这个child 处于第几列
        if (builder.isVertical) { // 纵向
            outRect.left = left[column];
            outRect.right = right[column];

            if (builder.includeTB) {
                if (position < builder.spanCount) {
                    // 第一行设置top
                    outRect.top = builder.decoration;
                }
                outRect.bottom = builder.decoration; // 设置bottom
            } else {
                if (position >= builder.spanCount) {
                    outRect.top = builder.decoration; // 非第一行设置top
                }
            }
        } else { // 横向
            outRect.top = left[column];
            outRect.bottom = right[column];

            if (builder.includeLR) {
                if (position < builder.spanCount) {
                    // 第一行设置top
                    outRect.left = builder.decoration;
                }
                outRect.right = builder.decoration; // 设置right
            } else {
                if (position >= builder.spanCount) {
                    outRect.left = builder.decoration; // 非第一列设置left
                }
            }
        }
    }
}

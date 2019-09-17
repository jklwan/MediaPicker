package com.chends.media.picker.ui;

import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.chends.media.picker.R;
import com.chends.media.picker.utils.PickerUtil;
import com.chends.media.picker.utils.statusbar.FlyMeStatusBarUtils;
import com.chends.media.picker.utils.statusbar.StatusBarUtils;

/**
 * base Activity
 * @author chends create on 2019/9/6.
 */
public abstract class BasePickerActivity extends AppCompatActivity {

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initStatus();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initStatus();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initStatus();
    }

    /**
     * 初始化状态栏<br/>
     * <ol><li>如果颜色为深色：只使用fitsSystemWindows</li>
     * <li>如果颜色为浅色：
     * <ol><li>如果状态栏颜色可变黑那么使用fitsSystemWindows</li>
     * <li>如果不能变黑，则使用自定义布局</li></ol>
     * </li></ol>
     */
    protected void initStatus() {
        StatusBarUtils.Builder builder = new StatusBarUtils.Builder(this);
        boolean black = FlyMeStatusBarUtils.isBlackColor(ContextCompat.getColor(this, colorPrimaryDark()), 50);
        builder.statusBarDarkFont(!black, 0.2f);
        // 添加自定义布局
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 添加布局
            View topView = findViewById(R.id.topView);
            if (topView != null) {
                ViewGroup.LayoutParams lp = topView.getLayoutParams();
                lp.height = PickerUtil.getStatusHeight(this);
                topView.setLayoutParams(lp);
                topView.setVisibility(View.VISIBLE);
            }
            builder.setStatusBarColor(ContextCompat.getColor(this, colorPrimaryDark()))
                    .setStatusBarView(topView);
        }
        builder.builder().init();
    }

    /**
     * 是否透明状态栏
     * @return <ul>是否透明状态栏<li>true：布局中不需要android:fitsSystemWindows="true",显示效果为全屏透明，键盘模式无效</li>
     * <Li>false：布局中需要android:fitsSystemWindows="true"</Li></ul>
     */
   /* public boolean isTranslucentStatus() {
        return true;
    }*/
    @ColorRes
    protected int colorPrimaryDark() {
        return R.color.color_media_picker_title_bg;
    }

}

package com.chends.media.picker.listener;

/**
 * @author chends create on 2019/9/5.
 */
public abstract class PickerCallback {
    /**
     * 选中，不选中
     * @param choose choose
     * @param path   path
     */
    public void onChooseChange(boolean choose, String path) {

    }

    /**
     * 选中的图片有问题时
     */
    public void onChooseError(String path) {

    }

}

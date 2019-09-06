package com.chends.media.picker.model;

/**
 * 单个文件
 * @author chends create on 2019/9/2.
 */
public class ItemBean {
    /**
     * 文件路径
     */
    public String path;

    /**
     * 文件类型
     */
    @Constant.ItemType
    public int type;
}

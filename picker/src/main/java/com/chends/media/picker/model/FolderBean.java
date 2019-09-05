package com.chends.media.picker.model;

/**
 * 文件夹
 * @author chends create on 2019/9/2.
 */
public class FolderBean {
    /**
     * 文件夹类型
     */
    @Constant.FolderType
    private int type;
    /**
     * 显示名称
     */
    private String name;
    /**
     * 文件夹id
     */
    private long mId;
}

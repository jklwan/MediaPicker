package com.chends.media.picker.model;

import android.text.TextUtils;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.utils.MediaLoader;
import com.chends.media.picker.utils.PickerUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PickerBean
 * @author chends create on 2019/9/5.
 */
public final class PickerBean implements Serializable {
    public Set<String> typeSet = new HashSet<>();
    public int maxNum = 1;
    public int spanCount = 3;
    public List<String> chooseList = new ArrayList<>();
    public List<ItemBean> chooseItem = new ArrayList<>();
    //public long audioLimit = -1;
    //public long videoLimit = -1;
    public Set<String> imageList = new HashSet<>(), videoList = new HashSet<>(),
            audioList = new HashSet<>();
    public boolean hasImage, hasVideo, hasAudio, hasAll, showPreview, reset = false;
    public MediaLoader loader;

    private PickerBean() {
    }

    private static final class Instance {
        private static final PickerBean INNER = new PickerBean();
    }

    public static PickerBean getInstance() {
        return Instance.INNER;
    }

    public static PickerBean resetInstance() {
        PickerBean bean = getInstance();
        bean.reset();
        return bean;
    }

    private void reset() {
        typeSet = new HashSet<>();
        maxNum = 1;
        spanCount = 3;
        chooseList.clear();
        chooseItem.clear();
        /*audioLimit = -1;
        videoLimit = -1;*/
        imageList = new HashSet<>();
        videoList = new HashSet<>();
        audioList = new HashSet<>();
        hasAll = false;
        hasImage = false;
        hasVideo = false;
        hasAudio = false;
        showPreview = false;
        reset = true;
        loader = null;
    }

    /**
     * 选择的类型
     * @param typeSet typeSet
     */
    public void addTypes(Set<String> typeSet) {
        if (typeSet != null) {
            for (String item : typeSet) {
                if (!TextUtils.isEmpty(item) && PickerUtil.support(item)) {
                    this.typeSet.add(item.toLowerCase());
                }
            }
        }
    }

    /**
     * 最大选择数量
     * @param maxNum 最小1
     */
    public void setMaxNum(int maxNum) {
        this.maxNum = Math.max(maxNum, 1);
    }

    /**
     * 已选择的列表
     * @param chooseList list
     */
    public void setChooseList(List<String> chooseList) {
        if (chooseList != null) {
            this.chooseList.clear();
            for (String item : chooseList) {
                if (!TextUtils.isEmpty(item)) {
                    this.chooseList.add(item);
                }
            }
        }
    }

    /**
     * videoLimit
     * @param second second
     */
    /*public void setVideoLimit(int second) {
        if (second > 0) {
            videoLimit = second;
        }
    }*/

    /**
     * audioLimit
     * @param second second
     */
    /*public void setAudioLimit(int second) {
        if (second > 0) {
            audioLimit = second;
        }
    }*/

    /**
     * 图片加载
     */
    public void setLoader(MediaLoader loader) {
        this.loader = loader;
    }

    /**
     * 每行显示数量
     * @param spanCount spanCount
     */
    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    /**
     * 是否显示预览，需要显示时需要引用picker_preview库
     */
    private void initPreview() {
        showPreview = PickerUtil.getPreview() != null;
    }

    /**
     * init，初始化某些参数
     */
    public void init() {
        initPreview();
        if (typeSet.isEmpty()) {
            typeSet = MimeType.allImage();
            for (String type : typeSet) {
                if (!TextUtils.isEmpty(type)) {
                    imageList.add(type);
                }
            }
            hasImage = true;
        } else {
            for (String type : typeSet) {
                if (!TextUtils.isEmpty(type)) {
                    if (type.startsWith(Constant.IMAGE_START)) {
                        imageList.add(type);
                        hasImage = true;
                    } else if (type.startsWith(Constant.VIDEO_START)) {
                        videoList.add(type);
                        hasVideo = true;
                    } else if (type.startsWith(Constant.AUDIO_START)) {
                        audioList.add(type);
                        hasAudio = true;
                    }
                }
            }
        }
        if (hasImage == hasVideo) {
            hasAll = hasImage;
        } else {
            hasAll = hasAudio;
        }
    }

    /**
     * 数据恢复
     * @param bean bean
     */
    public void restore(PickerBean bean) {
        if (bean == null) return;
        typeSet = bean.typeSet;
        maxNum = bean.maxNum;
        spanCount = bean.spanCount;
        chooseList = bean.chooseList;
        chooseItem = bean.chooseItem;
        /*audioLimit = -1;
        videoLimit = -1;*/
        imageList = bean.imageList;
        videoList = bean.videoList;
        audioList = bean.audioList;
        hasAll = bean.hasAll;
        hasImage = bean.hasImage;
        hasVideo = bean.hasVideo;
        hasAudio = bean.hasAudio;
        showPreview = bean.showPreview;
        reset = bean.reset;
        loader = bean.loader;
    }

}

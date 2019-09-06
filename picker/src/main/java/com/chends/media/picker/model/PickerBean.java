package com.chends.media.picker.model;

import android.text.TextUtils;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.utils.PickerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PickerBean
 * @author chends create on 2019/9/5.
 */
public final class PickerBean {
    public Set<String> typeSet = new HashSet<>();
    public int maxNum = 1;
    public List<String> chooseList = new ArrayList<>();
    public long audioLimit = -1;
    public long videoLimit = -1;
    public List<String> imageList = new ArrayList<>(), videoList = new ArrayList<>(),
            audioList = new ArrayList<>();
    public boolean hasImage, hasVideo, hasAudio;

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
        chooseList = new ArrayList<>();
        audioLimit = -1;
        videoLimit = -1;
        imageList = new ArrayList<>();
        videoList = new ArrayList<>();
        audioList = new ArrayList<>();
        hasImage = false;
        hasVideo = false;
        hasAudio = false;
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
    public void setVideoLimit(int second) {
        if (second > 0) {
            videoLimit = second;
        }
    }

    /**
     * audioLimit
     * @param second second
     */
    public void setAudioLimit(int second) {
        if (second > 0) {
            audioLimit = second;
        }
    }

    /**
     * init，初始化某些参数
     */
    public void init() {
        if (typeSet.isEmpty()) {
            typeSet = MimeType.allImage();
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
    }

}
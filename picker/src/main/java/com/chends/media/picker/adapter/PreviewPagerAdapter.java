/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chends.media.picker.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chends.media.picker.listener.PickerCallback;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.ui.PreviewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 预览页
 */
public class PreviewPagerAdapter extends FragmentPagerAdapter {
    private List<ItemBean> mList = new ArrayList<>();
    private PickerCallback callback;

    public PreviewPagerAdapter(FragmentManager manager, List<ItemBean> list) {
        super(manager);
        if (list != null){
            mList.addAll(list);
        }
    }

    public void setCallback(PickerCallback callback) {
        this.callback = callback;
    }

    @Override
    public Fragment getItem(int position) {
        String path = null;
        if (getMediaItem(position) != null){
            path = getMediaItem(position).getPath();
        }
        return PreviewFragment.newInstance(path).setCallback(callback);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return getMediaItem(position).hashCode();
    }

    public ItemBean getMediaItem(int position) {
        if (position < 0 || position >= mList.size()){
            return null;
        }
        return mList.get(position);
    }

}

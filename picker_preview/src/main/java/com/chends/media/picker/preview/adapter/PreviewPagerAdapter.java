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
package com.chends.media.picker.preview.adapter;

import android.view.View;

import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.preview.ui.PreviewFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * 预览页
 */
public class PreviewPagerAdapter extends FragmentPagerAdapter {
    private List<ItemBean> mList = new ArrayList<>();
    private View.OnClickListener listener;

    public PreviewPagerAdapter(FragmentManager manager, List<ItemBean> list) {
        super(manager);
        if (list != null) {
            mList.addAll(list);
        }
    }

    public PreviewPagerAdapter setListener(View.OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public Fragment getItem(int position) {
        return PreviewFragment.newInstance(getMediaItem(position)).setClickListener(listener);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        ItemBean item = getMediaItem(position);
        if (item == null) {
            return 0;
        }
        return item.hashCode();
    }

    public ItemBean getMediaItem(int position) {
        if (position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

}

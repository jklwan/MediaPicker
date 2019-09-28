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

import android.database.Cursor;
import android.view.View;

import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.preview.ui.PreviewFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * 预览页
 */
public class PreviewCursorPagerAdapter extends FragmentPagerAdapter {
    private Cursor cursor;
    private String columnIdName;
    private int mRowIDColumn;
    private View.OnClickListener listener;

    public PreviewCursorPagerAdapter(FragmentManager manager, String columnIdName) {
        super(manager);
        this.columnIdName = columnIdName;
        cursor = null;
    }

    public PreviewCursorPagerAdapter setListener(View.OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public Fragment getItem(int position) {
        if (!checkData(cursor)) {
            throw new IllegalStateException("Cursor error!");
        }
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("getItem:move cursor to position " + position);
        }

        return PreviewFragment.newInstance(getMediaItem(position)).setClickListener(listener);
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (!checkData(cursor)) {
            throw new IllegalStateException("Cursor error!");
        }
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("getItemId: move cursor to position " + position);
        }

        return cursor.getLong(mRowIDColumn);
    }

    /**
     * cursor检测
     * @param cursor cursor
     */
    private boolean checkData(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }

    public ItemBean getMediaItem(int position) {
        if (checkData(cursor) && cursor.moveToPosition(position)) {
            return ItemBean.valueOf(cursor);
        }
        return null;
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return;
        }
        if (newCursor != null) {
            cursor = newCursor;
            mRowIDColumn = cursor.getColumnIndexOrThrow(columnIdName);
        } else {
            cursor = null;
            mRowIDColumn = -1;
        }
        notifyDataSetChanged();
    }
}

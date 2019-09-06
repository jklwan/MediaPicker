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

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;


public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<VH> {

    private Cursor mCursor;
    private int mRowIDColumn;
    private String columnIdName;

    RecyclerViewCursorAdapter(Cursor c, String columnIdName) {
        this.columnIdName = columnIdName;
        setHasStableIds(true);
        swapCursor(c);
    }

    protected abstract void onBindViewHolder(VH holder, Cursor cursor);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (!checkData(mCursor)) {
            throw new IllegalStateException("Cursor error!");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("onBindViewHolder:move cursor to position " + position);
        }

        onBindViewHolder(holder, mCursor);
    }

    @Override
    public int getItemViewType(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("getItemViewType: move cursor to position " + position);
        }
        return getItemViewType(position, mCursor);
    }

    protected abstract int getItemViewType(int position, Cursor cursor);

    @Override
    public int getItemCount() {
        if (checkData(mCursor)) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!checkData(mCursor)) {
            throw new IllegalStateException("Cursor error!");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("getItemId: move cursor to position " + position);
        }

        return mCursor.getLong(mRowIDColumn);
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }

        if (newCursor != null) {
            mCursor = newCursor;
            mRowIDColumn = mCursor.getColumnIndexOrThrow(columnIdName);
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
            mRowIDColumn = -1;
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * cursor检测
     * @param cursor cursor
     */
    private boolean checkData(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }
}

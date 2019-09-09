package com.chends.media.picker.listener;

import android.database.Cursor;

/**
 * item loader callback
 * @author chends create on 2019/9/7.
 */
public interface ItemLoaderCallback {
    void onItemLoaderFinish(Cursor cursor, boolean isSearch);

    void onItemLoaderReset(boolean isSearch);
}

package com.chends.media.picker.listener;

import com.chends.media.picker.model.ItemBean;

/**
 * item click listener
 * @author chends create on 2019/9/7.
 */
public interface ItemClickListener {
    /**
     * item click
     * @param bean bean
     * @param position position
     */
    void onItemClick(ItemBean bean, int position);

    /**
     * select click
     * @param bean     bean
     * @param position position
     */
    void onItemSelectClick(ItemBean bean, int position);
}

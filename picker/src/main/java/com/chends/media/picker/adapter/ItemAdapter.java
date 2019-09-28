package com.chends.media.picker.adapter;

import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.R;
import com.chends.media.picker.listener.ItemClickListener;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.utils.PickerUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author chends create on 2019/9/7.
 */
public class ItemAdapter extends RecyclerViewCursorAdapter<ItemAdapter.ItemHolder> {
    private int wh;
    private ItemClickListener clickListener;

    public ItemAdapter(Cursor c, String columnIdName) {
        super(c, columnIdName);
        wh = Resources.getSystem().getDisplayMetrics().widthPixels / PickerBean.getInstance().spanCount;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setChoose(int position, String path) {
        notifyItemChanged(position, path);
    }

    @Override
    protected void onBindViewHolder(ItemHolder holder, Cursor cursor) {
        holder.onBind(cursor);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && (payloads.get(0) instanceof String)) {
            holder.setSelect((String) payloads.get(0));
            return;
        }
        onBindViewHolder(holder, getCursor());
    }

    @Override
    protected int getItemViewType(int position, Cursor cursor) {
        return 0;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media_picker_item, parent, false));
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView image, select;
        private View root, avLayout, filter;
        private TextView type, duration, audioName, imageType;

        public ItemHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            image = itemView.findViewById(R.id.image);
            select = itemView.findViewById(R.id.select);
            avLayout = itemView.findViewById(R.id.avLayout);
            type = itemView.findViewById(R.id.media_type);
            duration = itemView.findViewById(R.id.duration);
            audioName = itemView.findViewById(R.id.audioName);
            imageType = itemView.findViewById(R.id.imageType);
            filter = itemView.findViewById(R.id.image_filter);
        }

        public void onBind(Cursor cursor) {
            ItemBean bean = ItemBean.valueOf(cursor);
            loadImage(bean);
            setSelect(bean.getPath());
            int itemType = MimeType.getItemType(bean.getMimeType());
            if (itemType == Constant.TYPE_IMAGE) {
                avLayout.setVisibility(View.GONE);
                audioName.setVisibility(View.GONE);
                if (MimeType.isGif(bean.getMimeType())) {
                    imageType.setVisibility(View.VISIBLE);
                    imageType.setText(R.string.string_media_picker_gif);
                } else {
                    imageType.setVisibility(View.GONE);
                }
            } else {
                imageType.setVisibility(View.GONE);
                avLayout.setVisibility(View.VISIBLE);
                duration.setText(PickerUtil.getDuration(bean.getDuration()));
                if (itemType == Constant.TYPE_VIDEO) {
                    type.setText(R.string.string_media_picker_type_video);
                    audioName.setVisibility(View.GONE);
                } else {
                    audioName.setText(PickerUtil.getFileNameNoExtension(bean.getPath()));
                    audioName.setVisibility(View.VISIBLE);
                    type.setText(R.string.string_media_picker_type_audio);
                }
            }
            ItemClick click = new ItemClick(this, bean);
            select.setOnClickListener(click);
            root.setOnClickListener(click);
        }

        private class ItemClick implements View.OnClickListener {
            private ItemBean bean;
            private ItemHolder holder;

            public ItemClick(ItemHolder holder, ItemBean bean) {
                this.holder = holder;
                this.bean = bean;
            }

            @Override
            public void onClick(View v) {
                if (bean != null) {
                    if (v.getId() == R.id.root) {
                        if (clickListener != null) {
                            clickListener.onItemClick(bean, holder.getAdapterPosition());
                        }
                    } else if (v.getId() == R.id.select) {
                        if (clickListener != null) {
                            clickListener.onItemSelectClick(bean, holder.getAdapterPosition());
                        }
                    }
                }
            }
        }

        /**
         * 显示image
         * @param bean bean
         */
        private void loadImage(ItemBean bean) {
            image.setImageResource(R.drawable.ic_media_picker_image_default);
            if (PickerBean.getInstance().loader != null) {
                int itemType = MimeType.getItemType(bean.getMimeType());
                switch (itemType) {
                    case Constant.TYPE_IMAGE:
                        PickerBean.getInstance().loader.loadImageThumbnail(image, bean.getPath(),
                                wh, wh, MimeType.getImageType(bean.getMimeType(), bean.getPath()));
                        break;
                    case Constant.TYPE_VIDEO:
                        PickerBean.getInstance().loader.loadVideoThumbnail(image, bean.getPath(),
                                wh, wh);
                        break;
                    case Constant.TYPE_AUDIO:
                        PickerBean.getInstance().loader.loadAudioThumbnail(image, bean.getPath(),
                                wh, wh);
                        break;
                }
            }
        }

        public void setSelect(String path) {
            boolean choose = PickerBean.getInstance().chooseList.contains(path);
            filter.setVisibility(choose ? View.VISIBLE : View.GONE);
            select.setImageResource(choose ? R.drawable.ic_media_picker_checked : R.drawable.ic_media_picker_uncheck);
        }
    }
}

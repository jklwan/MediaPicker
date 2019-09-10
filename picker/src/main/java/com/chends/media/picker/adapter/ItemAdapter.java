package com.chends.media.picker.adapter;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

import java.util.List;

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
        private TextView type, duration, audioName;

        public ItemHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            image = itemView.findViewById(R.id.image);
            select = itemView.findViewById(R.id.select);
            avLayout = itemView.findViewById(R.id.avLayout);
            type = itemView.findViewById(R.id.media_type);
            duration = itemView.findViewById(R.id.duration);
            audioName = itemView.findViewById(R.id.audioName);
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
            } else {
                avLayout.setVisibility(View.VISIBLE);
                duration.setText(getDuration(bean.getDuration()));

                if (itemType == Constant.TYPE_VIDEO) {
                    type.setText(R.string.string_media_picker_type_video);
                    audioName.setVisibility(View.GONE);
                } else {
                    audioName.setText(getName(bean.getPath()));
                    audioName.setVisibility(View.VISIBLE);
                    type.setText(R.string.string_media_picker_type_audio);
                }
            }
            ItemClick click = new ItemClick(this, bean);
            select.setOnClickListener(click);
            root.setOnClickListener(click);
        }

        /**
         * 绝对路径获取名称
         * @param path path
         * @return name
         *
         */
        private String getName(String path) {
            int last = path.lastIndexOf('#');
            if (last < 0) {
                last = path.length();
            }
            int lasDot = path.lastIndexOf('.');
            if (lasDot != -1 && lasDot < last){
                last = lasDot;
            }
            int lastPath = path.lastIndexOf('/');
            String ext = "";
            if (lastPath > 0) {
                ext = path.substring(lastPath, last);
            }
            return ext;
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
         * 时间长度
         * @param duration duration
         * @return duration
         */
        private String getDuration(long duration) {
            long durationS = (duration - 1) / 1000 + 1;
            long second = durationS % 60;
            long minute = durationS / 60;
            StringBuilder builder = new StringBuilder();
            if (minute > 0) {
                if (minute < 10) {
                    builder.append("0");
                }
                builder.append(minute).append(":");
            } else {
                builder.append("00:");
            }
            if (second > 0) {
                if (second < 10) {
                    builder.append("0");
                }
                builder.append(second);
            } else {
                builder.append("00");
            }
            return builder.toString();
        }

        /**
         * 显示image
         * @param bean bean
         */
        private void loadImage(ItemBean bean) {
            image.setImageResource(R.drawable.ic_media_picker_image_default);
            if (PickerBean.getInstance().loader != null) {
                int type = MimeType.getItemType(bean.getMimeType());
                switch (type) {
                    case Constant.TYPE_IMAGE:
                        PickerBean.getInstance().loader.loadImageThumbnail(image, bean.getPath(),
                                wh, wh, MimeType.isGif(bean.getMimeType()));
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

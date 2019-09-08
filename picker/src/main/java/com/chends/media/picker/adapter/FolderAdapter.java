package com.chends.media.picker.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.R;
import com.chends.media.picker.listener.FolderSelectedListener;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.FolderBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.utils.PickerUtil;

import java.util.ArrayList;
import java.util.List;

import static com.chends.media.picker.model.Constant.Folder_Id_All;
import static com.chends.media.picker.model.Constant.Folder_Id_All_Audio;
import static com.chends.media.picker.model.Constant.Folder_Id_All_Video;

/**
 * @author chends create on 2019/9/7.
 */
public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderHolder> {
    private List<FolderBean> mList = new ArrayList<>();
    private FolderBean selectItem;
    private Context context;
    private FolderSelectedListener listener;
    private int wh;

    public FolderAdapter(Context context) {
        this(context, null);
    }

    public FolderAdapter(Context context, List<FolderBean> list) {
        this.context = context;
        mList.clear();
        if (list != null) {
            mList.addAll(list);
        }
        wh = PickerUtil.dp2px(80);
    }

    public void setListener(FolderSelectedListener listener) {
        this.listener = listener;
    }

    public void setList(List<FolderBean> list) {
        mList.clear();
        if (list != null && !list.isEmpty()) {
            mList.addAll(list);
        } else {
            selectItem = null;
        }
        notifyDataSetChanged();
    }

    public void setSelect(int position) {
        int oldPos = -1;
        if (selectItem != null) {
            oldPos = mList.indexOf(selectItem);
        }
        if (oldPos == position) {
            sendListener();
            return;
        }
        selectItem = getItem(position);
        if (oldPos != -1) {
            notifyItemChanged(oldPos, false);
        }
        if (selectItem != null) {
            notifyItemChanged(position, true);
            sendListener();
        }
    }

    /**
     * send listener
     */
    private void sendListener() {
        if (listener != null) {
            listener.onSelected(selectItem);
        }
    }

    private void setSelect(FolderBean bean) {
        int newPos = mList.indexOf(bean);
        if (newPos == -1) return;
        setSelect(newPos);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private FolderBean getItem(int position) {
        if (position < 0 || position >= mList.size()) return null;
        return mList.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
        holder.bindData(getItem(position));
        holder.itemView.setOnClickListener(new ItemClick(holder));
    }

    private class ItemClick implements View.OnClickListener {

        private RecyclerView.ViewHolder holder;

        public ItemClick(RecyclerView.ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            if (holder != null) {
                setSelect(holder.getAdapterPosition());
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FolderHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_media_picker_folder, parent, false));
    }

    public class FolderHolder extends RecyclerView.ViewHolder {
        private ImageView folderImage;
        private TextView folderName, folderCount;
        private View folderSelected;

        public FolderHolder(View itemView) {
            super(itemView);
            folderImage = itemView.findViewById(R.id.folder_image);
            folderName = itemView.findViewById(R.id.folder_name);
            folderCount = itemView.findViewById(R.id.folder_count);
            folderSelected = itemView.findViewById(R.id.folder_selected);
        }

        public void bindData(FolderBean bean) {
            if (bean != null) {
                loadImage(bean);
                folderName.setText(bean.getDisplayName());
                int res;
                if (Folder_Id_All.equals(bean.getId()) || Folder_Id_All_Audio.equals(bean.getId()) || Folder_Id_All_Video.equals(bean.getId())) {
                    res = R.string.string_media_picker_mediaCount;
                } else {
                    res = R.string.string_media_picker_imageCount;
                }
                folderCount.setText(context.getString(res, bean.getCount()));
                setSelected(bean.equals(selectItem));
            }
        }

        /**
         * 显示image
         * @param bean bean
         */
        private void loadImage(FolderBean bean) {
            if (PickerBean.getInstance().loader != null) {
                int type = MimeType.getItemType(bean.getMimeType());
                switch (type) {
                    case Constant.TYPE_IMAGE:
                        PickerBean.getInstance().loader.loadImage(folderImage, bean.getCoverPath(),
                                wh, wh, true, MimeType.isGif(bean.getMimeType()));
                        break;
                    case Constant.TYPE_VIDEO:
                        PickerBean.getInstance().loader.loadVideo(folderImage, bean.getCoverPath(),
                                wh, wh, true);
                        break;
                    case Constant.TYPE_AUDIO:
                        PickerBean.getInstance().loader.loadAudio(folderImage, bean.getCoverPath(),
                                wh, wh, true);
                        break;
                }
            }
        }

        public void setSelected(boolean selected) {
            folderSelected.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}

package com.chends.media.picker.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.R;
import com.chends.media.picker.listener.PickerCallback;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.utils.PickerUtil;
import com.github.piasy.biv.view.BigImageView;

/**
 * 预览页
 * @author chends create on 2019/9/6.
 */
public class PreviewFragment extends Fragment {
    private static final String BUNDLE_DATA = "path";
    private int w, h;
    private ItemBean item;
    private ImageView select;
    private PickerCallback callback;

    public PreviewFragment(){
        w = Resources.getSystem().getDisplayMetrics().widthPixels;
        h = Resources.getSystem().getDisplayMetrics().heightPixels;
    }
    public static PreviewFragment newInstance(ItemBean item) {
        PreviewFragment result = new PreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_DATA, item);
        result.setArguments(args);
        return result;
    }

    public PreviewFragment setCallback(PickerCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(BUNDLE_DATA)) {
            item = args.getParcelable(BUNDLE_DATA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_picker_preview, container, false);

        if (savedInstanceState != null) {
            if (item == null && savedInstanceState.containsKey(BUNDLE_DATA)) {
                item = savedInstanceState.getParcelable(BUNDLE_DATA);
            }
        }
        select = rootView.findViewById(R.id.select);
        if (item != null) {
            BigImageView imageView = rootView.findViewById(R.id.imageView);
            int type = MimeType.getItemType(item.getMimeType());
            imageView.setFailureImage(ContextCompat.getDrawable(requireActivity(),
                    R.drawable.ic_media_picker_image_default));
            if (PickerBean.getInstance().loader != null) {
                if (type == Constant.TYPE_IMAGE) {
                    PickerBean.getInstance().loader.loadImageFull(imageView,
                            item.getPath(), w, h, MimeType.isGif(item.getMimeType()));
                } else if (type == Constant.TYPE_VIDEO) {
                    PickerBean.getInstance().loader.loadVideoFull(imageView,
                            item.getPath(), w, h);
                } else if (type == Constant.TYPE_AUDIO) {
                    PickerBean.getInstance().loader.loadVideoFull(imageView,
                            item.getPath(), w, h);
                }
            }
            select.setImageResource(PickerBean.getInstance().chooseList.contains(item.getPath()) ?
                    R.drawable.ic_media_picker_checked : R.drawable.ic_media_picker_uncheck);
        }
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item != null && !TextUtils.isEmpty(item.getPath())) {
                    if (PickerUtil.selectPath(getActivity(), item)) {
                        boolean choose = PickerBean.getInstance().chooseList.contains(item.getPath());
                        if (choose) {
                            select.setImageResource(R.drawable.ic_media_picker_checked);
                        } else {
                            select.setImageResource(R.drawable.ic_media_picker_uncheck);
                        }
                        if (callback != null) {
                            callback.onChooseChange(choose, item.getPath());
                        }
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        View rootView = getView();
        if (rootView != null) {
            outState.putParcelable(BUNDLE_DATA, item);
        }
    }
}

package com.chends.media.picker.preview.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.preview.R;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * 预览页
 * @author chends create on 2019/9/6.
 */
public class PreviewFragment extends Fragment {
    private static final String BUNDLE_DATA = "path";
    private int w, h;
    private ItemBean item;
    private ImageView gifImage;
    private TextView imageInfo;

    public PreviewFragment() {
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
        gifImage = rootView.findViewById(R.id.gifImage);
        imageInfo = rootView.findViewById(R.id.imageInfo);
        if (item != null) {
            SubsamplingScaleImageView imageView = rootView.findViewById(R.id.imageView);
            int type = MimeType.getItemType(item.getMimeType());
            if (PickerBean.getInstance().loader != null) {
                if (type == Constant.TYPE_IMAGE) {
                    PickerBean.getInstance().loader.loadImageFull(imageView, item.getPath(), w, h,
                            MimeType.getImageType(item.getMimeType(), item.getPath()));
                } else if (type == Constant.TYPE_VIDEO) {
                    PickerBean.getInstance().loader.loadVideoFull(imageView, item.getPath(), w, h);
                } else if (type == Constant.TYPE_AUDIO) {
                    PickerBean.getInstance().loader.loadVideoFull(imageView, item.getPath(), w, h);
                }
            }
        }
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

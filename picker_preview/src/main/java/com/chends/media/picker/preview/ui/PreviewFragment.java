package com.chends.media.picker.preview.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
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

import java.io.File;

/**
 * 预览页
 * @author chends create on 2019/9/6.
 */
public class PreviewFragment extends Fragment {
    private static final String BUNDLE_DATA = "path";
    private int w, h;
    private ItemBean item;

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
        if (item != null && !TextUtils.isEmpty(item.getPath())) {
            SubsamplingScaleImageView imageView = rootView.findViewById(R.id.imageView);
            ImageView gifImage = rootView.findViewById(R.id.gifImage);
            TextView imageInfo = rootView.findViewById(R.id.imageInfo);
            View play = rootView.findViewById(R.id.play);
            int type = MimeType.getItemType(item.getMimeType());
            if (PickerBean.getInstance().loader != null) {
                if (type == Constant.TYPE_IMAGE) {
                    imageInfo.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                    int imageType = MimeType.getImageType(item.getPath());
                    switch (imageType) {
                        case Constant.TYPE_NORMAL:
                            gifImage.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                            loadImage(item.getPath());
                            break;
                        case Constant.TYPE_GIF:
                        case Constant.TYPE_APNG:
                        case Constant.TYPE_WEBP:
                        case Constant.TYPE_ANIMATED_WEBP:
                            PickerBean.getInstance().loader.loadImageFull(imageView,gifImage, item.getPath(), w, h,
                                    MimeType.getImageType(item.getPath()));
                            break;
                    }
                } else if (type == Constant.TYPE_VIDEO) {
                    imageInfo.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new PlayClick(item.getPath(), item.getMimeType()));
                    gifImage.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    PickerBean.getInstance().loader.loadVideoFull(imageView, item.getPath(), w, h);
                } else if (type == Constant.TYPE_AUDIO) {
                    imageInfo.setVisibility(View.VISIBLE);
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new PlayClick(item.getPath(), item.getMimeType()));
                    gifImage.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    PickerBean.getInstance().loader.loadVideoFull(imageView, item.getPath(), w, h);
                    imageInfo.setText(getName(item.getPath()));
                }
            }
        }
        return rootView;
    }

    private void loadImage(String path){

    }

    /**
     * 获取名称
     * @param path path
     * @return name
     */
    private String getName(String path) {
        int last = path.lastIndexOf('#');
        if (last < 0) {
            last = path.length();
        }
        int lastPath = path.lastIndexOf('/') + 1;
        String ext = "";
        if (lastPath > 0) {
            ext = path.substring(lastPath, last);
        }
        return ext;
    }

    private class PlayClick implements View.OnClickListener {
        private String path;
        private String mimeType;

        public PlayClick(String path, String mimeType) {
            this.path = path;
            this.mimeType = mimeType;
        }

        @Override
        public void onClick(View v) {
            openFile(path, mimeType);
        }
    }

    /**
     * 打开文件
     * @param path     path
     * @param mimeType mimeType
     */
    public void openFile(String path, String mimeType) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri fileUri;
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(requireActivity(),
                        requireActivity().getPackageName()+".picker_fileprovider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                fileUri = Uri.fromFile(file);
            }
            intent.setDataAndType(fileUri, mimeType);
            startActivity(intent);
        } catch (Exception ignore) {
        }
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

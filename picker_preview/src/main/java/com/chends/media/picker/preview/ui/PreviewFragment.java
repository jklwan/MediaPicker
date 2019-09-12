package com.chends.media.picker.preview.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.chends.media.picker.preview.listener.PreviewLoaderCallback;
import com.chends.media.picker.preview.utils.PreviewMediaLoader;
import com.chends.media.picker.preview.utils.PreviewUtil;
import com.chends.media.picker.utils.PickerUtil;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
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
    private SubsamplingScaleImageView imageView;
    private ImageView gifImage;

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
            imageView = rootView.findViewById(R.id.imageView);
            gifImage = rootView.findViewById(R.id.gifImage);
            TextView imageInfo = rootView.findViewById(R.id.imageInfo);
            View play = rootView.findViewById(R.id.play);
            int type = MimeType.getItemType(item.getMimeType());
            PreviewMediaLoader loader = null;
            if (PickerBean.getInstance().loader instanceof PreviewMediaLoader) {
                loader = (PreviewMediaLoader) PickerBean.getInstance().loader;
            }
            if (loader != null) {
                if (type == Constant.TYPE_IMAGE) {
                    imageInfo.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                    int imageType = MimeType.getImageType(item.getMimeType(), item.getPath());
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
                        case Constant.TYPE_SVG:
                            gifImage.setVisibility(View.GONE);
                            imageView.setVisibility(View.GONE);
                            loader.loadImageFull(imageView, gifImage, item.getPath(), w, h,
                                    imageType, callback);
                            break;
                    }
                } else if (type == Constant.TYPE_VIDEO) {
                    imageInfo.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new PlayClick(item.getPath(), item.getMimeType()));
                    gifImage.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    loader.loadVideoFull(imageView, item.getPath(), w, h, callback);
                } else if (type == Constant.TYPE_AUDIO) {
                    imageInfo.setVisibility(View.VISIBLE);
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new PlayClick(item.getPath(), item.getMimeType()));
                    gifImage.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    loader.loadAudioFull(imageView, item.getPath(), w, h, callback);
                    imageInfo.setText(PreviewUtil.getFileName(item.getPath()));
                }
            }
        }
        return rootView;
    }

    /**
     * 显示大图
     * @param path path
     */
    private void loadImage(String path) {
        int[] wh = PickerUtil.getImageWH(path);
        loadImage(wh, ImageSource.uri(Uri.fromFile(new File(path))));
    }

    /**
     * load image
     * @param wh     wh
     * @param source source
     */
    private void loadImage(int[] wh, ImageSource source) {
        imageView.setMinimumTileDpi(160);
        float result = 0.5f;
        if (wh[0] > 0 && wh[1] > 0) {
            // 使图片横向铺满
            boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            float minScale, maxScale;
            int realW, realH;
            if (isPortrait) {
                realW = w;
                realH = h;
            } else {
                realW = h;
                realH = w;
            }
            if (wh[0] <= wh[1]) {
                result = (float) realW / wh[0];
            } else {
                result = (float) realH / wh[1];
            }

            minScale = (float) realW / wh[0];
            if (minScale < 0.5f) {
                // 图片大于屏幕当前宽度的2倍
                // 最小铺满整个屏幕， 最大最大边界(1)
                maxScale = 1f;
            } else {
                // 最小铺满，最大2倍
                maxScale = 2f * minScale;
            }
            imageView.setMinScale(minScale);
            imageView.setMaxScale(maxScale);
            if ((isPortrait && ((wh[1] * minScale - h) > 1)) || ((wh[1] * minScale - w) > 1)) {
                imageView.setImage(source,
                        new ImageViewState(minScale, new PointF(0, 0), 0));
                imageView.setDoubleTapZoomScale(result);
                return;
            }
        }
        imageView.setDoubleTapZoomScale(result);
        imageView.setImage(source);
    }

    private PreviewLoaderCallback callback = new PreviewLoaderCallback() {
        @Override
        public void onLoadImage(boolean useScaleImage, Bitmap bitmap, boolean needRecycle) {
            if (isAlive()) {
                if (useScaleImage) {
                    imageView.setVisibility(View.VISIBLE);
                    gifImage.setVisibility(View.GONE);
                    if (bitmap != null) {
                        int[] wh = new int[]{bitmap.getWidth(), bitmap.getHeight()};
                        loadImage(wh, needRecycle ? ImageSource.bitmap(bitmap) : ImageSource.cachedBitmap(bitmap));
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                    gifImage.setVisibility(View.VISIBLE);
                    if (bitmap != null) {
                        gifImage.setImageBitmap(bitmap);
                    }
                }
            }
        }

        @Override
        public void onLoadImageUseScale(File file) {
            if (isAlive()) {
                imageView.setVisibility(View.VISIBLE);
                gifImage.setVisibility(View.GONE);
                loadImage(file.getAbsolutePath());
            }
        }

        @Override
        public void onLoadImageUseScale(ImageSource source) {
            if (isAlive()) {
                imageView.setVisibility(View.VISIBLE);
                gifImage.setVisibility(View.GONE);
                loadImage(new int[]{0, 0}, source);
            }
        }
    };

    private boolean isAlive() {
        return !isDetached() && getActivity() != null && imageView != null;
    }

    /**
     * 播放点击
     */
    private class PlayClick implements View.OnClickListener {
        private String path;
        private String mimeType;

        public PlayClick(String path, String mimeType) {
            this.path = path;
            this.mimeType = mimeType;
        }

        @Override
        public void onClick(View v) {
            PreviewUtil.openFile(requireActivity(), path, mimeType);
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
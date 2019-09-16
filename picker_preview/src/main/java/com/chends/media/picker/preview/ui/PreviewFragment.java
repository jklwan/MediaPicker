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
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.davemorrissey.labs.subscaleview.GifSubsamplingScaleImageView;
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
    private FrameLayout frameLayout;
    private View imageView;

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
        frameLayout = rootView.findViewById(R.id.frameLayout);
        if (item != null && !TextUtils.isEmpty(item.getPath())) {
            TextView imageInfo = rootView.findViewById(R.id.imageInfo);
            View play = rootView.findViewById(R.id.play);
            int type = MimeType.getItemType(item.getMimeType());
            PreviewMediaLoader loader = null;
            if (PickerBean.getInstance().loader instanceof PreviewMediaLoader) {
                loader = (PreviewMediaLoader) PickerBean.getInstance().loader;
            }
            if (type == Constant.TYPE_IMAGE) {
                imageInfo.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
                int imageType = MimeType.getImageType(item.getMimeType(), item.getPath());
                switch (imageType) {
                    case Constant.TYPE_NORMAL:
                        loadImage(item.getPath());
                        break;
                    case Constant.TYPE_GIF:
                        if (PreviewUtil.hasGifScale()) {
                            loadGifImage(item.getPath());
                        } else {
                            if (loader != null) {
                                loader.loadImageFull(frameLayout, item.getPath(), w, h,
                                        imageType, callback);
                            }
                        }
                        break;
                    case Constant.TYPE_APNG:
                    case Constant.TYPE_WEBP:
                    case Constant.TYPE_ANIMATED_WEBP:
                    case Constant.TYPE_SVG:
                        if (loader != null) {
                            loader.loadImageFull(frameLayout, item.getPath(), w, h,
                                    imageType, callback);
                        }
                        break;
                }
            } else if (type == Constant.TYPE_VIDEO) {
                imageInfo.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                play.setOnClickListener(new PlayClick(item.getPath(), item.getMimeType()));
                if (loader != null) {
                    loader.loadVideoFull(frameLayout, item.getPath(), w, h, callback);
                }
            } else if (type == Constant.TYPE_AUDIO) {
                imageInfo.setVisibility(View.VISIBLE);
                play.setVisibility(View.VISIBLE);
                play.setOnClickListener(new PlayClick(item.getPath(), item.getMimeType()));
                if (loader != null) {
                    loader.loadAudioFull(frameLayout, item.getPath(), w, h, callback);
                }
                imageInfo.setText(PreviewUtil.getFileName(item.getPath()));
            }
        }
        return rootView;
    }

    /**
     * 创建 SubsamplingScaleImageView
     * @return SubsamplingScaleImageView
     */
    private SubsamplingScaleImageView createSSIV() {
        if (!(imageView instanceof SubsamplingScaleImageView)) {
            frameLayout.removeView(imageView);
            imageView = new SubsamplingScaleImageView(requireActivity());
            frameLayout.addView(imageView, 0, getLayoutParams());
        }
        ((SubsamplingScaleImageView) imageView).setDebug(true);
        return (SubsamplingScaleImageView) imageView;
    }

    /**
     * 创建 GifSubsamplingScaleImageView
     * @return GifSubsamplingScaleImageView
     */
    private GifSubsamplingScaleImageView createGifSSIV() {
        if (!(imageView instanceof GifSubsamplingScaleImageView)) {
            frameLayout.removeView(imageView);
            imageView = new GifSubsamplingScaleImageView(requireActivity());
            frameLayout.addView(imageView, 0, getLayoutParams());
            ((GifSubsamplingScaleImageView) imageView).setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        }
        ((GifSubsamplingScaleImageView) imageView).setDebug(true);
        return (GifSubsamplingScaleImageView) imageView;
    }

    /**
     * 创建 ImageView
     * @return ImageView
     */
    private ImageView createImageView() {
        if (!(imageView instanceof AppCompatImageView)) {
            frameLayout.removeView(imageView);
            imageView = new AppCompatImageView(requireActivity());
            frameLayout.addView(imageView, 0, getLayoutParams());
        }
        return (ImageView) imageView;
    }

    private ViewGroup.LayoutParams getLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * 显示大图
     * @param path path
     */
    private void loadImage(String path) {
        int[] wh = PickerUtil.getImageWH(path);
        loadImage(wh, ImageSource.uri(Uri.fromFile(new File(path))).tilingDisabled(), true);
    }

    /**
     * load image
     * @param wh     wh
     * @param source source
     */
    private void loadImage(int[] wh, ImageSource source, boolean isFile) {
        if (isFile) {
            createSSIV().setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        } else {
            createSSIV().setOrientation(SubsamplingScaleImageView.ORIENTATION_0);
        }
        float result = 0.5f;
        if (wh[0] > 0 && wh[1] > 0) {
            // 使图片横向铺满
            boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            float minScale, maxScale;
            int realW;
            if (isPortrait) {
                realW = w;
            } else {
                realW = h;
            }
            minScale = (float) realW / wh[0];
            if (minScale < 0.5f) {
                // 图片大于屏幕当前宽度的2倍
                // 最小铺满整个屏幕， 最大最大边界(1)
                createSSIV().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                maxScale = 1f;
            } else if (minScale < 2f) {
                // 最小铺满，最大2倍
                createSSIV().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                maxScale = 2f * minScale;
            } else {
                createSSIV().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                // 图片宽度不到屏幕的一半
                // 最大屏幕宽度
                // 最小1/2屏幕
                maxScale = minScale;
                minScale = maxScale / 2f;
            }
            result = (maxScale - minScale) / 2f;
            createSSIV().setMinScale(minScale);
            createSSIV().setMaxScale(maxScale);
            if ((isPortrait && ((wh[1] * minScale - h) > 1)) || ((wh[1] * minScale - w) > 1)) {
                createSSIV().setDoubleTapZoomScale(result);
                createSSIV().setImage(source, new ImageViewState(minScale, new PointF(0, 0), 0));
                return;
            }
        }
        createSSIV().setDoubleTapZoomScale(result);
        createSSIV().setImage(source);
    }

    /**
     * 显示大图
     * @param path path
     */
    private void loadGifImage(String path) {
        int[] wh = PickerUtil.getImageWH(path);
        ImageSource source = ImageSource.uri(Uri.fromFile(new File(path))).tilingDisabled();
        float result = 0.5f;
        if (wh[0] > 0 && wh[1] > 0) {
            // 使图片横向铺满
            boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            float minScale, maxScale;
            int realW;
            if (isPortrait) {
                realW = w;
            } else {
                realW = h;
            }

            minScale = (float) realW / wh[0];
            if (minScale < 0.5f) {
                // 图片大于屏幕当前宽度的2倍
                // 最小铺满整个屏幕， 最大最大边界(1)
                createSSIV().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                maxScale = 1f;
            } else if (minScale < 2f) {
                // 最小铺满，最大2倍
                createSSIV().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                maxScale = 2f * minScale;
            } else {
                createSSIV().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                // 图片宽度不到屏幕的一半
                // 最大屏幕宽度
                // 最小1/2屏幕
                maxScale = minScale;
                minScale = maxScale / 2f;
            }
            result = (maxScale - minScale) / 2f;
            createGifSSIV().setMinScale(minScale);
            createGifSSIV().setMaxScale(maxScale);
            if ((isPortrait && ((wh[1] * minScale - h) > 1)) || ((wh[1] * minScale - w) > 1)) {
                createGifSSIV().setDoubleTapZoomScale(result);
                createGifSSIV().setImage(source,
                        new ImageViewState(minScale, new PointF(0, 0), 0));
                return;
            }
        }
        createGifSSIV().setDoubleTapZoomScale(result);
        createGifSSIV().setImage(source);
    }


    @Override
    public void onDestroyView() {
        if (imageView instanceof SubsamplingScaleImageView) {
            ((SubsamplingScaleImageView) imageView).recycle();
        } else {
            if (PreviewUtil.hasGifScale()) {
                if (imageView instanceof GifSubsamplingScaleImageView) {
                    ((GifSubsamplingScaleImageView) imageView).recycle();
                }
            }
        }
        if (frameLayout != null) {
            if (imageView != null) {
                frameLayout.removeView(imageView);
            }
        }
        imageView = null;
        Log.d("preview", "onDestroyView");
        super.onDestroyView();
    }

    private PreviewLoaderCallback callback = new PreviewLoaderCallback() {
        @Override
        public void onLoadImage(boolean useScaleImage, Bitmap bitmap, boolean needRecycle) {
            if (isAlive()) {
                if (useScaleImage) {
                    if (bitmap != null) {
                        int[] wh = new int[]{bitmap.getWidth(), bitmap.getHeight()};
                        loadImage(wh, needRecycle ? ImageSource.bitmap(bitmap) : ImageSource.cachedBitmap(bitmap),
                                false);
                    }
                } else {
                    if (bitmap != null) {
                        createImageView().setImageBitmap(bitmap);
                    }
                }
            }
        }

        @Override
        public void onLoadImageUseScale(File file) {
            if (isAlive()) {
                loadImage(file.getAbsolutePath());
            }
        }

        @Override
        public void onLoadImageUseScale(ImageSource source) {
            if (isAlive()) {
                loadImage(new int[]{0, 0}, source, false);
            }
        }

        @Nullable
        @Override
        public ImageView getImageView() {
            return createImageView();
        }
    };

    private boolean isAlive() {
        return !isDetached() && getActivity() != null && frameLayout != null;
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
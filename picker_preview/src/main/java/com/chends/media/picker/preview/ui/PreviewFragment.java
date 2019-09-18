package com.chends.media.picker.preview.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.chends.media.picker.scaleview.ImageSource;
import com.chends.media.picker.scaleview.ImageViewState;
import com.chends.media.picker.scaleview.SubsamplingScaleImageView;
import com.chends.media.picker.utils.PickerUtil;

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
    private View.OnClickListener listener;

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

    public PreviewFragment setClickListener(View.OnClickListener listener) {
        this.listener = listener;
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
        return createSSIV(false);
    }

    /**
     * 创建 gif SubsamplingScaleImageView
     * @return SubsamplingScaleImageView
     */
    private SubsamplingScaleImageView createGifSSIV() {
        return createSSIV(true);
    }

    /**
     * 创建 SubsamplingScaleImageView
     * @return SubsamplingScaleImageView
     */
    private SubsamplingScaleImageView createSSIV(boolean isGif) {
        if (!(imageView instanceof SubsamplingScaleImageView)) {
            frameLayout.removeView(imageView);
            imageView = new SubsamplingScaleImageView(requireActivity());
            frameLayout.addView(imageView, 0, getLayoutParams());
            ((SubsamplingScaleImageView) imageView).setDoubleTapZoomDuration(200);
            ((SubsamplingScaleImageView) imageView).setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
            ((SubsamplingScaleImageView) imageView).setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CUSTOM);
        }
        imageView.setOnClickListener(listener);
        if (isGif) {
            ((SubsamplingScaleImageView) imageView).setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        }
        ((SubsamplingScaleImageView) imageView).setIsGif(isGif);
        ((SubsamplingScaleImageView) imageView).setDebug(true);
        return (SubsamplingScaleImageView) imageView;
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
        imageView.setOnClickListener(listener);
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
        createSSIV().setOnImageEventListener(new TryReloadBitmap(path));
        loadImage(wh, ImageSource.uri(Uri.fromFile(new File(path))), true);
    }

    /**
     * 显示大图
     * @param bitmap bitmap
     */
    private void loadImage(Bitmap bitmap, boolean needRecycle){
        int[] wh = new int[]{bitmap.getWidth(), bitmap.getHeight()};
        if (Math.max(wh[0], wh[1]) >= PickerUtil.maxTextureSize()) {
            // 宽高大于最大宽高，进行缩放
            Bitmap scale = PreviewUtil.onlyScaleBitmap(bitmap, needRecycle);
            if (scale != bitmap) {
                wh = new int[]{scale.getWidth(), scale.getHeight()};
                loadImage(wh, ImageSource.bitmap(scale), false);
                return;
            }
        }
        loadImage(wh, needRecycle ? ImageSource.bitmap(bitmap) :
                ImageSource.cachedBitmap(bitmap), false);
    }

    /**
     * load image
     * @param wh     wh
     * @param source source
     */
    private void loadImage(int[] wh, @NonNull ImageSource source, boolean isFile) {
        SubsamplingScaleImageView imageView = createSSIV();
        if (isFile) {
            imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        } else {
            imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_0);
        }
        loadImage(imageView, wh, source);
    }

    /**
     * 显示大图
     * @param path path
     */
    private void loadGifImage(String path) {
        SubsamplingScaleImageView imageView = createGifSSIV();
        imageView.setOnImageEventListener(new TryReloadBitmap(path));
        int[] wh = PickerUtil.getImageWH(path);
        if (Math.max(wh[0], wh[1]) >= PickerUtil.maxTextureSize()) {
            // 宽高大于最大宽高，进行缩放
            wh = PreviewUtil.onlyScaleWH(wh);
        }
        ImageSource source = ImageSource.uri(Uri.fromFile(new File(path))).tilingDisabled();
        loadImage(imageView, wh, source);
    }

    /**
     * 显示图片，设置缩放级别
     * @param view   view
     * @param wh     wh
     * @param source source
     */
    private void loadImage(SubsamplingScaleImageView view, int[] wh, ImageSource source) {
        ImageViewState state = null;
        float result = 0.5f;
        /*if (Math.max(wh[0], wh[1]) >= PickerUtil.maxTextureSize()){
            //view.setBitmapDecoderClass();
        }*/
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
                realH = h;
            }
            float wScale = (float) realW / wh[0], hScale = (float) realH / wh[1];
            minScale = Math.min(wScale, hScale);
            maxScale = Math.max(wScale, hScale);
            if (minScale < 0.5f) {
                // 默认铺满显示
                state = new ImageViewState(maxScale, new PointF(0, 0), 0);
                // 图片一边大于屏幕当前宽度的2倍
                // 最小：居中铺满， 最大：小边铺满或大边最大显示
                maxScale = Math.max(maxScale, 1 / minScale);
            } else if (minScale < 2f) {
                // 最小：居中铺满，最大：短边铺满
                maxScale = 2f * minScale;
                state = new ImageViewState(minScale, new PointF(wh[0], wh[1]), 0);
            } else {
                // 图片宽度不到屏幕的一半
                // 最小：居中铺满1/2屏幕，最大：居中铺满整个屏幕
                maxScale = minScale;
                minScale = maxScale / 2f;
                state = new ImageViewState(maxScale, new PointF(wh[0], wh[1]), 0);
            }
            result = (maxScale - minScale) / 2f;
            view.setMinScale(minScale);
            view.setMaxScale(maxScale);
        }
        view.setDoubleTapZoomScale(result);
        if (state != null) {
            view.setImage(source, state);
        } else {
            view.setImage(source);
        }
    }

    /**
     * 使用文件方式加载失败重试（可能的原因：BitmapRegionDecoder创建失败（skia没有更新）
     */
    private class TryReloadBitmap extends SubsamplingScaleImageView.DefaultOnImageEventListener {
        private String path;

        public TryReloadBitmap(String path) {
            this.path = path;
        }

        @Override
        public void onImageLoadError(Exception e) {
            // reload
            if (!TextUtils.isEmpty(path)) {
                // 使用原始的加载方式
                try {
                    loadImage(BitmapFactory.decodeFile(path), true);
                } catch (Exception ignore){}
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (imageView instanceof SubsamplingScaleImageView) {
            ((SubsamplingScaleImageView) imageView).recycle();
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

    /**
     * callback
     */
    private PreviewLoaderCallback callback = new PreviewLoaderCallback() {
        @Override
        public void onLoadImage(boolean useScaleImage, Bitmap bitmap, boolean needRecycle) {
            if (isAlive() && bitmap != null) {
                if (useScaleImage) {
                    loadImage(bitmap, needRecycle);
                } else {
                    createImageView().setImageBitmap(bitmap);
                }
            }
        }

        @Override
        public void onLoadImageUseScale(File file) {
            if (isAlive() && file != null) {
                loadImage(file.getAbsolutePath());
            }
        }

        @Override
        public void onLoadImageUseScale(ImageSource source) {
            if (isAlive() && source != null) {
                loadImage(new int[]{0, 0}, source, false);
            }
        }

        @NonNull
        @Override
        public ImageView getImageView() {
            return createImageView();
        }
    };

    /**
     * 是否可用
     * @return true or false
     */
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
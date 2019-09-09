package com.chends.media.picker.sample;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup.LayoutParams;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.chends.media.picker.model.Constant;
import com.github.piasy.biv.view.BigImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 加载器
 */
public class MediaLoaderUtil implements ComponentCallbacks2 {
    private final static int errorResId = R.drawable.ic_media_picker_image_default;
    /**
     * 图片缓存的核心类
     */
    private LruCache<String, BitmapDrawable> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /*
     * 线程池的线程数量，默认为1
     private int mThreadCount = 1;*/
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<TaskRunnable> mTasks;
    /*
     * 轮询的线程
     * <p>
     * private Thread mPoolThread;
     */
    private Handler mPoolThreadHandler;

    /**
     * 运行在UI线程的handler，用于给ImageView设置图片
     */
    private Handler mHandler;

    /**
     * 引入一个值为1的信号量，防止mPoolThreadHandler未初始化完成
     */
    private volatile Semaphore mSemaphore = new Semaphore(0);

    /**
     * 引入一个信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mPoolSemaphore;

    private static volatile MediaLoaderUtil mInstance;

    private Bitmap mLoadingBitmap;

    /**
     * 队列的调度方式
     */
    public enum Type {
        FIFO, LIFO
    }

    private WeakReference<Context> contextReference;

    /**
     * 单例获得该实例对象
     * @return MediaLoaderUtil
     */
    public static MediaLoaderUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MediaLoaderUtil.class) {
                if (mInstance == null) {
                    mInstance = new MediaLoaderUtil(context, 3, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 单例获得该实例对象
     * @return MediaLoaderUtil
     */
    public static MediaLoaderUtil getInstance(Context context, int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (MediaLoaderUtil.class) {
                if (mInstance == null) {
                    mInstance = new MediaLoaderUtil(context, threadCount, type);
                }
            }
        }
        return mInstance;
    }

    /**
     * 清除缓存
     */
    public void clearAll() {
        mLruCache.evictAll();
    }

    private MediaLoaderUtil(Context context, int threadCount, Type type) {
        init(context, threadCount, type);
    }

    private static class PoolThread extends Handler {
        private WeakReference<MediaLoaderUtil> weakReference;

        public PoolThread(MediaLoaderUtil loader) {
            this.weakReference = new WeakReference<>(loader);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference != null && weakReference.get() != null) {
                MediaLoaderUtil loader = weakReference.get();
                TaskRunnable runnable = loader.getTask();
                if (runnable != null) {
                    // 执行runnable
                    loader.mThreadPool.execute(runnable);
                    try {
                        loader.mPoolSemaphore.acquire();
                    } catch (InterruptedException e) {
                        LogUtil.e("acquire error:" + e.getMessage());
                    }
                }
            }
        }
    }

    private void init(Context context, int threadCount, Type type) {
        // loop thread
        Thread mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                mPoolThreadHandler = new PoolThread(MediaLoaderUtil.this);
                // 释放一个信号量
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        // 获取应用程序最大可用内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        int maxCache;
        if (maxMemory == Long.MAX_VALUE) {
            maxCache = 0;
        } else {
            maxCache = (int) (maxMemory / 10);
        }
        if (maxCache <= 0) {
            Object o = context.getSystemService(Context.ACTIVITY_SERVICE);
            if (o != null) {
                ActivityManager am = (ActivityManager) o;
                maxCache = (1024 * 1024 * am.getMemoryClass() / 10);
            } else {
                maxCache = (1024 * 1024 * 10);
            }
        }

        mLruCache = new LruCache<String, BitmapDrawable>(maxCache) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return getBitmapSize(value.getBitmap());
                //return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<>();
        mType = type == null ? Type.LIFO : type;
        contextReference = new WeakReference<>(context.getApplicationContext());
        mLoadingBitmap = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), errorResId);
        context.getApplicationContext().registerComponentCallbacks(this);
    }

    /**
     * 获取bitmap大小
     * @param bitmap bitmap
     * @return size
     */
    private int getBitmapSize(Bitmap bitmap) {
        if (bitmap == null) return 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //API 19
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            //API 12
            return bitmap.getByteCount();
        } else {
            //earlier version
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    private static class ShowHandler extends Handler {
        private WeakReference<MediaLoaderUtil> weakReference;

        public ShowHandler(MediaLoaderUtil loader) {
            this.weakReference = new WeakReference<>(loader);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference != null && weakReference.get() != null && msg != null && msg.obj != null) {
                ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                ImageView imageView = holder.imageView;
                BitmapDrawable drawable = holder.drawable;
                String path = holder.path;
                if (imageView != null && imageView.getTag(R.id.tag_mediaPath) != null &&
                        TextUtils.equals(imageView.getTag(R.id.tag_mediaPath).toString(), path)) {
                    imageView.setImageDrawable(drawable);
                }
            }
        }
    }

    /**
     * 显示图片
     * @param path      path
     * @param imageView imageView
     * @param width     width
     * @param height    height
     */
    public void loadImage(String path, ImageView imageView, int width, int height) {
        load(path, imageView, width, height, Constant.TYPE_IMAGE);
    }

    /**
     * 显示图片
     * @param path      path
     * @param imageView imageView
     * @param width     width
     * @param height    height
     */
    public void loadVideo(String path, ImageView imageView, int width, int height) {
        load(path, imageView, width, height, Constant.TYPE_VIDEO);
    }

    /**
     * 显示图片
     * @param path      path
     * @param imageView imageView
     * @param width     width
     * @param height    height
     */
    public void loadAudio(String path, ImageView imageView, int width, int height) {
        load(path, imageView, width, height, Constant.TYPE_AUDIO);
    }

    public void loadVideoFull(String path, BigImageView view) {

    }

    public void loadAudioFull(String path, BigImageView view) {

    }

    /**
     * 加载图片
     * @param path      path
     * @param imageView imageView
     * @param width     w
     * @param height    h
     * @param type      type
     */
    private void load(String path, ImageView imageView, int width, int height, @Constant.ItemType int type) {
        if (TextUtils.isEmpty(path)) return;
        // UI线程
        if (mHandler == null) {
            mHandler = new ShowHandler(this);
        }

        // set tag
        imageView.setTag(R.id.tag_mediaPath, path);
        // set tag
        imageView.setTag(R.id.tag_mediaSize, new MediaSize(width, height, type));

        BitmapDrawable drawable = getBitmapFromLruCache(getKey(imageView, path));
        if (drawable != null) {
            //imageView.setImageDrawable(drawable);
            ImgBeanHolder holder = new ImgBeanHolder();
            holder.drawable = drawable;
            holder.imageView = imageView;
            holder.path = path;
            Message message = Message.obtain();
            message.obj = holder;
            // Log.e("TAG", "mHandler.sendMessage(message);");
            mHandler.sendMessage(message);
        } else {
            TaskRunnable runnable = new TaskRunnable(imageView, path);
            AsyncDrawable asyncDrawable = new AsyncDrawable(Resources.getSystem(), mLoadingBitmap, runnable);
            imageView.setImageBitmap(mLoadingBitmap);
            imageView.setTag(R.id.tag_imageLoader, asyncDrawable);
            addTask(runnable);
        }
    }

    public class AsyncDrawable extends BitmapDrawable {
        private WeakReference<TaskRunnable> reference;

        public AsyncDrawable(Resources res, Bitmap bitmap, TaskRunnable runnable) {
            super(res, bitmap);
            reference = new WeakReference<>(runnable);
        }

        public TaskRunnable getTaskRunnable() {
            return reference.get();
        }
    }

    /**
     * 获取传入的ImageView它所对应的TaskRunnable。
     */
    private TaskRunnable getTaskRunnable(ImageView imageView) {
        if (imageView != null) {
            Object drawable = imageView.getTag(R.id.tag_imageLoader);
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getTaskRunnable();
            }
        }
        return null;
    }

    /**
     * get key
     * @param imageView imageView
     * @param path      path
     * @return key
     */
    private String getKey(ImageView imageView, String path) {
        MediaSize tagSize = null;
        Object sizeTag = imageView.getTag(R.id.tag_mediaSize);
        if (sizeTag instanceof MediaSize) {
            tagSize = (MediaSize) sizeTag;
        }
        String key = path;
        if (tagSize != null && tagSize.width > 0 && tagSize.height > 0) {
            key = path + tagSize.width + tagSize.height;
        }
        return key;
    }

    /**
     * 加载图片Runnable
     */
    private class TaskRunnable implements Runnable {
        private WeakReference<ImageView> reference;
        private String path;

        public TaskRunnable(ImageView imageView, String path) {
            this.reference = new WeakReference<>(imageView);
            this.path = path;
        }

        @Override
        public void run() {
            if (reference != null && reference.get() != null) {
                ImageView imageView = getAttachedImageView();
                if (imageView != null) {
                    String key = getKey(imageView, path);

                    ImgBeanHolder holder = new ImgBeanHolder();
                    Bitmap bm = buildBitmap(imageView, path, holder);
                    if (bm == null) bm = mLoadingBitmap;
                    addBitmapToLruCache(key, new BitmapDrawable(Resources.getSystem(), bm));

                    holder.drawable = getBitmapFromLruCache(key);
                    holder.imageView = imageView;
                    holder.path = path;
                    Message message = Message.obtain();
                    message.obj = holder;
                    // Log.e("TAG", "mHandler.sendMessage(message);");
                    mHandler.sendMessage(message);
                }
            }
            // 必须释放，否则快速滑动时图片无法加载
            mPoolSemaphore.release();
        }

        /**
         * 获取当前TaskRunnable所关联的ImageView。
         */
        private ImageView getAttachedImageView() {
            ImageView imageView = reference.get();
            TaskRunnable runnable = getTaskRunnable(imageView);
            if (this == runnable) {
                return imageView;
            }
            return null;
        }
    }

    /**
     * 添加一个任务
     * @param runnable runnable
     */
    private synchronized void addTask(TaskRunnable runnable) {
        try {
            // 请求信号量，防止mPoolThreadHandler为null
            if (mPoolThreadHandler == null) {
                mSemaphore.acquire();
            }
        } catch (InterruptedException e) {
            LogUtil.e("acquire error:" + e.getMessage());
        }
        mTasks.add(runnable);

        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * 取出一个任务
     * @return Runnable
     */
    private synchronized TaskRunnable getTask() {
        if (mType == Type.FIFO) {
            return mTasks.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTasks.removeLast();
        }
        return null;
    }

    /**
     * 根据ImageView获得适当的压缩的宽和高
     * @param imageView reference
     * @return MediaSize
     */
    private MediaSize getImageViewWidth(ImageView imageView) {
        MediaSize mediaSize = new MediaSize();
        Object sizeTag = imageView.getTag(R.id.tag_mediaSize);
        if (sizeTag instanceof MediaSize) {
            mediaSize = (MediaSize) sizeTag;
            if (mediaSize.width > 0 && mediaSize.height > 0) {
                return mediaSize;
            }
        }
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        final LayoutParams params = imageView.getLayoutParams();
        // Get actual image width
        int width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth();
        if (width <= 0) {
            width = params.width; // Get layout width parameter
        }
        if (width <= 0) {
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check maxWidth parameter
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }
        // Get actual image height
        int height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight();
        if (height <= 0) {
            height = params.height; // Get layout height parameter
        }
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check maxHeight parameter
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        mediaSize.width = width;
        mediaSize.height = height;
        return mediaSize;
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     */
    private BitmapDrawable getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    /**
     * 往LruCache中添加一张图片
     * @param key            key
     * @param bitmapDrawable bitmapDrawable
     */
    private void addBitmapToLruCache(String key, BitmapDrawable bitmapDrawable) {
        if (getBitmapFromLruCache(key) == null) {
            if (bitmapDrawable != null) {
                mLruCache.put(key, bitmapDrawable);
            }
        }
    }

    /**
     * 生成图片
     * @param imageView imageView
     * @param filePath  filePath
     * @param holder    holder
     * @return bitmap
     */
    private Bitmap buildBitmap(ImageView imageView, String filePath, ImgBeanHolder holder) {
        MediaSize mediaSize;
        Bitmap source = null;
        int reqWidth, reqHeight;

        mediaSize = getImageViewWidth(imageView);
        reqWidth = mediaSize.width;
        reqHeight = mediaSize.height;
        switch (mediaSize.type) {
            case Constant.TYPE_AUDIO:
                try {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    byte[] artwork = null;
                    if (URLUtil.isNetworkUrl(filePath)) {
                        media.setDataSource(filePath, new HashMap<String, String>());
                        artwork = media.getEmbeddedPicture();
                    } else {
                        String absolutePath = null;
                        if (URLUtil.isContentUrl(filePath)) {
                            if (isAlive()) {
                                absolutePath = FileUtil.getFileAbsolutePath(contextReference.get(), Uri.parse(filePath));
                            }
                        } else {
                            absolutePath = filePath;
                        }
                        if (absolutePath != null && new File(absolutePath).exists()) {
                            if (isAlive()) {
                                String thumbnail = FileUtil.getAudioThumbnail(contextReference.get(), filePath);
                                if (!TextUtils.isEmpty(thumbnail)) {
                                    source = getBitmapFromPath(thumbnail, reqWidth, reqHeight);
                                } else {
                                    media.setDataSource(filePath);
                                    artwork = media.getEmbeddedPicture();
                                }
                            }
                        }
                    }
                    if (source == null) {
                        if (artwork != null) {
                            source = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
                        } else {
                            // default audio image
                        }
                    }
                } catch (Exception ignore) {
                }
                break;
            case Constant.TYPE_VIDEO:
                try {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    if (URLUtil.isNetworkUrl(filePath)) {
                        media.setDataSource(filePath, new HashMap<String, String>());
                        source = media.getFrameAtTime();
                    } else {
                        String absolutePath = null;
                        if (URLUtil.isContentUrl(filePath)) {
                            if (isAlive()) {
                                absolutePath = FileUtil.getFileAbsolutePath(contextReference.get(), Uri.parse(filePath));
                            }
                        } else {
                            absolutePath = filePath;
                        }
                        if (absolutePath != null && new File(absolutePath).exists()) {
                            if (isAlive()) {
                                String thumbnail = FileUtil.getVideoThumbnail(contextReference.get(), filePath);
                                if (!TextUtils.isEmpty(thumbnail)) {
                                    source = getBitmapFromPath(thumbnail, reqWidth, reqHeight);
                                } else {
                                    media.setDataSource(filePath);
                                    source = media.getFrameAtTime();
                                }
                            }
                            if (source == null) {
                                media.setDataSource(filePath);
                                source = media.getFrameAtTime();
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
                break;
            case Constant.TYPE_IMAGE:
                source = getBitmapFromPath(filePath, reqWidth, reqHeight);
                break;
        }
        if (source == null) {
            // 图片获取失败，使用默认图片
            source = buildErrorBitmap(reqWidth, reqHeight);
        }
        return BitmapUtil.scaleBitmap(reqWidth, reqHeight, filePath, source, mediaSize.type == Constant.TYPE_IMAGE);
    }

    /**
     * 根据宽高从文件中获取bitmap
     * @param filePath  文件路径
     * @param reqWidth  需要的宽
     * @param reqHeight 需要的高
     * @return bitmap
     */
    private Bitmap getBitmapFromPath(String filePath, int reqWidth, int reqHeight) {
        if (TextUtils.isEmpty(filePath)) return null;
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int widthInSize = 1, heightInSize = 1;

        while (width > reqWidth) {
            width /= 2;
            if (width > reqWidth) {
                widthInSize *= 2;
            }
        }
        while (height > reqHeight) {
            height /= 2;
            if (height > reqHeight) {
                heightInSize *= 2;
            }
        }
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = Math.min(widthInSize, heightInSize);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * buildError bitmap
     * @param reqWidth  reqWidth
     * @param reqHeight reqHeight
     * @return w h
     */
    private Bitmap buildErrorBitmap(int reqWidth, int reqHeight) {
        if (contextReference.get() == null) return null;
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(contextReference.get().getResources(), errorResId, options);
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int widthInSize = 1, heightInSize = 1;

        while (width > reqWidth) {
            width /= 2;
            if (width > reqWidth) {
                widthInSize *= 2;
            }
        }
        while (height > reqHeight) {
            height /= 2;
            if (height > reqHeight) {
                heightInSize *= 2;
            }
        }
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = Math.min(widthInSize, heightInSize);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(contextReference.get().getResources(), errorResId, options);
    }

    private class ImgBeanHolder {
        BitmapDrawable drawable;
        ImageView imageView;
        String path;
    }

    /**
     * 反射获得ImageView设置的最大宽度和高度
     * @param imageView object
     * @param fieldName fieldName
     * @return maxHeight/maxWidth
     */
    private int getImageViewFieldValue(ImageView imageView, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(imageView);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            LogUtil.e(e);
        }
        return value;
    }

    /**
     * is alive
     * @return is alive
     */
    private boolean isAlive() {
        return contextReference != null && contextReference.get() != null;
    }

    @Override
    public void onLowMemory() {
        clearAll();
    }

    @Override
    public void onTrimMemory(int level) {
        if (level > ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            clearAll();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }
}

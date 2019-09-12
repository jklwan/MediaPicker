package com.chends.media.picker.sample.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * @author chends create on 2019/9/9.
 */
public class FileUtil {
    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     * @param context context
     * @param uri     uri
     */
    public static String getFileAbsolutePath(Context context, Uri uri) {
        if (context == null || uri == null) return null;
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if ("home".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/documents/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                if (TextUtils.isEmpty(id)) {
                    return null;
                }
                if (id.startsWith("raw:")) {
                    return id.substring(4);
                }
                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                };
                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    try {
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception ignore) {
                    }
                }
                try {
                    String path = getDataColumn(context, uri, null, null);
                    if (path != null) {
                        return path;
                    }
                } catch (Exception ignore) {
                }
                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                return null;
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri;
                switch (type.toLowerCase(Locale.ENGLISH)) {
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                    default:
                        contentUri = MediaStore.Files.getContentUri("external");
                        break;
                }
                final String selection = MediaStore.MediaColumns._ID + "=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            // File
            return uri.getPath();
        }
        return null;
    }

    /**
     * 通过游标获取当前文件路径
     * @param context       context
     * @param uri           uri
     * @param selection     selection
     * @param selectionArgs selectionArgs
     * @return 路径，未找到返回null
     */
    public static String getDataColumn(Context context, @NonNull Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception ignore) {
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos on android M.
     */
    public static boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    /**
     * 从数据库查询视频缩略图
     * @param context   context
     * @param videoPath path
     * @return bitmap
     */
    @Nullable
    public static String getVideoThumbnail(Context context, String videoPath) {
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] videoColumns = {MediaStore.Video.Media._ID};
        String[] vColumns = {MediaStore.Video.Thumbnails.DATA};

        Cursor cursor = null, vCursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoColumns, MediaStore.Video.Media.DATA + "=?",
                    new String[]{videoPath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                vCursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        vColumns, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id,
                        null, null);
                if (vCursor != null && vCursor.moveToFirst()) {
                    return vCursor.getString(vCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                }
            }
        } catch (Exception e) {
            LogUtil.e(e);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (vCursor != null) {
                    vCursor.close();
                }
            } catch (Exception e) {
                LogUtil.e(e);
            }
        }
        return null;
    }

    /**
     * 从数据库查询音频专辑封面
     * @param context   context
     * @param audioPath path
     * @return bitmap
     */
    @Nullable
    public static String getAudioThumbnail(Context context, String audioPath) {
        String[] audioColumns = {MediaStore.Audio.Media.ALBUM_ID};
        String[] tColumns = {MediaStore.Audio.Albums.ALBUM_ART};

        Cursor cursor = null, tCursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    audioColumns, MediaStore.Audio.Media.DATA + "=?",
                    new String[]{audioPath}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                tCursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        tColumns, MediaStore.Audio.Albums._ID + "=" + albumId,
                        null, null);
                if (tCursor != null && tCursor.getCount() > 0 && tCursor.moveToFirst()) {
                    return tCursor.getString(tCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                }
            }
        } catch (Exception e) {
            LogUtil.e(e);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (tCursor != null) {
                    tCursor.close();
                }
            } catch (Exception e) {
                LogUtil.e(e);
            }
        }
        return null;
    }

    private static int maxTextureSize = -1;

    /**
     * 最大可显示图片宽高
     * @return size
     */
    public static int getMaxTextureSize() {
        if (maxTextureSize <= 0) {
            // Safe minimum default size
            final int IMAGE_MAX_BITMAP_DIMENSION = 1000;

            // Get EGL Display
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            // Initialise
            int[] version = new int[2];
            egl.eglInitialize(display, version);

            // Query total number of configurations
            int[] totalConfigurations = new int[1];
            egl.eglGetConfigs(display, null, 0, totalConfigurations);

            // Query actual list configurations
            EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
            egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

            int[] textureSize = new int[1];
            int maximumTextureSize = 0;

            // Iterate through all the configurations to located the maximum texture size
            for (int i = 0; i < totalConfigurations[0]; i++) {
                // Only need to check for width since opengl textures are always squared
                egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

                // Keep track of the maximum texture size
                if (maximumTextureSize < textureSize[0])
                    maximumTextureSize = textureSize[0];
            }

            // Release
            egl.eglTerminate(display);

            // Return largest texture size found, or default
            maxTextureSize = Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
        }
        return maxTextureSize;
    }
}

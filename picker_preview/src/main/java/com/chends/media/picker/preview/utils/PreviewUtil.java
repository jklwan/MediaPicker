package com.chends.media.picker.preview.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

/**
 * @author chends create on 2019/9/12.
 */
public class PreviewUtil {

    /**
     *
     */
    public void loadImage() {

    }

    /**
     * 获取文件全名称
     * @param path path
     * @return name
     */
    public static String getFileName(String path) {
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

    /**
     * 打开文件
     * @param context  context
     * @param path     path
     * @param mimeType 类型
     */
    public static void openFile(Context context, String path, String mimeType) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri fileUri;
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".picker_fileprovider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                fileUri = Uri.fromFile(file);
            }
            intent.setDataAndType(fileUri, mimeType);
            context.startActivity(intent);
        } catch (Exception ignore) {
        }
    }
}

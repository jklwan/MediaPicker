package com.chends.media.picker.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chends.media.picker.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * 预览页
 * @author chends create on 2019/9/6.
 */
public class PreviewFragment extends Fragment {
    private static final String BUNDLE_PATH = "path";

    private String path;

    public static PreviewFragment newInstance(String path) {
        PreviewFragment result = new PreviewFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_PATH, path);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(BUNDLE_PATH)) {
            path = args.getString(BUNDLE_PATH);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_picker_preview, container, false);

        if (savedInstanceState != null) {
            if (TextUtils.isEmpty(path) && savedInstanceState.containsKey(BUNDLE_PATH)) {
                path = savedInstanceState.getString(BUNDLE_PATH);
            }
        }
        if (!TextUtils.isEmpty(path)) {
            SubsamplingScaleImageView imageView = rootView.findViewById(R.id.imageView);
            imageView.setImage(ImageSource.uri(path));
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        View rootView = getView();
        if (rootView != null) {
            outState.putString(BUNDLE_PATH, path);
        }
    }
}

package com.chends.media.picker.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chends.media.picker.MediaPicker;
import com.chends.media.picker.MimeType;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private final int imageCode = 1, videoCode = 2, allCode = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void xmlClick(View view) {
        switch (view.getId()) {
            case R.id.chooseImage:
                MediaPicker.with(this)
                        .addTypes(MimeType.allImage())
                        .maxNum(8)
                        .start(imageCode);
                break;
            case R.id.chooseVideo:
                MediaPicker.with(this)
                        .addTypes(MimeType.allVideo())
                        .maxNum(3)
                        .start(videoCode);
                break;
            case R.id.chooseIVideo:
                MediaPicker.with(this)
                        .addTypes(MimeType.allImage())
                        .addTypes(MimeType.allVideo())
                        .maxNum(5)
                        .start(videoCode);
                break;
            case R.id.chooseIAudio:
                MediaPicker.with(this)
                        .addTypes(MimeType.allImage())
                        .addTypes(allAudio())
                        .maxNum(8)
                        .start(videoCode);
                break;
            case R.id.chooseAVideo:
                MediaPicker.with(this)
                        .addTypes(MimeType.allVideo())
                        .addTypes(allAudio())
                        .maxNum(3)
                        .start(videoCode);
                break;
            case R.id.chooseAll:
                MediaPicker.with(this)
                        .addTypes(MimeType.all())
                        .maxNum(9)
                        .start(allCode);
                break;
        }
    }

    private Set<String> allAudio(){
        return new HashSet<String>(){{
            add("audio/mpeg");
            add("audio/ogg");
            add("audio/aac");
        }};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case imageCode:
                break;
            case videoCode:
                break;
            case allCode:
                break;
        }
    }
}

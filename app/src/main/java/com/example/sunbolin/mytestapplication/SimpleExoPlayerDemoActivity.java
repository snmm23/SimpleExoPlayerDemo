package com.example.sunbolin.mytestapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import sun.bo.lin.exoplayer.ui.ExoPlayerLayout;
import sun.bo.lin.exoplayer.ui.ExoPlayerListener;

public class SimpleExoPlayerDemoActivity extends AppCompatActivity implements ExoPlayerListener {

    private ExoPlayerLayout exoPlayerLayout;

    private String videoUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_exo_player_demo);

        videoUrl = "http://phwoanpja.bkt.clouddn.com/2ff13eca058a12a3ad2c3f93406a03a2.mp4";

        exoPlayerLayout = findViewById(R.id.exoPlayerLayout);
        exoPlayerLayout.hideTitleView();
        exoPlayerLayout.setAlwaysShowController(false);
        exoPlayerLayout.setExoPlayerListener(this);
        exoPlayerLayout.setFullscreenButton(false);

        exoPlayerLayout.play(videoUrl, 0,false, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayerLayout.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        exoPlayerLayout.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayerLayout.onStop();
    }

    @Override
    protected void onDestroy() {
        exoPlayerLayout.releasePlayer();
        super.onDestroy();
    }


    @Override
    public void doHorizontalScreen() {
    }

    @Override
    public void doVerticalScreen() {
    }

    @Override
    public void setViewHistory(long longTime) {

    }

    @Override
    public void goBack(boolean isSure) {

    }

    @Override
    public void playerStart() {

    }

    @Override
    public void playerEnd() {

    }

    @Override
    public void playerError() {

    }

    @Override
    public void playerOnTouch() {

    }
}

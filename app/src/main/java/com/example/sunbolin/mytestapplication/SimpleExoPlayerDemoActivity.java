package com.example.sunbolin.mytestapplication;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

import sun.bo.lin.exoplayer.all.AllExoPlayerLayout;
import sun.bo.lin.exoplayer.all.AllExoPlayerListener;
import sun.bo.lin.exoplayer.all.StreamGroup;

public class SimpleExoPlayerDemoActivity extends AppCompatActivity implements AllExoPlayerListener {

    private AllExoPlayerLayout exoPlayerLayout;
    private View other;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_exo_player_demo);

        other = findViewById(R.id.other);
        exoPlayerLayout = findViewById(R.id.exoPlayerLayout);

        ArrayList<StreamGroup> streamGroups = new ArrayList<>();
        streamGroups.add(new StreamGroup("标准", "http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4"));

        exoPlayerLayout.setLaunchDate(this, this, "title", 0, streamGroups);
        exoPlayerLayout.play();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //do something
        int mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            other.setVisibility(View.VISIBLE);
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            other.setVisibility(View.GONE);
        }
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
    protected void onDestroy() {
        exoPlayerLayout.onDestroy();
        super.onDestroy();
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
}

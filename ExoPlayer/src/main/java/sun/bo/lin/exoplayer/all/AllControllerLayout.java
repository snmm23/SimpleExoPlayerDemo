package sun.bo.lin.exoplayer.all;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import sun.bo.lin.exoplayer.R;

/**
 * Created by sunbolin on 16/4/18.
 */
public class AllControllerLayout extends FrameLayout implements View.OnClickListener, OnSeekBarChangeListener {

    private final String TAG = "sunbolin ControllerLay";

    private AllControllerListener controllerListener;
    private ViewGroup mAnchor;
    private View mRoot;

    private LinearLayout titleLayout;
    private ImageView back;
    private TextView title;
    private ImageView voice;
    private TextView stream;
    //占位作用
    private ImageView voiceSize;
    private TextView streamSize;

    private ImageView mFullscreenLand;

    private SeekBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;
    private ImageView mPauseButton;
    private Handler mHandler;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private boolean isShowing = false;
    private boolean isFinish = false;
    private boolean isError = false;

    public AllControllerLayout(Context context) {
        super(context);
        mHandler = new MessageHandler(this);
        mRoot = null;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null) {
            initControllerView(mRoot);
        }
    }

    private View makeControllerView() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.all_exo_player_media_controller, this, false);
        initControllerView(mRoot);
        return mRoot;
    }

    @SuppressLint({"WrongViewCast"})
    private void initControllerView(View v) {
        titleLayout = v.findViewById(R.id.titleLayout);
        back = v.findViewById(R.id.back);
        title = v.findViewById(R.id.title);
        voice = v.findViewById(R.id.voice);
        stream = v.findViewById(R.id.stream);
        voiceSize = v.findViewById(R.id.voiceSize);
        streamSize = v.findViewById(R.id.streamSize);

        mFullscreenLand = v.findViewById(R.id.fullscreen_land);

        back.setOnClickListener(this);
        voice.setOnClickListener(this);
        stream.setOnClickListener(this);
        mFullscreenLand.setOnClickListener(this);

        mPauseButton = v.findViewById(R.id.pause);
        mPauseButton.setOnClickListener(this);
        mProgress = v.findViewById(R.id.mediacontroller_progress);
        mProgress.setOnSeekBarChangeListener(this);
        mProgress.setMax(1000);
        mEndTime = v.findViewById(R.id.time);
        mCurrentTime = v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        updateConfigurationViews();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateConfigurationViews();
    }

    private void updateConfigurationViews(){
        if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            titleLayout.setVisibility(View.GONE);
            voice.setVisibility(View.GONE);
            voiceSize.setVisibility(View.INVISIBLE);
            stream.setVisibility(View.GONE);
            streamSize.setVisibility(View.INVISIBLE);
            mFullscreenLand.setVisibility(VISIBLE);
        }else{
            titleLayout.setVisibility(View.VISIBLE);
            voiceSize.setVisibility(View.GONE);
            voice.setVisibility(View.VISIBLE);
            streamSize.setVisibility(View.GONE);
            stream.setVisibility(View.VISIBLE);
            mFullscreenLand.setVisibility(GONE);
        }
    }

    private String stringForTime(long timeMs) {
        int totalSeconds = (int) timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        return hours > 0 ?
                mFormatter.format("%02d:%02d:%02d", new Object[]{hours, minutes, seconds}).toString() :
                mFormatter.format("%02d:%02d", new Object[]{minutes, seconds}).toString();
    }

    private long setProgress() {
        if (controllerListener != null) {
            long position = controllerListener.getCurrentPosition();
            long duration = controllerListener.getDuration();
            if (mProgress != null) {
                if (duration > 0) {
                    if (!mProgress.isEnabled()) {
                        mProgress.setEnabled(true);
                    }
                    long percent = 1000L * position / duration;
                    mProgress.setProgress((int) percent);
                }
                int percent1 = controllerListener.getBufferPercentage();
                mProgress.setSecondaryProgress(percent1 * 10);
            }
            if (mEndTime != null) {
                mEndTime.setText(stringForTime(duration));
            }
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime(position));
                Log.e(TAG, "currentPlayTime = " + stringForTime(position));
            }
            return position;
        } else {
            return 0;
        }
    }

    private void updatePausePlay() {
        if (mRoot != null && mPauseButton != null && controllerListener != null) {
            if (isFinish) {
                mPauseButton.setImageResource(R.drawable.zanting);
            } else if (controllerListener.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.bofang);
            } else {
                mPauseButton.setImageResource(R.drawable.zanting);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.pause) {
            if(!isError){
                if (isFinish) {
                    doRestart();
                } else {
                    doPauseResume();
                }
            }
            show(AllExoPlayerLayout.hideTime);
        } else if (i == R.id.fullscreen_land) {
            doHorizontalScreen();
            show(AllExoPlayerLayout.hideTime);
        } else if (i == R.id.back) {
            doVerticalScreen();
            show(AllExoPlayerLayout.hideTime);
        } else if (i == R.id.voice){

        } else if (i == R.id.stream){
            controllerListener.showStreamDialog();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            long duration = controllerListener.getDuration();
            long newPosition = duration * (long) progress / 1000L;
            controllerListener.seekTo((int) newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newPosition));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        updatePausePlay();
        show(AllExoPlayerLayout.hideTime);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private class MessageHandler extends Handler {
        private final WeakReference<AllControllerLayout> mView;

        MessageHandler(AllControllerLayout view) {
            mView = new WeakReference(view);
        }

        public void handleMessage(Message msg) {
            AllControllerLayout view = mView.get();
            if (view != null && view.controllerListener != null) {
                switch (msg.what) {
                    case 1:
                        view.hide();
                        break;
                    case 2:
                        Log.e(TAG, "handleMessage isPlaying = " + view.controllerListener.isPlaying() + ", handleMessage isFinish = " + view.isFinish);
                        if (!view.isFinish && !isError)
                            sendMessageDelayed(obtainMessage(2), (1000 - view.setProgress() % 1000));
                        break;
                }
            }
        }
    }

    private void doPauseResume() {
        if (controllerListener != null) {
            if (controllerListener.isPlaying()) {
                controllerListener.pause();
            } else {
                controllerListener.start();
            }
        }
        updatePausePlay();
    }

    private void doRestart() {
        if (controllerListener != null) {
            Log.e(TAG, "doRestart()");
            controllerListener.restart();
            updatePausePlay();
        }
    }

    private void doHorizontalScreen() {
        if (controllerListener != null) {
            controllerListener.doHorizontalScreen();
            updatePausePlay();
        }
    }

    private void doVerticalScreen() {
        if (controllerListener != null) {
            controllerListener.doVerticalScreen();
            updatePausePlay();
        }
    }

    private void showControllerAnimation() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
        objectAnimator.setDuration(400);
        objectAnimator.start();
    }

    private void hideControllerAnimation(Animator.AnimatorListener animationListener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        objectAnimator.setDuration(400);
        objectAnimator.addListener(animationListener);
        objectAnimator.start();
    }

    public void setupListener(AllControllerListener listener) {
        controllerListener = listener;
        updatePausePlay();
    }

    public void setAnchorView(ViewGroup view) {
        mAnchor = view;
        LayoutParams frameParams = new LayoutParams(-1, -1);
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    public void setFinish(boolean flag) {
        Log.e(TAG, "setFinish , isFinish = " + flag);
        isFinish = flag;
        mHandler.removeMessages(2);
        if (!isFinish) {
            mHandler.sendEmptyMessage(2);
        } else {
            mProgress.setProgress(1000);
        }
        updatePausePlay();
    }

    public void setError(boolean flag) {
        isError = flag;
        mProgress.setEnabled(!isError);
        updatePausePlay();
    }


    public void showTitleName(String titleName){
        title.setText(titleName);
    }

    public void showStreamName(String streamName){
        stream.setText(streamName);
    }

    public void show(int timeout) {
        if(isError){
            return;
        }
        Log.e(TAG, "show()");
        if (mAnchor != null && !isShowing) {
            mAnchor.addView(AllControllerLayout.this, new LayoutParams(-1, -2, 80));
            isShowing = true;
            try {
                showControllerAnimation();
            } catch (Exception ignored) {
            }
        }

        updateConfigurationViews();
        updatePausePlay();

        mHandler.removeMessages(1);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), (long) timeout);
    }


    public void hide() {
        Log.e(TAG, "hide()");
        try {
            hideControllerAnimation(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    removeView();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    removeView();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } catch (Exception ignored) {
            removeView();
        }
    }

    private void removeView(){
        if (mAnchor != null) {
            mAnchor.removeView(AllControllerLayout.this);
        }
        isShowing = false;
    }

    public void releaseController() {
        if (mHandler != null) {
            mHandler.removeMessages(1);
            mHandler.removeMessages(2);
        }
        if (mAnchor != null) {
            mAnchor.removeView(AllControllerLayout.this);
        }
        isShowing = false;
        isFinish = false;
        isError = false;
    }

    public void finishController() {
        releaseController();
        controllerListener = null;
        mHandler = null;
        mFormatBuilder = null;
        mFormatter = null;
        mAnchor = null;
        System.gc();
    }

    public boolean isShowing() {
        return isShowing;
    }
}
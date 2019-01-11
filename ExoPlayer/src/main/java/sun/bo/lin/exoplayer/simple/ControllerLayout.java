package sun.bo.lin.exoplayer.simple;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
public class ControllerLayout extends FrameLayout implements View.OnClickListener, OnSeekBarChangeListener {

    private final String TAG = "sunbolin ControllerLay";

    private ControllerListener controllerListener;
    private ViewGroup mAnchor;
    private View mRoot;

    private LinearLayout titleLayout;
    private ImageView back;
    private TextView sure;

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

    private boolean alwaysShowController = false;

    public ControllerLayout(Context context) {
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
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.exo_player_media_controller, this, false);
        initControllerView(mRoot);
        return mRoot;
    }

    public void hideTitleView(){
        titleLayout.setVisibility(GONE);
    }

    @SuppressLint({"WrongViewCast"})
    private void initControllerView(View v) {
        titleLayout = v.findViewById(R.id.title_rl);
        back = v.findViewById(R.id.back);
        back.setOnClickListener(this);
        sure = v.findViewById(R.id.sure);
        sure.setOnClickListener(this);

        mPauseButton = v.findViewById(R.id.pause);
        mPauseButton.setOnClickListener(this);
        mProgress = v.findViewById(R.id.mediacontroller_progress);
        mProgress.setOnSeekBarChangeListener(this);
        mProgress.setMax(1000);
        mEndTime = v.findViewById(R.id.time);
        mCurrentTime = v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
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
                mPauseButton.setImageResource(R.drawable.exo_player_btn_stop);
            } else if (controllerListener.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.exo_player_btn_bofang);
            } else {
                mPauseButton.setImageResource(R.drawable.exo_player_btn_stop);
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
            show(ExoPlayerLayout.hideTime);
        } else if (i == R.id.back) {
            if (controllerListener != null)
                controllerListener.goBack(false);
        } else if (i == R.id.sure) {
            if (controllerListener != null)
                controllerListener.goBack(true);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            long duration = controllerListener.getDuration();
            long newposition = duration * (long) progress / 1000L;
            controllerListener.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        updatePausePlay();
        show(ExoPlayerLayout.hideTime);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private class MessageHandler extends Handler {
        private final WeakReference<ControllerLayout> mView;

        MessageHandler(ControllerLayout view) {
            mView = new WeakReference(view);
        }

        public void handleMessage(Message msg) {
            ControllerLayout view = mView.get();
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

    public void setupListener(ControllerListener listener) {
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

    public void show(int timeout) {
        if(isError){
            return;
        }
        Log.e(TAG, "show()");
        if (mAnchor != null && !isShowing) {
            mAnchor.addView(ControllerLayout.this, new LayoutParams(-1, -2, 80));
            isShowing = true;
            try {
                showControllerAnimation();
            } catch (Exception ignored) {
            }
        }
        updatePausePlay();
        mHandler.removeMessages(1);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), (long) timeout);
    }

    public void hide() {
        if(alwaysShowController && !isError){
            return;
        }
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
            mAnchor.removeView(ControllerLayout.this);
        }
        isShowing = false;
    }

    public void showSureBtn() {
        sure.setVisibility(View.VISIBLE);
    }

    public void releaseController() {
        if (mHandler != null) {
            mHandler.removeMessages(1);
            mHandler.removeMessages(2);
        }
        if (mAnchor != null) {
            mAnchor.removeView(ControllerLayout.this);
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

    public void setAlwaysShowController(boolean alwaysShowController){
        this.alwaysShowController = alwaysShowController;
    }
}
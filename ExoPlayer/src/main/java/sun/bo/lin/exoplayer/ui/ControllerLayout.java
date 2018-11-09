package sun.bo.lin.exoplayer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private RelativeLayout title_rl;
    private TextView sure;
    private SeekBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;
    private ImageView mPauseButton;
    private ImageView mFullscreenButton;
    private Handler mHandler;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private AlphaAnimation showControllerAnimation;
    private AlphaAnimation hideControllerAnimation;
    private boolean isShowing = false;
//    private boolean haveHideAbility = true;
//    private boolean haveShowAbility = true;
    private boolean isHideAnimation = false;
    private boolean isFinish = false;
    private boolean isError = false;
    private boolean isHorizontal = false;
    private boolean isPause = false;

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
        title_rl.setVisibility(GONE);
    }

    @SuppressLint({"WrongViewCast"})
    private void initControllerView(View v) {
        title_rl = v.findViewById(R.id.title_rl);
        ImageView back = v.findViewById(R.id.back);
        back.setOnClickListener(this);
        sure = v.findViewById(R.id.sure);
        sure.setOnClickListener(this);
        mPauseButton = v.findViewById(R.id.pause);
        mPauseButton.setOnClickListener(this);
        mFullscreenButton = v.findViewById(R.id.fullscreen);
        mFullscreenButton.setOnClickListener(this);
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

    public void setControllerProgress(long position, long duration){
        if(position != 0 && duration !=0){
            long percent = 1000L * position / duration;
            mProgress.setProgress((int) percent);
        }
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
//                else{
//                    mProgress.setProgress(0);
//                    mProgress.setEnabled(false);
//                }
                int percent1 = controllerListener.getBufferPercentage();
                mProgress.setSecondaryProgress(percent1 * 10);
            }
            if (mEndTime != null) {
                mEndTime.setText(stringForTime(duration));
            }
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime(position));
                controllerListener.setCurrentPlayTime(stringForTime(position));
                Log.e(TAG, "currentPlayTime = " + stringForTime(position));
            }
            return position;
        } else {
            return 0;
        }
    }

    private void updatePausePlay() {
        if (mRoot != null && mPauseButton != null && controllerListener != null) {
            if (isFinish || isError) {
                mPauseButton.setImageResource(R.drawable.exo_player_btn_chongbo);
            } else if (controllerListener.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.exo_player_btn_bofang);
            } else {
                mPauseButton.setImageResource(R.drawable.exo_player_btn_stop);
            }
        }
    }

    private void updateFullScreen() {
        if (mRoot != null && mFullscreenButton != null) {
            if (isHorizontal) {
                mFullscreenButton.setImageResource(R.drawable.exo_player_btn_suoxiao_white);
            } else {
                mFullscreenButton.setImageResource(R.drawable.exo_player_btn_fanda_white);
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
        } else if (i == R.id.fullscreen) {
            if (isHorizontal) {
                doVerticalScreen();
            } else {
                doHorizontalScreen();
            }
            show(ExoPlayerLayout.hideTime);
        }else if(i == R.id.back){
            if (controllerListener != null)
                controllerListener.goBack(false);
        }else if(i == R.id.sure){
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

    private void doHorizontalScreen() {
        if (controllerListener != null) {
            controllerListener.doHorizontalScreen();
            isHorizontal = true;
            updateFullScreen();
            updatePausePlay();
        }
    }

    private void doVerticalScreen() {
        if (controllerListener != null) {
            controllerListener.doVerticalScreen();
            isHorizontal = false;
            updateFullScreen();
            updatePausePlay();
        }
    }

    private void showControllerAnimation() {
        showControllerAnimation = new AlphaAnimation(0f, 1f);
        showControllerAnimation.setDuration(400);
        showControllerAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showControllerAnimation = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(showControllerAnimation);
    }

    private void hideControllerAnimation(Animation.AnimationListener animationListener) {
        hideControllerAnimation = new AlphaAnimation(1f, 0f);
        hideControllerAnimation.setDuration(400);
        hideControllerAnimation.setAnimationListener(animationListener);
        startAnimation(hideControllerAnimation);
    }

    public void setupListener(ControllerListener listener) {
        controllerListener = listener;
        updatePausePlay();
        updateFullScreen();
    }

    public void setAnchorView(ViewGroup view) {
        mAnchor = view;
        LayoutParams frameParams = new LayoutParams(-1, -1);
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }
//
//    public void haveHideAbility(boolean haveHide) {
//        haveHideAbility = haveHide;
//    }
//
//    public void haveShowAbility(boolean haveHide) {
//        haveShowAbility = haveHide;
//    }

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


//    public void gonePauseView() {
//        mPauseButton.setVisibility(INVISIBLE);
//    }
//
//    public void visiblePauseView() {
//        mPauseButton.setVisibility(VISIBLE);
//        updatePausePlay();
//    }

    public void show(int timeout) {
        if ( !isHideAnimation) {//haveShowAbility &&
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
            updateFullScreen();
            mHandler.removeMessages(1);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(1), (long) timeout);
        }
    }

    public void setPause(boolean flag){
        this.isPause = flag;
    }

    public void hide() {
        if(alwaysShowController){
            return;
        }
        if (!isHideAnimation) {//haveHideAbility &&
            Log.e(TAG, "hide()");
            if(isPause){
                if (mAnchor != null) {
                    mAnchor.removeView(ControllerLayout.this);
                }
                isShowing = false;
                isHideAnimation = false;
                updateFullScreen();
                hideControllerAnimation = null;
                return;
            }
            try {
                hideControllerAnimation(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        isHideAnimation = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (mAnchor != null) {
                            mAnchor.removeView(ControllerLayout.this);
                        }
                        isShowing = false;
                        isHideAnimation = false;
                        updateFullScreen();
                        hideControllerAnimation = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }

    public void updateDirection(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
        updateFullScreen();
        updatePausePlay();
    }

    public void replayVideo() {
        doRestart();
        show(ExoPlayerLayout.hideTime);
    }

    public void setFullscreenButton(boolean isShow) {
        mFullscreenButton.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void showSureBtn() {
        sure.setVisibility(View.VISIBLE);
    }

    public void releaseController() {
        if (mHandler != null) {
            mHandler.removeMessages(1);
            mHandler.removeMessages(2);
        }
        if (showControllerAnimation != null) {
            showControllerAnimation.cancel();
            showControllerAnimation = null;
        }
        if (hideControllerAnimation != null) {
            hideControllerAnimation.cancel();
            hideControllerAnimation = null;
        }
        if (mAnchor != null) {
            mAnchor.removeView(ControllerLayout.this);
            updateFullScreen();
        }
//        haveHideAbility = true;
//        haveShowAbility = true;
        isHideAnimation = false;
        isShowing = false;
        isFinish = false;
        isError = false;
        isHorizontal = false;
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
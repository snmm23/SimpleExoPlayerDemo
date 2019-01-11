package sun.bo.lin.exoplayer.all;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sun.bo.lin.exoplayer.R;
import sun.bo.lin.exoplayer.base.BasePlayerLayout;
import sun.bo.lin.exoplayer.util.CommonUtils;

/**
 *
 * Created by sunbolin on 16/4/18.
 */
public class AllExoPlayerLayout extends BasePlayerLayout implements View.OnTouchListener, OnClickListener, AllControllerListener, StreamSelectDialog.StreamSelectListener {

    private final String TAG = "sunbolin ExoPlayer";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static int hideTime = 5000;

    private PlayerView playerView;
    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    private AllControllerLayout mediaController;
    private FrameLayout player_loading;
    private Button player_error;
    private AllExoPlayerListener exoPlayerListener;
    private StreamSelectDialog streamSelectDialog;

    private Activity activity;
    private String titleName;
    private int selectStreamIndex = 0;
    private ArrayList<StreamGroup> streamGroups = null;
    private int startWindow;
    private long startPosition;
    private boolean isError = false;
    private boolean isPause = false;


    public AllExoPlayerLayout(Context context) {
        this(context, null);
    }

    public AllExoPlayerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllExoPlayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExoPlayerLayout);
            try {
                int resizeMode = a.getInt(R.styleable.ExoPlayerLayout_resize_mode, AspectRatioFrameLayout.RESIZE_MODE_FIT);
                playerView.setResizeMode(resizeMode);
            } finally {
                a.recycle();
            }
        }
    }

    private void initializePlayer(boolean shouldAutoPlay, int streamIndex) {
        if(streamIndex < 0 || streamIndex > streamGroups.size()){
            streamIndex = 0;
        }

        if (player == null) {
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenders()
                            ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            DefaultRenderersFactory rendererFactory =
                    new DefaultRenderersFactory(getContext(), extensionRendererMode);
            player = ExoPlayerFactory.newSimpleInstance(getContext(), rendererFactory, trackSelector);
            player.addListener(new PlayerEventListener());
            player.addAnalyticsListener(new EventLogger(trackSelector));
            mediaController.setupListener(this);
            mediaController.setEnabled(true);
            playerView.setPlayer(player);
        }

        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition);
        }
        player.setPlayWhenReady(shouldAutoPlay);
        player.prepare(buildMediaSource(Uri.parse(streamGroups.get(streamIndex).getUrl()), ""), !haveStartPosition, false);

        mediaController.show(hideTime);
        mediaController.showTitleName(titleName);
        mediaController.showStreamName(streamGroups.get(streamIndex).getName());
    }

    @SuppressWarnings("unchecked")
    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<StreamKey>) getOfflineStreamKeys()))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys()))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParserFactory(
                                new DefaultHlsPlaylistParserFactory(
                                        (List<StreamKey>) getOfflineStreamKeys()
                                ))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private List<?> getOfflineStreamKeys() {
        return Collections.emptyList();
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return CommonUtils.getInstance(getContext()).buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private boolean useExtensionRenders() {
        return false;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.all_exo_player_video_root, this);
        playerView = findViewById(R.id.player_view);
        player_loading = findViewById(R.id.player_loading);
        player_error = findViewById(R.id.player_error);
        mediaController = new AllControllerLayout(getContext());
        FrameLayout root = findViewById(R.id.root);
        mediaController.setAnchorView(root);
        mediaController.setOnTouchListener(this);
        root.setOnTouchListener(this);
        player_error.setOnClickListener(this);
        mediaDataSourceFactory = buildDataSourceFactory(true);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.player_error) {
            player_loading.setVisibility(VISIBLE);
            player_error.setVisibility(INVISIBLE);
            initializePlayer(false, selectStreamIndex);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                try {
                    if (mediaController.isShowing()) {
                        mediaController.hide();
                    } else {
                        mediaController.show(hideTime);
                    }
                } catch (NullPointerException var2) {
                    var2.printStackTrace();
                }
                break;
        }
        return false;
    }

    @Override
    public void start() {
        if (player != null)
            player.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        if (player != null)
            player.setPlayWhenReady(false);
    }

    @Override
    public void restart() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.seekTo(0);
        }
    }

    @Override
    public long getDuration() {
        return player != null ? player.getDuration() == -1L ? 0 : player.getDuration() : 0;
    }

    @Override
    public long getCurrentPosition() {
        return player != null ? (player.getDuration() == -1L ? 0 : player.getCurrentPosition()) : 0;
    }

    @Override
    public void seekTo(long var1) {
        if (player != null)
            player.seekTo((player.getDuration() == -1L ? 0 : Math.min(Math.max(0, var1), getDuration())));
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    @Override
    public int getBufferPercentage() {
        return player != null ? player.getBufferedPercentage() : 0;
    }

    @Override
    public void doHorizontalScreen() {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void doVerticalScreen() {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void showStreamDialog() {
        if(streamSelectDialog != null){
            streamSelectDialog.dismiss();
            streamSelectDialog = null;
        }
        streamSelectDialog = new StreamSelectDialog(activity, selectStreamIndex, streamGroups, this);
        streamSelectDialog.show();
    }

    public void setLaunchDate(Activity activity,  AllExoPlayerListener listener, String titleName, int selectStreamIndex, ArrayList<StreamGroup> streamGroups){
        this.activity = activity;
        this.exoPlayerListener = listener;
        this.titleName = titleName;
        this.selectStreamIndex = selectStreamIndex;
        this.streamGroups = streamGroups;
    }

    public void play() {
        if(streamGroups == null || streamGroups.size() == 0){
            return;
        }
        startPosition = 0;
        isError = false;
        mediaController.setError(isError);
        initializePlayer(true, selectStreamIndex);
    }

    private void release() {
        if (player != null) {
            updateStartPosition();
            player.release();
            player = null;
            trackSelector = null;
        }
        if(streamSelectDialog != null){
            streamSelectDialog.dismiss();
            streamSelectDialog = null;
        }
    }

    private void releasePlayer() {
        isError = false;
        if(streamGroups != null){
            streamGroups.clear();
            streamGroups = null;
        }
        selectStreamIndex = 0;
        if (mediaController != null) {
            mediaController.releaseController();
        }
        player_loading.setVisibility(INVISIBLE);
        player_error.setVisibility(INVISIBLE);

        if (mediaController != null)
            mediaController.finishController();
        exoPlayerListener = null;
        mediaDataSourceFactory = null;
        activity = null;
        clearStartPosition();
        System.gc();
    }


    public void onResume() {
        if (isPause) {
            initializePlayer(false, selectStreamIndex);
        }
        isPause = false;
    }

    public void onPause() {
        release();
        isPause = true;
    }

    public void onDestroy(){
        releasePlayer();
    }

    @Override
    public void onSwitchStream(int selectStreamIndex) {
        this.selectStreamIndex = selectStreamIndex;
        player.prepare(buildMediaSource(Uri.parse(streamGroups.get(selectStreamIndex).getUrl()), ""), false, false);
        mediaController.showStreamName(streamGroups.get(selectStreamIndex).getName());
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.e(TAG, "playWhenReady = " + playWhenReady + ", playbackState = " + playbackState);
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    Log.e("sunbolin", "onPlayerStateChanged = ExoPlayer.STATE_BUFFERING");
                    mediaController.setFinish(false);
                    player_loading.setVisibility(VISIBLE);
                    player_error.setVisibility(INVISIBLE);
                    break;
                case Player.STATE_READY:
                    Log.e("sunbolin", "onPlayerStateChanged = ExoPlayer.STATE_READY");
                    player_loading.setVisibility(INVISIBLE);
                    player_error.setVisibility(INVISIBLE);
                    mediaController.setFinish(false);
                    if (isError) {
                        isError = false;
                        mediaController.setError(isError);
                        mediaController.show(hideTime);
                    }
                    mediaController.show(hideTime);
                    if (exoPlayerListener != null) {
                        exoPlayerListener.playerStart();
                    }
                    break;
                case Player.STATE_ENDED:
                    mediaController.setFinish(true);
                    mediaController.show(hideTime);
                    if (isError) {
                        mediaController.hide();
                    }
                    player_loading.setVisibility(INVISIBLE);
                    player_error.setVisibility(INVISIBLE);
                    if (exoPlayerListener != null) {
                        exoPlayerListener.playerEnd();
                    }
                    break;
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            release();
            player_loading.setVisibility(INVISIBLE);
            player_error.setVisibility(VISIBLE);
            if (!isError) {
                isError = true;
                mediaController.setError(isError);
            }
            mediaController.hide();
            if (exoPlayerListener != null)
                exoPlayerListener.playerError();
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    private void clearStartPosition() {
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }
}
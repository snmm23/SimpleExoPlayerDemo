package sun.bo.lin.exoplayer.emptyplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.android.exoplayer2.BuildConfig;
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

import java.util.Collections;
import java.util.List;

import sun.bo.lin.exoplayer.R;
import sun.bo.lin.exoplayer.base.BasePlayerLayout;
import sun.bo.lin.exoplayer.util.CommonUtils;

/**
 * Created by sunbolin on 2016/12/19.
 */
public class EmptyPlayerLayout extends BasePlayerLayout {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private EmptyPlayerListener emptyPlayerListener;
    private PlayerView playerView;
    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private String videoInfo = null;

    private int startWindow;
    private long startPosition;

    public EmptyPlayerLayout(Context context) {
        this(context, null);
    }

    public EmptyPlayerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyPlayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EmptyPlayerLayout);
            try {
                int resizeMode = a.getInt(R.styleable.EmptyPlayerLayout_resize_mode, AspectRatioFrameLayout.RESIZE_MODE_FIT);
                playerView.setResizeMode(resizeMode);
            } finally {
                a.recycle();
            }
        }
    }

    private void initializePlayer() {
        if (player == null) {
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenders()
                            ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            DefaultRenderersFactory renderersFactory =
                    new DefaultRenderersFactory(getContext(), extensionRendererMode);
            player = ExoPlayerFactory.newSimpleInstance(getContext(), renderersFactory, trackSelector);
            player.addListener(new PlayerEventListener());
            player.addAnalyticsListener(new EventLogger(trackSelector));
            playerView.setPlayer(player);
            mediaDataSourceFactory = buildDataSourceFactory(true);
        }

        Uri[] uris;
        String[] extensions;
        uris = new Uri[]{Uri.parse(videoInfo)};
        extensions = new String[]{null};
        MediaSource[] mediaSources = new MediaSource[uris.length];
        for (int i = 0; i < uris.length; i++) {
            mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
        }
        MediaSource mediaSource = mediaSources[0];

        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition);
        }
        player.setPlayWhenReady(true);
        player.prepare(mediaSource, !haveStartPosition, false);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParserFactory(
                                new DefaultHlsPlaylistParserFactory((List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private List<?> getOfflineStreamKeys(Uri uri) {
        return Collections.emptyList();
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return CommonUtils.getInstance(getContext()).buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private boolean useExtensionRenders() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.empty_player_video, this);
        playerView = findViewById(R.id.player_view);
    }

    private void release() {
        if (player != null) {
            updateStartPosition();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    public void play(String url) {
        startPosition = 0;
        // 如果是网络视频，开始缓存
        videoInfo = url;
        initializePlayer();
    }


    public void start() {
        if (player != null)
            player.setPlayWhenReady(true);
    }

    public void pause() {
        if (player != null)
            player.setPlayWhenReady(false);
    }

    public void restart() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.seekTo(0);
        }
    }

    public void stop() {
        if (player != null) {
            if (videoInfo != null) {
                release();
            }
        }
    }

    public void setEmptyPlayerListener(EmptyPlayerListener listener) {
        emptyPlayerListener = listener;
    }

    public void releasePlayer() {
        release();
        videoInfo = null;
        emptyPlayerListener = null;
        mediaDataSourceFactory = null;
        clearStartPosition();
        System.gc();
    }

    public void onResume() {
        if (videoInfo != null) {
            if (player == null) {
                initializePlayer();
            }
        }
    }

    public void onPause() {
        if (videoInfo != null) {
            release();
        }
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String TAG = "sunbolin EmptyPlayer";
            Log.e(TAG, "playWhenReady = " + playWhenReady + ", playbackState = " + playbackState);
            switch (playbackState) {
                case Player.STATE_READY:
                    if (emptyPlayerListener != null)
                        emptyPlayerListener.playerStart();
                    break;
                case Player.STATE_ENDED:
                    if (emptyPlayerListener != null)
                        emptyPlayerListener.playerEnd();
                    break;
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            if (emptyPlayerListener != null)
                emptyPlayerListener.playerError();
            release();
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

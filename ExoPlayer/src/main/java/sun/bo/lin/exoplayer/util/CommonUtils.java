package sun.bo.lin.exoplayer.util;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

public class CommonUtils {

    private String userAgent;
    protected Context context;

    protected static CommonUtils instance;

    private CommonUtils(Context c) {
        context = c;
        userAgent = Util.getUserAgent(context, "ExoPlayerDemo");
    }

    public static CommonUtils getInstance(Context context) {
        if (instance == null) {
            instance = new CommonUtils(context);
        }
        return instance;
    }

    /** Returns a {@link DataSource.Factory}. */
    public DataSource.Factory buildDataSourceFactory(TransferListener listener) {
        return  new DefaultDataSourceFactory(context, listener, buildHttpDataSourceFactory(listener));
    }

    /** Returns a {@link HttpDataSource.Factory}. */
    private HttpDataSource.Factory buildHttpDataSourceFactory(
            TransferListener listener) {
        return new DefaultHttpDataSourceFactory(userAgent, listener);

    }

}

package sun.bo.lin.exoplayer.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * exo播放器基类
 */
public class BasePlayerLayout extends FrameLayout {

    public BasePlayerLayout(@NonNull Context context) {
        super(context);
    }

    public BasePlayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePlayerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 是否需要缓存, m3u8和ts格式文件不能缓存
     */
    public boolean needCache(String url) {
        return (!url.contains(".m3u8") && !url.contains(".ts")) && (url.contains("http") || url.contains("https"));
    }

}

package sun.bo.lin.exoplayer.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by sunbolin on 2018/10/19.
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
}

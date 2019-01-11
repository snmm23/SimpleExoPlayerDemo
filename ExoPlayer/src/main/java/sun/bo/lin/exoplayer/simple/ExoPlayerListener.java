package sun.bo.lin.exoplayer.simple;

import sun.bo.lin.exoplayer.base.BasePlayerListener;

/**
 * Created by sunbolin on 2016/12/27.
 */
public interface ExoPlayerListener extends BasePlayerListener {

    void goBack(boolean isSure);

    void playerOnTouch();
}

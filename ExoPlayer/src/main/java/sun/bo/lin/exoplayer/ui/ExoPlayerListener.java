package sun.bo.lin.exoplayer.ui;

/**
 * Created by sunbolin on 2016/12/27.
 */
public interface ExoPlayerListener {

    void doHorizontalScreen();

    void doVerticalScreen();

    void setViewHistory(long longTime);

    void goBack(boolean isSure);

    void playerStart();

    void playerEnd();

    void playerError();

    void playerOnTouch();
}

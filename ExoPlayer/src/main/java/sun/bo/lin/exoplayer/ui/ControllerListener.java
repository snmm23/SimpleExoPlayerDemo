package sun.bo.lin.exoplayer.ui;

/**
 * Created by sunbolin on 2016/12/28.
 */
interface ControllerListener {

    void doHorizontalScreen();

    void doVerticalScreen();

    void setCurrentPlayTime(String currentTime);

    void start();

    void pause();

    void restart();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long var1);

    boolean isPlaying();

    int getBufferPercentage();

    void goBack(boolean isSure);
}
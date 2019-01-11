package sun.bo.lin.exoplayer.base;

public interface BaseControllerListener {

    void start();

    void pause();

    void restart();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long var1);

    boolean isPlaying();

    int getBufferPercentage();
}

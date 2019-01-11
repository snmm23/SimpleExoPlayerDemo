package sun.bo.lin.exoplayer.all;

import sun.bo.lin.exoplayer.base.BaseControllerListener;

/**
 * Created by sunbolin on 2016/12/28.
 */
interface AllControllerListener extends BaseControllerListener {

    void doHorizontalScreen();

    void doVerticalScreen();

    void showStreamDialog();
}
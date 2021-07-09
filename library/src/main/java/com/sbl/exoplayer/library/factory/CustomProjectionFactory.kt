package com.sbl.exoplayer.library.factory

import com.asha.vrlib.common.MDDirection
import com.asha.vrlib.strategy.projection.AbsProjectionStrategy
import com.asha.vrlib.strategy.projection.IMDProjectionFactory
import com.asha.vrlib.strategy.projection.MultiFishEyeProjection

/**
 * sunbolin 2021/7/9
 */
class CustomProjectionFactory : IMDProjectionFactory {

    override fun createStrategy(mode: Int): AbsProjectionStrategy? {
        return when (mode) {
            CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL -> MultiFishEyeProjection(
                0.745f,
                MDDirection.VERTICAL
            )
            else -> null
        }
    }

    companion object {
        const val CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL = 9611
    }
}
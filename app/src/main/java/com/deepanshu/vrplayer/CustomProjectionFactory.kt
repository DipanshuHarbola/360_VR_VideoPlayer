package com.deepanshu.vrplayer

import com.asha.vrlib.common.MDDirection
import com.asha.vrlib.strategy.projection.AbsProjectionStrategy
import com.asha.vrlib.strategy.projection.IMDProjectionFactory
import com.asha.vrlib.strategy.projection.MultiFishEyeProjection

class CustomProjectionFactory : IMDProjectionFactory {
    companion object {
        const val CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL = 9611
    }

    override fun createStrategy(mode: Int): AbsProjectionStrategy? {
        return when (mode) {
            CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL -> MultiFishEyeProjection(
                0.745f, MDDirection.VERTICAL
            )
            else -> null
        }
    }
}
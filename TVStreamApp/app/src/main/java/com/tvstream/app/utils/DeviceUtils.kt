package com.tvstream.app.utils

import android.content.Context
import android.content.res.Configuration

/**
 * DeviceUtils – lightweight utility for runtime environment detection.
 *
 * Used by MainActivity to decide which fragment to load (TV vs. Phone)
 * without relying on separate APKs or build flavors.
 */
object DeviceUtils {

    /**
     * Returns true if the device is an Android TV or leanback-capable device.
     * Detection is done via UiModeManager / Configuration, which is the
     * recommended approach per Android TV documentation.
     */
    fun isTV(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE)
                as android.app.UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
}

/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.touchscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.lineageos.internal.util.FileUtils;
import org.lineageos.settings.device.R;

public class TouchscreenUtils {

    public static void setTouchscreenMargin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int margin = Constants.TOUCHSCREEN_MARGIN_STEP *
                prefs.getInt(Constants.TOUCHSCREEN_MARGIN_KEY,
                        context.getResources().getInteger(R.integer.touchscreen_margin_default));
        FileUtils.writeLine(Constants.TOUCHSCREEN_MARGIN_SYS_FILE, Integer.toString(margin));
    }
}

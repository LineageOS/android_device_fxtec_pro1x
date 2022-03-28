/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import lineageos.providers.LineageSettings;

import org.lineageos.settings.device.doze.DozeUtils;
import org.lineageos.settings.device.keyboard.KeyboardUtils;
import org.lineageos.settings.device.touchscreen.TouchscreenUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String FORCE_SHOW_NAVBAR_UPDATED = "force_show_navbar_updated";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (!prefs.contains(FORCE_SHOW_NAVBAR_UPDATED)) {
            LineageSettings.System.putInt(context.getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, 1);
            prefs.edit().putBoolean(FORCE_SHOW_NAVBAR_UPDATED, true).commit();
        }

        DozeUtils.checkDozeService(context);
        KeyboardUtils.setKeyboardKeymap(prefs);
        TouchscreenUtils.setTouchscreenMargin(context);
    }
}

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

import org.lineageos.settings.device.doze.DozeUtils;
import org.lineageos.settings.device.keyboard.KeyboardUtils;
import org.lineageos.settings.device.touchscreen.TouchscreenUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        DozeUtils.checkDozeService(context);
        KeyboardUtils.setKeyboardKeymap(prefs);
        TouchscreenUtils.setTouchscreenMargin(context);
    }
}

/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.touchscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.PreferenceFragment;

import org.lineageos.internal.util.FileUtils;
import org.lineageos.settings.device.R;
import org.lineageos.settings.device.widget.SeekBarPreference;

public class TouchscreenSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = TouchscreenSettingsFragment.class.getSimpleName();

    private SeekBarPreference mMarginSeekBar;
    private SharedPreferences mPrefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.touchscreen_panel);

        mMarginSeekBar = findPreference(Constants.TOUCHSCREEN_MARGIN_KEY);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        doUpdateMarginPreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        if (Constants.TOUCHSCREEN_MARGIN_KEY.equals(key)) {
            doUpdateMarginPreference();
        }
    }

    private void doUpdateMarginPreference() {
        final Context context = getContext();
        final int margin = Constants.TOUCHSCREEN_MARGIN_STEP *
                mPrefs.getInt(Constants.TOUCHSCREEN_MARGIN_KEY,
                        context.getResources().getInteger(R.integer.touchscreen_margin_default));

        FileUtils.writeLine(Constants.TOUCHSCREEN_MARGIN_SYS_FILE, Integer.toString(margin));
        mMarginSeekBar.setSummary(getString(R.string.touchscreen_margin_summary, margin));
    }
}

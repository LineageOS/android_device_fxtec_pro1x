/*
 * Copyright (C) 2022-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.touchscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;
import com.android.settingslib.widget.SliderPreference;

import org.lineageos.internal.util.FileUtils;
import org.lineageos.settings.device.R;

public class TouchscreenSettingsFragment extends SettingsBasePreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = TouchscreenSettingsFragment.class.getSimpleName();

    private SliderPreference mMarginSlider;
    private SharedPreferences mPrefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.touchscreen_panel, rootKey);

        mMarginSlider = findPreference(Constants.TOUCHSCREEN_MARGIN_KEY);
        mMarginSlider.setSliderIncrement(1);
        mMarginSlider.setTickVisible(true);
        mMarginSlider.setUpdatesContinuously(true);

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
        mMarginSlider.setSummary(getString(R.string.touchscreen_margin_summary, margin));
    }
}

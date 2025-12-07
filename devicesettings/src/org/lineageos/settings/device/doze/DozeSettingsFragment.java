/*
 * Copyright (C) 2022-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.doze;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.device.R;

public class DozeSettingsFragment extends SettingsBasePreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private SwitchPreferenceCompat mAlwaysOnDisplayPreference;

    private SwitchPreferenceCompat mPickUpPreference;
    private SwitchPreferenceCompat mPocketPreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.doze_settings, rootKey);

        SharedPreferences prefs = getActivity().getSharedPreferences("doze_settings",
                Context.MODE_PRIVATE);
        if (savedInstanceState == null && !prefs.getBoolean("first_help_shown", false)) {
            showHelp();
        }

        boolean dozeEnabled = DozeUtils.isDozeEnabled(getActivity());

        MainSwitchPreference switchBar = findPreference(Constants.DOZE_ENABLE);
        switchBar.setOnPreferenceChangeListener(this);
        switchBar.setChecked(dozeEnabled);

        mAlwaysOnDisplayPreference = findPreference(Constants.ALWAYS_ON_DISPLAY);
        mAlwaysOnDisplayPreference.setEnabled(dozeEnabled);
        mAlwaysOnDisplayPreference.setChecked(DozeUtils.isAlwaysOnEnabled(getActivity()));
        mAlwaysOnDisplayPreference.setOnPreferenceChangeListener(this);

        PreferenceCategory tiltSensorCategory =
                getPreferenceScreen().findPreference(Constants.CATEGORY_TILT_SENSOR);
        PreferenceCategory proximitySensorCategory =
                getPreferenceScreen().findPreference(Constants.CATEGORY_PROX_SENSOR);

        mPickUpPreference = findPreference(Constants.GESTURE_PICK_UP_KEY);
        mPickUpPreference.setEnabled(dozeEnabled);
        mPickUpPreference.setOnPreferenceChangeListener(this);

        mPocketPreference = findPreference(Constants.GESTURE_POCKET_KEY);
        mPocketPreference.setEnabled(dozeEnabled);
        mPocketPreference.setOnPreferenceChangeListener(this);

        // Hide proximity sensor related features if the device doesn't support them
        if (!DozeUtils.getProxCheckBeforePulse(getActivity())) {
            getPreferenceScreen().removePreference(proximitySensorCategory);
        }

        // Hide AOD if not supported and set all its dependents otherwise
        if (!DozeUtils.alwaysOnDisplayAvailable(getActivity())) {
            getPreferenceScreen().removePreference(mAlwaysOnDisplayPreference);
        } else {
            tiltSensorCategory.setDependency(Constants.ALWAYS_ON_DISPLAY);
            proximitySensorCategory.setDependency(Constants.ALWAYS_ON_DISPLAY);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = (Boolean) newValue;
        if (Constants.ALWAYS_ON_DISPLAY.equals(preference.getKey())) {
            DozeUtils.enableAlwaysOn(getActivity(), isChecked);
        } else if (Constants.DOZE_ENABLE.equals(preference.getKey())) {
            DozeUtils.enableDoze(getActivity(), isChecked);
            DozeUtils.checkDozeService(getActivity());

            if (!isChecked) {
                DozeUtils.enableAlwaysOn(getActivity(), false);
                mAlwaysOnDisplayPreference.setChecked(false);
            }
            mAlwaysOnDisplayPreference.setEnabled(isChecked);

            mPickUpPreference.setEnabled(isChecked);
            mPocketPreference.setEnabled(isChecked);
        }

        mHandler.post(() -> DozeUtils.checkDozeService(getActivity()));

        return true;
    }

    private void showHelp() {
        AlertDialog helpDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.doze_settings_help_title)
                .setMessage(R.string.doze_settings_help_text)
                .setPositiveButton(R.string.dialog_ok,
                        (dialog, which) -> {
                            getActivity()
                                    .getSharedPreferences("doze_settings", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("first_help_shown", true)
                                    .commit();
                            dialog.cancel();
                        })
                .create();
        helpDialog.show();
    }
}

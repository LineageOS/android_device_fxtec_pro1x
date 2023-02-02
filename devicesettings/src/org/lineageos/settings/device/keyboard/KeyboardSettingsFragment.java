/*
 * Copyright (C) 2020-2022 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import org.lineageos.internal.util.FileUtils;
import org.lineageos.settings.device.R;

import java.io.File;

public class KeyboardSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = KeyboardSettingsFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    private ListPreference mLayoutPref;
    private SharedPreferences mPrefs;
    private SwitchPreference mKeymapCustomPref;
    private SwitchPreference mKeymapSpacePowerPref;
    private SwitchPreference mKeymapFnKeysPref;
    private SwitchPreference mKeymapAltGrPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.keyboard_panel);

        mLayoutPref = findPreference(Constants.KEYBOARD_LAYOUT_KEY);
        mKeymapCustomPref = findPreference(Constants.KEYBOARD_KEYMAP_CUSTOM_KEY);
        mKeymapSpacePowerPref = findPreference(Constants.KEYBOARD_KEYMAP_SPACEPOWER_KEY);
        mKeymapFnKeysPref = findPreference(Constants.KEYBOARD_KEYMAP_FNKEYS_KEY);
        mKeymapAltGrPref = findPreference(Constants.KEYBOARD_KEYMAP_ALTGR_KEY);

        mLayoutPref.setValue(SystemProperties.get(Constants.KEYBOARD_LAYOUT_PROPERTY,
                    getResources().getString(R.string.keyboard_layout_default)));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        doUpdateKeymapPreferences();
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
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case Constants.KEYBOARD_LAYOUT_KEY:
                doUpdateLayoutPreference();
                break;
            case Constants.KEYBOARD_KEYMAP_CUSTOM_KEY:
            case Constants.KEYBOARD_KEYMAP_SPACEPOWER_KEY:
            case Constants.KEYBOARD_KEYMAP_FNKEYS_KEY:
            case Constants.KEYBOARD_KEYMAP_ALTGR_KEY:
                doUpdateKeymapPreferences();
                break;
        }
    }

    private void doUpdateLayoutPreference() {
        String value = mLayoutPref.getValue();
        SystemProperties.set(Constants.KEYBOARD_LAYOUT_PROPERTY, value);

        InputManager iM = InputManager.getInstance();
        InputDeviceIdentifier iDId = iM.getInputDevice(0).getIdentifier();
        String lang = KeyboardUtils.getLanguage(getContext(), value);
        String lD = KeyboardUtils.getLayoutDescriptor(iDId, lang);
        if (lD != null) {
            iM.setCurrentKeyboardLayoutForInputDevice(iDId, lD);
        }
    }

    private void doUpdateKeymapPreferences() {
        String value = mLayoutPref.getValue();
        KeyboardUtils.installKeymap(value);

        mKeymapFnKeysPref.setEnabled(true);
        mKeymapSpacePowerPref.setEnabled(true);
        mKeymapAltGrPref.setEnabled(true);

        File customKeymapFile = new File(Constants.KEYBOARD_KEYMAP_CFG_FILE);
        if (customKeymapFile.exists()) {
            if (DEBUG) Log.d(TAG, "Found custom keymap at " +
                    Constants.KEYBOARD_KEYMAP_CFG_FILE);
            mKeymapCustomPref.setEnabled(true);
            mKeymapCustomPref.setSummary(getResources().getString(
                    R.string.keyboard_keymap_custom_summary_available));
        } else {
            mKeymapCustomPref.setChecked(false);
            mKeymapCustomPref.setEnabled(false);
            mKeymapCustomPref.setSummary(getResources().getString(
                    R.string.keyboard_keymap_custom_summary_disabled));
        }

        if (mKeymapCustomPref.isChecked()) {
            if (KeyboardUtils.installCustomKeymap()) {
                mKeymapCustomPref.setSummary(getResources().getString(
                        R.string.keyboard_keymap_custom_summary_enabled));
                mKeymapFnKeysPref.setChecked(false);
                mKeymapFnKeysPref.setEnabled(false);
                mKeymapSpacePowerPref.setChecked(false);
                mKeymapSpacePowerPref.setEnabled(false);
                mKeymapAltGrPref.setChecked(false);
                mKeymapAltGrPref.setEnabled(false);
            } else {
                mKeymapCustomPref.setChecked(false);
                Context context = getContext();
                Toast.makeText(context, context.getResources().getString(
                        R.string.keyboard_keymap_custom_failed),
                        Toast.LENGTH_LONG).show();
            }
        }

        if (mKeymapFnKeysPref.isChecked()) {
            for (int i = 0; i < Constants.KEYBOARD_KEYMAP_FNKEYS_TEXT.length; ++i) {
                KeyboardUtils.writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                        Constants.KEYBOARD_KEYMAP_FNKEYS_TEXT[i] + "\n");
            }
            mKeymapCustomPref.setChecked(false);
        }

        if (mKeymapSpacePowerPref.isChecked()) {
            for (int i = 0; i < Constants.KEYBOARD_KEYMAP_SPACEPOWER_TEXT.length; ++i) {
                KeyboardUtils.writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                        Constants.KEYBOARD_KEYMAP_SPACEPOWER_TEXT[i] + "\n");
            }
            mKeymapCustomPref.setChecked(false);
        }

        if (mKeymapAltGrPref.isChecked()) {
            for (int i = 0; i < Constants.KEYBOARD_KEYMAP_ALTGR_TEXT.length; ++i) {
               KeyboardUtils. writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                        Constants.KEYBOARD_KEYMAP_ALTGR_TEXT[i] + "\n");
            }
            mKeymapCustomPref.setChecked(false);
        }
    }
}

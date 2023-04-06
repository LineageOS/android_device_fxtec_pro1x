/*
 * Copyright (C) 2022 The LineageOS Project
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
import android.hardware.input.KeyboardLayout;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

import org.lineageos.settings.device.R;

public class KeyboardUtils {

    private static final String TAG = KeyboardUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static boolean installCustomKeymap() {
	 return installKeymap(Constants.KEYBOARD_KEYMAP_CFG_FILE);
    }

    public static boolean installKeymap(String keymap) {
        if (DEBUG) Log.d(TAG, "Installing keymap: " + keymap);
        BufferedReader in = null;
        BufferedWriter out = null;

        try {
            in = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(
                        new File(keymap)
                    )
                )
            );
            if (DEBUG) Log.d(TAG, "Opened input: " + keymap);

            out = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(
                        new File(Constants.KEYBOARD_KEYMAP_SYS_FILE)
                    )
                )
            );
            if (DEBUG) Log.d(TAG, "Opened output: " + Constants.KEYBOARD_KEYMAP_SYS_FILE);

            for (String line; (line = in.readLine()) != null;) {
                out.write(line);
                out.newLine();
            }
        } catch(FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: ", e);
            return false;
        } catch(IOException e) {
            Log.e(TAG, "IOException: ", e);
            return false;
        } finally {
            try {
                out.close();
                in.close();
            } catch(IOException e) {
                Log.e(TAG, "IOException: ", e);
                return false;
            }
        }

        if (DEBUG) Log.d(TAG, "Wrote keymap to kernel");
        return true;
    }

    public static void setKeyboardKeymap(SharedPreferences prefs) {
        if (!prefs.contains(Constants.KEYBOARD_KEYMAP_CUSTOM_KEY)) {
            File f = new File(Constants.KEYBOARD_KEYMAP_CFG_FILE);
            prefs.edit().putBoolean(Constants.KEYBOARD_KEYMAP_CUSTOM_KEY, f.exists()).commit();
        }
        if (prefs.getBoolean(Constants.KEYBOARD_KEYMAP_CUSTOM_KEY, false)) {
            installCustomKeymap();
        } else {
            if (prefs.getBoolean(Constants.KEYBOARD_KEYMAP_SPACEPOWER_KEY, false)) {
                for (int i = 0; i < Constants.KEYBOARD_KEYMAP_SPACEPOWER_TEXT.length; ++i) {
                    writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                            Constants.KEYBOARD_KEYMAP_SPACEPOWER_TEXT[i] + "\n");
                }
            }

            if (prefs.getBoolean(Constants.KEYBOARD_KEYMAP_FNKEYS_KEY, false)) {
                for (int i = 0; i < Constants.KEYBOARD_KEYMAP_FNKEYS_TEXT.length; ++i) {
                    writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                            Constants.KEYBOARD_KEYMAP_FNKEYS_TEXT[i] + "\n");
                }
            }

            if (prefs.getBoolean(Constants.KEYBOARD_KEYMAP_ALTGR_KEY, false)) {
                for (int i = 0; i < Constants.KEYBOARD_KEYMAP_ALTGR_TEXT.length; ++i) {
                    writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                            Constants.KEYBOARD_KEYMAP_ALTGR_TEXT[i] + "\n");
                }
            }

            if (prefs.getBoolean(Constants.KEYBOARD_KEYMAP_BACKSLASH_KEY, false)) {
                for (int i = 0; i < Constants.KEYBOARD_KEYMAP_BACKSLASH_TEXT.length; ++i) {
                    writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                            Constants.KEYBOARD_KEYMAP_BACKSLASH_TEXT[i] + "\n");
                }
            }

            if (prefs.getBoolean(Constants.KEYBOARD_KEYMAP_HUNGARIAN_KEY, false)) {
                for (int i = 0; i < Constants.KEYBOARD_KEYMAP_HUNGARIAN_TEXT.length; ++i) {
                    writeFile(Constants.KEYBOARD_KEYMAP_SYS_FILE,
                            Constants.KEYBOARD_KEYMAP_HUNGARIAN_TEXT[i] + "\n");
                }
            }
        }
    }

    public static boolean writeFile(String filename, String text) {
        boolean result = false;
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(text);
            writer.flush();
            result = true;
        }
        catch (Exception e) { /* Ignore */ }
        return result;
    }

    private static final class KeyboardLayoutDescriptor {
        public String packageName;
        public String receiverName;
        public String keyboardLayoutName;

        public static String format(String packageName,
                String receiverName, String keyboardName) {
            return packageName + "/" + receiverName + "/" + keyboardName;
        }

        public static KeyboardLayoutDescriptor parse(String descriptor) {
            int pos = descriptor.indexOf('/');
            if (pos < 0 || pos + 1 == descriptor.length()) {
                return null;
            }
            int pos2 = descriptor.indexOf('/', pos + 1);
            if (pos2 < pos + 2 || pos2 + 1 == descriptor.length()) {
                return null;
            }

            KeyboardLayoutDescriptor result = new KeyboardLayoutDescriptor();
            result.packageName = descriptor.substring(0, pos);
            result.receiverName = descriptor.substring(pos + 1, pos2);
            result.keyboardLayoutName = descriptor.substring(pos2 + 1);
            return result;
        }
    }

    public static String getLayoutDescriptor(InputDeviceIdentifier iDId, String layoutName) {
        String result = null;
        InputManager iM = InputManager.getInstance();

        for (KeyboardLayout layout : iM.getKeyboardLayoutsForInputDevice(iDId)) {
            String layoutDescriptor = layout.getDescriptor();
            String name = KeyboardLayoutDescriptor.parse(layoutDescriptor).keyboardLayoutName;
            if (name.equals(layoutName)) {
                result = layoutDescriptor;
                break;
            }
        }
        return result;
    }

    public static String getLanguage(Context cTxt, String layout) {
        String result = null;
        String[] layouts = cTxt.getResources().getStringArray(R.array.keyboard_layout_values);
        String[] languages = cTxt.getResources().getStringArray(R.array.keyboard_layout_languages);

        for (int i = 0 ; i < layouts.length; i++) {
            if (layout.equals(layouts[i])) {
                result = languages[i];
                break;
            }
        }
        return result;
    }
}

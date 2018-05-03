/*
 * Copyright (C) 2018 The LineageOS Project
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

package org.lineageos.hardware;

import android.util.Log;
import com.android.server.HwSmartDisplayService;

import lineageos.hardware.DisplayMode;
import org.lineageos.internal.util.FileUtils;

/*
 * Display Modes API
 *
 * A device may implement a list of preset display modes for different
 * viewing intents, such as movies, photos, or extra vibrance. These
 * modes may have multiple components such as gamma correction, white
 * point adjustment, etc, but are activated by a single control point.
 *
 * This API provides support for enumerating and selecting the
 * modes supported by the hardware.
 */

public class DisplayModeControl {

    private static final int LEVEL_COLOR_ENHANCEMENT_SUPPORT_NONE = 0;
    private static final int LEVEL_COLOR_ENHANCEMENT_SUPPORT_LOW = 1;
    private static final int LEVEL_COLOR_ENHANCEMENT_SUPPORT_HIGH = 2;

    private static final int MODE_COMFORT = 1;
    private static final int MODE_COLOR_ENHANCEMENT = 2;

    private static final String DEFAULT_PATH = "/data/misc/.displaymodedefault";
    private static final DisplayMode[] DISPLAY_MODES = {
        new DisplayMode(0, "Normal"),
        new DisplayMode(1, "Vivid"),
    };

    private static HwSmartDisplayService sHwSmartDisplayService;
    private static boolean sColorEnhancementSupported;
    private static int sColorEnhancementCurrentMode;

    static {
        try {
            sHwSmartDisplayService = new HwSmartDisplayService();
            sHwSmartDisplayService.init_native();

            sColorEnhancementSupported = sHwSmartDisplayService.nativeGetFeatureSupported(
                    MODE_COLOR_ENHANCEMENT) == LEVEL_COLOR_ENHANCEMENT_SUPPORT_HIGH;
            sColorEnhancementCurrentMode = 0;

            if (FileUtils.isFileReadable(DEFAULT_PATH)) {
                setMode(getDefaultMode(), false);
            } else {
                /* If default mode is not set yet, set current mode as default */
                setMode(getCurrentMode(), true);
            }
        } catch (Throwable t) {
            sColorEnhancementSupported = false;
            sColorEnhancementCurrentMode = 0;
        }
    }

    /*
     * All HAF classes should export this boolean.
     * Real implementations must, of course, return true
     */
    public static boolean isSupported() {
        return sHwSmartDisplayService != null && sColorEnhancementSupported &&
                FileUtils.isFileWritable(DEFAULT_PATH) &&
                FileUtils.isFileReadable(DEFAULT_PATH);
    }

    /*
     * Get the list of available modes. A mode has an integer
     * identifier and a string name.
     *
     * It is the responsibility of the upper layers to
     * map the name to a human-readable format or perform translation.
     */
    public static DisplayMode[] getAvailableModes() {
        if (sHwSmartDisplayService == null || !sColorEnhancementSupported) {
            return new DisplayMode[0];
        }
        return DISPLAY_MODES;
    }

    /*
     * Get the name of the currently selected mode. This can return
     * null if no mode is selected.
     */
    public static DisplayMode getCurrentMode() {
        if (sHwSmartDisplayService == null || !sColorEnhancementSupported) {
            return null;
        }
        return DISPLAY_MODES[sColorEnhancementCurrentMode];
    }

    /*
     * Selects a mode from the list of available modes by it's
     * string identifier. Returns true on success, false for
     * failure. It is up to the implementation to determine
     * if this mode is valid.
     */
    public static boolean setMode(DisplayMode mode, boolean makeDefault) {
        if (sHwSmartDisplayService == null || !sColorEnhancementSupported) {
            return false;
        }
        sColorEnhancementCurrentMode = mode.id;
        sHwSmartDisplayService.nativeSetSmartDisplay(MODE_COLOR_ENHANCEMENT,
                sColorEnhancementCurrentMode);
        if (makeDefault) {
            FileUtils.writeLine(DEFAULT_PATH, String.valueOf(sColorEnhancementCurrentMode));
        }
        return true;
    }

    /*
     * Gets the preferred default mode for this device by it's
     * string identifier. Can return null if there is no default.
     */
    public static DisplayMode getDefaultMode() {
        if (sHwSmartDisplayService == null || !sColorEnhancementSupported) {
            return null;
        }
        try {
            int mode = Integer.parseInt(FileUtils.readOneLine(DEFAULT_PATH));
            return DISPLAY_MODES[mode];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}

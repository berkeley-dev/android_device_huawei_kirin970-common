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

import android.os.SystemProperties;

/*
 * Disable fingerprint gestures
 */
public class KeyDisabler {
    private static String FPNAV_ENABLED_PROP = "sys.fpnav.enabled";

    /*
     * Always return true in our case
     */
    public static boolean isSupported() {
        return true;
    }

    /*
     * Are the fingerprint gestures currently disabled?
     */
    public static boolean isActive() {
        return SystemProperties.get(FPNAV_ENABLED_PROP, "0").equals("1");
    }

    /*
     * Disable fingerprint gestures
     */
    public static boolean setActive(boolean state) {
        SystemProperties.set(FPNAV_ENABLED_PROP, state ? "0" : "1");
        return true;
    }
}

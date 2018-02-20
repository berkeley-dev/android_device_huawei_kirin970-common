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

package org.lineageos.settings.device;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;
import com.android.internal.util.ArrayUtils;

public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = KeyHandler.class.getSimpleName();

    private static final int KEY_FINGERPRINT_LONGPRESS = 28;
    private static final int KEY_FINGERPRINT_LEFT = 105;
    private static final int KEY_FINGERPRINT_RIGHT = 106;
    private static final int KEY_FINGERPRINT_CLICK = 174;
    private static String FPNAV_ENABLED_PROP = "sys.fpnav.enabled";

    private static final int[] sSupportedGestures = new int[] {
        KEY_FINGERPRINT_LONGPRESS,
        KEY_FINGERPRINT_LEFT,
        KEY_FINGERPRINT_RIGHT,
        KEY_FINGERPRINT_CLICK,
        // Ignored key list
        113, 117, 118, 119
    };

    private final Context mContext;
    private Handler mHandler;

    public KeyHandler(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void handleNavbarToggle(boolean enabled) {
        SystemProperties.set(FPNAV_ENABLED_PROP, enabled ? "0" : "1");
    }

    public KeyEvent handleKeyEvent(KeyEvent event) {
        KeyguardManager keyguardManager =
                (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isKeySupported = ArrayUtils.contains(sSupportedGestures, event.getScanCode());

        if (!isKeySupported) {
            return event;
        }

        if (event.getAction() != KeyEvent.ACTION_UP) {
            return null;
        }

        switch (event.getScanCode()) {
            case KEY_FINGERPRINT_LONGPRESS:
                if (!keyguardManager.inKeyguardRestrictedInputMode()) {
                    triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_HOME);
                }
                break;
            case KEY_FINGERPRINT_LEFT:
            case KEY_FINGERPRINT_RIGHT:
                if (!keyguardManager.inKeyguardRestrictedInputMode()) {
                    triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_APP_SWITCH);
                }
                break;
            case KEY_FINGERPRINT_CLICK:
                triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_BACK);
                break;
        }

        return null;
    }

    private void triggerVirtualKeypress(final Handler handler, final int keyCode) {
        final InputManager im = InputManager.getInstance();
        long now = SystemClock.uptimeMillis();

        final KeyEvent downEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_CLASS_BUTTON);
        final KeyEvent upEvent = KeyEvent.changeAction(downEvent,
                KeyEvent.ACTION_UP);

        // add a small delay to make sure everything behind got focus
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.injectInputEvent(downEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            }
        }, 10);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.injectInputEvent(upEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            }
        }, 20);
    }
}

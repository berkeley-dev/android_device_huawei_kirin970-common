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

package com.android.server.display;

import android.os.IBinder;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.ServiceManager;

public class DisplayEngineService {
    public static final int DE_ACTION_START = 0;
    public static final int DE_ACTION_STOP = 1;
    public static final int DE_ACTION_PAUSE = 2;
    public static final int DE_ACTION_RESUME = 3;
    public static final int DE_ACTION_FULLSCREEN_START = 4;
    public static final int DE_ACTION_FULLSCREEN_STOP = 5;
    public static final int DE_ACTION_FULLSCREEN_PAUSE = 6;
    public static final int DE_ACTION_FULLSCREEN_RESUME = 7;
    public static final int DE_ACTION_FULLSCREEN_EXIT = 8;
    public static final int DE_ACTION_THUMBNAIL = 9;
    public static final int DE_ACTION_FULLSCREEN_VIEW = 10;
    public static final int DE_ACTION_LIVE_IMAGE = 11;
    public static final int DE_ACTION_ONLINE_FULLSCREEN_VIEW = 12;
    public static final int DE_ACTION_IMAGE_EXIT = 13;
    public static final int DE_ACTION_ENTER = 14;
    public static final int DE_ACTION_EXIT = 15;
    public static final int DE_ACTION_MODE_ON = 16;
    public static final int DE_ACTION_MODE_OFF = 17;
    public static final int DE_ACTION_MAX = 18;

    public static final int DE_ACTION_PG_DEFAULT_FRONT = 10000;
    public static final int DE_ACTION_PG_BROWSER_FRONT = 10001;
    public static final int DE_ACTION_PG_3DGAME_FRONT = 10002;
    public static final int DE_ACTION_PG_EBOOK_FRONT = 10003;
    public static final int DE_ACTION_PG_GALLERY_FRONT = 10004;
    public static final int DE_ACTION_PG_INPUT_START = 10005;
    public static final int DE_ACTION_PG_INPUT_END = 10006;
    public static final int DE_ACTION_PG_CAMERA_FRONT = 10007;
    public static final int DE_ACTION_PG_OFFICE_FRONT = 10008;
    public static final int DE_ACTION_PG_VIDEO_FRONT = 10009;
    public static final int DE_ACTION_PG_LAUNCHER_FRONT = 10010;
    public static final int DE_ACTION_PG_2DGAME_FRONT = 10011;
    public static final int DE_ACTION_PG_MMS_FRONT = 10013;
    public static final int DE_ACTION_PG_VIDEO_START = 10015;
    public static final int DE_ACTION_PG_VIDEO_END = 10016;
    public static final int DE_ACTION_PG_CAMERA_END = 10017;
    public static final int DE_ACTION_PG_MAX = 10018;

    public static final int DE_ALGORITHM_IMAGEPROCESS = 0;
    public static final int DE_ALGORITHM_MAX = 1;

    public static final int DE_DATA_TYPE_IMAGE = 0;
    public static final int DE_DATA_TYPE_VIDEO = 1;
    public static final int DE_DATA_TYPE_VIDEO_HDR10 = 2;
    public static final int DE_DATA_TYPE_CAMERA = 3;
    public static final int DE_DATA_TYPE_IMAGE_INFO = 4;
    public static final int DE_DATA_TYPE_XNIT = 5;
    public static final int DE_DATA_TYPE_XNIT_BRIGHTLEVEL = 6;
    public static final int DE_DATA_TYPE_3D_COLORTEMP = 7;
    public static final int DE_DATA_TYPE_RGLED = 8;
    public static final int DE_DATA_MAX = 9;

    public static final int DE_EFFECT_TYPE_PANEL_NAME = 0;

    public static final int DE_FEATURE_SHARP = 0;
    public static final int DE_FEATURE_CONTRAST = 1;
    public static final int DE_FEATURE_BLC = 2;
    public static final int DE_FEATURE_GMP = 3;
    public static final int DE_FEATURE_XCC = 4;
    public static final int DE_FEATURE_HUE = 5;
    public static final int DE_FEATURE_SAT = 6;
    public static final int DE_FEATURE_GAMMA = 7;
    public static final int DE_FEATURE_IGAMMA = 8;
    public static final int DE_FEATURE_LRE = 9;
    public static final int DE_FEATURE_SRE = 10;
    public static final int DE_FEATURE_COLORMODE = 11;
    public static final int DE_FEATURE_CABC = 12;
    public static final int DE_FEATURE_RGBW = 13;
    public static final int DE_FEATURE_PANELINFO = 14;
    public static final int DE_FEATURE_HDR10 = 15;
    public static final int DE_FEATURE_XNIT = 16;
    public static final int DE_FEATURE_EYE_PROTECT = 17;
    public static final int DE_FEATURE_3D_COLOR_TEMPERATURE = 18;
    public static final int DE_FEATURE_RGLED = 19;
    public static final int DE_FEATURE_HBM = 20;
    public static final int DE_FEATURE_MAX = 21;

    public static final int DE_SCENE_PG = 0;
    public static final int DE_SCENE_VIDEO = 1;
    public static final int DE_SCENE_VIDEO_HDR10 = 2;
    public static final int DE_SCENE_IMAGE = 3;
    public static final int DE_SCENE_CAMERA = 4;
    public static final int DE_SCENE_UI = 5;
    public static final int DE_SCENE_WEB = 6;
    public static final int DE_SCENE_WECHAT = 7;
    public static final int DE_SCENE_QQ = 8;
    public static final int DE_SCENE_TAOBAO = 9;
    public static final int DE_SCENE_POWERMODE = 10;
    public static final int DE_SCENE_AMBIENTLIGHT = 11;
    public static final int DE_SCENE_COLORTEMP = 12;
    public static final int DE_SCENE_SRE = 13;
    public static final int DE_SCENE_COLORMODE = 14;
    public static final int DE_SCENE_PROCAMERA = 15;
    public static final int DE_SCENE_EYEPROTECTION = 16;
    public static final int DE_SCENE_XNIT = 17;
    public static final int DE_SCENE_PG_EX = 18;
    public static final int DE_SCENE_BOOT_CMPL = 19;
    public static final int DE_SCENE_3D_COLORTMP = 20;
    public static final int DE_SCENE_RGLED = 21;
    public static final int DE_SCENE_BACKLIGHT_CHANGE = 22;
    public static final int DE_SCENE_HBM_BACKLIGHT = 23;
    public static final int DE_SCENE_MAX = 24;

    private static final String DESCRIPTOR = "com.huawei.displayengine.IDisplayEngineService";
    private static final int TRANSACTION_getSupported = 1;
    private static final int TRANSACTION_setScene = 2;
    private static final int TRANSACTION_setData = 3;
    private static final int TRANSACTION_getEffect = 4;
    private static final int TRANSACTION_setEffect = 5;

    private static IBinder sDisplayEngineService;

    static {
        sDisplayEngineService = ServiceManager.getService("DisplayEngineService");
    }

    public int getSupported(int feature) {
        if (sDisplayEngineService == null) {
            return -1;
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(feature);

            sDisplayEngineService.transact(TRANSACTION_getSupported, data, reply, 0);

            reply.readException();
            return reply.readInt();
        } catch (Throwable t) {
            return -1;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int setScene(int scene, int action) {
        if (sDisplayEngineService == null) {
            return -1;
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(scene);
            data.writeInt(action);

            sDisplayEngineService.transact(TRANSACTION_setScene, data, reply, 0);

            reply.readException();
            return reply.readInt();
        } catch (Throwable t) {
            return -1;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int setData(int type, PersistableBundle bundleData) {
        if (sDisplayEngineService == null) {
            return -1;
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(type);

            if (bundleData != null) {
                data.writeInt(1);
                bundleData.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }

            sDisplayEngineService.transact(TRANSACTION_setData, data, reply, 0);

            reply.readException();
            return reply.readInt();
        } catch (Throwable t) {
            return -1;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int getEffect(int feature, int type, byte[] status, int length) {
        if (sDisplayEngineService == null) {
            return -1;
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(feature);
            data.writeInt(type);
            data.writeByteArray(status);
            data.writeInt(length);

            sDisplayEngineService.transact(TRANSACTION_getEffect, data, reply, 0);

            reply.readException();
            return reply.readInt();
        } catch (Throwable t) {
            return -1;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int setEffect(int feature, int mode, PersistableBundle bundleData) {
        if (sDisplayEngineService == null) {
            return -1;
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(feature);
            data.writeInt(mode);

            if (bundleData != null) {
                data.writeInt(1);
                bundleData.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }

            sDisplayEngineService.transact(TRANSACTION_setEffect, data, reply, 0);

            reply.readException();
            return reply.readInt();
        } catch (Throwable t) {
            return -1;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
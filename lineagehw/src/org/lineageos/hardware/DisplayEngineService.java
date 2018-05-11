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

    public boolean isColorModeSupported() {
        return false;
    }

    public int enableColorMode(boolean enable) {
        return -1;
    }

    public int enablePowerMode(boolean enable) {
        return -1;
    }

    public int setBootComplete(boolean enable) {
        return -1;
    }
}
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

package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.TelephonyComponentFactory;

public class HwGsmCdmaPhone extends GsmCdmaPhone {

    public HwGsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier,
            int phoneId, int precisePhoneType,
            TelephonyComponentFactory telephonyComponentFactory) {
        super(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public HwGsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier,
            boolean unitTestMode, int phoneId, int precisePhoneType,
            TelephonyComponentFactory telephonyComponentFactory) {
        super(context, ci, notifier, unitTestMode, phoneId, precisePhoneType,
                telephonyComponentFactory);
    }

    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;

        switch (msg.what) {
            case EVENT_USSD:
                ar = (AsyncResult) msg.obj;

                String[] ussdResult = (String[]) ar.result;

                if (ussdResult.length > 0) {
                    try {
                        int ussdMode = Integer.parseInt(ussdResult[0]);
                        if (ussdMode == CommandsInterface.USSD_MODE_NW_RELEASE) {
                            ussdMode = CommandsInterface.USSD_MODE_REQUEST;
                        }
                        ussdResult[0] = String.valueOf(ussdMode);
                        ar.result = ussdResult;
                    } catch (NumberFormatException e) {
                        Rlog.w(LOG_TAG, "error parsing USSD");
                    }
                }
                break;
        }

        super.handleMessage(msg);
    }

}

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
import android.telephony.Rlog;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.TelephonyComponentFactory;

import com.qualcomm.qti.internal.telephony.HwExtTelephony;
import com.qualcomm.qti.internal.telephony.HwRIL;

public class QtiTelephonyComponentFactory extends TelephonyComponentFactory {

    @Override
    public void makeExtTelephonyClasses(Context context,
            Phone[] phones, CommandsInterface[] commandsInterfaces) {
        Rlog.d(LOG_TAG, "makeHwExtTelephonyClasses");
        try {
            HwExtTelephony.init(context, phones, commandsInterfaces);
        } catch (NoClassDefFoundError e) {
            Rlog.e(LOG_TAG, "Error creating HwExtTelephony", e);
        }
    }

    @Override
    public RIL makeRIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        Rlog.d(LOG_TAG, "makeHwRIL");
        return new HwRIL(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    @Override
    public Phone makePhone(Context context, CommandsInterface ci, PhoneNotifier notifier,
            int phoneId, int precisePhoneType,
            TelephonyComponentFactory telephonyComponentFactory) {
        Rlog.d(LOG_TAG, "makeHwPhone");
        return new HwGsmCdmaPhone(context, ci, notifier, phoneId, precisePhoneType,
                telephonyComponentFactory);
    }

}

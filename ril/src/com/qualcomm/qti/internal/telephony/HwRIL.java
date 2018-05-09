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
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.Registrant;
import android.telephony.SignalStrength;

import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
import com.android.internal.telephony.RadioResponse;
import com.android.internal.telephony.RadioIndication;

import com.qualcomm.qti.internal.telephony.HwRadioResponse;
import com.qualcomm.qti.internal.telephony.HwRadioIndication;

public class HwRIL extends RIL {

    public HwRIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    @Override
    protected RadioResponse createRadioResponse(RIL ril) {
        return new HwRadioResponse(ril);
    }

    @Override
    protected RadioIndication createRadioIndication(RIL ril) {
        return new HwRadioIndication(ril);
    }

    RILRequest processResp(RadioResponseInfo i) {
        return processResponse(i);
    }

    void processRespDone(RILRequest r, RadioResponseInfo i, Object o) {
        processResponseDone(r, i, o);
    }

    void processInd(int i) {
        processIndication(i);
    }


    Registrant getSignalStrengthRegistrant() {
        return mSignalStrengthRegistrant;
    }

    static SignalStrength convertHalSignalStrength(
            android.hardware.radio.V1_0.SignalStrength signalStrength) {
        return new SignalStrength(signalStrength.gw.signalStrength,
                signalStrength.gw.bitErrorRate,
                signalStrength.cdma.dbm,
                signalStrength.cdma.ecio,
                signalStrength.evdo.dbm,
                signalStrength.evdo.ecio,
                signalStrength.evdo.signalNoiseRatio,
                signalStrength.lte.signalStrength,
                signalStrength.lte.rsrp,
                signalStrength.lte.rsrq,
                signalStrength.lte.rssnr,
                signalStrength.lte.cqi,
                signalStrength.tdScdma.rscp,
                false /* gsmFlag - don't care; will be changed by SST */);
    }

}

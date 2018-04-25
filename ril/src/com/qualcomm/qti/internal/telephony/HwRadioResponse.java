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

import android.hardware.radio.V1_0.RadioError;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.SignalStrength;

import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
import com.android.internal.telephony.RadioResponse;

import com.qualcomm.qti.internal.telephony.HwRIL;

public class HwRadioResponse extends RadioResponse {

    HwRIL mHwRil;

    public HwRadioResponse(RIL ril) {
        super(ril);
        if (ril instanceof HwRIL) {
           mHwRil = (HwRIL) ril;
        }
    }

    /**
     * Helper function to send response msg
     * @param msg Response message to be sent
     * @param ret Return object to be included in the response message
     */
    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, null);
            msg.sendToTarget();
        }
    }

    @Override
    public void getSignalStrengthResponse(RadioResponseInfo responseInfo,
                                          android.hardware.radio.V1_0.SignalStrength sigStrength) {
        if (mHwRil != null) {
            responseSignalStrength(responseInfo, sigStrength);
        } else {
            super.getSignalStrengthResponse(responseInfo, sigStrength);
        }
    }

    private void responseSignalStrength(RadioResponseInfo responseInfo,
                                        android.hardware.radio.V1_0.SignalStrength sigStrength) {
        RILRequest rr = mHwRil.processResp(responseInfo);

        if (rr != null) {
            SignalStrength ret = HwRIL.convertHalSignalStrength(sigStrength, mHwRil);
            if (responseInfo.error == RadioError.NONE) {
                sendMessageResponse(rr.getResult(), ret);
            }
            mHwRil.processRespDone(rr, responseInfo, ret);
        }
    }

}

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
import android.content.res.Resources;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.Message;
import android.os.Registrant;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RadioResponse;
import com.android.internal.telephony.RadioIndication;
import com.android.internal.telephony.SubscriptionController;

import com.qualcomm.qti.internal.telephony.HwRadioResponse;
import com.qualcomm.qti.internal.telephony.HwRadioIndication;

import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GSM;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE_CA;

public class HwRIL extends RIL {

    // Custom signal strength thresholds
    private static int[][] sSignalCust;

    Integer mInstanceId;

    public HwRIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);

        mInstanceId = instanceId;

        // Create custom signal strength thresholds based on Huawei's
        if (sSignalCust == null) {
            final int THRESHOLDS = 4;
            final int STEPS = THRESHOLDS - 1;
            sSignalCust = new int[3][THRESHOLDS];
            String[][] hwSignalCust = {
                   SystemProperties.get("gsm.sigcust.gsm",
                           "5,false,-109,-103,-97,-91,-85").split(","),
                   SystemProperties.get("gsm.sigcust.lte",
                           "5,false,-120,-115,-110,-105,-97").split(","),
                   SystemProperties.get("gsm.sigcust.umts",
                           "5,false,-112,-105,-99,-93,-87").split(",")
            };
            for (int i = 0; i < sSignalCust.length; i++) {
                // Get the highest and the lowest dBm values
                int max = Integer.parseInt(hwSignalCust[i][hwSignalCust[i].length - 1]);
                int min = Integer.parseInt(hwSignalCust[i][hwSignalCust[i].length -
                        Integer.parseInt(hwSignalCust[i][0])]);
                // Default distance between thresholds
                int step = (max - min) / STEPS;
                // Extra distance that needs to be accounted for
                int rem = (max - min) % STEPS;

                // Fill the array with the basic step distance
                for (int j = 0; j < sSignalCust[i].length; j++) {
                    sSignalCust[i][j] = min + step * j;
                }

                // Make the max line up
                sSignalCust[i][sSignalCust[i].length - 1] += rem;

                // Distribute the remainder
                int j = sSignalCust[i].length - 2;
                while (rem > 0 && j > 0) {
                    sSignalCust[i][j]++;
                    j--;
                    rem--;
                }
            }
        }
    }

    @Override
    protected RadioResponse createRadioResponse(RIL ril) {
        return new HwRadioResponse(ril);
    }

    @Override
    protected RadioIndication createRadioIndication(RIL ril) {
        return new HwRadioIndication(ril);
    }

    Object processResp(RadioResponseInfo i) {
        return processResponse(i);
    }

    Message getMsgFromRequest(Object r) {
        return getMessageFromRequest(r);
    }

    void processRespDone(Object r, RadioResponseInfo i, Object o) {
        processResponseDone(r, i, o);
    }

    Registrant getSignalStrengthRegistrant() {
        return mSignalStrengthRegistrant;
    }

    static SignalStrength convertHalSignalStrength(
            android.hardware.radio.V1_0.SignalStrength signalStrength, HwRIL ril) {
        int gsmSignalStrength = signalStrength.gw.signalStrength;
        int gsmBitErrorRate = signalStrength.gw.bitErrorRate;
        int cdmaDbm = signalStrength.cdma.dbm;
        int cdmaEcio = signalStrength.cdma.ecio;
        int evdoDbm = signalStrength.evdo.dbm;
        int evdoEcio = signalStrength.evdo.ecio;
        int evdoSnr = signalStrength.evdo.signalNoiseRatio;
        int lteSignalStrength = signalStrength.lte.signalStrength;
        int lteRsrp = signalStrength.lte.rsrp;
        int lteRsrq = signalStrength.lte.rsrq;
        int lteRssnr = signalStrength.lte.rssnr;
        int lteCqi = signalStrength.lte.cqi;
        int tdScdmaRscp = signalStrength.tdScdma.rscp;

        TelephonyManager tm = (TelephonyManager)
                ril.mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(ril.mInstanceId);
        int radioTech = tm.getDataNetworkType(subId);

        if (radioTech == NETWORK_TYPE_UNKNOWN) {
            radioTech = tm.getVoiceNetworkType(subId);
        }

        int[] threshRsrp = CarrierConfigManager.getDefaultConfig().getIntArray(
                CarrierConfigManager.KEY_LTE_RSRP_THRESHOLDS_INT_ARRAY);

        if (sSignalCust != null && threshRsrp.length == 4) {
            switch (radioTech) {
                case NETWORK_TYPE_LTE_CA:
                case NETWORK_TYPE_LTE:
                    if (lteRsrp > -44) { // None or Unknown
                        lteRsrp = -43;
                        lteRssnr = 301;
                        lteSignalStrength = 99;
                    } else if (lteRsrp >= sSignalCust[1][3]) { // Great
                        lteRsrp = threshRsrp[3];
                        lteRssnr = 130;
                        lteSignalStrength = 12;
                    } else if (lteRsrp >= sSignalCust[1][2]) { // Good
                        lteRsrp = threshRsrp[2];
                        lteRssnr = 45;
                        lteSignalStrength = 8;
                    } else if (lteRsrp >= sSignalCust[1][1]) { // Moderate
                        lteRsrp = threshRsrp[1];
                        lteRssnr = 10;
                        lteSignalStrength = 5;
                    } else if (lteRsrp >= sSignalCust[1][0]) { // Poor
                        lteRsrp = threshRsrp[0];
                        lteRssnr = -30;
                        lteSignalStrength = 0;
                    } else { // None or Unknown
                        lteRsrp = -140;
                        lteRssnr = -200;
                        lteSignalStrength = 99;
                    }
                    break;
                case NETWORK_TYPE_HSPAP:
                case NETWORK_TYPE_HSPA:
                case NETWORK_TYPE_HSUPA:
                case NETWORK_TYPE_HSDPA:
                case NETWORK_TYPE_UMTS:
                    lteRsrp = (gsmSignalStrength & 0xFF) - 256;
                    if (lteRsrp > -20) { // None or Unknown
                        lteRsrp = -43;
                        lteRssnr = 301;
                        lteSignalStrength = 99;
                    } else if (lteRsrp >= sSignalCust[2][3]) { // Great
                        lteRsrp = threshRsrp[3];
                        lteRssnr = 130;
                        lteSignalStrength = 12;
                    } else if (lteRsrp >= sSignalCust[2][2]) { // Good
                        lteRsrp = threshRsrp[2];
                        lteRssnr = 45;
                        lteSignalStrength = 8;
                    } else if (lteRsrp >= sSignalCust[2][1]) { // Moderate
                        lteRsrp = threshRsrp[1];
                        lteRssnr = 10;
                        lteSignalStrength = 5;
                    } else if (lteRsrp >= sSignalCust[2][0]) { // Poor
                        lteRsrp = threshRsrp[0];
                        lteRssnr = -30;
                        lteSignalStrength = 0;
                    } else { // None or Unknown
                        lteRsrp = -140;
                        lteRssnr = -200;
                        lteSignalStrength = 99;
                    }
                    break;
                default:
                    lteRsrp = (gsmSignalStrength & 0xFF) - 256;
                    if (lteRsrp > -20) { // None or Unknown
                        lteRsrp = -43;
                        lteRssnr = 301;
                        lteSignalStrength = 99;
                    } else if (lteRsrp >= sSignalCust[0][3]) { // Great
                        lteRsrp = threshRsrp[3];
                        lteRssnr = 130;
                        lteSignalStrength = 12;
                    } else if (lteRsrp >= sSignalCust[0][2]) { // Good
                        lteRsrp = threshRsrp[2];
                        lteRssnr = 45;
                        lteSignalStrength = 8;
                    } else if (lteRsrp >= sSignalCust[0][1]) { // Moderate
                        lteRsrp = threshRsrp[1];
                        lteRssnr = 10;
                        lteSignalStrength = 5;
                    } else if (lteRsrp >= sSignalCust[0][0]) { // Poor
                        lteRsrp = threshRsrp[0];
                        lteRssnr = -30;
                        lteSignalStrength = 0;
                    } else { // None or Unknown
                        lteRsrp = -140;
                        lteRssnr = -200;
                        lteSignalStrength = 99;
                    }
            }
        }

        return new SignalStrength(gsmSignalStrength,
                gsmSignalStrength,
                cdmaDbm,
                cdmaEcio,
                evdoDbm,
                evdoEcio,
                evdoSnr,
                lteSignalStrength,
                lteRsrp,
                lteRsrq,
                lteRssnr,
                lteCqi,
                tdScdmaRscp,
                false /* gsmFlag - don't care; will be changed by SST */);
    }

}

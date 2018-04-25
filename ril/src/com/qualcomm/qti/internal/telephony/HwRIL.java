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
import android.os.SystemProperties;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
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

    Integer mInstanceId;

    public HwRIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);

        mInstanceId = instanceId;
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
            android.hardware.radio.V1_0.SignalStrength signalStrength, HwRIL ril) {
        String[] signalCustGsm = SystemProperties.get("gsm.sigcust.gsm",
                "5,false,-109,-103,-97,-91,-85").split(",");
        String[] signalCustLte = SystemProperties.get("gsm.sigcust.lte",
                "5,false,-120,-115,-110,-105,-97").split(",");
        String[] signalCustUmts = SystemProperties.get("gsm.sigcust.umts",
                "5,false,-112,-105,-99,-93,-87").split(",");

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

        if (signalCustLte.length == 7 &&
                (radioTech == NETWORK_TYPE_LTE || radioTech == NETWORK_TYPE_LTE_CA)) {
            if (lteRsrp > -44) { // None or Unknown
                lteSignalStrength = 64;
                lteRssnr = -200;
            } else if (lteRsrp >= Integer.parseInt(signalCustLte[5])) { // Great
                lteSignalStrength = 63;
                lteRssnr = 300;
            } else if (lteRsrp >= Integer.parseInt(signalCustLte[4])) { // Good
                lteSignalStrength = 11;
                lteRssnr = 129;
            } else if (lteRsrp >= Integer.parseInt(signalCustLte[3])) { // Moderate
                lteSignalStrength = 7;
                lteRssnr = 44;
            } else if (lteRsrp >= Integer.parseInt(signalCustLte[2])) { // Poor
                lteSignalStrength = 4;
                lteRssnr = 9;
            } else if (lteRsrp >= -140) { // None or Unknown
                lteSignalStrength = 64;
                lteRssnr = -200;
            }
        } else if (signalCustUmts.length == 7 &&
                (radioTech == NETWORK_TYPE_HSPAP || radioTech == NETWORK_TYPE_HSPA ||
                radioTech == NETWORK_TYPE_HSUPA || radioTech == NETWORK_TYPE_HSDPA ||
                radioTech == NETWORK_TYPE_UMTS)) {
            lteRsrp = (gsmSignalStrength & 0xFF) - 256;
            if (lteRsrp > -20) { // None or Unknown
                lteSignalStrength = 64;
                lteRssnr = -200;
            } else if (lteRsrp >= Integer.parseInt(signalCustUmts[5])) { // Great
                lteSignalStrength = 63;
                lteRssnr = 300;
            } else if (lteRsrp >= Integer.parseInt(signalCustUmts[4])) { // Good
                lteSignalStrength = 11;
                lteRssnr = 129;
            } else if (lteRsrp >= Integer.parseInt(signalCustUmts[3])) { // Moderate
                lteSignalStrength = 7;
                lteRssnr = 44;
            } else if (lteRsrp >= Integer.parseInt(signalCustUmts[2])) { // Poor
                lteSignalStrength = 4;
                lteRssnr = 9;
            } else if (lteRsrp >= -140) { // None or Unknown
                lteSignalStrength = 64;
                lteRssnr = -200;
            }
        } else if (signalCustGsm.length == 7 &&
                (radioTech == NETWORK_TYPE_GSM || radioTech == NETWORK_TYPE_EDGE ||
                radioTech == NETWORK_TYPE_GPRS || radioTech == NETWORK_TYPE_UNKNOWN)) {
            lteRsrp = (gsmSignalStrength & 0xFF) - 256;
            if (lteRsrp > -20) { // None or Unknown
                lteSignalStrength = 64;
                lteRsrq = -21;
                lteRssnr = -200;
            } else if (lteRsrp >= Integer.parseInt(signalCustGsm[5])) { // Great
                lteSignalStrength = 63;
                lteRsrq = -3;
                lteRssnr = 300;
            } else if (lteRsrp >= Integer.parseInt(signalCustGsm[4])) { // Good
                lteSignalStrength = 11;
                lteRsrq = -7;
                lteRssnr = 129;
            } else if (lteRsrp >= Integer.parseInt(signalCustGsm[3])) { // Moderate
                lteSignalStrength = 7;
                lteRsrq = -12;
                lteRssnr = 44;
            } else if (lteRsrp >= Integer.parseInt(signalCustGsm[2])) { // Poor
                lteSignalStrength = 4;
                lteRsrq = -17;
                lteRssnr = 9;
            } else if (lteRsrp >= -140) { // None or Unknown
                lteSignalStrength = 64;
                lteRsrq = -21;
                lteRssnr = -200;
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

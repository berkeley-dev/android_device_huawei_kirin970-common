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
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;

import org.codeaurora.internal.Client;
import org.codeaurora.internal.IDepersoResCallback;
import org.codeaurora.internal.IDsda;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.INetworkCallback;
import org.codeaurora.internal.Token;

import java.util.Iterator;

import static android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID;

import static android.telephony.TelephonyManager.SIM_ACTIVATION_STATE_ACTIVATED;
import static android.telephony.TelephonyManager.SIM_ACTIVATION_STATE_DEACTIVATED;

import static android.telephony.TelephonyManager.MultiSimVariants.DSDA;

import static com.android.internal.telephony.uicc.IccCardStatus.CardState.CARDSTATE_PRESENT;

public class HwExtTelephony extends IExtTelephony.Stub {

    class UiccStatus {

        boolean mProvisioned;
        int mStatus;

        UiccStatus(int status) {
            mProvisioned = true;
            mStatus = status;
        }

    }

    // Service name
    private static final String EXT_TELEPHONY_SERVICE_NAME = "extphone";

    // Intents (+ extras) to broadcast
    private static final String ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED =
            "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED";
    private static final String EXTRA_NEW_PROVISION_STATE = "newProvisionState";

    // UICC States
    private static final int PROVISIONED = 1;
    private static final int NOT_PROVISIONED = 0;
    private static final int INVALID_STATE = -1;
    private static final int CARD_NOT_PRESENT = -2;

    // Error codes
    private static final int SUCCESS = 0;
    private static final int GENERIC_FAILURE = -1;
    private static final int INVALID_INPUT = -2;
    private static final int BUSY = -3;

    // From IccCardProxy.java
    private static final int EVENT_ICC_CHANGED = 3;

    private static HwExtTelephony sInstance;

    private CommandsInterface[] mCommandsInterfaces;
    private Context mContext;
    private Handler mHandler;
    private Phone[] mPhones;
    private SubscriptionManager mSubscriptionManager;
    private TelecomManager mTelecomManager;
    private TelephonyManager mTelephonyManager;
    private UiccController mUiccController;
    private UiccStatus mUiccStatus[];
    private boolean mBusy;


    public static void init(Context context, Phone[] phones,
            CommandsInterface[] commandsInterfaces) {
        sInstance = getInstance(context, phones, commandsInterfaces);
    }

    public static HwExtTelephony getInstance(Context context, Phone[] phones,
            CommandsInterface[] commandsInterfaces) {
        if (sInstance == null) {
            sInstance = new HwExtTelephony(context, phones, commandsInterfaces);
        }

        return sInstance;
    }

    private HwExtTelephony(Context context, Phone[] phones,
            CommandsInterface[] commandsInterfaces) {
        if (ServiceManager.getService(EXT_TELEPHONY_SERVICE_NAME) == null) {
            ServiceManager.addService(EXT_TELEPHONY_SERVICE_NAME, this);
        }

        mCommandsInterfaces = commandsInterfaces;

        mContext = context;

        // Keep track of ICC state changes
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                AsyncResult ar;

                if (msg.what == EVENT_ICC_CHANGED) {
                    ar = (AsyncResult) msg.obj;
                    if (ar != null && ar.result != null) {
                        iccStatusChanged((Integer) ar.result);
                    }
                }
            }
        };

        mPhones = phones;

        mSubscriptionManager = (SubscriptionManager) mContext.getSystemService(
                Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        mTelecomManager = TelecomManager.from(context);

        mTelephonyManager = TelephonyManager.from(context);

        // Assume everything present is provisioned by default
        mUiccStatus = new UiccStatus[mPhones.length];
        for (int i = 0; i < mPhones.length; i++) {
            if (mPhones[i] == null) {
                mUiccStatus[i] = null;
            } else if (mPhones[i].getUiccCard() == null) {
                mUiccStatus[i] = new UiccStatus(CARD_NOT_PRESENT);
            } else {
                mUiccStatus[i] = new UiccStatus(mPhones[i].getUiccCard().getCardState()
                        == CARDSTATE_PRESENT ? PROVISIONED : CARD_NOT_PRESENT);
            }
        }

        mBusy = false;

        mUiccController = UiccController.getInstance();
        mUiccController.registerForIccChanged(mHandler, EVENT_ICC_CHANGED, null);
    }

    private synchronized void iccStatusChanged(int slotId) {
        if (slotId >= mPhones.length || mPhones[slotId] == null) {
            return;
        }

        UiccCard card = mPhones[slotId].getUiccCard();

        if (card == null || card.getCardState() != CARDSTATE_PRESENT) {
            mUiccStatus[slotId].mStatus = CARD_NOT_PRESENT;
        } else {
            if (mUiccStatus[slotId].mProvisioned &&
                    mUiccStatus[slotId].mStatus != PROVISIONED) {
                mUiccStatus[slotId].mStatus = NOT_PROVISIONED;
                activateUiccCard(slotId);
            } else if (!mUiccStatus[slotId].mProvisioned &&
                    mUiccStatus[slotId].mStatus != NOT_PROVISIONED) {
                mUiccStatus[slotId].mStatus = PROVISIONED;
                deactivateUiccCard(slotId);
            }
        }

        broadcastUiccActivation(slotId);
    }

    private void setUiccActivation(int slotId, boolean activate) {
        UiccCard card = mPhones[slotId].getUiccCard();

        int numApps = card.getNumApplications();

        mUiccStatus[slotId].mProvisioned = activate;

        for (int i = 0; i < numApps; i++) {
            if (card.getApplicationIndex(i) == null) {
                continue;
            }

            mCommandsInterfaces[slotId].setUiccSubscription(i, activate, null);
        }
    }

    private void broadcastUiccActivation(int slotId) {
        Intent intent = new Intent(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED);
        intent.putExtra(PhoneConstants.PHONE_KEY, slotId);
        intent.putExtra(EXTRA_NEW_PROVISION_STATE, mUiccStatus[slotId].mStatus);
        mContext.sendBroadcast(intent);
    }

    @Override
    public int getCurrentUiccCardProvisioningStatus(int slotId) {
        if (slotId >= mUiccStatus.length || mUiccStatus[slotId] == null) {
            return INVALID_INPUT;
        }

        return mUiccStatus[slotId].mStatus;
    }

    @Override
    public int getUiccCardProvisioningUserPreference(int slotId) {
        if (slotId >= mUiccStatus.length || mUiccStatus[slotId] == null) {
            return INVALID_INPUT;
        }

        return mUiccStatus[slotId].mProvisioned ? PROVISIONED : NOT_PROVISIONED;
    }

    @Override
    public int activateUiccCard(int slotId) {
        if (slotId >= mPhones.length || mPhones[slotId] == null ||
                slotId >= mCommandsInterfaces.length || mCommandsInterfaces[slotId] == null) {
            return INVALID_INPUT;
        }

        if (mBusy) {
            return BUSY;
        }

        if (mUiccStatus[slotId].mStatus == PROVISIONED) {
            return SUCCESS;
        }

        if (mUiccStatus[slotId].mStatus != NOT_PROVISIONED) {
            return INVALID_INPUT;
        }

        mBusy = true;

        mUiccStatus[slotId].mStatus = PROVISIONED;

        setUiccActivation(slotId, true);
        mPhones[slotId].setVoiceActivationState(SIM_ACTIVATION_STATE_ACTIVATED);
        mPhones[slotId].setDataActivationState(SIM_ACTIVATION_STATE_ACTIVATED);

        mBusy = false;

        broadcastUiccActivation(slotId);

        return SUCCESS;
    }

    @Override
    public int deactivateUiccCard(int slotId) {
        if (slotId >= mPhones.length || mPhones[slotId] == null ||
                slotId >= mCommandsInterfaces.length || mCommandsInterfaces[slotId] == null) {
            return INVALID_INPUT;
        }

        if (mBusy) {
            return BUSY;
        }

        if (mUiccStatus[slotId].mStatus == NOT_PROVISIONED) {
            return SUCCESS;
        }

        if (mUiccStatus[slotId].mStatus != PROVISIONED) {
            return INVALID_INPUT;
        }

        mBusy = true;

        int subIdToDeactivate = mPhones[slotId].getSubId();
        int subIdToMakeDefault = INVALID_SUBSCRIPTION_ID;

        mUiccStatus[slotId].mStatus = NOT_PROVISIONED;

        // Find first provisioned sub that isn't what we're deactivating
        for (int i = 0; i < mPhones.length; i++) {
            if (mUiccStatus[i].mStatus == PROVISIONED) {
                subIdToMakeDefault = mPhones[i].getSubId();
                break;
            }
        }

        // Make sure defaults are now sane
        PhoneAccountHandle accountHandle = mTelecomManager.getUserSelectedOutgoingPhoneAccount();
        PhoneAccount account = mTelecomManager.getPhoneAccount(accountHandle);

        if (mSubscriptionManager.getDefaultSmsSubscriptionId() == subIdToDeactivate) {
            mSubscriptionManager.setDefaultSmsSubId(subIdToMakeDefault);
        }

        if (mSubscriptionManager.getDefaultDataSubscriptionId() == subIdToDeactivate) {
            mSubscriptionManager.setDefaultDataSubId(subIdToMakeDefault);
        }

        if (mTelephonyManager.getSubIdForPhoneAccount(account) == subIdToDeactivate) {
            mTelecomManager.setUserSelectedOutgoingPhoneAccount(
                    subscriptionIdToPhoneAccountHandle(subIdToMakeDefault));
        }

        mPhones[slotId].setVoiceActivationState(SIM_ACTIVATION_STATE_DEACTIVATED);
        mPhones[slotId].setDataActivationState(SIM_ACTIVATION_STATE_DEACTIVATED);
        setUiccActivation(slotId, false);

        mBusy = false;

        broadcastUiccActivation(slotId);

        return SUCCESS;
    }

    @Override
    public boolean isSMSPromptEnabled() {
        // I hope we don't use this
        return false;
    }

    @Override
    public void setSMSPromptEnabled(boolean enabled) {
        // I hope we don't use this
    }

    @Override
    public int getPhoneIdForECall() {
        // I hope we don't use this
        return -1;
    }

    @Override
    public void setPrimaryCardOnSlot(int slotId) {
        // I hope we don't use this
    }

    @Override
    public boolean isFdnEnabled() {
        // I hope we don't use this
        return false;
    }

    @Override
    public int getPrimaryStackPhoneId() {
        // I hope we don't use this
        return -1;
    }

    @Override
    public boolean isEmergencyNumber(String number) {
        // This is lame...
        return PhoneNumberUtils.isEmergencyNumber(number);
    }

    @Override
    public boolean isLocalEmergencyNumber(String number) {
        // This is lame...
        return PhoneNumberUtils.isLocalEmergencyNumber(mContext, number);
    }

    @Override
    public boolean isPotentialEmergencyNumber(String number) {
        // This is lame...
        return PhoneNumberUtils.isPotentialEmergencyNumber(number);
    }

    @Override
    public boolean isPotentialLocalEmergencyNumber(String number) {
        // This is lame...
        return PhoneNumberUtils.isPotentialLocalEmergencyNumber(mContext, number);
    }

    @Override
    public boolean isDeviceInSingleStandby() {
        // I hope we don't use this
        return false;
    }

    @Override
    public boolean setLocalCallHold(int subId, boolean enable) {
        // TODO: Do something here
        return false;
    }

    @Override
    public void switchToActiveSub(int subId) {
        // I hope we don't use this
    }

    @Override
    public void setDsdaAdapter(IDsda dsdaAdapter) {
        // I hope we don't use this
    }

    @Override
    public int getActiveSubscription() {
        // I hope we don't use this
        return -1;
    }

    @Override
    public boolean isDsdaEnabled() {
        return mTelephonyManager.getMultiSimConfiguration() == DSDA;
    }

    @Override
    public void supplyIccDepersonalization(String netpin, String type, IDepersoResCallback callback,
            int phoneId) {
        // I hope we don't use this
    }

    @Override
    public int getPrimaryCarrierSlotId() {
        // I hope we don't use this
        return -1;
    }

    @Override
    public boolean isPrimaryCarrierSlotId(int slotId) {
        // I hope we don't use this
        return false;
    }

    @Override
    public boolean setSmscAddress(int slotId, String smsc) {
        // I hope we don't use this
        return false;
    }

    @Override
    public String getSmscAddress(int slotId) {
        // I hope we don't use this
        return null;
    }

    @Override
    public boolean isVendorApkAvailable(String packageName) {
        // I hope we don't use this
        return false;
    }

    @Override
    public int getCurrentPrimaryCardSlotId() {
        // I hope we don't use this
        return -1;
    }

    @Override
    public Token enable5g(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Token disable5g(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Token enable5gOnly(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Token query5gStatus(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Token queryNrDcParam(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Token queryNrBearerAllocation(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Token queryNrSignalStrength(int slotId, Client client) {
        // I hope we don't use this
        return new Token(-1);
    }

    @Override
    public Client registerCallback(String packageName, INetworkCallback callback) {
        // I hope we don't use this
        return new Client(-1, -1, packageName, callback);
    }

    @Override
    public void unRegisterCallback(INetworkCallback callback) {
        // I hope we don't use this
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(final int subId) {
        final Iterator<PhoneAccountHandle> phoneAccounts =
                mTelecomManager.getCallCapablePhoneAccounts().listIterator();

        while (phoneAccounts.hasNext()) {
            final PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
            final PhoneAccount phoneAccount = mTelecomManager.getPhoneAccount(phoneAccountHandle);
            if (subId == mTelephonyManager.getSubIdForPhoneAccount(phoneAccount)) {
                return phoneAccountHandle;
            }
        }

        return null;
    }

}

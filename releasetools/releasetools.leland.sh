#!/sbin/sh
#
# Copyright (C) 2018 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ "$(cat /proc/device-tree/hisi,product_name)" = "LLD-L31" ]; then
    # Keep NFC
else
    # Remove NFC
    rm -rf /system/app/NfcNci
    rm -rf /system/etc/permissions/android.hardware.nfc.hce.xml
    rm -rf /system/etc/permissions/android.hardware.nfc.xml
    rm -rf /system/etc/permissions/com.android.nfc_extras.xml
    rm -rf /system/framework/com.android.nfc_extras.jar
    rm -rf /system/priv-app/Tag
fi

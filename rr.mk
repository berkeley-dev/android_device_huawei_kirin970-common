#
# Copyright (C) 2017 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
$(call inherit-product, $(LOCAL_PATH)/pre-base.mk)
$(call inherit-product, $(LOCAL_PATH)/full_bkl.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/rr/config/common_full_phone.mk)

# Boot animation
TARGET_SCREEN_HEIGHT := 2160
TARGET_SCREEN_WIDTH := 1080

RR_BUILDTYPE = OpenKirin

PRODUCT_NAME := rr_bkl
PRODUCT_DEVICE := bkl
PRODUCT_BRAND := Huawei
PRODUCT_MODEL := Honor-View10

# Override device name
PRODUCT_BUILD_PROP_OVERRIDES += \
    TARGET_DEVICE=kirin970

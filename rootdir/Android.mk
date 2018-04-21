LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE       := init.kirin970.rc
LOCAL_MODULE_TAGS  := optional eng
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES    := etc/init.kirin970.rc
LOCAL_MODULE_PATH  := $(TARGET_OUT_ETC)/init
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE       := init.kirin970.ab.rc
LOCAL_MODULE_TAGS  := optional eng
LOCAL_POST_INSTALL_CMD := \
    mkdir -p $(PRODUCT_OUT)/system && \
    ln -sf /system $(PRODUCT_OUT)/system/system
include $(BUILD_PHONY_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE       := init.kirin970.environ.rc
LOCAL_MODULE_TAGS  := optional eng
LOCAL_POST_INSTALL_CMD := \
    mkdir -p $(PRODUCT_OUT)/system/etc/init && \
    sed -e 's?%BOOTCLASSPATH%?$(PRODUCT_BOOTCLASSPATH)?g' $(LOCAL_PATH)/etc/init.kirin970.environ.rc > $(PRODUCT_OUT)/system/etc/init/init.kirin970.environ.rc && \
    sed -i -e 's?%SYSTEMSERVERCLASSPATH%?$(PRODUCT_SYSTEM_SERVER_CLASSPATH)?g' $(PRODUCT_OUT)/system/etc/init/init.kirin970.environ.rc
include $(BUILD_PHONY_PACKAGE)

LOCAL_PATH := $(call my-dir)

INSTALLED_RAMDISK_TARGET := $(PRODUCT_OUT)/ramdisk.img
$(INSTALLED_RAMDISK_TARGET): $(MKBOOTFS) $(INTERNAL_RAMDISK_FILES) | $(MINIGZIP)
	$(call pretty,"Target ram disk: $@")
	$(hide) $(MKBOOTFS) -d $(TARGET_OUT) $(TARGET_ROOT_OUT) | $(BOOT_RAMDISK_COMPRESSOR) > $@.gz
	$(hide) $(MKBOOTIMG) --kernel /dev/null --ramdisk $(BUILT_RAMDISK_TARGET).gz --cmdline buildvariant=user --base 0x10000000 --pagesize 2048 --output $@

LOCAL_PATH := $(call my-dir)

#----------------------------------------------------------------------
# Compile kernel partition image
#----------------------------------------------------------------------
INSTALLED_KERNELIMAGE_TARGET := $(PRODUCT_OUT)/kernel.img

# For some reason, this isn't exposed outside build/core/Makefile
INSTALLED_KERNEL_TARGET := $(PRODUCT_OUT)/kernel

INTERNAL_KERNEL_CMDLINE := $(strip $(BOARD_KERNEL_CMDLINE) buildvariant=$(TARGET_BUILD_VARIANT) $(VERITY_KEYID))
INTERNAL_KERNEL_MKBOOTIMG_VERSION_ARGS := \
	--os_version $(BOARD_KERNEL_OS_VERSION) \
	--os_patch_level $(BOARD_KERNEL_OS_PATCH_LEVEL)

INTERNAL_KERNELIMAGE_ARGS := \
	--kernel $(INSTALLED_KERNEL_TARGET) \
	--ramdisk /dev/null \
	--base $(BOARD_KERNEL_BASE) \
	--pagesize $(BOARD_KERNEL_PAGESIZE) \
	--kernel_offset $(BOARD_KERNEL_OFFSET) \
	--second_offset $(BOARD_KERNEL_SECOND_OFFSET) \
	--tags_offset $(BOARD_KERNEL_TAGS_OFFSET) \
	--ramdisk_offset $(BOARD_RAMDISK_OFFSET) \
	--cmdline "$(INTERNAL_KERNEL_CMDLINE)"

ALL_PREBUILT += $(INSTALLED_KERNELIMAGE_TARGET)
$(INSTALLED_KERNELIMAGE_TARGET): $(MKBOOTIMG) $(INSTALLED_KERNEL_TARGET)
	$(call pretty,"Target kernel image: $@")
	$(hide) $(MKBOOTIMG) $(INTERNAL_KERNELIMAGE_ARGS) $(INTERNAL_KERNEL_MKBOOTIMG_VERSION_ARGS) --output $@
	$(hide) $(call assert-max-image-size,$@,$(BOARD_KERNELIMAGE_PARTITION_SIZE))

.PHONY: kernelimage-nodeps
kernelimage-nodeps: $(MKBOOTIMG) $(BOOT_SIGNER)
	@echo "make $@: ignoring dependencies"
	$(hide) $(MKBOOTIMG) $(INTERNAL_KERNELIMAGE_ARGS) $(INTERNAL_MKBOOTIMG_VERSION_ARGS) --output $(INSTALLED_KERNELIMAGE_TARGET)
	$(hide) $(call assert-max-image-size,$(INSTALLED_KERNELIMAGE_TARGET),$(BOARD_KERNELIMAGE_PARTITION_SIZE))

.PHONY: kernelimage
kernelimage: $(INSTALLED_KERNELIMAGE_TARGET)

#----------------------------------------------------------------------
# Compile ramdisk partition image
#----------------------------------------------------------------------
INSTALLED_RAMDISKIMAGE_TARGET := $(PRODUCT_OUT)/ramdisk-image.img

# For some reason, this isn't exposed outside build/core/Makefile
BUILT_RAMDISK_TARGET := $(PRODUCT_OUT)/ramdisk.img

INTERNAL_RAMDISKIMAGE_ARGS := \
	--kernel /dev/null \
	--ramdisk $(BUILT_RAMDISK_TARGET) \
	--cmdline "buildvariant=$(TARGET_BUILD_VARIANT)" \
	--base $(BOARD_RAMDISK_BASE) \
	--pagesize $(BOARD_KERNEL_PAGESIZE) \

$(INSTALLED_RAMDISKIMAGE_TARGET): $(MKBOOTIMG) $(BUILT_RAMDISK_TARGET)
	$(call pretty,"Target ram disk image: $@")
	$(hide) $(MKBOOTIMG) $(INTERNAL_RAMDISKIMAGE_ARGS) --output $@
	$(hide) $(call assert-max-image-size,$@,$(BOARD_RAMDISKIMAGE_PARTITION_SIZE))

.PHONY: ramdiskimage
ramdiskimage: $(INSTALLED_RAMDISKIMAGE_TARGET)

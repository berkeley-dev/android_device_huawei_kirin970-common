#ifndef _BDROID_BUILDCFG_H
#define _BDROID_BUILDCFG_H

#include <cutils/properties.h>
#include <string.h>

#include "osi/include/osi.h"

typedef struct {
    const char *product_device;
    const char *product_model;
} device_t;

static const device_t devices[] = {
    {"HWALP", "Mate 10"},
    {"HWBKL", "Honor View 10"},
    {"HWBLA", "Mate 10 Pro"},
    {"HWCLT", "P20 Pro"},
    {"HWCOL", "Honor 10"},
    {"HWEML", "P20"},
};

static inline const char *BtmGetDefaultName()
{
    char product_device[PROPERTY_VALUE_MAX];
    property_get("ro.product.device", product_device, "");

    for (unsigned int i = 0; i < ARRAY_SIZE(devices); i++) {
        device_t device = devices[i];

        if (strcmp(device.product_device, product_device) == 0) {
            return device.product_model;
        }
    }

    // Fallback to ro.product.model
    return "";
}

#define BTM_DEF_LOCAL_NAME BtmGetDefaultName()
#define BTM_BYPASS_EXTRA_ACL_SETUP TRUE

#define BLE_INCLUDED TRUE
#define BLE_VND_INCLUDED TRUE

#endif

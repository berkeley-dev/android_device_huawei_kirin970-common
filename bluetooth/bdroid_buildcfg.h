#ifndef _BDROID_BUILDCFG_H
#define _BDROID_BUILDCFG_H

#include <cutils/properties.h>
#include <string.h>

static inline const char *BtmGetDefaultName()
{
    char product_device[PROPERTY_VALUE_MAX];
    property_get("ro.product.device", product_device, "");

    if (strcmp(product_device, "HWBKL") == 0)
        return "Honor View 10";
    if (strcmp(product_device, "HWCLT") == 0)
        return "P20 Pro";

    // Fallback to ro.product.model
    return "";
}

#define BTM_DEF_LOCAL_NAME BtmGetDefaultName()
#define BTM_BYPASS_EXTRA_ACL_SETUP TRUE

#define BLE_INCLUDED TRUE
#define BLE_VND_INCLUDED TRUE

#endif

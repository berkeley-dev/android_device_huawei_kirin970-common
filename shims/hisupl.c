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

#include <dlfcn.h>
#include <hardware/hardware.h>
#include <stdlib.h>
#include <string.h>

#define LOG_TAG "libshims_hisupl"
#include <cutils/log.h>

static typeof(hw_get_module_by_class) *hw_get_module_by_class_real = NULL;

int hw_get_module_by_class(const char *class_id, const char *inst,
                           const struct hw_module_t **module)
{
    if (!hw_get_module_by_class_real) {
        hw_get_module_by_class_real = dlsym(RTLD_NEXT, "hw_get_module_by_class");
    }

    if (hw_get_module_by_class_real) {
        if (class_id && strcmp(class_id, "hisupl.hi1102") == 0) {
            return hw_get_module_by_class_real("hisupl", "hi1102", module);
        }

        return hw_get_module_by_class_real(class_id, inst, module);
    }

    /*
     * If we could not find hw_get_module_by_class, print an error and bail out,
     * we should not pretend everything's fine and return default_value.
     */
    ALOGE("Could not find hw_get_module_by_class, aborting");
    abort();

    return 0;
}

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

#define LOG_TAG "odm-init"

#include <android-base/logging.h>
#include <android-base/properties.h>
#include <algorithm>
#include <cstring>
#include <fstream>
#include <map>
#include <vector>

constexpr auto CMDLINE_PATH = "/proc/cmdline";
constexpr auto CMDLINE_PRODUCT_ID = "productid=";
constexpr auto PHONE_PROP_PATH = "/odm/phone.prop";

using android::base::SetProperty;

using PropertyPair = std::pair<std::string, std::string>;
using PropertiesVector = std::vector<PropertyPair>;

bool GetProductId(std::string& out) {
    std::ifstream file(CMDLINE_PATH);

    if (!file.is_open()) {
        LOG(ERROR) << "Unable to open: " << CMDLINE_PATH;
        return false;
    }

    std::string str((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    size_t offset = str.find(CMDLINE_PRODUCT_ID);

    if (offset == std::string::npos) {
        LOG(ERROR) << "Unable to find product id";
        return false;
    }

    out = str.substr(offset + strlen(CMDLINE_PRODUCT_ID), 10);

    std::transform(out.begin(), out.end(), out.begin(), ::toupper);

    return true;
}

bool GetPropertiesFromPhoneProp(std::map<std::string, PropertiesVector>& out) {
    std::ifstream file(PHONE_PROP_PATH);

    if (!file.is_open()) {
        LOG(ERROR) << "Unable to open: " << PHONE_PROP_PATH;
        return false;
    }

    std::string currentProductId;
    std::string line;

    while (std::getline(file, line)) {
        if (line.empty()) {
            continue;
        }

        // Example product id line format: [0X39620484]:
        if (line.length() == 13 && line.at(0) == '[' && line.at(11) == ']') {
            currentProductId = line.substr(1, 10);
            continue;
        }

        // Checking if currentProductId isn't empty just in case someone breaks their phone.prop
        if (!currentProductId.empty()) {
            size_t offset = line.find('=');

            if (offset != std::string::npos) {
                out[currentProductId].push_back({line.substr(0, offset), line.substr(offset + 1)});
            }
        }
    }

    return true;
}

int main() {
    std::string productId;

    if (!GetProductId(productId)) {
        return -1;
    }

    std::map<std::string, PropertiesVector> properties;

    if (!GetPropertiesFromPhoneProp(properties)) {
        return -1;
    }

    auto it = properties.find(productId);

    if (it != properties.end()) {
        for (const auto& prop : properties.at(productId)) {
            if (!SetProperty(prop.first, prop.second)) {
                LOG(ERROR) << "Unable to set property " << prop.first << " to " << prop.second;
            }
        }
    }

    return 0;
}

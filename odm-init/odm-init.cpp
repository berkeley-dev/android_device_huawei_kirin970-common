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

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/properties.h>
#include <android-base/strings.h>
#include <algorithm>
#include <cstring>
#include <fstream>
#include <map>
#include <vector>

constexpr auto CMDLINE_PATH = "/proc/cmdline";
constexpr auto CMDLINE_PRODUCT_ID = "productid";
constexpr auto PHONE_PROP_PATH = "/odm/phone.prop";

using android::base::ReadFileToString;
using android::base::SetProperty;
using android::base::Split;
using android::base::Trim;

using PropertyPair = std::pair<std::string, std::string>;
using PropertiesVector = std::vector<PropertyPair>;

bool GetProductId(std::string& out) {
    std::string str;

    if (!ReadFileToString(CMDLINE_PATH, &str)) {
        LOG(ERROR) << "Unable to open: " << CMDLINE_PATH;
        return false;
    }

    for (const auto& entry : Split(Trim(str), " ")) {
        std::vector<std::string> pieces = Split(entry, "=");

        if (pieces.size() == 2 && pieces.at(0) == CMDLINE_PRODUCT_ID) {
            out = pieces.at(1);
            std::transform(out.begin(), out.end(), out.begin(), ::toupper);
            return true;
        }
    }

    return false;
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
            std::vector<std::string> pieces = Split(line, "=");

            if (pieces.size() == 2) {
                out[currentProductId].push_back({pieces.at(0), pieces.at(1)});
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

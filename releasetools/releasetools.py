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

def FullOTA_InstallEnd(info):
    info.script.AppendExtra('mount("ext4", "EMMC", "/dev/block/bootdevice/by-name/system", "/system");');
    info.script.AppendExtra('run_program("/sbin/sed", "-i", "/genfscon exfat/d", "/system/etc/selinux/plat_sepolicy.cil");');
    info.script.AppendExtra('run_program("/sbin/sed", "-i", "/genfscon fuseblk/d", "/system/etc/selinux/plat_sepolicy.cil");');
    info.script.AppendExtra('unmount("/system");');

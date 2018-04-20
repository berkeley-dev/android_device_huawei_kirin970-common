#!/sbin/sh

# Remove duplicated genfscon rules
sed -i "/genfscon exfat/d" /system/etc/selinux/plat_sepolicy.cil
sed -i "/genfscon fuseblk/d" /system/etc/selinux/plat_sepolicy.cil

# Remove definitions and labels that conflict with 8.0 vendor image
if [ "$(grep ro.build.version.release /vendor/build.prop)" = "ro.build.version.release=8.0.0" ]; then
    # Remove duplicated type definitions
    sed -i "/(type cust_data_file)/d;/(roletype object_r cust_data_file)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type dmd_device)/d;/(roletype object_r dmd_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type exception_device)/d;/(roletype object_r exception_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type hisee_blkdev)/d;/(roletype object_r hisee_blkdev)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type hisee_data_file)/d;/(roletype object_r hisee_data_file)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type jank_device)/d;/(roletype object_r jank_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type logcat_data_file)/d;/(roletype object_r logcat_data_file)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type logcat_device)/d;/(roletype object_r logcat_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type secure_storage_block_device)/d;/(roletype object_r secure_storage_block_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type shex_block_device)/d;/(roletype object_r shex_block_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type splash2_block_device)/d;/(roletype object_r splash2_block_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type splash2_data_file)/d;/(roletype object_r splash2_data_file)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type teecd_data_file)/d;/(roletype object_r teecd_data_file)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type thirdmodem_block_device)/d;/(roletype object_r thirdmodem_block_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type thirdmodemnvmbkp_block_device)/d;/(roletype object_r thirdmodemnvmbkp_block_device)/d" /system/etc/selinux/plat_sepolicy.cil
    sed -i "/(type thirdmodemnvm_block_device)/d;/(roletype object_r thirdmodemnvm_block_device)/d" /system/etc/selinux/plat_sepolicy.cil

    # Remove duplicated labels (Block Devices)
    sed -i "/\/dev\/block\/bootdevice\/by-name\/3rdmodem/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/3rdmodemnvm/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/3rdmodemnvmbkp/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/hisee_fs/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/sensorhub/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/hisee_img/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/secure_storage/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/block\/bootdevice\/by-name\/splash2/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (Cust Data)
    sed -i "/\/data\/custom.bin/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/product.bin/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/test_nv.bin/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/test_ver.bin/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/cust_ver.bin/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/facapp/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/custom_cota\\\.bin/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (DMD)
    sed -i "/\/dev\/dsm/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (HiSEE)
    sed -i "/\/hisee_fs(\/.*)?/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (HWLog)
    sed -i "/\/dev\/hwlog_jank/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/hwlog_switch/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/dev\/hwlog_exception/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (Logging (yes dumb...))
    sed -i "/\/data\/log(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/log\/gps(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/log\/wifi(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/hwzd_logs(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/hisi_logs(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/log\/hi110x(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/perf_data_hs.data/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/android_logs(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/log\/fingerprint(\/.*)?/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (Secure storage)
    sed -i "/\/sec_storage/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/sec_storage(\/.*)?/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/sec_storage_data/d" /system/etc/selinux/plat_file_contexts
    sed -i "/\/data\/sec_storage_data_users/d" /system/etc/selinux/plat_file_contexts

    # Remove duplicated labels (Splash2)
    sed -i "/\/splash2(\/.*)?/d" /system/etc/selinux/plat_file_contexts
fi

exit 0

#!/bin/bash
#
# SPDX-FileCopyrightText: 2016 The CyanogenMod Project
# SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

set -e

DEVICE=pro1x
VENDOR=fxtec

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

ANDROID_ROOT="${MY_DIR}/../../.."

export TARGET_ENABLE_CHECKELF=false

HELPER="${ANDROID_ROOT}/tools/extract-utils/extract_utils.sh"
if [ ! -f "${HELPER}" ]; then
    echo "Unable to find helper script at ${HELPER}"
    exit 1
fi
source "${HELPER}"

# Default to sanitizing the vendor folder before extraction
CLEAN_VENDOR=true

ONLY_FIRMWARE=
KANG=
SECTION=

while [ "${#}" -gt 0 ]; do
    case "${1}" in
        --only-firmware )
                ONLY_FIRMWARE=true
                ;;
        -n | --no-cleanup )
                CLEAN_VENDOR=false
                ;;
        -k | --kang )
                KANG="--kang"
                ;;
        -s | --section )
                SECTION="${2}"; shift
                CLEAN_VENDOR=false
                ;;
        * )
                SRC="${1}"
                ;;
    esac
    shift
done

if [ -z "${SRC}" ]; then
    SRC="adb"
fi

function blob_fixup() {
    case "${1}" in
        system_ext/lib64/lib-imsvideocodec.so)
            [ "$2" = "" ] && return 0
            grep -q libgui_shim.so "${2}" || "${PATCHELF}" --add-needed libgui_shim.so "${2}"
            ;;
        vendor/lib64/vendor.qti.hardware.camera.postproc@1.0-service-impl.so)
            [ "$2" = "" ] && return 0
            "${SIGSCAN}" -p "13 0A 00 94" -P "1F 20 03 D5" -f "${2}"
            ;;
        vendor/lib64/hw/camera.qcom.so)
            [ "$2" = "" ] && return 0
            grep -q libcamera_metadata_shim.so "${2}" || "${PATCHELF}" --add-needed libcamera_metadata_shim.so "${2}"
            ;;
        vendor/lib64/libwvhidl.so)
            [ "$2" = "" ] && return 0
            grep -q libcrypto_shim.so "${2}" || "${PATCHELF}" --add-needed "libcrypto_shim.so" "${2}"
            ;;
        system_ext/lib*/libwfdmmsrc_system.so)
            [ "$2" = "" ] && return 0
            grep -q "libgui_shim.so" "${2}" || "${PATCHELF}" --add-needed "libgui_shim.so" "${2}"
            ;;
        system_ext/lib64/libwfdnative.so)
            [ "$2" = "" ] && return 0
            grep -q "libinput_shim.so" "${2}" || "${PATCHELF}" --add-needed "libinput_shim.so" "${2}"
            ;;

        *)
            return 1
            ;;
    esac

    return 0
}

function blob_fixup_dry() {
    blob_fixup "$1" ""
}

# Initialize the helper
setup_vendor "${DEVICE}" "${VENDOR}" "${ANDROID_ROOT}" false "${CLEAN_VENDOR}"

if [ -z "${ONLY_FIRMWARE}" ]; then
    extract "${MY_DIR}/proprietary-files.txt" "${SRC}" "${KANG}" --section "${SECTION}"
fi

if [ -z "${SECTION}" ]; then
    extract_firmware "${MY_DIR}/proprietary-firmware.txt" "${SRC}"
fi

"${MY_DIR}/setup-makefiles.sh"

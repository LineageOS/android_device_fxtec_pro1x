#
# Copyright (C) 2022 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from pro1x device
$(call inherit-product, device/fxtec/pro1x/device.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_NAME := lineage_pro1x
PRODUCT_DEVICE := pro1x
PRODUCT_MANUFACTURER := Fx_tec_Pro1X
PRODUCT_BRAND := Fx_tec_Pro1X
PRODUCT_MODEL := QX1050

PRODUCT_SYSTEM_DEVICE := QX1050
PRODUCT_SYSTEM_NAME := QX1050_2_EEA

PRODUCT_GMS_CLIENTID_BASE := android-fxtec

PRODUCT_BUILD_PROP_OVERRIDES += \
    PRIVATE_BUILD_DESC="QX1050_2_EEA-user 11 RKQ1.211130.001 eng.leilia.20220913 release-keys" \
    TARGET_DEVICE=$(PRODUCT_SYSTEM_DEVICE) \
    TARGET_PRODUCT=$(PRODUCT_SYSTEM_NAME)

BUILD_FINGERPRINT := Fx_tec_Pro1X/QX1050_2_EEA/QX1050:11/RKQ1.211130.001/eng.leilia.20220913:user/release-keys

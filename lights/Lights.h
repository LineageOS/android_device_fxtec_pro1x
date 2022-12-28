/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <aidl/android/hardware/light/BnLights.h>

namespace aidl {
namespace android {
namespace hardware {
namespace light {

enum led_type {
    RED,
    GREEN,
    BLUE,
    WHITE,
};

class Lights : public BnLights {
public:
    Lights();

    ndk::ScopedAStatus setLightState(int id, const HwLightState& state) override;
    ndk::ScopedAStatus getLights(std::vector<HwLight>* types) override;

private:
    void setKeyboardBacklight(const HwLightState& state);
    void setSpeakerLight(const HwLightState& state);
    void handleSpeakerLight();

    bool setLedBlink(led_type led, int onMS, int offMS);
    bool setLedBrightness(led_type led, int value);

    bool IsLit(uint32_t color);
    uint32_t RgbaToBrightness(uint32_t color);
    uint32_t ReadIntFromFile(const std::string& path, uint32_t defaultValue);
    bool WriteToFile(const std::string& path, uint32_t content);

    uint32_t mKeyboardMaxBrightness;
    HwLightState mAttention;
    HwLightState mNotification;
    HwLightState mBattery;
};

}  // namespace light
}  // namespace hardware
}  // namespace android
}  // namespace aidl
/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include "Lights.h"

#include <android-base/file.h>

namespace aidl {
namespace android {
namespace hardware {
namespace light {

#define LED_PATH(led)                       "/sys/class/leds/" led "/"

static const std::string led_paths[] {
    [RED] = LED_PATH("red"),
    [GREEN] = LED_PATH("green"),
    [BLUE] = LED_PATH("blue"),
};

static const std::string kKeyboardBacklightPath = "/sys/class/leds/button-backlight/brightness";
static const std::string kKeyboardMaxBacklightPath = "/sys/class/leds/button-backlight/max_brightness";

#define AutoHwLight(light) {.id = (int)light, .type = light, .ordinal = 0}

// List of supported lights
const static std::vector<HwLight> kAvailableLights = {
    AutoHwLight(LightType::ATTENTION),
    AutoHwLight(LightType::BATTERY),
    AutoHwLight(LightType::KEYBOARD),
    AutoHwLight(LightType::NOTIFICATIONS)
};

Lights::Lights() {
    mKeyboardMaxBrightness = ReadIntFromFile(kKeyboardMaxBacklightPath, 255);
}

// AIDL methods
ndk::ScopedAStatus Lights::setLightState(int id, const HwLightState& state) {
    switch (id) {
        case (int)LightType::ATTENTION:
            mAttention = state;
            handleSpeakerLight();
            break;
        case (int)LightType::BATTERY:
            mBattery = state;
            handleSpeakerLight();
            break;
        case (int)LightType::KEYBOARD:
            setKeyboardBacklight(state);
            break;
        case (int)LightType::NOTIFICATIONS:
            mNotification = state;
            handleSpeakerLight();
            break;
        default:
            return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
            break;
    }

    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus Lights::getLights(std::vector<HwLight>* lights) {
    for (auto i = kAvailableLights.begin(); i != kAvailableLights.end(); i++) {
        lights->push_back(*i);
    }
    return ndk::ScopedAStatus::ok();
}

// device methods
void Lights::setKeyboardBacklight(const HwLightState& state) {
    WriteToFile(kKeyboardBacklightPath, IsLit(state.color) ? mKeyboardMaxBrightness : 0);
}

void Lights::handleSpeakerLight() {
    if (IsLit(mBattery.color)) {
        setSpeakerLight(mBattery);
    } else if (IsLit(mAttention.color)) {
        setSpeakerLight(mAttention);
    } else if (IsLit(mNotification.color)) {
        setSpeakerLight(mNotification);
    } else {
        setLedBrightness(RED, 0);
        setLedBrightness(GREEN, 0);
        setLedBrightness(BLUE, 0);
    }
}

void Lights::setSpeakerLight(const HwLightState& state) {
    uint32_t red, green, blue;
    bool rc = true;

    red = (state.color >> 16) & 0xFF;
    green = (state.color >> 8) & 0xFF;
    blue = state.color & 0xFF;

    switch (state.flashMode) {
        case FlashMode::HARDWARE:
        case FlashMode::TIMED:
            if (!!red)
                rc = setLedBlink(RED, state.flashOnMs, state.flashOffMs);
            if (!!green)
                rc &= setLedBlink(GREEN, state.flashOnMs, state.flashOffMs);
            if (!!blue)
                rc &= setLedBlink(BLUE, state.flashOnMs, state.flashOffMs);
            if (rc)
                break;
            FALLTHROUGH_INTENDED;
        case FlashMode::NONE:
        default:
            setLedBrightness(RED, red);
            setLedBrightness(GREEN, green);
            setLedBrightness(BLUE, blue);
            break;
    }

    return;
}

bool Lights::setLedBlink(led_type led, int onMS, int offMS) {
    bool rc = true;

    rc &= WriteToFile(led_paths[led] + "delay_on", onMS);
    rc &= WriteToFile(led_paths[led] + "delay_off", offMS);
    return rc;
}

bool Lights::setLedBrightness(led_type led, int value) {
    return WriteToFile(led_paths[led] + "brightness", value);
}

// Utils
bool Lights::IsLit(uint32_t color) {
    return color & 0x00ffffff;
}

uint32_t Lights::ReadIntFromFile(const std::string& path, uint32_t defaultValue) {
    std::string buf;

    if (::android::base::ReadFileToString(path, &buf)) {
        return std::stoi(buf);
    }
    return defaultValue;
}

bool Lights::WriteToFile(const std::string& path, uint32_t content) {
    return ::android::base::WriteStringToFile(std::to_string(content), path);
}

}  // namespace light
}  // namespace hardware
}  // namespace android
}  // namespace aidl

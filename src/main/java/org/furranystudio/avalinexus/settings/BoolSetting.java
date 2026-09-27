/**
 * File: BoolSetting.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.settings;

import org.furranystudio.avalinexus.ConfigValue;

public final class BoolSetting implements SettingsRegistry.Setting {

    private final ConfigValue<Boolean> value;

    public BoolSetting(ConfigValue<Boolean> value) {
        this.value = value;
    }

    @Override
    public String get() {
        return String.valueOf(value.get());
    }

    @Override
    public String trySet(String rawValue) {
        if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
            return "'" + rawValue + "' is not a valid boolean (use true/false).";
        }
        value.set(Boolean.parseBoolean(rawValue));
        return null;
    }
}

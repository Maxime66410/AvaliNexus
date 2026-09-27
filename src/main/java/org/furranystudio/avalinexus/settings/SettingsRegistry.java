/**
 * File: SettingsRegistry.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.settings;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SettingsRegistry {

    public interface Setting {
        String get();

        // null if ok, otherwise the error message
        String trySet(String rawValue);
    }

    private static final Map<String, Setting> SETTINGS = new LinkedHashMap<>();

    private SettingsRegistry() {
    }

    public static void register(String name, Setting setting) {
        SETTINGS.put(name, setting);
    }

    public static Setting get(String name) {
        return SETTINGS.get(name);
    }

    public static Iterable<String> names() {
        return SETTINGS.keySet();
    }
}

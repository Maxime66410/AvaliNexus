/**
 * File: Config.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.furranystudio.avalinexus.settings.BoolSetting;
import org.furranystudio.avalinexus.settings.SettingsRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final ConfigValue<Boolean> DEBUG_LOGGING = new ConfigValue<>("debugLogging", false);

    private static final ConfigValue<?>[] VALUES = {
        DEBUG_LOGGING
    };

    private Config() {
    }

    public static void registerSettings() {
        SettingsRegistry.register("debugLogging", new BoolSetting(DEBUG_LOGGING));
    }

    public static void load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                for (ConfigValue<?> value : VALUES) {
                    applyIfPresent(value, root);
                }
            } catch (IOException | RuntimeException e) {
                AvaliNexus.LOGGER.error("[AvaliNexus] Failed to read {}, using defaults.", path, e);
            }
        }
        save();
    }

    @SuppressWarnings("unchecked")
    private static void applyIfPresent(ConfigValue<?> value, JsonObject root) {
        if (!root.has(value.key())) {
            return;
        }
        Object defaultValue = value.defaultValue();
        if (defaultValue instanceof Integer) {
            ((ConfigValue<Integer>) value).setRaw(root.get(value.key()).getAsInt());
        } else if (defaultValue instanceof Boolean) {
            ((ConfigValue<Boolean>) value).setRaw(root.get(value.key()).getAsBoolean());
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (ConfigValue<?> value : VALUES) {
            Object current = value.get();
            if (current instanceof Integer i) {
                root.addProperty(value.key(), i);
            } else if (current instanceof Boolean b) {
                root.addProperty(value.key(), b);
            }
        }

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Failed to write {}", path, e);
        }
    }

    private static Path configPath() {
        return Platform.getGameDir().resolve("config").resolve("avalinexus.json");
    }
}

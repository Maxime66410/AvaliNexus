/**
 * File: ArchiveData.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.archive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArchiveData {

    private static final Identifier ENTRIES = AvaliNexus.id("archives/entries.json");

    public record Archives(List<ArchiveCategory> categories, List<ArchiveEntry> entries) {
    }

    // Reloaded when the resource manager changes, so /reload picks up edited archives
    private static ResourceManager loadedFrom;
    private static Archives archives = new Archives(List.of(), List.of());

    private ArchiveData() {
    }

    public static Archives get(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            archives = load(manager);
        }
        return archives;
    }

    private static Archives load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(ENTRIES);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing archives file {}", ENTRIES);
            return new Archives(List.of(), List.of());
        }
        List<ArchiveCategory> categories = new ArrayList<>();
        List<ArchiveEntry> entries = new ArrayList<>();
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (JsonElement category : root.getAsJsonArray("categories")) {
                categories.add(new ArchiveCategory(category.getAsString()));
            }
            for (JsonElement element : root.getAsJsonArray("entries")) {
                JsonObject entry = element.getAsJsonObject();
                entries.add(new ArchiveEntry(entry.get("id").getAsString(), entry.get("category").getAsString(),
                    Identifier.parse(entry.get("icon").getAsString())));
            }
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read archives file {}", ENTRIES, e);
        }
        return new Archives(categories, entries);
    }
}

/**
 * File: DialogueData.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DialogueData {

    private static final Identifier AVALI_DIALOGUE = AvaliNexus.id("dialogue/avali.json");
    private static final DialogueLine FALLBACK = new DialogueLine("avalinexus.dialogue.avali.fallback", null, null, null);

    // Reloaded when the resource manager changes, so /reload picks up edited dialogue files
    private static ResourceManager loadedFrom;
    private static Map<String, List<DialogueLine>> sections = Map.of();

    private DialogueData() {
    }

    public static DialogueLine pick(MinecraftServer server, String section, AvaliEntity avali, DialogueLine avoid) {
        List<DialogueLine> candidates = new ArrayList<>();
        for (DialogueLine line : lines(server, section)) {
            if (line.matches(avali)) {
                candidates.add(line);
            }
        }
        if (candidates.size() > 1) {
            candidates.remove(avoid);
        }
        if (candidates.isEmpty()) {
            return FALLBACK;
        }
        return candidates.get(avali.getRandom().nextInt(candidates.size()));
    }

    private static List<DialogueLine> lines(MinecraftServer server, String section) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            sections = load(manager);
        }
        return sections.getOrDefault(section, List.of());
    }

    private static Map<String, List<DialogueLine>> load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(AVALI_DIALOGUE);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing dialogue file {}", AVALI_DIALOGUE);
            return Map.of();
        }
        Map<String, List<DialogueLine>> result = new HashMap<>();
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                List<DialogueLine> lines = new ArrayList<>();
                for (JsonElement line : entry.getValue().getAsJsonArray()) {
                    lines.add(DialogueLine.fromJson(line.getAsJsonObject()));
                }
                result.put(entry.getKey(), lines);
            }
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read dialogue file {}", AVALI_DIALOGUE, e);
        }
        return result;
    }
}

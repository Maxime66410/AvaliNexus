/**
 * File: AvaliComposedTexture.java
 * Author: Maxime66410
 * Created: 2026-11-07
 * Last Modified: 2026-11-08
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.cache.BakedModelCache;
import com.geckolib.cache.GeckoLibResources;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AvaliComposedTexture {

    public static final DataTicket<Composed> TICKET = DataTicket.create("avalinexus_composed", Composed.class);

    private static final long KEEP = 10000L;
    private static final Map<List<AvaliLooks.Look>, Entry> ENTRIES = new HashMap<>();
    private static final Map<Identifier, Optional<Source>> SOURCES = new HashMap<>();
    private static BakedModelCache models;
    private static long composedFrame = -1L;
    private static int next;

    public record Composed(Identifier texture, List<Identifier> sources) {
    }

    private record Source(int width, int height, int[] pixels) {
    }

    private static final class Entry {
        private final Composed composed;
        private long lastUsed;

        private Entry(Composed composed) {
            this.composed = composed;
        }
    }

    private AvaliComposedTexture() {
    }

    public static Composed get(AvaliEntity avali) {
        if (avali.isInvisible() || !AvaliLooks.tinted(avali)) {
            return null;
        }
        List<AvaliLooks.Look> layers = new ArrayList<>(5); // Hmm 5 layers
        layers.add(new AvaliLooks.Look(AvaliLooks.texture(avali), avali.getColors().base()));
        add(layers, AvaliLooks.commonPattern(avali));
        add(layers, AvaliLooks.pattern(avali));
        add(layers, AvaliLooks.feathers(avali));
        add(layers, AvaliLooks.fixed(avali));
        checkModels();
        long now = Util.getMillis();
        Entry entry = ENTRIES.get(layers);
        if (entry == null) {
            long frame = Minecraft.getInstance().getFrameTimeNs();
            if (frame == composedFrame) {
                return null;
            }
            composedFrame = frame;
            try (Zone zone = Profiler.get().zone("avali_compose")) {
                entry = new Entry(compose(layers));
            }
            ENTRIES.put(layers, entry);
        }
        entry.lastUsed = now;
        return entry.composed;
    }

    public static void tick() {
        checkModels();
        long now = Util.getMillis();
        Iterator<Entry> iterator = ENTRIES.values().iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (now - entry.lastUsed >= KEEP) {
                release(entry);
                iterator.remove();
            }
        }
    }

    private static void add(List<AvaliLooks.Look> layers, AvaliLooks.Look look) {
        if (look != null) {
            layers.add(look);
        }
    }

    private static void checkModels() {
        BakedModelCache current = GeckoLibResources.getBakedModels();
        if (current != models) {
            models = current;
            ENTRIES.values().forEach(AvaliComposedTexture::release);
            ENTRIES.clear();
            SOURCES.clear();
        }
    }

    private static void release(Entry entry) {
        if (entry.composed != null) {
            Minecraft.getInstance().getTextureManager().release(entry.composed.texture());
        }
    }

    private static Composed compose(List<AvaliLooks.Look> layers) {
        int count = layers.size();
        int[][] pixels = new int[count][];
        int[] reds = new int[count];
        int[] greens = new int[count];
        int[] blues = new int[count];
        List<Identifier> sources = new ArrayList<>(count);
        int width = 0;
        int height = 0;
        for (int i = 0; i < count; i++) {
            AvaliLooks.Look look = layers.get(i);
            Source source = SOURCES.computeIfAbsent(look.texture(), AvaliComposedTexture::load).orElse(null);
            if (source == null || (i > 0 && (source.width() != width || source.height() != height))) {
                return null;
            }
            width = source.width();
            height = source.height();
            pixels[i] = source.pixels();
            reds[i] = (look.color() >> 16) & 0xFF;
            greens[i] = (look.color() >> 8) & 0xFF;
            blues[i] = look.color() & 0xFF;
            sources.add(look.texture());
        }
        int[] composed = new int[width * height];
        for (int index = 0; index < composed.length; index++) {
            for (int i = count - 1; i >= 0; i--) {
                int pixel = pixels[i][index];
                int alpha = pixel >>> 24;
                if (alpha * 10 >= 255) {
                    composed[index] = alpha << 24 | tint((pixel >> 16) & 0xFF, blues[i]) << 16
                        | tint((pixel >> 8) & 0xFF, greens[i]) << 8 | tint(pixel & 0xFF, reds[i]);
                    break;
                }
            }
        }
        NativeImage image = new NativeImage(width, height, false);
        MemoryUtil.memIntBuffer(image.getPointer(), composed.length).put(composed);
        Identifier id = AvaliNexus.id("dynamic/avali/" + next++);
        try (Zone zone = Profiler.get().zone("avali_compose_upload")) {
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
        }
        return new Composed(id, List.copyOf(sources));
    }

    private static int tint(int value, int tint) {
        return (value * tint + 127) / 255;
    }

    private static Optional<Source> load(Identifier texture) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
            return Optional.of(new Source(image.getWidth(), image.getHeight(), image.getPixelsABGR()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}

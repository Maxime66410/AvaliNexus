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
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

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

    private static final int PER_WINDOW = 3;
    private static final long WINDOW = 16L;
    private static final long KEEP = 10000L;
    private static final Map<List<AvaliLooks.Look>, Entry> ENTRIES = new HashMap<>();
    private static final Map<Identifier, Optional<Source>> SOURCES = new HashMap<>();
    private static BakedModelCache models;
    private static long windowStart;
    private static int made;
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
            if (now - windowStart >= WINDOW) {
                windowStart = now;
                made = 0;
            }
            if (made >= PER_WINDOW) {
                return null;
            }
            made++;
            entry = new Entry(compose(layers));
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
        int[] tints = new int[count];
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
            tints[i] = look.color();
            sources.add(look.texture());
        }
        NativeImage image = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = x + y * width;
                for (int i = count - 1; i >= 0; i--) {
                    int pixel = pixels[i][index];
                    int alpha = ARGB.alpha(pixel);
                    if (alpha * 10 >= 255) {
                        image.setPixel(x, y, ARGB.color(alpha, tint(ARGB.red(pixel), ARGB.red(tints[i])),
                            tint(ARGB.green(pixel), ARGB.green(tints[i])), tint(ARGB.blue(pixel), ARGB.blue(tints[i]))));
                        break;
                    }
                }
            }
        }
        Identifier id = AvaliNexus.id("dynamic/avali/" + next++);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
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
            return Optional.of(new Source(image.getWidth(), image.getHeight(), image.getPixels()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}

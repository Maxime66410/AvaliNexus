/**
 * File: ModKeyCategory.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.client;

import net.minecraft.client.KeyMapping;
import org.furranystudio.avalinexus.AvaliNexus;

public final class ModKeyCategory {

    public static final KeyMapping.Category AVALI_NEXUS = new KeyMapping.Category(AvaliNexus.id("main"));

    private ModKeyCategory() {
    }
}

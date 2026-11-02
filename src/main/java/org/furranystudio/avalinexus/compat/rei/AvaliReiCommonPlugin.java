/**
 * File: AvaliReiCommonPlugin.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.rei;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import org.furranystudio.avalinexus.AvaliNexus;

public class AvaliReiCommonPlugin implements REICommonPlugin {

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(AvaliNexus.id("machine"), AvaliDisplay.SERIALIZER);
    }
}

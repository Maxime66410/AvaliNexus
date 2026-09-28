/**
 * File: AvaliNexusModMenu.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.furranystudio.avalinexus.client.settings.AvaliSettingsScreen;

// Only loaded when Mod Menu is installed, it adds the config button to our entry
public final class AvaliNexusModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AvaliSettingsScreen::new;
    }
}

/**
 * File: Platform.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import java.nio.file.Path;

public final class Platform {

    private static Path gameDir;

    private Platform() {
    }

    public static void init(Path gameDir) {
        Platform.gameDir = gameDir;
    }

    public static Path getGameDir() {
        return gameDir;
    }
}

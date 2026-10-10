/**
 * File: RailStats.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.item.weapon;

public record RailStats(float damage, int interval, int magazine, int reload, float speed,
                        float hipSpread, float aimSpread, float zoom, float recoil, boolean automatic, int pellets,
                        boolean shellByShell) {
}

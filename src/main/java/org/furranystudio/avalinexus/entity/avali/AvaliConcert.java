/**
 * File: AvaliConcert.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliMusicGoal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvaliConcert {

    private static final Map<ResourceKey<Level>, List<AvaliConcert>> ACTIVE = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<Silence>> SILENCES = new HashMap<>();
    private static final double ARRIVED = 1.0;

    private record Silence(Vec3 position, long until) {
    }

    private final ResourceKey<Level> dimension;
    private final AvaliMusic.Song song;
    private final Vec3 center;
    private final AvaliEntity[] performers;
    private final long gatherUntil;
    private long startTime = -1;
    private boolean ended;

    private AvaliConcert(ServerLevel level, AvaliMusic.Song song, Vec3 center, long gatherUntil) {
        this.dimension = level.dimension();
        this.song = song;
        this.center = center;
        this.performers = new AvaliEntity[song.parts().size()];
        this.gatherUntil = gatherUntil;
    }

    public static boolean isQuiet(ServerLevel level, Vec3 position, double radius) {
        long now = level.getGameTime();
        List<AvaliConcert> active = ACTIVE.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        active.removeIf(concert -> concert.ended || !concert.leader().isAlive());
        List<Silence> silences = SILENCES.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        silences.removeIf(silence -> silence.until() <= now);
        double radiusSq = radius * radius;
        return active.stream().noneMatch(concert -> concert.center.distanceToSqr(position) <= radiusSq)
            && silences.stream().noneMatch(silence -> silence.position().distanceToSqr(position) <= radiusSq);
    }

    public static AvaliConcert start(ServerLevel level, AvaliEntity leader, AvaliMusic.Song song, Vec3 center, int gatherTicks) {
        AvaliConcert concert = new AvaliConcert(level, song, center, level.getGameTime() + (song.isGroup() ? gatherTicks : 0));
        concert.join(leader);
        ACTIVE.computeIfAbsent(level.dimension(), key -> new ArrayList<>()).add(concert);
        return concert;
    }

    public static int stopNear(ServerLevel level, Vec3 position, double radius) {
        List<AvaliConcert> near = ACTIVE.getOrDefault(level.dimension(), List.of()).stream()
            .filter(concert -> concert.center.distanceTo(position) <= radius).toList();
        near.forEach(concert -> concert.end(level, 0));
        return near.size();
    }

    public void recruit(ServerLevel level, double radius) {
        AvaliEntity leader = leader();
        level.getEntitiesOfClass(AvaliEntity.class, leader.getBoundingBox().inflate(radius),
                other -> other != leader && other.getPackId() != null && other.getPackId().equals(leader.getPackId())
                    && other.getConcert() == null && !other.isCompanion() && AvaliMusicGoal.isCalm(other) && leader.distanceTo(other) <= radius)
            .stream().sorted(Comparator.comparingDouble(leader::distanceToSqr))
            .takeWhile(other -> !isFull())
            .forEach(this::join);
    }

    public boolean join(AvaliEntity avali) {
        for (int i = 0; i < performers.length; i++) {
            if (performers[i] == null) {
                performers[i] = avali;
                avali.setConcert(this);
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        for (AvaliEntity performer : performers) {
            if (performer == null) {
                return false;
            }
        }
        return true;
    }

    public void tick(ServerLevel level) {
        if (startTime >= 0 || ended) {
            return;
        }
        boolean gathered = true;
        for (int i = 0; i < performers.length; i++) {
            if (performers[i] != null && performers[i].position().distanceTo(spot(i)) > ARRIVED) {
                gathered = false;
            }
        }
        if (gathered || level.getGameTime() >= gatherUntil) {
            startTime = level.getGameTime();
        }
    }

    public void end(ServerLevel level, int silenceTicks) {
        if (ended) {
            return;
        }
        ended = true;
        ACTIVE.getOrDefault(dimension, List.of()).remove(this);
        if (startTime >= 0) {
            SILENCES.computeIfAbsent(dimension, key -> new ArrayList<>()).add(new Silence(center, level.getGameTime() + silenceTicks));
        }
    }

    public void leave(ServerLevel level, AvaliEntity avali, int silenceTicks) {
        if (leader() == avali) {
            end(level, silenceTicks);
            return;
        }
        for (int i = 0; i < performers.length; i++) {
            if (performers[i] == avali) {
                performers[i] = null;
            }
        }
    }

    public Vec3 spot(int part) {
        if (!song.isGroup()) {
            return center;
        }
        float angle = Mth.TWO_PI * part / performers.length;
        return center.add(Mth.cos(angle) * groupRadius(), 0.0, Mth.sin(angle) * groupRadius());
    }

    private double groupRadius() {
        return Math.max(1.5, performers.length * 0.75);
    }

    public int partOf(AvaliEntity avali) {
        for (int i = 0; i < performers.length; i++) {
            if (performers[i] == avali) {
                return i;
            }
        }
        return -1;
    }

    public AvaliEntity leader() {
        return performers[0];
    }

    public AvaliMusic.Song song() {
        return song;
    }

    public Vec3 center() {
        return center;
    }

    public boolean isPlaying() {
        return startTime >= 0 && !ended;
    }

    public boolean isEnded() {
        return ended;
    }

    public long elapsed(long now) {
        return now - startTime;
    }
}

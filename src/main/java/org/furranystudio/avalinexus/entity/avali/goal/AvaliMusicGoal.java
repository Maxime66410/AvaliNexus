/**
 * File: AvaliMusicGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliConcert;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;
import org.furranystudio.avalinexus.entity.avali.AvaliMusic;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliGesture;

import java.util.EnumSet;
import java.util.List;

public class AvaliMusicGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final int REPATH_INTERVAL = 20;
    private static final int TICKS_PER_DAY = 24000;
    private static final int TICKS_PER_SECOND = 20;
    private static final double ARRIVED = 1.0;
    private static final double WALK_SPEED = 0.8;
    private static final double LOOK_RANGE = 8.0;
    private static final double PARTICLE_HEIGHT = 0.3;

    private final AvaliEntity avali;
    private int checkCooldown;
    private int repath;
    private boolean performing;

    public AvaliMusicGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public static boolean isCalm(AvaliEntity avali) {
        return !avali.isSleeping() && !avali.isPassenger() && !avali.isInWater() && avali.getTalkingTo() == null && !avali.isFollowingOwner()
            && avali.getTarget() == null && avali.hurtTime == 0 && !avali.getMood().earsDown();
    }

    @Override
    public boolean canUse() {
        AvaliConcert concert = avali.getConcert();
        if (concert != null) {
            return !concert.isEnded() && isCalm(avali);
        }
        if (--checkCooldown > 0 || !(avali.level() instanceof ServerLevel level)) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        AvaliBehavior.Music music = AvaliBehavior.get(level.getServer()).music();
        if (!isCalm(avali) || level.getGameTime() < avali.getMusicRestUntil() || !AvaliFight.enemies(avali).isEmpty()) {
            return false;
        }
        long time = level.getOverworldClockTime();
        long timeOfDay = time % TICKS_PER_DAY;
        boolean evening = timeOfDay >= music.eveningStart() && timeOfDay <= music.eveningEnd();
        if (avali.getRandom().nextDouble() >= music.startChance() * (evening ? music.eveningMultiplier() : 1.0)) {
            return false;
        }
        long day = time / TICKS_PER_DAY;
        List<AvaliMusic.Song> songs = AvaliMusic.get(level.getServer()).stream()
            .filter(song -> !avali.playedToday(song.id(), day)).toList();
        if (songs.isEmpty()) {
            return false;
        }
        AvaliMusic.Song song = songs.get(avali.getRandom().nextInt(songs.size()));
        Vec3 center = song.isGroup() ? avali.getPackCenter() : avali.position();
        if (!AvaliConcert.isQuiet(level, center, music.zoneRadius())) {
            return false;
        }
        AvaliConcert started = AvaliConcert.start(level, avali, song, center, music.gatherSeconds() * TICKS_PER_SECOND);
        AvaliNexus.debug("{}: starts the song {}{}", avali.debugName(), song.id(), song.isGroup() ? " with its pack" : "");
        if (song.isGroup()) {
            started.recruit(level, music.joinRadius());
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        AvaliConcert concert = avali.getConcert();
        return concert != null && !concert.isEnded() && isCalm(avali);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        repath = 0;
        performing = false;
    }

    @Override
    public void stop() {
        AvaliConcert concert = avali.getConcert();
        if (avali.level() instanceof ServerLevel level && concert != null) {
            AvaliBehavior.Music music = AvaliBehavior.get(level.getServer()).music();
            if (performing) {
                avali.markPlayed(concert.song().id(), level.getOverworldClockTime() / TICKS_PER_DAY);
                avali.setMusicRestUntil(level.getGameTime() + (long) music.restSeconds() * TICKS_PER_SECOND);
            }
            concert.leave(level, avali, silenceTicks(music));
            avali.setConcert(null);
        }
        if (performing) {
            avali.stopGesture();
            avali.setDialogueFace(null);
        }
        performing = false;
        avali.getNavigation().stop();
    }

    @Override
    public void tick() {
        AvaliConcert concert = avali.getConcert();
        if (!(avali.level() instanceof ServerLevel level) || concert == null) {
            return;
        }
        int part = concert.partOf(avali);
        if (part < 0) {
            return;
        }
        if (concert.leader() == avali) {
            concert.tick(level);
        }
        moveToSpot(concert.spot(part));
        lookAround(concert);
        if (!concert.isPlaying()) {
            return;
        }
        AvaliBehavior.Music music = AvaliBehavior.get(level.getServer()).music();
        if (!performing) {
            performing = true;
            avali.playGesture(AvaliGesture.WAG_TAIL, Integer.MAX_VALUE);
            if (!music.faces().isEmpty()) {
                avali.setDialogueFace(music.faces().get(avali.getRandom().nextInt(music.faces().size())));
            }
        }
        AvaliMusic.Song song = concert.song();
        long elapsed = concert.elapsed(level.getGameTime());
        if (elapsed >= song.length()) {
            if (concert.leader() == avali) {
                concert.end(level, silenceTicks(music));
            }
            return;
        }
        AvaliMusic.Part played = song.parts().get(part);
        int[] notes = played.notes().get((int) elapsed);
        if (notes != null) {
            AvaliMusic.playNotes(level, avali.getEyePosition(), avali.position().add(0.0, avali.getBbHeight() + PARTICLE_HEIGHT, 0.0),
                played, notes, music);
        }
    }

    private void moveToSpot(Vec3 spot) {
        if (avali.position().distanceTo(spot) <= ARRIVED) {
            avali.getNavigation().stop();
            return;
        }
        if (--repath <= 0 || avali.getNavigation().isDone()) {
            repath = REPATH_INTERVAL;
            avali.getNavigation().moveTo(spot.x, spot.y, spot.z, WALK_SPEED);
        }
    }

    private void lookAround(AvaliConcert concert) {
        if (concert.song().isGroup()) {
            Vec3 center = concert.center();
            avali.getLookControl().setLookAt(center.x, avali.getEyeY(), center.z);
            return;
        }
        Player player = avali.level().getNearestPlayer(avali, LOOK_RANGE);
        if (player != null) {
            avali.getLookControl().setLookAt(player);
        }
    }

    private int silenceTicks(AvaliBehavior.Music music) {
        return Mth.nextInt(avali.getRandom(), music.silenceMinSeconds(), music.silenceMaxSeconds()) * TICKS_PER_SECOND;
    }
}

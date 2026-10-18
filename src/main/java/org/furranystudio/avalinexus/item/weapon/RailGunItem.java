/**
 * File: RailGunItem.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.item.weapon;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.client.weapon.RailGunRenderer;
import org.furranystudio.avalinexus.entity.projectile.NexiteQuill;
import org.furranystudio.avalinexus.registry.RegistryEntry;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RailGunItem extends Item implements GeoItem {

    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation AIM = RawAnimation.begin().thenLoop("aim");
    public static final RawAnimation RELOAD = RawAnimation.begin().thenPlay("reload");
    public static final RawAnimation RELOAD_START = RawAnimation.begin().thenPlay("reload_start");
    public static final RawAnimation RELOAD_SHELL = RawAnimation.begin().thenLoop("reload_shell");
    public static final RawAnimation RELOAD_END = RawAnimation.begin().thenPlay("reload_end");
    public static final RawAnimation FIRE = RawAnimation.begin().thenPlay("fire");
    public static final DataTicket<Boolean> AIMING = DataTicket.create("avalinexus_rail_aiming", Boolean.class);
    public static final DataTicket<Boolean> RELOADING = DataTicket.create("avalinexus_rail_reloading", Boolean.class);
    public static final DataTicket<ReloadStep> RELOAD_STEP = DataTicket.create("avalinexus_rail_reload_step", ReloadStep.class);
    public static final DataTicket<Boolean> FIRST_PERSON = DataTicket.create("avalinexus_rail_first_person", Boolean.class);

    private static final int BAR_COLOR = 0xFF8C1A;
    private static final DustParticleOptions MUZZLE_FLASH = new DustParticleOptions(0xFFFFFF, 0.8F);
    // Must match the reload_start, reload_shell and reload_end animation lengths
    public static final int SHELL_START = 8;
    public static final int SHELL_INSERT = 10;
    public static final int SHELL_END = 10;

    public enum ReloadStep {
        NONE, FULL, START, SHELL, END
    }
    private static final Map<UUID, Long> NEXT_SHOT = new HashMap<>();

    private final RailStats stats;
    private final RegistryEntry<SoundEvent> fireSound;
    private final RegistryEntry<RailMagazineItem> magazine;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RailGunItem(RailStats stats, RegistryEntry<SoundEvent> fireSound, RegistryEntry<RailMagazineItem> magazine, Properties properties) {
        super(properties);
        this.stats = stats;
        this.fireSound = fireSound;
        this.magazine = magazine;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            new AnimationController<RailGunItem>("pose", 4, test -> {
                if (!test.getDataOrDefault(FIRST_PERSON, false) || test.getDataOrDefault(RELOADING, false)) {
                    return PlayState.STOP;
                }
                return test.setAndContinue(test.getDataOrDefault(AIMING, false) ? AIM : IDLE);
            }),
            // A blend would delay the reload and cut its end
            // Reset between reloads or the finished animation never plays again
            new AnimationController<RailGunItem>("reload", 0, test -> {
                if (!test.getDataOrDefault(FIRST_PERSON, false) || !test.getDataOrDefault(RELOADING, false)) {
                    test.controller().reset();
                    return PlayState.STOP;
                }
                return test.setAndContinue(switch (test.getDataOrDefault(RELOAD_STEP, ReloadStep.FULL)) {
                    case START -> RELOAD_START;
                    case SHELL -> RELOAD_SHELL;
                    case END -> RELOAD_END;
                    default -> RELOAD;
                });
            }),
            new AnimationController<RailGunItem>("shoot", 0, test -> PlayState.STOP).triggerableAnim("fire", FIRE));
    }

    // The hand and the hotbar render the same gun each frame and would reset each other
    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // The renderer is a client class, only loaded when the client asks for it
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private RailGunRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new RailGunRenderer(RailGunItem.this);
                }
                return renderer;
            }
        });
    }

    public RailStats stats() {
        return stats;
    }

    public static int ammo(ItemStack gun) {
        return gun.getOrDefault(RailWeapons.AMMO.get(), 0);
    }

    public static boolean isReloading(ItemStack gun) {
        return gun.has(RailWeapons.RELOAD_END.get());
    }

    public static ReloadStep reloadStep(ItemStack gun, long now) {
        Long start = gun.get(RailWeapons.RELOAD_START.get());
        if (!isReloading(gun)) {
            return ReloadStep.NONE;
        }
        if (start == null || !(gun.getItem() instanceof RailGunItem item) || !item.stats.shellByShell()) {
            return ReloadStep.FULL;
        }
        long elapsed = now - start;
        int shells = gun.getOrDefault(RailWeapons.RELOAD_SHELLS.get(), 0);
        if (elapsed < SHELL_START) {
            return ReloadStep.START;
        }
        return elapsed < SHELL_START + (long) shells * SHELL_INSERT ? ReloadStep.SHELL : ReloadStep.END;
    }

    public static float reloadProgress(ItemStack gun, float now) {
        Long start = gun.get(RailWeapons.RELOAD_START.get());
        Long end = gun.get(RailWeapons.RELOAD_END.get());
        if (start == null || end == null || end <= start) {
            return 0.0F;
        }
        return Math.clamp((now - start) / (end - start), 0.0F, 1.0F);
    }

    private static void stopReload(ItemStack gun) {
        gun.remove(RailWeapons.RELOAD_END.get());
        gun.remove(RailWeapons.RELOAD_START.get());
        gun.remove(RailWeapons.RELOAD_SHELLS.get());
        gun.remove(RailWeapons.RELOAD_LOADED.get());
    }

    private static void cutShells(ItemStack gun, int shells) {
        long start = gun.getOrDefault(RailWeapons.RELOAD_START.get(), 0L);
        gun.set(RailWeapons.RELOAD_SHELLS.get(), shells);
        gun.set(RailWeapons.RELOAD_END.get(), start + SHELL_START + (long) shells * SHELL_INSERT + SHELL_END);
    }

    public boolean shellByShell() {
        return stats.shellByShell();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || isReloading(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack gun, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack gun) {
        return ItemUseAnimation.NONE;
    }

    public static boolean isAiming(Player player) {
        return player.isUsingItem() && player.getUseItem().getItem() instanceof RailGunItem;
    }

    public void fire(ServerPlayer player, ItemStack gun) {
        ServerLevel level = player.level();
        long now = level.getGameTime();
        if (isReloading(gun)) {
            if (stats.shellByShell() && reloadStep(gun, now) != ReloadStep.END) {
                int loaded = gun.getOrDefault(RailWeapons.RELOAD_LOADED.get(), 0);
                cutShells(gun, Math.min(gun.getOrDefault(RailWeapons.RELOAD_SHELLS.get(), 0), loaded + 1));
            }
            return;
        }
        if (now < NEXT_SHOT.getOrDefault(player.getUUID(), 0L)) {
            return;
        }
        // One tick of slack so packet timing never eats a shot
        NEXT_SHOT.put(player.getUUID(), now + Math.max(1, stats.interval() - 1));
        int ammo = ammo(gun);
        boolean creative = player.hasInfiniteMaterials();
        if (ammo <= 0 && !creative) {
            ModSounds.play(level, player, ModSounds.RAIL_EMPTY.get(), 0.6F, 0.05F);
            startReload(player, gun);
            return;
        }
        float spread = isAiming(player) ? stats.aimSpread() : stats.hipSpread();
        for (int i = 0; i < stats.pellets(); i++) {
            Projectile.spawnProjectileFromRotation((serverLevel, shooter, stack) -> new NexiteQuill(serverLevel, shooter, stats.damage()),
                level, new ItemStack(RailWeapons.NEXITE_QUILL.get()), player, 0.0F, stats.speed(), spread);
        }
        if (!creative) {
            gun.set(RailWeapons.AMMO.get(), ammo - 1);
        }
        triggerAnim(player, GeoItem.getOrAssignId(gun, level), "shoot", "fire");
        ModSounds.play(level, player, fireSound.get(), 0.9F, 0.08F);
        muzzleFlash(level, muzzle(player, player.getLookAngle(), isAiming(player)));
    }

    public void shootAt(LivingEntity shooter, ItemStack gun, LivingEntity target) {
        if (!(shooter.level() instanceof ServerLevel level)) {
            return;
        }
        double dx = target.getX() - shooter.getX();
        double dy = target.getY(0.5) - shooter.getEyeY();
        double dz = target.getZ() - shooter.getZ();
        for (int i = 0; i < stats.pellets(); i++) {
            Projectile.spawnProjectileUsingShoot((serverLevel, owner, stack) -> new NexiteQuill(serverLevel, owner, stats.damage()),
                level, new ItemStack(RailWeapons.NEXITE_QUILL.get()), shooter, dx, dy, dz, stats.speed(), stats.hipSpread());
        }
        triggerAnim(shooter, GeoItem.getOrAssignId(gun, level), "shoot", "fire");
        ModSounds.play(level, shooter, fireSound.get(), 0.9F, 0.08F);
        muzzleFlash(level, shooter.getEyePosition().add(new Vec3(dx, dy, dz).normalize().scale(0.9)).add(0.0, -0.2, 0.0));
    }

    private static Vec3 muzzle(LivingEntity shooter, Vec3 look, boolean aiming) {
        Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
        return shooter.getEyePosition().add(look.scale(0.9)).add(right.scale(aiming ? 0.0 : 0.3)).add(0.0, aiming ? -0.08 : -0.2, 0.0);
    }

    private static void muzzleFlash(ServerLevel level, Vec3 muzzle) {
        level.sendParticles(MUZZLE_FLASH, muzzle.x, muzzle.y, muzzle.z, 4, 0.03, 0.03, 0.03, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, muzzle.x, muzzle.y, muzzle.z, 3, 0.02, 0.02, 0.02, 0.15);
    }

    public void startReload(ServerPlayer player, ItemStack gun) {
        if (isReloading(gun) || ammo(gun) >= stats.magazine()) {
            return;
        }
        if (!player.hasInfiniteMaterials()) {
            if (magazine == null && countQuills(player.getInventory()) == 0) {
                player.sendOverlayMessage(Component.translatable("item.avalinexus.rail_gun.no_quills"));
                return;
            }
            if (magazine != null && bestMagazine(player.getInventory()) < 0) {
                player.sendOverlayMessage(Component.translatable("item.avalinexus.rail_gun.no_magazine"));
                return;
            }
        }
        player.stopUsingItem();
        long now = player.level().getGameTime();
        gun.set(RailWeapons.RELOAD_START.get(), now);
        if (stats.shellByShell()) {
            int missing = stats.magazine() - ammo(gun);
            int shells = player.hasInfiniteMaterials() ? missing : Math.min(missing, countQuills(player.getInventory()));
            gun.set(RailWeapons.RELOAD_LOADED.get(), 0);
            cutShells(gun, shells);
        } else {
            gun.set(RailWeapons.RELOAD_END.get(), now + stats.reload());
        }
        ModSounds.play(player.level(), player, ModSounds.RAIL_RELOAD_START.get(), 0.8F, 0.04F);
    }

    @Override
    public void inventoryTick(ItemStack gun, ServerLevel level, Entity entity, EquipmentSlot slot) {
        GeoItem.getOrAssignId(gun, level);
        Long end = gun.get(RailWeapons.RELOAD_END.get());
        if (end == null || !(entity instanceof Player player)) {
            return;
        }
        if (slot != EquipmentSlot.MAINHAND) {
            stopReload(gun);
            return;
        }
        if (stats.shellByShell()) {
            tickShells(gun, level, player);
            if (level.getGameTime() >= gun.getOrDefault(RailWeapons.RELOAD_END.get(), 0L)) {
                stopReload(gun);
                ModSounds.play(level, player, ModSounds.RAIL_SHOTGUN_PUMP.get(), 0.8F, 0.04F);
            }
            return;
        }
        if (level.getGameTime() < end) {
            return;
        }
        stopReload(gun);
        if (player.hasInfiniteMaterials()) {
            gun.set(RailWeapons.AMMO.get(), stats.magazine());
        } else if (!swapMagazine(gun, player.getInventory())) {
            return;
        }
        ModSounds.play(level, player, ModSounds.RAIL_RELOAD_END.get(), 0.8F, 0.04F);
    }

    private void tickShells(ItemStack gun, ServerLevel level, Player player) {
        long elapsed = level.getGameTime() - gun.getOrDefault(RailWeapons.RELOAD_START.get(), 0L);
        int shells = gun.getOrDefault(RailWeapons.RELOAD_SHELLS.get(), 0);
        int loaded = gun.getOrDefault(RailWeapons.RELOAD_LOADED.get(), 0);
        int due = (int) Math.clamp((elapsed - SHELL_START) / SHELL_INSERT, 0, shells);
        while (loaded < due) {
            if (!player.hasInfiniteMaterials() && takeQuills(player.getInventory(), 1) == 0) {
                cutShells(gun, loaded);
                break;
            }
            loaded++;
            gun.set(RailWeapons.AMMO.get(), ammo(gun) + 1);
            gun.set(RailWeapons.RELOAD_LOADED.get(), loaded);
            ModSounds.play(level, player, ModSounds.RAIL_SHELL_INSERT.get(), 0.7F, 0.06F);
        }
    }

    public void ejectMagazine(ServerPlayer player, ItemStack gun) {
        if (magazine == null || isReloading(gun) || !gun.getOrDefault(RailWeapons.MAGAZINE_IN.get(), false)) {
            return;
        }
        ItemStack out = RailMagazineItem.withAmmo(magazine.get(), ammo(gun));
        gun.set(RailWeapons.AMMO.get(), 0);
        gun.set(RailWeapons.MAGAZINE_IN.get(), false);
        if (!player.getInventory().add(out)) {
            player.spawnAtLocation(player.level(), out);
        }
        ModSounds.play(player.level(), player, ModSounds.RAIL_RELOAD_START.get(), 0.8F, 0.04F);
    }

    // The magazine taken out goes back in the slot the new one came from, with what it had left
    private boolean swapMagazine(ItemStack gun, Inventory inventory) {
        int slot = bestMagazine(inventory);
        if (slot < 0) {
            return false;
        }
        int loaded = ammo(inventory.getItem(slot));
        ItemStack previous = gun.getOrDefault(RailWeapons.MAGAZINE_IN.get(), false)
            ? RailMagazineItem.withAmmo(magazine.get(), ammo(gun))
            : ItemStack.EMPTY;
        inventory.setItem(slot, previous);
        gun.set(RailWeapons.AMMO.get(), loaded);
        gun.set(RailWeapons.MAGAZINE_IN.get(), true);
        return true;
    }

    private int bestMagazine(Inventory inventory) {
        int best = -1;
        int bestAmmo = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(magazine.get()) && ammo(stack) > bestAmmo) {
                best = i;
                bestAmmo = ammo(stack);
            }
        }
        return best;
    }

    public int countMagazines(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(magazine.get()) && ammo(stack) > 0) {
                count++;
            }
        }
        return count;
    }

    public boolean usesMagazines() {
        return magazine != null;
    }

    public static int countQuills(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(RailWeapons.NEXITE_QUILL.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int takeQuills(Inventory inventory, int wanted) {
        int taken = 0;
        for (int i = 0; i < inventory.getContainerSize() && taken < wanted; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(RailWeapons.NEXITE_QUILL.get())) {
                int take = Math.min(wanted - taken, stack.getCount());
                stack.shrink(take);
                taken += take;
            }
        }
        return taken;
    }

    @Override
    public boolean isBarVisible(ItemStack gun) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack gun) {
        return Math.round(13.0F * ammo(gun) / stats.magazine());
    }

    @Override
    public int getBarColor(ItemStack gun) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack gun, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.avalinexus.rail_gun.ammo", ammo(gun), stats.magazine()).withStyle(style -> style.withColor(0xFFA640)));
        tooltip.accept(Component.translatable("item.avalinexus.rail_gun.controls", Component.keybind("key.avalinexus.reload")).withStyle(style -> style.withColor(0xAAAAAA)));
    }
}

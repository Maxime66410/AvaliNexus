/**
 * File: AvaliFight.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali;

import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public final class AvaliFight {

    private AvaliFight() {
    }

    public static AvaliCombat.Data data(AvaliEntity avali) {
        return AvaliCombat.get(avali.level().getServer());
    }

    public static boolean isValidEnemy(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity instanceof AvaliEntity) {
            return false;
        }
        return !(entity instanceof Player player) || !player.isCreative() && !player.isSpectator();
    }

    public static List<LivingEntity> enemies(AvaliEntity avali) {
        AvaliCombat.Data data = data(avali);
        LivingEntity threat = avali.getThreat();
        return avali.level().getEntitiesOfClass(LivingEntity.class, avali.getBoundingBox().inflate(data.enemyRadius()),
            entity -> isValidEnemy(entity) && avali.distanceTo(entity) <= data.enemyRadius()
                && (entity == threat || entity instanceof Mob mob && mob.getTarget() instanceof AvaliEntity
                    || entity instanceof Enemy && data.hunts(entity.getType())));
    }

    public static LivingEntity nearest(AvaliEntity avali, List<LivingEntity> entities) {
        return entities.stream().min(Comparator.comparingDouble(avali::distanceToSqr)).orElse(null);
    }

    public static Creeper swellingCreeper(AvaliEntity avali) {
        double radius = data(avali).creeper().detectRadius();
        return avali.level().getEntitiesOfClass(Creeper.class, avali.getBoundingBox().inflate(radius),
                creeper -> creeper.isAlive() && creeper.getSwellDir() > 0 && avali.distanceTo(creeper) <= radius)
            .stream().min(Comparator.comparingDouble(avali::distanceToSqr)).orElse(null);
    }

    public static int allies(AvaliEntity avali) {
        double radius = data(avali).allyRadius();
        return avali.level().getEntitiesOfClass(AvaliEntity.class, avali.getBoundingBox().inflate(radius),
            other -> other.isAlive() && !other.isBaby() && other.getPackId() != null
                && other.getPackId().equals(avali.getPackId()) && avali.distanceTo(other) <= radius).size();
    }

    public static boolean outnumbers(AvaliEntity avali, int allies, int enemies) {
        return enemies > 0 && allies >= enemies * data(avali).outnumberRatio();
    }

    public static boolean outclasses(AvaliEntity avali, List<LivingEntity> enemies) {
        if (enemies.isEmpty()) {
            return false;
        }
        AvaliCombat.Data data = data(avali);
        ItemStack weapon = avali.getGear().strongestWeapon();
        float damage = (float) AvaliEquipment.weaponDamage(weapon);
        double speed = AvaliEquipment.attacksPerSecond(weapon);
        DamageSource ours = avali.damageSources().mobAttack(avali);
        double timeToWin = 0.0;
        double incoming = 0.0;
        for (LivingEntity enemy : enemies) {
            float hit = CombatRules.getDamageAfterAbsorb(enemy, damage, ours, enemy.getArmorValue(),
                (float) enemy.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            timeToWin += enemy.getHealth() / Math.max(hit * speed, 0.01);
            double enemyDamage = enemy.getAttribute(Attributes.ATTACK_DAMAGE) != null
                ? enemy.getAttributeValue(Attributes.ATTACK_DAMAGE) : data.enemyDefaultDamage();
            float taken = CombatRules.getDamageAfterAbsorb(avali, (float) enemyDamage, avali.damageSources().mobAttack(enemy),
                avali.getArmorValue(), (float) avali.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            incoming += taken * data.enemyAttacksPerSecond();
        }
        double timeToLose = avali.getHealth() / Math.max(incoming, 0.01);
        return timeToLose >= timeToWin * data.powerMargin();
    }

    public static boolean shouldFlee(AvaliEntity avali, List<LivingEntity> enemies) {
        if (enemies.isEmpty()) {
            return false;
        }
        if (avali.isBaby()) {
            return true;
        }
        if (avali.isCornered()) {
            return false;
        }
        AvaliCombat.Data data = data(avali);
        if (avali.getHealth() < avali.getMaxHealth() * data.fleeHealth()) {
            return true;
        }
        if (avali.getGear().hasRanged() && !avali.getGear().hasMelee() && avali.distanceTo(nearest(avali, enemies)) <= data.rangedMin()) {
            return true;
        }
        if (outclasses(avali, enemies)) {
            return false;
        }
        int allies = allies(avali);
        if (outnumbers(avali, allies, enemies.size())) {
            return false;
        }
        return !avali.getGear().hasWeapon() || allies <= 1 || enemies.size() >= data.tooManyEnemies();
    }

    public static boolean canAttack(AvaliEntity avali, List<LivingEntity> enemies, boolean fighting) {
        if (avali.isBaby()) {
            return false;
        }
        if (avali.isCornered()) {
            return true;
        }
        AvaliCombat.Data data = data(avali);
        float threshold = fighting ? data.fleeHealth() : data.attackHealth();
        if (avali.getHealth() < avali.getMaxHealth() * threshold) {
            return false;
        }
        if (outclasses(avali, enemies)) {
            return true;
        }
        int allies = allies(avali);
        int count = Math.max(enemies.size(), 1);
        if (outnumbers(avali, allies, count)) {
            return true;
        }
        return avali.getGear().hasWeapon() && avali.getGear().hasFood() && allies > 1 && count < data.tooManyEnemies();
    }
}

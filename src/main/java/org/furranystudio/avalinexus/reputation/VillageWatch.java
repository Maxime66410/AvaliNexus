/**
 * File: VillageWatch.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.reputation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.mixin.CompoundContainerAccessor;
import org.furranystudio.avalinexus.worldgen.structure.AvaliSettlements;
import org.furranystudio.avalinexus.worldgen.structure.AvaliVillageStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class VillageWatch {

    public static final TagKey<Block> MACHINES = TagKey.create(Registries.BLOCK, AvaliNexus.id("village_machines"));
    public static final String FRAME_TAG = "avalinexus_village";

    private VillageWatch() {
    }

    public static boolean isVillageBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(level.getBlockEntity(pos) instanceof Container) && !state.is(MACHINES) || VillageData.get(level.getServer()).isBroken(pos)) {
            return false;
        }
        StructureStart start = villageAt(level, pos);
        return start != null && wasGenerated(level, start, pos, state.getBlock());
    }

    public static void forget(ServerLevel level, BlockPos pos) {
        VillageData.get(level.getServer()).markBroken(pos);
    }

    public static void broke(ServerPlayer player, BlockPos pos) {
        forget(player.level(), pos);
        crime(player, pos, AvaliReputation.Change.BREAK_MACHINE);
    }

    public static void crime(ServerPlayer player, BlockPos pos, AvaliReputation.Change change) {
        ServerLevel level = player.level();
        StructureStart start = villageAt(level, pos);
        UUID packId = start == null ? null : AvaliVillageStructure.packId(level.getSeed(), start);
        if (packId == null) {
            return;
        }
        double range = AvaliBehavior.get(level.getServer()).village().watchDistance();
        AvaliEntity witness = level.getEntitiesOfClass(AvaliEntity.class, player.getBoundingBox().inflate(range),
                avali -> packId.equals(avali.getPackId()) && !avali.isCompanion() && !avali.isSleeping()
                    && avali.distanceTo(player) <= range && avali.hasLineOfSight(player) && avali.canWitness(player))
            .stream().findFirst().orElse(null);
        if (witness == null) {
            return;
        }
        AvaliReputation.change(player, witness.getPack(), change);
        witness.witness(player, AvaliReputation.tier(player, witness.getPack()).ordinal() <= ReputationTier.WARY.ordinal());
    }

    public static void watch(ServerPlayer player, AbstractContainerMenu menu) {
        Map<BlockPos, Boolean> checked = new HashMap<>();
        List<BlockPos> positions = new ArrayList<>();
        for (Slot slot : menu.slots) {
            BlockPos pos = position(slot);
            if (pos != null && !checked.computeIfAbsent(pos, key -> isVillageBlock(player.level(), key))) {
                pos = null;
            }
            positions.add(pos);
        }
        Set<BlockPos> watched = new LinkedHashSet<>();
        positions.stream().filter(pos -> pos != null).forEach(watched::add);
        if (watched.isEmpty()) {
            return;
        }
        VillageData data = VillageData.get(player.level().getServer());
        for (BlockPos pos : watched) {
            if (!data.hasShare(pos)) {
                data.setShare(pos, totals(menu, positions, pos));
            }
        }
        menu.addSlotListener(new Theft(player, data, positions, watched, totals(menu, positions, null)));
    }

    private static StructureStart villageAt(ServerLevel level, BlockPos pos) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structure -> structure.value() instanceof AvaliVillageStructure);
        return start.isValid() ? start : null;
    }

    private static boolean wasGenerated(ServerLevel level, StructureStart start, BlockPos pos, Block block) {
        for (StructurePiece piece : start.getPieces()) {
            Identifier template = AvaliSettlements.template(piece);
            if (template == null || !(piece instanceof PoolElementStructurePiece pool) || !piece.getBoundingBox().isInside(pos)) {
                continue;
            }
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(pool.getRotation());
            if (level.getServer().getStructureTemplateManager().getOrCreate(template).filterBlocks(pool.getPosition(), settings, block)
                    .stream().anyMatch(info -> info.pos().equals(pos))) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos position(Slot slot) {
        Container container = slot.container;
        if (container instanceof CompoundContainer compound) {
            Container first = ((CompoundContainerAccessor) compound).avalinexus$first();
            container = slot.getContainerSlot() < first.getContainerSize() ? first : ((CompoundContainerAccessor) compound).avalinexus$second();
        }
        return container instanceof BlockEntity entity ? entity.getBlockPos() : null;
    }

    private static Map<Item, Integer> totals(AbstractContainerMenu menu, List<BlockPos> positions, BlockPos only) {
        Map<Item, Integer> totals = new HashMap<>();
        for (int index = 0; index < positions.size(); index++) {
            BlockPos pos = positions.get(index);
            ItemStack stack = menu.slots.get(index).getItem();
            if (pos != null && (only == null || only.equals(pos)) && !stack.isEmpty()) {
                totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return totals;
    }

    private static final class Theft implements ContainerListener {
        private final ServerPlayer player;
        private final VillageData data;
        private final List<BlockPos> positions;
        private final Set<BlockPos> watched;
        private Map<Item, Integer> last;

        private Theft(ServerPlayer player, VillageData data, List<BlockPos> positions, Set<BlockPos> watched, Map<Item, Integer> last) {
            this.player = player;
            this.data = data;
            this.positions = positions;
            this.watched = watched;
            this.last = last;
        }

        @Override
        public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
            if (index < 0 || index >= positions.size() || positions.get(index) == null) {
                return;
            }
            Map<Item, Integer> now = totals(menu, positions, null);
            int stolen = 0;
            for (Map.Entry<Item, Integer> entry : last.entrySet()) {
                int lost = entry.getValue() - now.getOrDefault(entry.getKey(), 0);
                if (lost > 0) {
                    stolen += data.take(watched, entry.getKey(), lost);
                }
            }
            last = now;
            if (stolen > 0) {
                crime(player, positions.get(index), AvaliReputation.Change.STEAL);
            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu menu, int index, int value) {
        }
    }
}

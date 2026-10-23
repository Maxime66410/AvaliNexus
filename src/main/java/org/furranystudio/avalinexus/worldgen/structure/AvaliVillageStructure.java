/**
 * File: AvaliVillageStructure.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.worldgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.tank.GrapheneTankBlockEntity;
import org.furranystudio.avalinexus.block.tank.TankStructure;
import org.furranystudio.avalinexus.entity.avali.AvaliAppearance;
import org.furranystudio.avalinexus.entity.avali.AvaliNames;
import org.furranystudio.avalinexus.fluid.FluidContent;
import org.furranystudio.avalinexus.reputation.VillageWatch;
import org.furranystudio.avalinexus.worldgen.ModStructures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AvaliVillageStructure extends Structure {

    public record Layout(int weight, int size, int maxDistance, int minBuildings, int maxBuildings, int minWorkshops, int maxWorkshops) {
        public static final Codec<Layout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1000).fieldOf("weight").forGetter(Layout::weight),
            Codec.intRange(1, 20).fieldOf("size").forGetter(Layout::size),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(Layout::maxDistance),
            Codec.intRange(0, 64).fieldOf("min_buildings").forGetter(Layout::minBuildings),
            Codec.intRange(0, 64).fieldOf("max_buildings").forGetter(Layout::maxBuildings),
            Codec.intRange(0, 16).fieldOf("min_workshops").forGetter(Layout::minWorkshops),
            Codec.intRange(0, 16).fieldOf("max_workshops").forGetter(Layout::maxWorkshops)
        ).apply(instance, Layout::new));
    }

    public record Workshop(Identifier template, Block machine, String shop) {
        public static final Codec<Workshop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(Workshop::template),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("machine").forGetter(Workshop::machine),
            Codec.STRING.fieldOf("shop").forGetter(Workshop::shop)
        ).apply(instance, Workshop::new));
    }

    public record Fields(Identifier template, int max, String job) {
        public static final Codec<Fields> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(Fields::template),
            Codec.intRange(0, 16).fieldOf("max").forGetter(Fields::max),
            Codec.STRING.fieldOf("job").forGetter(Fields::job)
        ).apply(instance, Fields::new));
    }

    public record Loot(Identifier template, ResourceKey<LootTable> table) {
        public static final Codec<Loot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(Loot::template),
            LootTable.KEY_CODEC.fieldOf("table").forGetter(Loot::table)
        ).apply(instance, Loot::new));
    }

    public static final MapCodec<AvaliVillageStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        settingsCodec(instance),
        StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(village -> village.startPool),
        Codec.intRange(0, 64).fieldOf("max_slope").forGetter(village -> village.maxSlope),
        Codec.intRange(1, 64).fieldOf("slope_sample").forGetter(village -> village.slopeSample),
        Codec.intRange(1, 64).fieldOf("attempts").forGetter(village -> village.attempts),
        Layout.CODEC.listOf().fieldOf("layouts").forGetter(village -> village.layouts),
        Codec.STRING.fieldOf("house_prefix").forGetter(village -> village.housePrefix),
        Codec.intRange(0, 16).fieldOf("min_houses").forGetter(village -> village.minHouses),
        Identifier.CODEC.fieldOf("warehouse").forGetter(village -> village.warehouse),
        Identifier.CODEC.fieldOf("archives").forGetter(village -> village.archives),
        Codec.intRange(0, 16).fieldOf("max_archives").forGetter(village -> village.maxArchives),
        Workshop.CODEC.listOf().fieldOf("workshops").forGetter(village -> village.workshops),
        Fields.CODEC.fieldOf("fields").forGetter(village -> village.fields),
        Loot.CODEC.listOf().fieldOf("loot").forGetter(village -> village.loot),
        Codec.floatRange(0.0F, 1.0F).fieldOf("tank_fill_min").forGetter(village -> village.tankFillMin),
        Codec.floatRange(0.0F, 1.0F).fieldOf("tank_fill_max").forGetter(village -> village.tankFillMax)
    ).apply(instance, AvaliVillageStructure::new));

    private static final long VILLAGE_SALT = 0x41564C56494C4CL;
    private static final Identifier DOOR = AvaliNexus.id("door");
    private static final Set<String> FRAMES = Set.of("minecraft:item_frame", "minecraft:glow_item_frame");

    private final Holder<StructureTemplatePool> startPool;
    private final int maxSlope;
    private final int slopeSample;
    private final int attempts;
    private final List<Layout> layouts;
    private final String housePrefix;
    private final int minHouses;
    private final Identifier warehouse;
    private final Identifier archives;
    private final int maxArchives;
    private final List<Workshop> workshops;
    private final Fields fields;
    private final List<Loot> loot;
    private final float tankFillMin;
    private final float tankFillMax;

    public AvaliVillageStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxSlope, int slopeSample, int attempts,
                                 List<Layout> layouts, String housePrefix, int minHouses, Identifier warehouse, Identifier archives,
                                 int maxArchives, List<Workshop> workshops, Fields fields, List<Loot> loot, float tankFillMin, float tankFillMax) {
        super(settings);
        this.startPool = startPool;
        this.maxSlope = maxSlope;
        this.slopeSample = slopeSample;
        this.attempts = attempts;
        this.layouts = layouts;
        this.housePrefix = housePrefix;
        this.minHouses = minHouses;
        this.warehouse = warehouse;
        this.archives = archives;
        this.maxArchives = maxArchives;
        this.workshops = workshops;
        this.fields = fields;
        this.loot = loot;
        this.tankFillMin = tankFillMin;
        this.tankFillMax = Math.max(tankFillMin, tankFillMax);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.AVALI_VILLAGE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        if (layouts.isEmpty() || !isFlatAndDry(context, x, z)) {
            return Optional.empty();
        }
        Layout layout = pickLayout(context.random());
        for (int attempt = 0; attempt < attempts; attempt++) {
            Optional<GenerationStub> stub = JigsawPlacement.addPieces(context, startPool, Optional.empty(), layout.size(), new BlockPos(x, 0, z),
                false, Optional.of(Heightmap.Types.WORLD_SURFACE_WG), new JigsawStructure.MaxDistance(layout.maxDistance()), PoolAliasLookup.EMPTY,
                JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
            if (stub.isEmpty()) {
                return stub;
            }
            List<StructurePiece> kept = select(stub.get().getPiecesBuilder().build().pieces(), layout);
            if (kept != null) {
                StructurePiecesBuilder builder = new StructurePiecesBuilder();
                for (StructurePiece piece : kept) {
                    builder.addPiece(piece == kept.getFirst() ? piece : settle(context, piece));
                }
                return Optional.of(new GenerationStub(stub.get().position(), Either.right(builder)));
            }
        }
        return Optional.empty();
    }

    private boolean isFlatAndDry(GenerationContext context, int x, int z) {
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int sampleX = x + dx * slopeSample;
                int sampleZ = z + dz * slopeSample;
                int surface = height(context, sampleX, sampleZ, Heightmap.Types.WORLD_SURFACE_WG);
                if (surface != height(context, sampleX, sampleZ, Heightmap.Types.OCEAN_FLOOR_WG)) {
                    return false;
                }
                lowest = Math.min(lowest, surface);
                highest = Math.max(highest, surface);
            }
        }
        return highest - lowest <= maxSlope;
    }

    private static int height(GenerationContext context, int x, int z, Heightmap.Types type) {
        return context.chunkGenerator().getFirstFreeHeight(x, z, type, context.heightAccessor(), context.randomState());
    }

    private Layout pickLayout(RandomSource random) {
        int total = layouts.stream().mapToInt(Layout::weight).sum();
        int roll = random.nextInt(total);
        for (Layout layout : layouts) {
            roll -= layout.weight();
            if (roll < 0) {
                return layout;
            }
        }
        return layouts.getLast();
    }

    private List<StructurePiece> select(List<StructurePiece> pieces, Layout layout) {
        Set<StructurePiece> dropped = new HashSet<>();
        Set<Workshop> built = new HashSet<>();
        List<StructurePiece> houses = new ArrayList<>();
        int archived = 0;
        int warehouses = 0;
        int planted = 0;
        for (StructurePiece piece : pieces) {
            Identifier template = AvaliSettlements.template(piece);
            if (template == null || piece == pieces.getFirst()) {
                continue;
            }
            Workshop workshop = workshop(template);
            if (template.toString().startsWith(housePrefix)) {
                houses.add(piece);
            } else if (workshop != null) {
                if (!built.add(workshop) || built.size() > layout.maxWorkshops()) {
                    built.remove(workshop);
                    dropped.add(piece);
                }
            } else if (isField(template)) {
                if (++planted > fields.max()) {
                    dropped.add(piece);
                }
            } else if (matches(template, archives)) {
                if (++archived > maxArchives) {
                    dropped.add(piece);
                }
            } else if (matches(template, warehouse) && ++warehouses > 1) {
                dropped.add(piece);
            }
        }
        int others = built.size() + Math.min(archived, maxArchives) + Math.min(warehouses, 1) + Math.min(planted, fields.max());
        int allowedHouses = Math.max(minHouses, layout.maxBuildings() - others);
        for (int index = allowedHouses; index < houses.size(); index++) {
            dropped.add(houses.get(index));
        }
        int keptHouses = Math.min(houses.size(), allowedHouses);
        int total = keptHouses + others;
        if (warehouses < 1 || keptHouses < minHouses || built.size() < layout.minWorkshops()
                || total < layout.minBuildings() || total > layout.maxBuildings()) {
            return null;
        }
        return pieces.stream().filter(piece -> !dropped.contains(piece)).toList();
    }

    private static boolean matches(Identifier template, Identifier prefix) {
        return template.toString().startsWith(prefix.toString());
    }

    public boolean isField(Identifier template) {
        return template != null && matches(template, fields.template());
    }

    public String fieldJob() {
        return fields.job();
    }

    private Workshop workshop(Identifier template) {
        return template == null ? null : workshops.stream().filter(workshop -> matches(template, workshop.template())).findFirst().orElse(null);
    }

    private ResourceKey<LootTable> lootFor(Identifier template) {
        return template == null ? null : loot.stream().filter(entry -> matches(template, entry.template())).map(Loot::table).findFirst().orElse(null);
    }

    private static StructurePiece settle(GenerationContext context, StructurePiece piece) {
        if (!(piece instanceof PoolElementStructurePiece pool) || pool.getElement().getProjection() != StructureTemplatePool.Projection.RIGID) {
            return piece;
        }
        BlockPos door = pool.getElement().getShuffledJigsawBlocks(context.structureTemplateManager(), pool.getPosition(), pool.getRotation(),
                RandomSource.create(0L)).stream().filter(jigsaw -> DOOR.equals(jigsaw.name())).map(StructureTemplate.JigsawBlockInfo::pos)
            .findFirst().orElse(pool.getBoundingBox().getCenter());
        StructurePiece grounded = AvaliSettlements.onGround(context.structureTemplateManager(), piece);
        grounded.move(0, height(context, door.getX(), door.getZ(), Heightmap.Types.WORLD_SURFACE_WG) - grounded.getBoundingBox().minY(), 0);
        return grounded;
    }

    private static BlockPos anchor(List<StructurePiece> pieces) {
        BoundingBox start = pieces.getFirst().getBoundingBox();
        return new BlockPos(start.getCenter().getX(), start.minY(), start.getCenter().getZ());
    }

    public static UUID packId(long seed, StructureStart start) {
        return start.getPieces().isEmpty() ? null : new UUID(seed ^ VILLAGE_SALT, anchor(start.getPieces()).asLong());
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                           BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
        List<StructurePiece> list = pieces.pieces();
        if (list.isEmpty()) {
            return;
        }
        BlockPos anchor = anchor(list);
        MinecraftServer server = level.getServer();
        String pack = AvaliNames.pack(server, RandomSource.create(level.getSeed() ^ anchor.asLong()));
        UUID packId = new UUID(level.getSeed() ^ VILLAGE_SALT, anchor.asLong());
        DyeColor dye = AvaliSettlements.closestDye(AvaliAppearance.packColor(server, pack));
        AvaliSettlements.recolorTapestries(level.getChunk(chunkPos.x(), chunkPos.z()), list, dye);
        markFrames(level.getChunk(chunkPos.x(), chunkPos.z()), list);
        Map<BlockPos, String> jobs = new HashMap<>();
        Map<BlockPos, BlockPos> machines = artisans(server.getStructureTemplateManager(), list, jobs);

        for (StructurePiece piece : list) {
            BoundingBox box = piece.getBoundingBox();
            if (!box.intersects(chunkBox)) {
                continue;
            }
            ResourceKey<LootTable> table = lootFor(AvaliSettlements.template(piece));
            for (BlockPos pos : BlockPos.betweenClosed(Math.max(box.minX(), chunkBox.minX()), Math.max(box.minY(), chunkBox.minY()),
                    Math.max(box.minZ(), chunkBox.minZ()), Math.min(box.maxX(), chunkBox.maxX()), Math.min(box.maxY(), chunkBox.maxY()),
                    Math.min(box.maxZ(), chunkBox.maxZ()))) {
                BlockState state = level.getBlockState(pos);
                BlockState recolored = AvaliSettlements.recolor(state, dye, true);
                BlockEntity entity = recolored == null ? level.getBlockEntity(pos) : null;
                if (recolored != null) {
                    level.setBlock(pos, recolored, Block.UPDATE_CLIENTS);
                } else if (entity instanceof GrapheneTankBlockEntity) {
                    fillTank(level, pos.immutable());
                } else if (table != null && entity instanceof RandomizableContainer && !(entity instanceof HopperBlockEntity)) {
                    RandomizableContainer.setBlockEntityLootTable(level, random, pos, table);
                } else if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                    BlockPos bed = pos.immutable();
                    AvaliSettlements.spawnAvali(level, bed, avali -> avali.joinVillage(level, anchor, pack, packId, bed, machines.get(bed),
                        jobs.get(bed)));
                }
            }
        }
    }

    private static void markFrames(ChunkAccess chunk, List<StructurePiece> pieces) {
        if (!(chunk instanceof ProtoChunk proto)) {
            return;
        }
        for (CompoundTag entity : proto.getEntities()) {
            String id = entity.getStringOr("id", "");
            if (!FRAMES.contains(id) || entity.getCompoundOrEmpty("Item").isEmpty()) {
                continue;
            }
            ListTag position = entity.getListOrEmpty("Pos");
            BlockPos pos = BlockPos.containing(position.getDoubleOr(0, 0.0), position.getDoubleOr(1, 0.0), position.getDoubleOr(2, 0.0));
            if (pieces.stream().anyMatch(piece -> piece.getBoundingBox().inflatedBy(1).isInside(pos))) {
                ListTag tags = entity.getListOrEmpty("Tags").copy();
                tags.add(StringTag.valueOf(VillageWatch.FRAME_TAG));
                entity.put("Tags", tags);
            }
        }
    }

    private Map<BlockPos, BlockPos> artisans(StructureTemplateManager templates, List<StructurePiece> pieces, Map<BlockPos, String> jobs) {
        List<BlockPos> beds = new ArrayList<>();
        List<BlockPos> fieldCenters = new ArrayList<>();
        Map<BlockPos, Workshop> machines = new HashMap<>();
        for (StructurePiece piece : pieces) {
            Identifier template = AvaliSettlements.template(piece);
            if (template == null || !(piece instanceof PoolElementStructurePiece pool)) {
                continue;
            }
            StructureTemplate structure = templates.getOrCreate(template);
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(pool.getRotation());
            for (StructureTemplate.StructureBlockInfo info : structure.filterBlocks(pool.getPosition(), settings, ModBlocks.AVALI_BED.get())) {
                if (info.state().hasProperty(BedBlock.PART) && info.state().getValue(BedBlock.PART) == BedPart.HEAD) {
                    beds.add(info.pos());
                }
            }
            if (isField(template)) {
                fieldCenters.add(piece.getBoundingBox().getCenter());
            }
            Workshop workshop = workshop(template);
            if (workshop != null) {
                structure.filterBlocks(pool.getPosition(), settings, workshop.machine()).stream().findFirst()
                    .ifPresent(info -> machines.put(info.pos(), workshop));
            }
        }
        Map<BlockPos, BlockPos> artisans = new HashMap<>();
        machines.keySet().stream().sorted().forEach(machine ->
            beds.stream().filter(bed -> !artisans.containsKey(bed)).min(Comparator.comparingDouble(bed -> bed.distSqr(machine)))
                .ifPresent(bed -> {
                    artisans.put(bed, machine);
                    jobs.put(bed, machines.get(machine).shop());
                }));
        fieldCenters.stream().sorted().findFirst().flatMap(field -> beds.stream().filter(bed -> !jobs.containsKey(bed))
                .min(Comparator.<BlockPos>comparingDouble(bed -> bed.distSqr(field)).thenComparing(Comparator.naturalOrder())))
            .ifPresent(bed -> jobs.put(bed, fields.job()));
        return artisans;
    }

    private void fillTank(WorldGenLevel level, BlockPos pos) {
        List<BlockPos> members = TankStructure.members(level, pos).stream()
            .sorted(Comparator.<BlockPos>comparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ)).toList();
        if (members.isEmpty() || !members.getFirst().equals(pos)) {
            return;
        }
        List<GrapheneTankBlockEntity> tanks = new ArrayList<>();
        Fluid fluid = Fluids.EMPTY;
        int total = 0;
        for (BlockPos member : members) {
            if (level.getBlockEntity(member) instanceof GrapheneTankBlockEntity tank) {
                tanks.add(tank);
                total += tank.content().amount();
                if (fluid == Fluids.EMPTY && !tank.content().isEmpty()) {
                    fluid = tank.content().fluid();
                }
            }
        }
        if (total <= 0) {
            return;
        }
        float fill = tankFillMin + RandomSource.create(level.getSeed() ^ pos.asLong()).nextFloat() * (tankFillMax - tankFillMin);
        int left = Math.round(total * fill);
        for (GrapheneTankBlockEntity tank : tanks) {
            int amount = Math.min(left, GrapheneTankBlockEntity.CAPACITY);
            tank.setContent(amount > 0 ? FluidContent.of(fluid, amount) : FluidContent.EMPTY);
            left -= amount;
        }
    }
}

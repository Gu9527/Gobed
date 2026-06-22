package com.example.gobed;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoBedChunkGenerator extends ChunkGenerator {
    private static final int MIN_Y = -64;
    private static final int EDGE_WIDTH = 2;

    public static final String DEFAULT_BORDER_BLOCK = "minecraft:deepslate";
    public static final String DEFAULT_INNER_BLOCK = "minecraft:smooth_stone";

    /**
     * 实例字段：替代原先的 static {@code AtomicReference}。
     * 解决了多服务器/多维度共享全局状态导致的竞态与互相污染问题（见审查 #1），
     * 也消除了每次传送都要把配置写到 {@code user.dir/gobed_settings.json} 的设计缺陷（见审查 #6）。
     *
     * <p>非 final：保留"玩家通过 3×3 结构选择维度方块"的原特性。
     * 初值来自 Codec（加载存档时回填），运行期可经 {@link #setBlockSettings} 修改；
     * 运行期修改是易失的（不会回写 level.dat），重启后回到 Codec 默认值。
     * 已生成的区块方块保存在 region 文件里不受影响，只影响新生成的区块。</p>
     */
    private String borderBlockId;
    private String innerBlockId;

    public static final MapCodec<GoBedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            net.minecraft.util.ExtraCodecs.NON_EMPTY_STRING
                .optionalFieldOf("border_block", DEFAULT_BORDER_BLOCK)
                .forGetter(g -> g.borderBlockId),
            net.minecraft.util.ExtraCodecs.NON_EMPTY_STRING
                .optionalFieldOf("inner_block", DEFAULT_INNER_BLOCK)
                .forGetter(g -> g.innerBlockId)
        ).apply(instance, instance.stable(GoBedChunkGenerator::new))
    );

    public GoBedChunkGenerator(BiomeSource biomeSource) {
        this(biomeSource, DEFAULT_BORDER_BLOCK, DEFAULT_INNER_BLOCK);
    }

    public GoBedChunkGenerator(BiomeSource biomeSource, String borderBlockId, String innerBlockId) {
        super(biomeSource);
        this.borderBlockId = borderBlockId;
        this.innerBlockId = innerBlockId;
    }

    public String getBorderBlockId() {
        return borderBlockId;
    }

    public String getInnerBlockId() {
        return innerBlockId;
    }

    /**
     * 由 {@code GoBedPortalBlock} 在玩家用合法 3×3 结构传送时调用，让新生成区块使用玩家选定的方块。
     * 仅修改本维度的 generator 实例，不写盘、不影响其它维度（见审查 #1、#6）。
     */
    public void setBlockSettings(String border, String inner) {
        this.borderBlockId = border;
        this.innerBlockId = inner;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * 同步生成：本生成器没有噪声阶段，所以直接把区块填好并立即返回。
     * 保留 {@link CompletableFuture} 是为了符合 {@link ChunkGenerator} 的契约（见审查 #18）。
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        buildChunkBlocks(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }

    private void buildChunkBlocks(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int startX = pos.getMinBlockX();
        int startZ = pos.getMinBlockZ();

        // 让 dimensionHeight / chunkUnitSize 真正生效（见审查 #2、#3）
        int dimensionHeight = ModConfigUtil.COMMON.dimensionHeight.get();
        int unitBlocks = ModConfigUtil.COMMON.chunkUnitSize.get() * 16;

        BlockState borderState = resolveBorderBlock().defaultBlockState();
        BlockState innerState = resolveInnerBlock().defaultBlockState();
        BlockState bedrockState = Blocks.BEDROCK.defaultBlockState();

        // 复用单个 mutable 坐标，每区块减少约 1024 个临时 BlockPos 对象（见审查 #19）
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int x = startX; x < startX + 16; x++) {
            int modX = Math.floorMod(x, unitBlocks);
            boolean isEdgeX = modX < EDGE_WIDTH || modX >= unitBlocks - EDGE_WIDTH;
            for (int z = startZ; z < startZ + 16; z++) {
                int modZ = Math.floorMod(z, unitBlocks);
                boolean isEdge = isEdgeX
                        || modZ < EDGE_WIDTH
                        || modZ >= unitBlocks - EDGE_WIDTH;
                BlockState fillState = isEdge ? borderState : innerState;

                for (int y = 1; y <= dimensionHeight; y++) {
                    chunk.setBlockState(mpos.set(x, y, z), fillState, false);
                }
                chunk.setBlockState(mpos.set(x, 0, z), bedrockState, false);
            }
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public int getGenDepth() {
        // 与实际生成高度保持一致（含基岩层），原值 384 与现实不符（见审查 #20）
        return ModConfigUtil.COMMON.dimensionHeight.get() + 1;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, net.minecraft.world.level.LevelHeightAccessor level, RandomState randomState) {
        return ModConfigUtil.COMMON.dimensionHeight.get();
    }

    @Override
    public net.minecraft.world.level.NoiseColumn getBaseColumn(int x, int z, net.minecraft.world.level.LevelHeightAccessor level, RandomState randomState) {
        int dimensionHeight = ModConfigUtil.COMMON.dimensionHeight.get();
        int unitBlocks = ModConfigUtil.COMMON.chunkUnitSize.get() * 16;

        BlockState borderState = resolveBorderBlock().defaultBlockState();
        BlockState innerState = resolveInnerBlock().defaultBlockState();

        int modX = Math.floorMod(x, unitBlocks);
        int modZ = Math.floorMod(z, unitBlocks);
        boolean isEdge = modX < EDGE_WIDTH
                || modX >= unitBlocks - EDGE_WIDTH
                || modZ < EDGE_WIDTH
                || modZ >= unitBlocks - EDGE_WIDTH;
        BlockState fillState = isEdge ? borderState : innerState;

        BlockState[] states = new BlockState[dimensionHeight + 1];
        states[0] = Blocks.BEDROCK.defaultBlockState();
        for (int i = 1; i <= dimensionHeight; i++) {
            states[i] = fillState;
        }
        return new net.minecraft.world.level.NoiseColumn(0, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add(net.minecraft.network.chat.Component.translatable("gobed.chunk_generator.name").getString());
    }

    /**
     * 解析边框方块：先用本实例的配置值；解析失败或被列入黑名单时回退到 {@link Blocks#DEEPSLATE}。
     */
    private Block resolveBorderBlock() {
        return resolveBlock(borderBlockId, DEFAULT_BORDER_BLOCK, Blocks.DEEPSLATE,
                ModConfigUtil.COMMON.borderBlacklist.get());
    }

    /**
     * 解析内侧方块：同上，回退到 {@link Blocks#SMOOTH_STONE}。
     */
    private Block resolveInnerBlock() {
        return resolveBlock(innerBlockId, DEFAULT_INNER_BLOCK, Blocks.SMOOTH_STONE,
                ModConfigUtil.COMMON.innerBlacklist.get());
    }

    private Block resolveBlock(String id, String defaultId, Block fallback, List<? extends String> blacklist) {
        String resolved = id == null || id.isBlank() ? defaultId : id;
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(resolved));
        if (block == Blocks.AIR && !resolved.equals("minecraft:air")) {
            block = fallback;
        }
        String key = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (blacklist.contains(key)) {
            block = fallback;
        }
        return block;
    }
}

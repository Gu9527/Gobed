package com.example.gobed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class GoBedChunkGenerator extends ChunkGenerator {
    private static final int MIN_Y = -64;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final AtomicReference<String> BORDER_BLOCK_ID = new AtomicReference<>("minecraft:deepslate");
    private static final AtomicReference<String> INNER_BLOCK_ID = new AtomicReference<>("minecraft:smooth_stone");

    public static final MapCodec<GoBedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
        ).apply(instance, instance.stable(GoBedChunkGenerator::new))
    );

    public GoBedChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        loadSettings();
    }

    public static void setBlockSettings(String border, String inner) {
        BORDER_BLOCK_ID.set(border);
        INNER_BLOCK_ID.set(inner);
        saveSettings();
    }

    private static Path getSettingsPath() {
        return Path.of(System.getProperty("user.dir"), "gobed_settings.json");
    }

    public static void saveSettings() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("border_block", BORDER_BLOCK_ID.get());
            obj.addProperty("inner_block", INNER_BLOCK_ID.get());
            Files.writeString(getSettingsPath(), GSON.toJson(obj));
        } catch (IOException e) {
            GoBedMod.LOGGER.error("Failed to save settings", e);
        }
    }

    public static void loadSettings() {
        Path path = getSettingsPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                JsonObject obj = GSON.fromJson(json, JsonObject.class);
                if (obj.has("border_block")) BORDER_BLOCK_ID.set(obj.get("border_block").getAsString());
                if (obj.has("inner_block")) INNER_BLOCK_ID.set(obj.get("inner_block").getAsString());
            } catch (IOException e) {
                GoBedMod.LOGGER.error("Failed to load settings", e);
            }
        }
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

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

        Block borderBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(BORDER_BLOCK_ID.get()));
        Block innerBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(INNER_BLOCK_ID.get()));

        if (borderBlock == null) borderBlock = Blocks.DEEPSLATE;
        if (innerBlock == null) innerBlock = Blocks.SMOOTH_STONE;

        if (ModConfigUtil.COMMON.borderBlacklist.get().contains(BuiltInRegistries.BLOCK.getKey(borderBlock).toString())) {
            borderBlock = Blocks.DEEPSLATE;
        }
        if (ModConfigUtil.COMMON.innerBlacklist.get().contains(BuiltInRegistries.BLOCK.getKey(innerBlock).toString())) {
            innerBlock = Blocks.SMOOTH_STONE;
        }

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                boolean isEdge = (Math.floorMod(x, 32) < 2) || (Math.floorMod(x, 32) >= 30) || (Math.floorMod(z, 32) < 2) || (Math.floorMod(z, 32) >= 30);
                BlockState blockState = isEdge ? borderBlock.defaultBlockState() : innerBlock.defaultBlockState();

                for (int y = 1; y <= 64; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), blockState, false);
                }
                chunk.setBlockState(new BlockPos(x, 0, z), Blocks.BEDROCK.defaultBlockState(), false);
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
        return 384;
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
        return 64;
    }

    @Override
    public net.minecraft.world.level.NoiseColumn getBaseColumn(int x, int z, net.minecraft.world.level.LevelHeightAccessor level, RandomState randomState) {
        Block borderBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(BORDER_BLOCK_ID.get()));
        Block innerBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(INNER_BLOCK_ID.get()));
        if (borderBlock == null) borderBlock = Blocks.DEEPSLATE;
        if (innerBlock == null) innerBlock = Blocks.SMOOTH_STONE;

        if (ModConfigUtil.COMMON.borderBlacklist.get().contains(BuiltInRegistries.BLOCK.getKey(borderBlock).toString())) {
            borderBlock = Blocks.DEEPSLATE;
        }
        if (ModConfigUtil.COMMON.innerBlacklist.get().contains(BuiltInRegistries.BLOCK.getKey(innerBlock).toString())) {
            innerBlock = Blocks.SMOOTH_STONE;
        }

        boolean isEdge = (Math.floorMod(x, 32) < 2) || (Math.floorMod(x, 32) >= 30) || (Math.floorMod(z, 32) < 2) || (Math.floorMod(z, 32) >= 30);
        BlockState fillState = isEdge ? borderBlock.defaultBlockState() : innerBlock.defaultBlockState();

        BlockState[] states = new BlockState[65];
        states[0] = Blocks.BEDROCK.defaultBlockState();
        for (int i = 1; i <= 64; i++) {
            states[i] = fillState;
        }
        return new net.minecraft.world.level.NoiseColumn(0, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add(net.minecraft.network.chat.Component.translatable("gobed.chunk_generator.name").getString());
    }
}

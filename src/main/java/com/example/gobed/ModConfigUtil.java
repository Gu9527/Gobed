package com.example.gobed;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class ModConfigUtil {
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public static class Common {
        public final ModConfigSpec.IntValue portalLightLevel;
        public final ModConfigSpec.IntValue spawnHeight;
        public final ModConfigSpec.IntValue chunkUnitSize;
        public final ModConfigSpec.IntValue dimensionHeight;
        public final ModConfigSpec.BooleanValue enableTeleportation;
        public final ModConfigSpec.ConfigValue<List<? extends String>> borderBlacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> innerBlacklist;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("gobed");
            portalLightLevel = builder
                    .comment("Portal block light level (0-15)")
                    .defineInRange("portalLightLevel", 11, 0, 15);
            spawnHeight = builder
                    .comment("Spawn height after teleport")
                    .defineInRange("spawnHeight", 64, -64, 320);
            chunkUnitSize = builder
                    .comment("Chunk unit size (2 = 32x32 blocks)")
                    .defineInRange("chunkUnitSize", 2, 1, 16);
            dimensionHeight = builder
                    .comment("Dimension surface height")
                    .defineInRange("dimensionHeight", 64, 1, 320);
            enableTeleportation = builder
                    .comment("Enable teleportation")
                    .define("enableTeleportation", true);
            borderBlacklist = builder
                    .comment("Blacklisted blocks for border (cannot be used as border)")
                    .defineList("borderBlacklist",
                            () -> Arrays.asList("minecraft:bedrock"),
                            obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null);
            innerBlacklist = builder
                    .comment("Blacklisted blocks for inner (cannot be used as inner)")
                    .defineList("innerBlacklist",
                            () -> Arrays.asList("minecraft:bedrock"),
                            obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null);
            builder.pop();
        }
    }
}

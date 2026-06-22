package com.example.gobed;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(GoBedMod.MOD_ID)
public class GoBedMod {
    public static final String MOD_ID = "gobed";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Level> GO_BED_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "go_bed")
    );

    public GoBedMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ModConfigUtil.COMMON_SPEC, "gobed.toml");

        GoBedRegistry.register(modEventBus);
        GoBedCreativeTab.register(modEventBus);

        modEventBus.addListener(GoBedMod::onRegister);

        // GoBedEvents 改用 @EventBusSubscriber 自动注册，不再手动 register（见审查 #22）

        // 客户端侧注册配置界面（见审查 #12）
        if (FMLEnvironment.dist == Dist.CLIENT) {
            GoBedClient.registerClientConfig(modContainer);
        }

        LOGGER.info("Go Bed Mod is loading!");
    }

    public static void onRegister(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "go_bed_generator"),
            () -> GoBedChunkGenerator.CODEC);
    }
}

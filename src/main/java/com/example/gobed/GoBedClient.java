package com.example.gobed;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

/**
 * 客户端入口：把 {@link ConfigScreen} 注册成 Mod 列表里的"配置"界面（见审查 #12）。
 * 仅在客户端（{@link Dist#CLIENT}）加载，避免服务端加载到 {@code client.gui} 类。
 * <p>
 * NeoForge 21.1 使用 {@link IConfigScreenFactory} 作为扩展点，
 * 通过 {@link ModContainer#registerExtensionPoint(Class, java.util.function.Supplier)} 注册。
 */
public final class GoBedClient {
    private GoBedClient() {
    }

    public static void registerClientConfig(ModContainer modContainer) {
        Supplier<IConfigScreenFactory> factory = () -> (container, modListScreen) -> new ConfigScreen(container, modListScreen);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}

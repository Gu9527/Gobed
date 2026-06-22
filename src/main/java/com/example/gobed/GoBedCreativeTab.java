package com.example.gobed;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GoBedCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoBedMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GO_BED_TAB = TABS.register("go_bed_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.gobed.go_bed_tab"))
                    .icon(() -> new ItemStack(GoBedRegistry.PORTAL.get()))
                    .displayItems((params, output) -> {
                        output.accept(GoBedRegistry.PORTAL.get());
                        output.accept(GoBedRegistry.AE2_GIFT_PACKAGE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}

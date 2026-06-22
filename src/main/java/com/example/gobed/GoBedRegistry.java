package com.example.gobed;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GoBedRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, GoBedMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GoBedMod.MOD_ID);

    public static final DeferredHolder<Block, GoBedPortalBlock> PORTAL = BLOCKS.register("portal",
            () -> new GoBedPortalBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .lightLevel(state -> 11)));

    public static final DeferredHolder<Item, BlockItem> PORTAL_ITEM = ITEMS.register("portal",
            () -> new BlockItem(PORTAL.get(), new Item.Properties()));

    public static final DeferredHolder<Item, AE2GiftPackageItem> AE2_GIFT_PACKAGE = ITEMS.register("ae2_gift_package",
            () -> new AE2GiftPackageItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}

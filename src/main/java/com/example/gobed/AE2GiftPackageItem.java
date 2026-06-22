package com.example.gobed;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

public class AE2GiftPackageItem extends Item {
    public AE2GiftPackageItem(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            NonNullList<ItemStack> contents = createPackageContents();

            for (ItemStack content : contents) {
                if (!content.isEmpty()) {
                    if (!player.getInventory().add(content)) {
                        player.drop(content, false);
                    }
                }
            }

            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private NonNullList<ItemStack> createPackageContents() {
        NonNullList<ItemStack> contents = NonNullList.create();

        // AE2 core items
        contents.add(createItemStack("ae2", "creative_energy_cell", 1));
        contents.add(createItemStack("ae2", "flux_covered_cable", 64));
        contents.add(createItemStack("ae2", "wireless_access_point", 1));
        contents.add(createItemStack("ae2", "wireless_crafting_terminal", 1));
        contents.add(createItemStack("ae2", "crafting_terminal", 1));

        // ExtendedAE Plus
        if (ModList.get().isLoaded("extendedae_plus")) {
            contents.add(createItemStack("extendedae_plus", "infinity_biginite_cell", 2));
            contents.add(createItemStack("extendedae_plus", "assembler_matrix_speed_plus", 1));
            contents.add(createItemStack("extendedae_plus", "assembler_matrix_pattern_plus", 64));
            contents.add(createItemStack("extendedae_plus", "assembler_matrix_pattern_plus", 36));
            contents.add(createItemStack("extendedae_plus", "assembler_matrix_crafter_plus", 23));
            contents.add(createItemStack("extendedae_plus", "assembler_matrix_upload_core", 1));
        } else {
            for (int i = 0; i < 8; i++) {
                contents.add(createItemStack("ae2", "item_storage_cell_256k", 1));
            }
        }

        // ExtendedAE
        if (ModList.get().isLoaded("extendedae")) {
            contents.add(createItemStack("extendedae", "ex_drive", 1));
        } else {
            contents.add(createItemStack("ae2", "drive", 1));
        }

        // AE2 WirelessLib
        if (ModList.get().isLoaded("ae2wtlib")) {
            contents.add(createItemStack("ae2wtlib", "quantum_bridge_card", 1));
            for (int i = 0; i < 8; i++) {
                contents.add(createItemStack("ae2", "quantum_ring", 1));
            }
            contents.add(createItemStack("ae2", "quantum_link", 1));
            contents.addAll(createEntangledSingularityPair());
        }

        // AE2LT
        contents.add(createItemStack("ae2lt", "overloaded_frequency_card", 1));
        contents.add(createItemStack("ae2lt", "advanced_wireless_overloaded_controller", 1));

        // Advanced AE
        contents.add(createItemStack("advanced_ae", "quantum_core", 1));

        // MinforMax
        contents.add(createItemStack("minformax", "extra_drop_upgrade_tier4", 64));
        contents.add(createItemStack("minformax", "speed_upgrade_tier4", 64));
        contents.add(createItemStack("minformax", "processing_upgrade_tier4", 64));
        contents.add(createItemStack("minformax", "fortune_upgrade_tier4", 64));
        contents.add(createItemStack("minformax", "auto_smelting_upgrade", 64));
        contents.add(createItemStack("minformax", "ultimate_processing_upgrade", 64));
        contents.add(createItemStack("minformax", "eternal_generator", 64));
        contents.add(createItemStack("minformax", "farmer", 64));
        contents.add(createItemStack("minformax", "ore_coalescer", 1));
        contents.add(createItemStack("minformax", "index_inscriber", 1));
        contents.add(createItemStack("minformax", "configuration_tool", 1));
        contents.add(createItemStack("minformax", "chaos_shard", 64));
        contents.add(createItemStack("minformax", "memory_shard", 64));

        // OmniTools
        contents.add(createItemStack("omnitools", "omni_wrench", 1));
        contents.add(createItemStack("omnitools", "omni_vajra", 1));

        return contents;
    }

    private NonNullList<ItemStack> createEntangledSingularityPair() {
        NonNullList<ItemStack> pair = NonNullList.create();
        Item singularityItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ae2", "quantum_entangled_singularity"));

        if (singularityItem != Items.AIR) {
            long frequency = new Date().getTime() * 100 + (System.nanoTime() % 100);

            ItemStack singularity1 = new ItemStack(singularityItem, 1);
            ItemStack singularity2 = new ItemStack(singularityItem, 1);

            pair.add(singularity1);
            pair.add(singularity2);
        }

        return pair;
    }

    private ItemStack createItemStack(String modId, String itemId, int count) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modId, itemId));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    private static final ChatFormatting[] RAINBOW = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
            ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED
    };

    private Component createRainbowText(String text) {
        MutableComponent result = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            ChatFormatting color = RAINBOW[(i + (int)(System.currentTimeMillis() / 100)) % RAINBOW.length];
            result.append(Component.literal(String.valueOf(c)).withStyle(Style.EMPTY.withColor(color)));
        }
        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(createRainbowText("一路来的努力和汗水终于有了回报,AE！给我下单，让我看看你的极限！"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}

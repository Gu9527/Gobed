package com.example.gobed;

import net.minecraft.ChatFormatting;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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

import java.util.ArrayList;
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
            ItemStack cell = createPortableCell(contents);

            if (!cell.isEmpty()) {
                if (!player.getInventory().add(cell)) {
                    player.drop(cell, false);
                }
            } else {
                for (ItemStack content : contents) {
                    if (!content.isEmpty()) {
                        if (!player.getInventory().add(content)) {
                            player.drop(content, false);
                        }
                    }
                }
            }

            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private ItemStack createPortableCell(NonNullList<ItemStack> contents) {
        if (!ModList.get().isLoaded("ae2")) return ItemStack.EMPTY;

        Item cellItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ae2", "portable_item_cell_16k"));
        if (cellItem == Items.AIR) return ItemStack.EMPTY;

        ItemStack cell = new ItemStack(cellItem, 1);

        List<ItemStack> nonEmpty = new ArrayList<>();
        for (ItemStack s : contents) {
            if (!s.isEmpty()) nonEmpty.add(s);
        }

        // Merge same items into one GenericStack per type
        java.util.Map<Item, Long> merged = new java.util.LinkedHashMap<>();
        for (ItemStack s : nonEmpty) {
            merged.merge(s.getItem(), (long) s.getCount(), Long::sum);
        }

        List<GenericStack> stacks = new ArrayList<>();
        for (java.util.Map.Entry<Item, Long> e : merged.entrySet()) {
            AEItemKey key = AEItemKey.of(new ItemStack(e.getKey(), 1));
            if (key != null) {
                stacks.add(new GenericStack(key, e.getValue()));
            }
        }

        if (!stacks.isEmpty()) {
            cell.set(AEComponents.STORAGE_CELL_INV, stacks);
        }
        cell.set(AEComponents.STORED_ENERGY, 32000.0);
        cell.set(AEComponents.ENERGY_CAPACITY, 32000.0);

        return cell;
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
        if (ModList.get().isLoaded("ae2lt")) {
            contents.add(createItemStack("ae2lt", "overloaded_frequency_card", 1));
            contents.add(createItemStack("ae2lt", "advanced_wireless_overloaded_controller", 1));
        }

        // Advanced AE
        if (ModList.get().isLoaded("advanced_ae")) {
            contents.add(createItemStack("advanced_ae", "quantum_core", 1));
        }

        // MinforMax
        if (ModList.get().isLoaded("minformax")) {
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
        }

        // OmniTools
        if (ModList.get().isLoaded("omnitools")) {
            contents.add(createItemStack("omnitools", "omni_wrench", 1));
            contents.add(createItemStack("omnitools", "omni_vajra", 1));
        }

        return contents;
    }

    /**
     * 制造一对配对的量子纠缠奇点。两个 ItemStack 写入相同的频率值即可在 AE2 量子网络中配对使用。
     * 原实现里 frequency 算出来后从未被应用（审查 #7a），现修复为写入 NBT。
     *
     * <p>TODO: AE2 在 1.21 可能改用 {@code DataComponent} 存储频率；如需正式兼容，
     * 实施时应核对 AE2 当前版本的官方 API 替换此处 NBT 写法。</p>
     */
    private NonNullList<ItemStack> createEntangledSingularityPair() {
        NonNullList<ItemStack> pair = NonNullList.create();
        Item singularityItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ae2", "quantum_entangled_singularity"));

        if (singularityItem != Items.AIR) {
            long frequency = (System.currentTimeMillis() << 20) ^ System.nanoTime();

            ItemStack singularity1 = new ItemStack(singularityItem, 1);
            ItemStack singularity2 = new ItemStack(singularityItem, 1);
            applyFrequency(singularity1, frequency);
            applyFrequency(singularity2, frequency); // 同一频率 → 配对

            pair.add(singularity1);
            pair.add(singularity2);
        }

        return pair;
    }

    private void applyFrequency(ItemStack stack, long frequency) {
        // MC 1.21 uses DataComponents instead of direct NBT on ItemStack（见审查 #7a）
        net.minecraft.world.item.component.CustomData.update(
                DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong("freq", frequency));
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
            ChatFormatting.LIGHT_PURPLE
    };

    private Component createRainbowText(String text) {
        net.minecraft.network.chat.MutableComponent result = net.minecraft.network.chat.Component.empty();
        int offset = (int) ((System.currentTimeMillis() / 120) % RAINBOW.length);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            ChatFormatting color = RAINBOW[(offset + RAINBOW.length - i % RAINBOW.length) % RAINBOW.length];
            result.append(Component.literal(String.valueOf(c)).withStyle(
                    net.minecraft.network.chat.Style.EMPTY.withColor(color)));
        }
        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(createRainbowText("一路来的努力和汗水终于有了回报,AE！给我下单，让我看看你的极限！"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}

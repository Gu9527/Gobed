package com.example.gobed;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class GoBedPortalBlock extends Block {

    public GoBedPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("  X O X").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("  O P O").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("  X O X").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("gobed.tooltip.p").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gobed.tooltip.o").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gobed.tooltip.x").withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!ModConfigUtil.COMMON.enableTeleportation.get()) {
                player.displayClientMessage(Component.translatable("gobed.teleport.disabled").withStyle(ChatFormatting.RED), true);
                return InteractionResult.SUCCESS;
            }

            Block[] structureBlocks = readStructure(level, pos);

            if (structureBlocks != null) {
                Block borderBlock = structureBlocks[0];
                Block innerBlock = structureBlocks[1];

                String borderId = BuiltInRegistries.BLOCK.getKey(borderBlock).toString();
                String innerId = BuiltInRegistries.BLOCK.getKey(innerBlock).toString();

                GoBedChunkGenerator.setBlockSettings(borderId, innerId);

                teleportPlayer(serverLevel, (ServerPlayer) player);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("gobed.teleport.need_structure").withStyle(ChatFormatting.RED), true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private Block[] readStructure(Level level, BlockPos center) {
        BlockPos north = center.north();
        BlockPos south = center.south();
        BlockPos east = center.east();
        BlockPos west = center.west();
        BlockPos northEast = north.east();
        BlockPos northWest = north.west();
        BlockPos southEast = south.east();
        BlockPos southWest = south.west();

        BlockState northState = level.getBlockState(north);
        BlockState northEastState = level.getBlockState(northEast);

        if (northState.isAir() || northEastState.isAir()) return null;

        Block crossBlock = northState.getBlock();
        Block cornerBlock = northEastState.getBlock();

        if (crossBlock == this || cornerBlock == this) return null;

        if (crossBlock == level.getBlockState(south).getBlock() &&
            crossBlock == level.getBlockState(east).getBlock() &&
            crossBlock == level.getBlockState(west).getBlock() &&
            cornerBlock == level.getBlockState(northWest).getBlock() &&
            cornerBlock == level.getBlockState(southEast).getBlock() &&
            cornerBlock == level.getBlockState(southWest).getBlock()) {

            String borderId = BuiltInRegistries.BLOCK.getKey(cornerBlock).toString();
            String innerId = BuiltInRegistries.BLOCK.getKey(crossBlock).toString();

            if (ModConfigUtil.COMMON.borderBlacklist.get().contains(borderId)) {
                return null;
            }
            if (ModConfigUtil.COMMON.innerBlacklist.get().contains(innerId)) {
                return null;
            }

            return new Block[]{cornerBlock, crossBlock};
        }

        return null;
    }

    private void teleportPlayer(ServerLevel level, ServerPlayer player) {
        ServerLevel targetLevel = level.getServer().getLevel(GoBedMod.GO_BED_DIMENSION);
        if (targetLevel != null) {
            int spawnHeight = ModConfigUtil.COMMON.spawnHeight.get();
            player.teleportTo(targetLevel, 0.5, spawnHeight, 0.5, player.getYRot(), player.getXRot());
            player.displayClientMessage(Component.translatable("gobed.teleport.success").withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("gobed.teleport.failed").withStyle(ChatFormatting.RED), true);
        }
    }
}

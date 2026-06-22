package com.example.gobed;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GoBedPortalBlock extends Block {

    public GoBedPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§e━━━ 搭建方式 ━━━").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("   §cX §7O §cX").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("   §7O §aP §7O").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("   §cX §7O §cX").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§aP§7 = 传送方块  §7O§7 = 内侧方块  §cX§7 = 边框方块").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 2; i++) {
            double dx = (random.nextDouble() - 0.5) * 1.2;
            double dy = random.nextDouble() * 0.8;
            double dz = (random.nextDouble() - 0.5) * 1.2;
            level.addParticle(ParticleTypes.PORTAL, x + dx, y + dy, z + dz, -dx * 0.4, -dy * 0.1, -dz * 0.4);
        }

        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.ENCHANT,
                    x + (random.nextDouble() - 0.5) * 0.6,
                    y + random.nextDouble() * 0.6 + 0.2,
                    z + (random.nextDouble() - 0.5) * 0.6,
                    (random.nextDouble() - 0.5) * 0.02,
                    random.nextDouble() * 0.01,
                    (random.nextDouble() - 0.5) * 0.02);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!ModConfigUtil.COMMON.enableTeleportation.get()) {
                player.displayClientMessage(Component.translatable("gobed.teleport.disabled").withStyle(ChatFormatting.RED), true);
                return InteractionResult.SUCCESS;
            }

            // 在维度内右键 -> 返回主世界
            if (level.dimension() == GoBedMod.GO_BED_DIMENSION) {
                teleportBack(serverLevel, (ServerPlayer) player);
                return InteractionResult.SUCCESS;
            }

            // 在主世界右键 -> 检测结构 -> 传送到维度
            Block[] structureBlocks = readStructure(level, pos);

            if (structureBlocks != null) {
                Block borderBlock = structureBlocks[0];
                Block innerBlock = structureBlocks[1];

                String borderId = blockIdOrEmpty(borderBlock);
                String innerId = blockIdOrEmpty(innerBlock);

                if (borderId.isEmpty() || innerId.isEmpty()) {
                    player.displayClientMessage(Component.translatable("gobed.teleport.need_structure").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.SUCCESS;
                }

                ServerLevel goBedLevel = level.getServer().getLevel(GoBedMod.GO_BED_DIMENSION);
                if (goBedLevel != null
                        && goBedLevel.getChunkSource().getGenerator() instanceof GoBedChunkGenerator generator) {
                    generator.setBlockSettings(borderId, innerId);
                }

                teleportToDimension(serverLevel, (ServerPlayer) player, level, pos);
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

            String borderId = blockIdOrEmpty(cornerBlock);
            String innerId = blockIdOrEmpty(crossBlock);

            if (borderId.isEmpty() || innerId.isEmpty()) {
                return null;
            }
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

    /**
     * 安全获取方块注册 id；未注册方块返回空字符串以避免 {@link ResourceLocation} NPE（见审查 #5）。
     */
    private String blockIdOrEmpty(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? "" : key.toString();
    }

    private void teleportToDimension(ServerLevel sourceLevel, ServerPlayer player, Level sourceDimension, BlockPos portalPos) {
        ServerLevel targetLevel = sourceLevel.getServer().getLevel(GoBedMod.GO_BED_DIMENSION);
        if (targetLevel != null) {
            int spawnHeight = ModConfigUtil.COMMON.spawnHeight.get();
            BlockPos safe = findSafeSpawn(targetLevel, spawnHeight);

            // 保存返回位置到玩家数据
            saveReturnPos(player, sourceDimension.dimension().location(), portalPos);

            // 在维度内放置传送方块
            targetLevel.setBlockAndUpdate(safe, this.defaultBlockState());

            player.teleportTo(targetLevel, safe.getX() + 0.5, safe.getY() + 1, safe.getZ() + 0.5, player.getYRot(), player.getXRot());
            player.displayClientMessage(Component.translatable("gobed.teleport.success").withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("gobed.teleport.failed").withStyle(ChatFormatting.RED), true);
        }
    }

    private void teleportBack(ServerLevel goBedLevel, ServerPlayer player) {
        // 读取返回位置
        Object[] returnData = loadReturnPos(player);
        ResourceLocation dimensionKey = (ResourceLocation) returnData[0];
        BlockPos returnPos = (BlockPos) returnData[1];

        ServerLevel targetLevel = goBedLevel.getServer().getLevel(ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dimensionKey));
        if (targetLevel != null && returnPos != null) {
            player.teleportTo(targetLevel, returnPos.getX() + 0.5, returnPos.getY() + 1, returnPos.getZ() + 0.5, player.getYRot(), player.getXRot());
            player.displayClientMessage(Component.translatable("gobed.teleport.back").withStyle(ChatFormatting.GREEN), true);
        } else {
            // 回退到主世界出生点
            ServerLevel overworld = goBedLevel.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
                player.displayClientMessage(Component.translatable("gobed.teleport.back").withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.translatable("gobed.teleport.failed").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    private void saveReturnPos(ServerPlayer player, ResourceLocation dimension, BlockPos pos) {
        CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag gobedTag = tag.getCompound("gobed");
        gobedTag.putString("returnDim", dimension.toString());
        gobedTag.putInt("returnX", pos.getX());
        gobedTag.putInt("returnY", pos.getY());
        gobedTag.putInt("returnZ", pos.getZ());
        tag.put("gobed", gobedTag);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
    }

    private Object[] loadReturnPos(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag gobedTag = tag.getCompound("gobed");
        ResourceLocation dim = ResourceLocation.tryParse(gobedTag.getString("returnDim"));
        if (dim == null) dim = Level.OVERWORLD.location();
        BlockPos pos = new BlockPos(gobedTag.getInt("returnX"), gobedTag.getInt("returnY"), gobedTag.getInt("returnZ"));
        return new Object[]{dim, pos};
    }

    /**
     * 在 (0, startHeight, 0) 附近寻找"脚下有方块、头顶两格空气"的安全落点。
     * 多人服务器下避免所有玩家都被传到同一格、互相踩头/卡进方块里（见审查 #9）。
     */
    private BlockPos findSafeSpawn(ServerLevel level, int startHeight) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(0, startHeight, 0);
        for (int y = Math.min(startHeight, maxY - 1); y > minY; y--) {
            mpos.setX(0).setZ(0).setY(y);
            boolean feetAir = level.getBlockState(mpos).isAir();
            boolean headAir = level.getBlockState(mpos.setY(y + 1)).isAir();
            boolean groundSolid = !level.getBlockState(mpos.setY(y - 1)).isAir();
            if (feetAir && headAir && groundSolid) {
                return new BlockPos(0, y, 0);
            }
        }
        return new BlockPos(0, Math.max(startHeight, minY + 1), 0);
    }
}

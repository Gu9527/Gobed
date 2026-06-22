package com.example.gobed;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 通过 {@link EventBusSubscriber} 自动注册到 GAME 事件总线，
 * 替代 {@code GoBedMod} 中手动 {@code NeoForge.EVENT_BUS.register(...)} 的旧写法（见审查 #22）。
 */
@EventBusSubscriber(modid = GoBedMod.MOD_ID)
public class GoBedEvents {
    private static final long NOON_TIME = 6000L;

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // 只对 GoBed 自有维度生效，避免对主世界/下界/末地/其它模组维度每 tick 干扰（见审查 #8）
        if (!serverLevel.dimension().equals(GoBedMod.GO_BED_DIMENSION)) {
            return;
        }
        long timeOfDay = serverLevel.getDayTime() % 24000L;
        if (timeOfDay != NOON_TIME) {
            long currentDay = serverLevel.getDayTime() / 24000L;
            serverLevel.setDayTime(currentDay * 24000L + NOON_TIME);
        }
    }
}

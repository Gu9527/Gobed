package com.example.gobed;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class GoBedEvents {
    private static final long NOON_TIME = 6000L;

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            long timeOfDay = serverLevel.getDayTime() % 24000L;
            if (timeOfDay != NOON_TIME) {
                long currentDay = serverLevel.getDayTime() / 24000L;
                serverLevel.setDayTime(currentDay * 24000L + NOON_TIME);
            }
        }
    }
}

package com.example.gobed;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private EditBox lightLevelBox;
    private EditBox spawnHeightBox;
    private EditBox chunkUnitBox;
    private EditBox dimensionHeightBox;
    private EditBox borderBlacklistBox;
    private EditBox innerBlacklistBox;
    private Button enableTeleportBtn;
    private boolean enableTeleport = true;
    private String errorMsg = "";

    public ConfigScreen(Screen parent) {
        super(Component.translatable("gobed.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;

        this.addRenderableWidget(Button.builder(Component.translatable("gobed.config.save"), button -> {
            saveConfig();
            this.minecraft.setScreen(parent);
        }).bounds(centerX - 60, this.height - 35, 120, 20).build());

        this.lightLevelBox = new EditBox(this.font, centerX - 50, startY + 30, 100, 20, Component.translatable("gobed.config.portal_light_level"));
        this.lightLevelBox.setValue(String.valueOf(ModConfigUtil.COMMON.portalLightLevel.get()));
        this.addWidget(lightLevelBox);

        this.spawnHeightBox = new EditBox(this.font, centerX - 50, startY + 80, 100, 20, Component.translatable("gobed.config.spawn_height"));
        this.spawnHeightBox.setValue(String.valueOf(ModConfigUtil.COMMON.spawnHeight.get()));
        this.addWidget(spawnHeightBox);

        this.chunkUnitBox = new EditBox(this.font, centerX - 50, startY + 130, 100, 20, Component.translatable("gobed.config.chunk_unit"));
        this.chunkUnitBox.setValue(String.valueOf(ModConfigUtil.COMMON.chunkUnitSize.get()));
        this.addWidget(chunkUnitBox);

        this.dimensionHeightBox = new EditBox(this.font, centerX - 50, startY + 180, 100, 20, Component.translatable("gobed.config.dimension_height"));
        this.dimensionHeightBox.setValue(String.valueOf(ModConfigUtil.COMMON.dimensionHeight.get()));
        this.addWidget(dimensionHeightBox);

        enableTeleport = ModConfigUtil.COMMON.enableTeleportation.get();
        this.enableTeleportBtn = Button.builder(
            Component.translatable(enableTeleport ? "gobed.config.teleport_on" : "gobed.config.teleport_off"),
            button -> {
                enableTeleport = !enableTeleport;
                button.setMessage(Component.translatable(enableTeleport ? "gobed.config.teleport_on" : "gobed.config.teleport_off"));
            }
        ).bounds(centerX - 60, startY + 230, 120, 20).build();
        this.addRenderableWidget(enableTeleportBtn);

        this.borderBlacklistBox = new EditBox(this.font, centerX - 50, startY + 280, 100, 20, Component.translatable("gobed.config.border_blacklist"));
        this.borderBlacklistBox.setValue(String.join(",", ModConfigUtil.COMMON.borderBlacklist.get()));
        this.addWidget(borderBlacklistBox);

        this.innerBlacklistBox = new EditBox(this.font, centerX - 50, startY + 330, 100, 20, Component.translatable("gobed.config.inner_blacklist"));
        this.innerBlacklistBox.setValue(String.join(",", ModConfigUtil.COMMON.innerBlacklist.get()));
        this.addWidget(innerBlacklistBox);
    }

    private boolean validateAndSet(String value, int min, int max, String errorRangeKey, String errorNumberKey,
                                    ModConfigSpec.IntValue configValue) {
        try {
            int val = Integer.parseInt(value);
            if (val < min || val > max) {
                errorMsg = Component.translatable(errorRangeKey).getString();
                return false;
            }
            configValue.set(val);
            return true;
        } catch (NumberFormatException e) {
            errorMsg = Component.translatable(errorNumberKey).getString();
            return false;
        }
    }

    private void saveConfig() {
        errorMsg = "";

        if (!validateAndSet(lightLevelBox.getValue(), 0, 15,
                "gobed.config.error.light_range", "gobed.config.error.light_number",
                ModConfigUtil.COMMON.portalLightLevel)) return;

        if (!validateAndSet(spawnHeightBox.getValue(), -64, 320,
                "gobed.config.error.spawn_range", "gobed.config.error.spawn_number",
                ModConfigUtil.COMMON.spawnHeight)) return;

        if (!validateAndSet(chunkUnitBox.getValue(), 1, 16,
                "gobed.config.error.chunk_range", "gobed.config.error.chunk_number",
                ModConfigUtil.COMMON.chunkUnitSize)) return;

        if (!validateAndSet(dimensionHeightBox.getValue(), 1, 320,
                "gobed.config.error.height_range", "gobed.config.error.height_number",
                ModConfigUtil.COMMON.dimensionHeight)) return;

        ModConfigUtil.COMMON.enableTeleportation.set(enableTeleport);

        String borderRaw = borderBlacklistBox.getValue().trim();
        String innerRaw = innerBlacklistBox.getValue().trim();
        if (!borderRaw.isEmpty()) {
            ModConfigUtil.COMMON.borderBlacklist.set(List.of(borderRaw.split(",")));
        } else {
            ModConfigUtil.COMMON.borderBlacklist.set(List.of("minecraft:bedrock"));
        }
        if (!innerRaw.isEmpty()) {
            ModConfigUtil.COMMON.innerBlacklist.set(List.of(innerRaw.split(",")));
        } else {
            ModConfigUtil.COMMON.innerBlacklist.set(List.of("minecraft:bedrock"));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 30;

        String title = Component.translatable("gobed.config.title").getString();
        guiGraphics.drawString(this.font, title, centerX - this.font.width(title) / 2, 10, 0xFFFFFF);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.portal_light_level").getString(), centerX - 120, startY + 18, 0xAAAAAA);
        this.lightLevelBox.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.spawn_height").getString(), centerX - 120, startY + 68, 0xAAAAAA);
        this.spawnHeightBox.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.chunk_unit").getString(), centerX - 120, startY + 118, 0xAAAAAA);
        this.chunkUnitBox.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.dimension_height").getString(), centerX - 120, startY + 168, 0xAAAAAA);
        this.dimensionHeightBox.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.enable_teleport").getString(), centerX - 120, startY + 218, 0xAAAAAA);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.border_blacklist").getString(), centerX - 120, startY + 268, 0xAAAAAA);
        this.borderBlacklistBox.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.inner_blacklist").getString(), centerX - 120, startY + 318, 0xAAAAAA);
        this.innerBlacklistBox.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!errorMsg.isEmpty()) {
            guiGraphics.drawString(this.font, "§c" + errorMsg, centerX - 120, this.height - 55, 0xFF5555);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}

package com.example.gobed;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigScreen extends Screen {
    private final ModContainer modContainer;
    private final Screen parent;
    private EditBox lightLevelBox;
    private EditBox spawnHeightBox;
    private EditBox chunkUnitBox;
    private EditBox dimensionHeightBox;
    private EditBox borderBlacklistBox;
    private EditBox innerBlacklistBox;
    private Button enableTeleportBtn;
    private boolean enableTeleport = true;
    private List<String> errors = new ArrayList<>();
    private int labelX;

    public ConfigScreen(ModContainer modContainer, Screen parent) {
        super(Component.translatable("gobed.config.title"));
        this.modContainer = modContainer;
        this.parent = parent;
    }

    public ConfigScreen(Screen parent) {
        this(null, parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        labelX = centerX - 150;
        int boxX = centerX + 10;
        int boxW = 120;
        int boxH = 18;
        int rowH = 36;
        int startY = 25;

        this.addRenderableWidget(Button.builder(Component.translatable("gobed.config.save"), button -> {
            if (saveConfig()) {
                this.minecraft.setScreen(parent);
            }
        }).bounds(centerX - 60, this.height - 30, 120, 20).build());

        lightLevelBox = addRenderableWidget(new EditBox(this.font, boxX, startY, boxW, boxH, Component.empty()));
        lightLevelBox.setValue(String.valueOf(ModConfigUtil.COMMON.portalLightLevel.get()));

        spawnHeightBox = addRenderableWidget(new EditBox(this.font, boxX, startY + rowH, boxW, boxH, Component.empty()));
        spawnHeightBox.setValue(String.valueOf(ModConfigUtil.COMMON.spawnHeight.get()));

        chunkUnitBox = addRenderableWidget(new EditBox(this.font, boxX, startY + rowH * 2, boxW, boxH, Component.empty()));
        chunkUnitBox.setValue(String.valueOf(ModConfigUtil.COMMON.chunkUnitSize.get()));

        dimensionHeightBox = addRenderableWidget(new EditBox(this.font, boxX, startY + rowH * 3, boxW, boxH, Component.empty()));
        dimensionHeightBox.setValue(String.valueOf(ModConfigUtil.COMMON.dimensionHeight.get()));

        enableTeleport = ModConfigUtil.COMMON.enableTeleportation.get();
        enableTeleportBtn = addRenderableWidget(Button.builder(
            Component.translatable(enableTeleport ? "gobed.config.teleport_on" : "gobed.config.teleport_off"),
            button -> {
                enableTeleport = !enableTeleport;
                button.setMessage(Component.translatable(enableTeleport ? "gobed.config.teleport_on" : "gobed.config.teleport_off"));
            }
        ).bounds(boxX, startY + rowH * 4, boxW, boxH).build());

        borderBlacklistBox = addRenderableWidget(new EditBox(this.font, boxX, startY + rowH * 5, boxW, boxH, Component.empty()));
        borderBlacklistBox.setValue(String.join(",", ModConfigUtil.COMMON.borderBlacklist.get()));

        innerBlacklistBox = addRenderableWidget(new EditBox(this.font, boxX, startY + rowH * 6, boxW, boxH, Component.empty()));
        innerBlacklistBox.setValue(String.join(",", ModConfigUtil.COMMON.innerBlacklist.get()));
    }

    private boolean saveConfig() {
        errors.clear();

        Integer light = parseInt(lightLevelBox.getValue(), 0, 15,
                Component.translatable("gobed.config.error.light_range").getString(),
                Component.translatable("gobed.config.error.light_number").getString());
        Integer spawn = parseInt(spawnHeightBox.getValue(), -64, 320,
                Component.translatable("gobed.config.error.spawn_range").getString(),
                Component.translatable("gobed.config.error.spawn_number").getString());
        Integer chunk = parseInt(chunkUnitBox.getValue(), 1, 16,
                Component.translatable("gobed.config.error.chunk_range").getString(),
                Component.translatable("gobed.config.error.chunk_number").getString());
        Integer height = parseInt(dimensionHeightBox.getValue(), 1, 320,
                Component.translatable("gobed.config.error.height_range").getString(),
                Component.translatable("gobed.config.error.height_number").getString());

        if (!errors.isEmpty()) {
            return false;
        }

        ModConfigUtil.COMMON.portalLightLevel.set(light);
        ModConfigUtil.COMMON.spawnHeight.set(spawn);
        ModConfigUtil.COMMON.chunkUnitSize.set(chunk);
        ModConfigUtil.COMMON.dimensionHeight.set(height);
        ModConfigUtil.COMMON.enableTeleportation.set(enableTeleport);
        ModConfigUtil.COMMON.borderBlacklist.set(parseList(borderBlacklistBox.getValue()));
        ModConfigUtil.COMMON.innerBlacklist.set(parseList(innerBlacklistBox.getValue()));
        return true;
    }

    private Integer parseInt(String value, int min, int max, String rangeMsg, String numberMsg) {
        try {
            int val = Integer.parseInt(value.trim());
            if (val < min || val > max) {
                errors.add(rangeMsg);
                return null;
            }
            return val;
        } catch (NumberFormatException e) {
            errors.add(numberMsg);
            return null;
        }
    }

    private List<String> parseList(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return List.of("minecraft:bedrock");
        }
        return Arrays.stream(trimmed.split("\\s*,\\s*"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 25;
        int rowH = 36;

        guiGraphics.drawString(this.font, Component.translatable("gobed.config.title"),
                centerX - this.font.width(Component.translatable("gobed.config.title")) / 2, 5, 0xFFFFFF);

        int labelY = startY + 4;
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.portal_light_level"), labelX, labelY, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.spawn_height"), labelX, labelY + rowH, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.chunk_unit"), labelX, labelY + rowH * 2, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.dimension_height"), labelX, labelY + rowH * 3, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.enable_teleport"), labelX, labelY + rowH * 4, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.border_blacklist"), labelX, labelY + rowH * 5, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gobed.config.inner_blacklist"), labelX, labelY + rowH * 6, 0xAAAAAA);

        int errorY = this.height - 50 - Math.max(0, errors.size() - 1) * 11;
        for (String err : errors) {
            guiGraphics.drawString(this.font, "§c" + err, labelX, errorY, 0xFF5555);
            errorY += 11;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}

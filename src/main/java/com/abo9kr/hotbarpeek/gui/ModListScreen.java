/*
 * HotBarPeek
 * Copyright (c) 2026 abo9kr. All Rights Reserved.
 * لا يجوز نسخ أو إعادة توزيع هذا الكود بدون إذن كتابي من المطوّر.
 */
package com.abo9kr.hotbarpeek.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModListScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int LIST_TOP = 40;

    private final List<ModEntry> mods = new ArrayList<>();
    private String statusMessage = "";

    public ModListScreen() {
        super(Text.literal("HotBarPeek"));
    }

    @Override
    protected void init() {
        super.init();
        loadMods();
        buildWidgets();
    }

    private void loadMods() {
        mods.clear();
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods").toAbsolutePath().normalize();

        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModOrigin origin = container.getOrigin();
            if (origin.getKind() != ModOrigin.Kind.PATH) {
                continue;
            }
            List<Path> paths = origin.getPaths();
            if (paths.isEmpty()) {
                continue;
            }
            Path path = paths.get(0).toAbsolutePath().normalize();
            if (!path.startsWith(modsDir) || !path.toString().toLowerCase().endsWith(".jar")) {
                continue;
            }
            mods.add(new ModEntry(container.getMetadata().getId(), container.getMetadata().getName(), path));
        }
        mods.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
    }

    private void buildWidgets() {
        clearChildren();
        int centerX = this.width / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), btn -> refresh())
                .dimensions(centerX - 100, 10, 200, 20)
                .build());

        int y = LIST_TOP;
        for (ModEntry entry : mods) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Delete").formatted(Formatting.RED),
                            btn -> deleteMod(entry))
                    .dimensions(centerX + 90, y, 60, 20)
                    .build());
            y += ROW_HEIGHT;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
                .dimensions(centerX - 100, this.height - 30, 200, 20)
                .build());
    }

    private void deleteMod(ModEntry entry) {
        try {
            Files.deleteIfExists(entry.jarPath);
            statusMessage = "تم حذف: " + entry.name;
        } catch (IOException e) {
            statusMessage = "فشل حذف " + entry.name + " (قد يكون الملف قيد الاستخدام، أعد تشغيل اللعبة وحاول مرة أخرى)";
        }
        refresh();
    }

    private void refresh() {
        loadMods();
        buildWidgets();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        int centerX = this.width / 2;
        int y = LIST_TOP;
        for (ModEntry entry : mods) {
            context.drawTextWithShadow(this.textRenderer,
                    Text.literal(entry.name + " (" + entry.id + ")"),
                    centerX - 150, y + 6, 0xFFFFFF);
            y += ROW_HEIGHT;
        }

        if (mods.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("لا توجد موّدات إضافية في مجلد mods"), this.width / 2, LIST_TOP + 10, 0xAAAAAA);
        }

        if (!statusMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(statusMessage),
                    this.width / 2, this.height - 45, 0xFFFF55);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class ModEntry {
        final String id;
        final String name;
        final Path jarPath;

        ModEntry(String id, String name, Path jarPath) {
            this.id = id;
            this.name = name;
            this.jarPath = jarPath;
        }
    }
}

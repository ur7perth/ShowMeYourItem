/*
 * HotBarPeek
 * Copyright (c) 2026 abo9kr. All Rights Reserved.
 * لا يجوز نسخ أو إعادة توزيع هذا الكود بدون إذن كتابي من المطوّر.
 */
package com.abo9kr.hotbarpeek;

import com.abo9kr.hotbarpeek.gui.ModListScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HotBarPeekClient implements ClientModInitializer {

    private boolean comboWasActive = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.currentScreen != null) {
            comboWasActive = false;
            return;
        }

        long windowHandle = client.getWindow().getHandle();
        boolean qDown = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_Q);
        boolean f6Down = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_F6);
        boolean comboActive = qDown && f6Down;

        if (comboActive && !comboWasActive) {
            comboWasActive = true;
            client.setScreen(new ModListScreen());
        } else if (!comboActive) {
            comboWasActive = false;
        }
    }
}

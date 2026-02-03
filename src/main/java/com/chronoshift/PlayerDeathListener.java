/*
 * Chronoshift - A Hytale Mod
 * Copyright (c) 2025 veerack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this mod and associated documentation files (the "Mod"), to deal
 * in the Mod without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Mod, and to permit persons to whom the Mod is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Mod.
 *
 * THE MOD IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE MOD OR THE USE OR OTHER DEALINGS IN THE MOD.
 */


package com.chronoshift;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerDeathListener {

    private final CheckpointManager checkpointManager;

    public PlayerDeathListener(CheckpointManager checkpointManager) {
        this.checkpointManager = checkpointManager;
    }

    public void register(Object registry) {
        String[] possibleEventNames = {
            "com.hypixel.hytale.server.core.modules.entity.damage.event.PlayerDeathEvent",
            "com.hypixel.hytale.server.core.event.events.entity.PlayerDeathEvent",
            "com.hypixel.hytale.server.core.event.events.player.PlayerDeathEvent"
        };

        for (String eventName : possibleEventNames) {
            try {
                Class<?> eventClass = Class.forName(eventName);
                Method m = registry.getClass().getMethod("registerGlobal", Class.class, Consumer.class);
                m.invoke(registry, eventClass, (Consumer<Object>) this::handlePlayerDeath);
                break;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            } catch (Throwable ignored) {}
        }
    }

    private void handlePlayerDeath(Object event) {
        try {
            Object playerObj = invokeMethod(event, "getEntity");
            if (!(playerObj instanceof Player)) return;

            Player player = (Player) playerObj;
            UUID playerUuid = player.getUuid();

            CheckpointData data = checkpointManager.getCheckpointData(playerUuid.toString());

            if (data.hasCharm() && data.getCheckpoint() != null) {
                invokeMethod(event, "setCancelled", true);
                restoreCheckpoint(player, data.getCheckpoint());

                checkpointManager.removeCharm(playerUuid.toString());
                checkpointManager.saveCheckpoints();

                player.sendMessage(Message.raw("CHRONOSHIFT ACTIVATED! You have been restored to your checkpoint.").color(Color.MAGENTA));
            }
        } catch (Exception e) {
            System.err.println("[Chronoshift] Error handling player death: " + e.getMessage());
        }
    }

    private Object invokeMethod(Object obj, String methodName, Object... args) throws Exception {
        try {
            Class<?>[] argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i] instanceof Boolean ? boolean.class : args[i].getClass();
            }
            return obj.getClass().getMethod(methodName, argTypes).invoke(obj, args);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object invokeMethod(Object obj, String methodName) throws Exception {
        try {
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void restoreCheckpoint(Player player, CheckpointData.Checkpoint checkpoint) {
        try {
            System.err.println("[Chronoshift] Restored player to checkpoint at " +
                    checkpoint.getWorld() + " (" + checkpoint.getX() + ", " +
                    checkpoint.getY() + ", " + checkpoint.getZ() + ")");
        } catch (Exception e) {
            System.err.println("[Chronoshift] Failed to restore checkpoint: " + e.getMessage());
        }
    }
}

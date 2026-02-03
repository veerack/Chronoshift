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
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class PlayerInventoryListener {

    private final CheckpointManager checkpointManager;

    public PlayerInventoryListener(CheckpointManager checkpointManager) {
        this.checkpointManager = checkpointManager;
    }

    public void register(Object registry) {
        String[] pickupEventNames = {
            "com.hypixel.hytale.server.core.event.events.inventory.InventoryPickupItemEvent",
            "com.hypixel.hytale.server.core.event.events.inventory.PickupItemEvent",
            "com.hypixel.hytale.server.core.event.events.inventory.ItemPickupEvent"
        };

        for (String eventName : pickupEventNames) {
            try {
                Class<?> eventClass = Class.forName(eventName);
                Method m = registry.getClass().getMethod("registerGlobal", Class.class, Consumer.class);
                m.invoke(registry, eventClass, (Consumer<Object>) this::handlePickup);
                break;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            } catch (Throwable ignored) {}
        }

        // Register inventory click event to prevent removing items when Chronoshift is present
        String[] clickEventNames = {
            "com.hypixel.hytale.server.core.event.events.inventory.InventoryClickEvent",
            "com.hypixel.hytale.server.core.event.events.inventory.ClickInventoryEvent"
        };

        for (String eventName : clickEventNames) {
            try {
                Class<?> eventClass = Class.forName(eventName);
                Method m = registry.getClass().getMethod("registerGlobal", Class.class, Consumer.class);
                m.invoke(registry, eventClass, (Consumer<Object>) this::handleInventoryClick);
                break;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            } catch (Throwable ignored) {}
        }
    }

    private void handlePickup(Object event) {
        try {
            Object playerObj = invokeMethod(event, "getPlayer");
            if (!(playerObj instanceof Player)) return;

            Player player = (Player) playerObj;

            Object itemObj = invokeMethod(event, "getItem");
            if (!(itemObj instanceof ItemStack)) return;

            ItemStack item = (ItemStack) itemObj;
            if (!"Chronoshift".equals(item.getItemId())) return;

            CheckpointData data = checkpointManager.getCheckpointData(player.getUuid().toString());
            if (!data.hasCharm() || data.getCheckpoint() == null) {
                player.sendMessage(Message.raw("Chronoshift acquired! Use /tc to save a checkpoint.").color(Color.CYAN));
            }
        } catch (Exception e) {
            System.err.println("[Chronoshift] Error in pickup handler: " + e.getMessage());
        }
    }

    private Object invokeMethod(Object obj, String methodName) throws Exception {
        try {
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void handleInventoryClick(Object event) {
        try {
            Object playerObj = invokeMethod(event, "getPlayer");
            if (!(playerObj instanceof Player)) return;

            Player player = (Player) playerObj;

            // Check if player has Chronoshift
            if (!hasChronoshift(player)) return;

            // Try to get the click action
            Object actionObj = null;
            try {
                actionObj = invokeMethod(event, "getAction");
            } catch (Exception ignored) {}

            // Get action name
            String action = actionObj != null ? actionObj.toString().toUpperCase() : "";

            // Cancel any click that could remove items (dropping, picking up, swapping)
            boolean isRemoval = action.contains("DROP") || action.contains("THROW") ||
                               action.contains("PICKUP") || action.contains("SWAP") ||
                               action.contains("MOVE_TO_CURSOR") || action.contains("HOTBAR_SWAP");

            if (isRemoval) {
                try {
                    java.lang.reflect.Method cancelMethod = event.getClass().getMethod("setCancelled", boolean.class);
                    cancelMethod.invoke(event, true);
                    player.sendMessage(Message.raw("§cCannot remove items while Chronoshift is active!"));
                    System.err.println("[Chronoshift] Cancelled inventory action: " + action);
                } catch (Exception e) {
                    System.err.println("[Chronoshift] Failed to cancel event: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("[Chronoshift] Error in inventory click handler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean hasChronoshift(Player player) {
        try {
            Inventory inv = player.getInventory();
            if (inv == null) return false;

            ItemContainer everything = inv.getCombinedEverything();
            if (everything == null) return false;

            final boolean[] found = {false};
            everything.forEach((slot, itemStack) -> {
                if (itemStack != null && "Chronoshift".equals(itemStack.getItemId())) {
                    found[0] = true;
                }
            });
            return found[0];
        } catch (Exception e) {
            return false;
        }
    }
}

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

import com.hypixel.hytale.server.core.entity.entities.Player;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class EventSniffer {

    /* well, I couldn't think of any better way to do this...........
    * open-source exists also to embarrass yourself :D */

    public void registerAll(Object registry) {
        String[][] eventsToTry = {
            {"PlayerJoinEvent", "com.hypixel.hytale.server.core.event.events.player.PlayerJoinEvent"},
            {"PlayerReadyEvent", "com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent"},
            {"PlayerQuitEvent", "com.hypixel.hytale.server.core.event.events.player.PlayerQuitEvent"},
            {"PlayerDeathEvent", "com.hypixel.hytale.server.core.event.events.player.PlayerDeathEvent"},
            {"PlayerRespawnEvent", "com.hypixel.hytale.server.core.event.events.player.PlayerRespawnEvent"},
            {"PlayerMoveEvent", "com.hypixel.hytale.server.core.event.events.player.PlayerMoveEvent"},
            {"EntityDamageEvent", "com.hypixel.hytale.server.core.event.events.entity.EntityDamageEvent"},
            {"EntityDeathEvent", "com.hypixel.hytale.server.core.event.events.entity.EntityDeathEvent"},
            {"InventoryPickupItemEvent", "com.hypixel.hytale.server.core.event.events.inventory.InventoryPickupItemEvent"},
            {"PickupItemEvent", "com.hypixel.hytale.server.core.event.events.inventory.PickupItemEvent"},
            {"ItemPickupEvent", "com.hypixel.hytale.server.core.event.events.inventory.ItemPickupEvent"},
            {"BlockBreakEvent", "com.hypixel.hytale.server.core.event.events.block.BlockBreakEvent"},
            {"BlockPlaceEvent", "com.hypixel.hytale.server.core.event.events.block.BlockPlaceEvent"},
        };

        for (String[] eventInfo : eventsToTry) {
            String eventName = eventInfo[0];
            String eventClass = eventInfo[1];

            try {
                Class<?> cls = Class.forName(eventClass);
                Method m = registry.getClass().getMethod("registerGlobal", Class.class, Consumer.class);

                Consumer<Object> handler = (Object event) -> {
                    System.out.println("[Chronoshift Sniffer] ✓✓✓ EVENT FIRED: " + eventName);
                    try {
                        if (event != null) {
                            System.out.println("  Event class: " + event.getClass().getName());
                            try {
                                Method getPlayer = event.getClass().getMethod("getPlayer");
                                Object player = getPlayer.invoke(event);
                                if (player instanceof Player) {
                                    Player p = (Player) player;
                                    System.out.println("  Player: " + p.getUuid());
                                }
                            } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                };

                m.invoke(registry, cls, handler);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            } catch (Throwable ignored) {}
        }
    }
}

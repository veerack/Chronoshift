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

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.chronoshift.utils.ChatColors;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChronoshiftActivationSystem extends DamageEventSystem {

    private final CheckpointManager checkpointManager;
    private final ChronoshiftEffects effects;

    private final Map<UUID, Long> activatingPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> immunePlayers = new ConcurrentHashMap<>();

    private static final float ACTIVATION_THRESHOLD = 0.2f;

    public ChronoshiftActivationSystem(CheckpointManager checkpointManager, ChronoshiftEffects effects) {
        this.checkpointManager = checkpointManager;
        this.effects = effects;
    }

    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer, Damage event) {

        if (event == null || event.isCancelled()) return;

        Player victim = chunk.getComponent(index, Player.getComponentType());
        if (victim == null) return;

        UUID playerUuid = victim.getUuid();
        if (playerUuid == null) return;

        float currentHealth = getPlayerHealth(store, index, chunk);
        float newHealth = currentHealth - event.getAmount();

        if (immunePlayers.containsKey(playerUuid)) {
            event.setCancelled(true);
            event.setAmount(0f);
            return;
        }

        if (activatingPlayers.containsKey(playerUuid)) {
            return;
        }

        CheckpointData data = checkpointManager.getCheckpointData(playerUuid.toString());
        if (!data.hasCharm() || data.getCheckpoint() == null) {
            return;
        }

        if (!hasTimewiseClockInOffHand(victim)) {
            return;
        }

        if (newHealth <= ACTIVATION_THRESHOLD) {
            activatingPlayers.put(playerUuid, System.currentTimeMillis());
            event.setCancelled(true);
            event.setAmount(0f);
            startActivationSequence(victim, data.getCheckpoint());
        }
    }

    private void startActivationSequence(Player player, CheckpointData.Checkpoint checkpoint) {
        UUID playerUuid = player.getUuid();

        try {
            effects.playTeleportSound(player);
            effects.changeItemAnimation(player, "Items/timecharm/ClockAnimFast.blockyanim");
            effects.applyFreezeEffect(player);
            immunePlayers.put(playerUuid, true);

            String playerName = getPlayerName(playerUuid);

            com.hypixel.hytale.server.core.util.EventTitleUtil.showEventTitleToPlayer(
                    com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(playerUuid),
                    Message.raw("» CHRONOSHIFT «").color(new Color(255, 0, 255)),
                    Message.raw("You are about to rewind time!").color(new Color(255, 255, 0)),
                    true
            );

            scheduleRestoration(player, checkpoint, 3000L);

        } catch (Exception e) {
            System.err.println("[TimewiseClock] Activation failed: " + e.getMessage());
            activatingPlayers.remove(playerUuid);
            immunePlayers.remove(playerUuid);
        }
    }

    private void scheduleRestoration(Player player, CheckpointData.Checkpoint checkpoint, long delayMillis) {
        UUID playerUuid = player.getUuid();

        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                effects.showTeleportParticles(player, 2.0f, "Entity");
                Thread.sleep(2000);

                restorePlayer(player, checkpoint);

                activatingPlayers.remove(playerUuid);
                immunePlayers.remove(playerUuid);

                checkpointManager.removeCharm(playerUuid.toString());
                checkpointManager.saveCheckpoints();

                player.sendMessage(ChatColors.parse("» &f The &l&gyellow{Chronoshift}&r&f brought you back in time and disappeared..."));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                activatingPlayers.remove(playerUuid);
                immunePlayers.remove(playerUuid);
            } catch (Exception e) {
                System.err.println("[Chronoshift] Restoration failed: " + e.getMessage());
                activatingPlayers.remove(playerUuid);
                immunePlayers.remove(playerUuid);
            }
        }).start();
    }

    private void restorePlayer(Player player, CheckpointData.Checkpoint checkpoint) {
        try {
            effects.restoreInventory(player, checkpoint.getInventoryData());
            effects.teleportPlayer(player, checkpoint.getWorld(), checkpoint.getX(), checkpoint.getY(),
                    checkpoint.getZ(), checkpoint.getYaw(), checkpoint.getPitch(), checkpoint.getInventoryData());
            effects.setPlayerHealth(player, 100.0f);
            effects.removeFreezeEffect(player);
        } catch (Exception e) {
            System.err.println("[Chronoshift] Restore failed: " + e.getMessage());
        }
    }

    public boolean isPlayerImmune(UUID playerUuid) {
        return immunePlayers.containsKey(playerUuid);
    }

    public void removeImmunity(UUID playerUuid) {
        immunePlayers.remove(playerUuid);
        activatingPlayers.remove(playerUuid);
    }

    private float getPlayerHealth(Store<EntityStore> store, int index, ArchetypeChunk<EntityStore> chunk) {
        try {
            EntityStatMap statMap = chunk.getComponent(index, EntityStatMap.getComponentType());
            if (statMap != null) {
                int healthIndex = DefaultEntityStatTypes.getHealth();
                EntityStatValue healthValue = statMap.get(healthIndex);
                if (healthValue != null) {
                    return healthValue.get();
                }
            }
        } catch (Throwable e) {
            System.err.println("[Chronoshift] Failed to read health: " + e.getMessage());
        }
        return 20.0f;
    }

    private boolean hasTimewiseClockInOffHand(Player player) {
        try {
            Inventory inv = player.getInventory();
            if (inv == null) return false;

            ItemContainer utility = inv.getUtility();
            if (utility == null) return false;

            ItemStack item = utility.getItemStack((short) 0);
            if (item == null) return false;

            return "Chronoshift".equals(item.getItemId());
        } catch (Exception e) {
            System.err.println("[Chronoshift] Failed to check off-hand: " + e.getMessage());
            return false;
        }
    }

    private String getPlayerName(UUID playerUuid) {
        try {
            var universe = com.hypixel.hytale.server.core.universe.Universe.get();
            if (universe != null) {
                var playerRef = universe.getPlayer(playerUuid);
                if (playerRef != null && playerRef.getUsername() != null) {
                    return playerRef.getUsername();
                }
            }
        } catch (Throwable ignored) {}
        return "Player";
    }
}

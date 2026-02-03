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

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.world.SpawnParticleSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.Predictable;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldNotificationHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;

import com.chronoshift.utils.TeleportUtil;

import java.lang.reflect.Method;

public class ChronoshiftEffects {

    private static final float FREEZE_DURATION = 10.0f;
    private static final float RESTORE_HEALTH = 100.0f;

    public void applyFreezeEffect(Player player) {
        try {
            Universe universe = Universe.get();
            if (universe == null) return;

            PlayerRef playerRef = universe.getPlayer(player.getUuid());
            if (playerRef == null || !playerRef.isValid()) return;

            World world = getWorld(playerRef);
            if (world == null) return;

            world.execute(() -> {
                try {
                    Ref<EntityStore> entityRef = (Ref<EntityStore>) playerRef.getReference();
                    Store<EntityStore> store = world.getEntityStore().getStore();

                    if (entityRef == null || store == null) return;

                    EntityEffect freezeEffect = EntityEffect.getAssetMap().getAsset("Freeze");
                    if (freezeEffect == null) return;

                    EffectControllerComponent effectController = store.getComponent(
                            entityRef, EffectControllerComponent.getComponentType());

                    if (effectController != null) {
                        effectController.addEffect(entityRef, freezeEffect, FREEZE_DURATION,
                                OverlapBehavior.OVERWRITE, store);
                    }
                } catch (Exception e) {
                    System.err.println("[Chronoshift] Freeze failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Chronoshift] Freeze error: " + e.getMessage());
        }
    }

    public void removeFreezeEffect(Player player) {
        try {
            Universe universe = Universe.get();
            if (universe == null) return;

            PlayerRef playerRef = universe.getPlayer(player.getUuid());
            if (playerRef == null || !playerRef.isValid()) return;

            World world = getWorld(playerRef);
            if (world == null) return;

            world.execute(() -> {
                try {
                    Ref<EntityStore> entityRef = (Ref<EntityStore>) playerRef.getReference();
                    Store<EntityStore> store = world.getEntityStore().getStore();

                    if (entityRef == null || store == null) return;

                    EffectControllerComponent effectController = store.getComponent(
                            entityRef, EffectControllerComponent.getComponentType());

                    if (effectController != null) {
                        effectController.clearEffects(entityRef, store);
                    }
                } catch (Exception e) {
                    System.err.println("[Chronoshift] Unfreeze failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Chronoshift] Unfreeze error: " + e.getMessage());
        }
    }

    public void changeItemAnimation(Player player, String animationName) {
        try {
            Object itemStack = invokeMethod(player, "getItemInHand");
            if (itemStack == null) return;

            try {
                Method setPropertyMethod = itemStack.getClass().getMethod("setProperty", String.class, Object.class);
                setPropertyMethod.invoke(itemStack, "animation", animationName);
            } catch (Throwable ignored) {}
        } catch (Exception e) {
            System.err.println("[Chronoshift] Animation change failed: " + e.getMessage());
        }
    }

    public void playTeleportSound(Player player) {
        try {
            int soundIndex = com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex("TeleportSFX");

            Universe universe = Universe.get();
            if (universe == null) return;

            PlayerRef playerRef = universe.getPlayer(player.getUuid());
            if (playerRef == null || !playerRef.isValid()) return;

            World world = getWorld(playerRef);
            if (world == null) return;

            world.execute(() -> {
                try {
                    Ref<EntityStore> entityRef = (Ref<EntityStore>) playerRef.getReference();
                    Store<EntityStore> store = world.getEntityStore().getStore();

                    if (entityRef == null || store == null) return;

                    com.hypixel.hytale.server.core.modules.entity.component.TransformComponent transform = store.getComponent(
                            entityRef, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());

                    if (transform == null) return;

                    com.hypixel.hytale.server.core.universe.world.SoundUtil.playSoundEvent3dToPlayer(
                            entityRef,
                            soundIndex,
                            com.hypixel.hytale.protocol.SoundCategory.SFX,
                            transform.getPosition(),
                            store
                    );
                } catch (Exception e) {
                    System.err.println("[Chronoshift] Sound playback failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Chronoshift] Sound error: " + e.getMessage());
        }
    }

    public void showTeleportParticles(Player player, float scale, String target) {
        try {
            Universe universe = Universe.get();
            if (universe == null) return;

            PlayerRef playerRef = universe.getPlayer(player.getUuid());
            if (playerRef == null || !playerRef.isValid() || playerRef.getTransform() == null) return;

            World world = getWorld(playerRef);
            if (world == null) return;

            Vector3d pos = playerRef.getTransform().getPosition();
            if (pos == null) return;

            WorldNotificationHandler nh = world.getNotificationHandler();
            if (nh == null) return;

            Position position = new Position(pos.x, pos.y, pos.z);
            Direction dir = new Direction(0f, 0f, 0f);
            com.hypixel.hytale.protocol.Color color =
                    new com.hypixel.hytale.protocol.Color((byte) 255, (byte) 255, (byte) 255);

            Packet pkt = new SpawnParticleSystem("Teleport", position, dir, scale, color);

            int cx = (int) Math.floor(pos.x / 16.0);
            int cz = (int) Math.floor(pos.z / 16.0);

            nh.sendPacketIfChunkLoaded(pkt, cx, cz);
        } catch (Exception e) {
            System.err.println("[Chronoshift] Particle effect failed: " + e.getMessage());
        }
    }

    public void teleportPlayer(Player player, String worldName, double x, double y, double z,
            float yaw, float pitch, String inventoryData) {
        try {
            Universe universe = Universe.get();
            if (universe == null) return;

            World world = universe.getWorld(worldName);
            if (world == null) return;

            PlayerRef playerRef = universe.getPlayer(player.getUuid());
            if (playerRef == null || !playerRef.isValid()) return;

            world.execute(() -> {
                try {
                    Ref<EntityStore> playerEntityRef = (Ref<EntityStore>) playerRef.getReference();
                    if (playerEntityRef == null) return;

                    Store<EntityStore> store = world.getEntityStore().getStore();
                    if (store == null) return;

                    Vector3d pos = new Vector3d(x, y, z);
                    Vector3f rot = new Vector3f(pitch, yaw, 0f);

                    TeleportUtil.teleport(store, playerEntityRef, world, pos, rot);

                    clearMatchingDroppedItems(store, x, y, z, inventoryData);
                } catch (Exception e) {
                    System.err.println("[Chronoshift] Teleport failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Chronoshift] Teleport error: " + e.getMessage());
        }
    }

    private void clearMatchingDroppedItems(Store<EntityStore> store, double x, double y, double z, String inventoryData) {
        try {
            java.util.Set<String> savedItemIds = parseSavedItems(inventoryData);
            if (savedItemIds.isEmpty()) return;

            store.forEachEntityParallel((index, archetypeChunk, commandBuffer) -> {
                try {
                    if (archetypeChunk.getArchetype().contains(Player.getComponentType())) {
                        return;
                    }

                    com.hypixel.hytale.server.core.modules.entity.component.TransformComponent transform = archetypeChunk.getComponent(
                            index, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());

                    if (transform == null) return;

                    Vector3d entityPos = transform.getPosition();
                    if (entityPos == null) return;

                    double distance = Math.sqrt(
                            Math.pow(entityPos.x - x, 2) +
                            Math.pow(entityPos.y - y, 2) +
                            Math.pow(entityPos.z - z, 2)
                    );

                    if (distance <= 25.0) {
                        String itemId = getEntityItemId(archetypeChunk, index);
                        if (itemId != null && savedItemIds.contains(itemId)) {
                            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index),
                                    com.hypixel.hytale.component.RemoveReason.REMOVE);
                        }
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception e) {
            System.err.println("[Chronoshift] Clear dropped items failed: " + e.getMessage());
        }
    }

    private java.util.Set<String> parseSavedItems(String inventoryData) {
        java.util.Set<String> itemIds = new java.util.HashSet<>();
        if (inventoryData == null || inventoryData.isEmpty()) return itemIds;

        String[] items = inventoryData.split("\\|");
        for (String item : items) {
            String[] parts = item.split(":");
            if (parts.length >= 2) {
                itemIds.add(parts[1]);
            }
        }
        return itemIds;
    }

    private String getEntityItemId(com.hypixel.hytale.component.ArchetypeChunk<EntityStore> chunk, int index) {
        try {
            com.hypixel.hytale.server.core.modules.entity.item.ItemComponent itemEntity = chunk.getComponent(
                    index, com.hypixel.hytale.server.core.modules.entity.item.ItemComponent.getComponentType());

            if (itemEntity != null && itemEntity.getItemStack() != null) {
                return itemEntity.getItemStack().getItemId();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void setPlayerHealth(Player player, float health) {
        try {
            Universe universe = Universe.get();
            if (universe == null) return;

            PlayerRef playerRef = universe.getPlayer(player.getUuid());
            if (playerRef == null || !playerRef.isValid()) return;

            World world = getWorld(playerRef);
            if (world == null) return;

            world.execute(() -> {
                try {
                    Ref<EntityStore> entityRef = (Ref<EntityStore>) playerRef.getReference();
                    if (entityRef == null) return;

                    Store<EntityStore> store = world.getEntityStore().getStore();
                    if (store == null) return;

                    EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
                    if (statMap != null) {
                        int healthIndex = DefaultEntityStatTypes.getHealth();
                        statMap.setStatValue(Predictable.SELF, healthIndex, RESTORE_HEALTH);
                    }
                } catch (Exception e) {
                    System.err.println("[Chronoshift] Health restore failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Chronoshift] Health restore error: " + e.getMessage());
        }
    }

    public void restoreInventory(Player player, String inventoryData) {
        if (inventoryData == null || inventoryData.isEmpty()) return;

        try {
            Inventory inv = player.getInventory();
            if (inv == null) return;

            ItemContainer everything = inv.getCombinedEverything();
            if (everything == null) return;

            // remove chronoshift (tentative)
            removeChronoshift(inv);

            // will this work? no clue lol
            String[] items = inventoryData.split("\\|");

            for (String item : items) {
                String[] parts = item.split(":");
                if (parts.length == 3) {
                    short slot = Short.parseShort(parts[0]);
                    String itemId = parts[1];
                    int count = Integer.parseInt(parts[2]);

                    ItemStack itemStack = new ItemStack(itemId, count);
                    everything.addItemStackToSlot(slot, itemStack);
                }
            }
        } catch (Exception e) {
            System.err.println("[Chronoshift] Inventory restore failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeChronoshift(Inventory inv) {
        try {
            // check offhand
            ItemContainer utility = inv.getUtility();
            if (utility != null) {
                ItemStack offhandItem = utility.getItemStack((short) 0);
                if (offhandItem != null && "Chronoshift".equals(offhandItem.getItemId())) {
                    // use removeItemStackFromSlot to remove it
                    utility.removeItemStackFromSlot((short) 0);
                    return;
                }
            }

            // check main inventory if not in offhand
            ItemContainer everything = inv.getCombinedEverything();
            if (everything == null) return;

            final short[][] slotsToRemove = new short[1][];
            final int[] count = {0};

            everything.forEach((slot, itemStack) -> {
                if (itemStack != null && "Chronoshift".equals(itemStack.getItemId())) {
                    if (slotsToRemove[0] == null) {
                        slotsToRemove[0] = new short[10];
                    }
                    if (count[0] >= slotsToRemove[0].length) {
                        short[] newSlots = new short[slotsToRemove[0].length * 2];
                        System.arraycopy(slotsToRemove[0], 0, newSlots, 0, slotsToRemove[0].length);
                        slotsToRemove[0] = newSlots;
                    }
                    slotsToRemove[0][count[0]++] = slot;
                }
            });

            // remove each chronoshift item by replacing with nothing (empty) -- no idea if this will actually work,
            // but its worth trying, right?
            if (slotsToRemove[0] != null) {
                for (int i = 0; i < count[0]; i++) {
                    ItemStack air = new ItemStack("empty", 1);
                    everything.addItemStackToSlot(slotsToRemove[0][i], air);
                }
            }
        } catch (Exception e) {
            System.err.println("[Chronoshift] Failed to remove Chronoshift: " + e.getMessage());
        }
    }

    private World getWorld(PlayerRef playerRef) {
        try {
            var wid = playerRef.getWorldUuid();
            return wid != null ? Universe.get().getWorld(wid) : null;
        } catch (Throwable ignored) {
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
}

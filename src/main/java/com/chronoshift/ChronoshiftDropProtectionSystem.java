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
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ChronoshiftDropProtectionSystem extends EntityEventSystem<EntityStore, DropItemEvent.PlayerRequest> {

    private final CheckpointManager checkpointManager;

    public ChronoshiftDropProtectionSystem(CheckpointManager checkpointManager) {
        super(DropItemEvent.PlayerRequest.class);
        this.checkpointManager = checkpointManager;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(
            int index,
            ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            DropItemEvent.PlayerRequest event
    ) {
        if (event == null || event.isCancelled()) return;

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        // Check if player has Chronoshift and an active checkpoint
        CheckpointData data = checkpointManager.getCheckpointData(player.getUuid().toString());
        boolean hasActiveCheckpoint = data.hasCharm() && data.getCheckpoint() != null;

        boolean hasChronoshift = hasChronoshift(player);

        if (hasChronoshift && hasActiveCheckpoint) {
            // Cannot drop ANY items while Chronoshift is active with checkpoint
            event.setCancelled(true);
            player.sendMessage(com.chronoshift.utils.ChatColors.parse("&c» &cCannot drop items while Chronoshift is active!"));
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

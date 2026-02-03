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
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import com.chronoshift.utils.ChatColors;

import java.awt.Color;

public class ChronoshiftCommand extends CommandBase {

    private final CheckpointManager checkpointManager;

    public ChronoshiftCommand(CheckpointManager checkpointManager) {
        super("tc", "Save a Chronoshift checkpoint");
        this.checkpointManager = checkpointManager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            ctx.sendMessage(Message.raw("[Chronoshift] Only players can use this command.").color(Color.RED));
            return;
        }

        String playerUuid = ctx.sender().getUuid().toString();
        handleSave(ctx, playerUuid);
    }

    private void handleSave(CommandContext ctx, String playerUuid) {
        try {
            CheckpointData data = checkpointManager.getCheckpointData(playerUuid);

            // if already has checkpoint, deactivate it
            if (data.hasCharm() && data.getCheckpoint() != null) {
                data.setHasCharm(false);
                data.setCheckpoint(null);
                checkpointManager.saveCheckpoints();
                ctx.sendMessage(ChatColors.parse("&f» &fThe &l&gyellow{Chronoshift}&r&f has been deactivated."));
                return;
            }

            // otherwise, activate new checkpoint
            var universe = com.hypixel.hytale.server.core.universe.Universe.get();
            if (universe == null) {
                ctx.sendMessage(Message.raw("Could not get universe").color(Color.RED));
                return;
            }

            var playerRef = universe.getPlayer(java.util.UUID.fromString(playerUuid));
            if (playerRef == null || !playerRef.isValid() || playerRef.getTransform() == null) {
                ctx.sendMessage(Message.raw("Could not get player reference").color(Color.RED));
                return;
            }

            var pos = playerRef.getTransform().getPosition();
            var rot = playerRef.getTransform().getRotation();

            String worldName = "flat_world";
            try {
                var worldId = playerRef.getWorldUuid();
                if (worldId != null) {
                    var world = universe.getWorld(worldId);
                    if (world != null) {
                        worldName = world.getName();
                    }
                }
            } catch (Throwable ignored) {}

            String inventoryData = serializeInventory((Player) ctx.sender());

            CheckpointData.Checkpoint checkpoint = new CheckpointData.Checkpoint(
                worldName, pos.x, pos.y, pos.z,
                rot.y, rot.x, 20.0f,
                inventoryData,
                System.currentTimeMillis()
            );

            data.setHasCharm(true);
            data.setCheckpoint(checkpoint);
            checkpointManager.saveCheckpoints();

            ctx.sendMessage(ChatColors.parse("&f» &fThe &l&gyellow{Chronoshift}&r&f started bending time..."));
        } catch (Exception e) {
            ctx.sendMessage(Message.raw("Error: " + e.getMessage()).color(Color.RED));
        }
    }

    private String serializeInventory(Player player) {
        try {
            Inventory inv = player.getInventory();
            if (inv == null) return "";

            ItemContainer everything = inv.getCombinedEverything();
            if (everything == null) return "";

            StringBuilder sb = new StringBuilder();
            everything.forEach((slot, itemStack) -> {
                if (itemStack != null && !itemStack.isEmpty()) {
                    if ("Chronoshift".equals(itemStack.getItemId())) {
                        return;
                    }
                    if (sb.length() > 0) sb.append("|");
                    sb.append(slot).append(":").append(itemStack.getItemId()).append(":").append(itemStack.getQuantity());
                }
            });

            return sb.toString();
        } catch (Exception e) {
            System.err.println("[Chronoshift] Failed to serialize inventory: " + e.getMessage());
            return "";
        }
    }
}

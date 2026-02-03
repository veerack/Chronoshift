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


package com.chronoshift.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Teleportation helper with fallbacks for different server builds.
 */
public final class TeleportUtil {

    private TeleportUtil() {}

    public static void teleport(Store<EntityStore> store, Ref<EntityStore> ent, World world, Vector3d pos, Vector3f rot) {
        if (store == null || ent == null || pos == null) return;
        if (rot == null) rot = new Vector3f(0, 0, 0);

        Teleport tp = buildTeleport(world, pos, rot);

        // Prefer 2-arg (same as JoinLeaveMessages teleport)
        try {
            store.addComponent(ent, Teleport.getComponentType(), tp);
            return;
        } catch (Throwable ignored) {}

        // Fallback 3-arg (some builds require world)
        if (world != null) {
            try {
                store.addComponent(ent, Teleport.getComponentType(), tp);
            } catch (Throwable ignored) {}
        }
    }

    public static void teleport(Store<EntityStore> store, Ref<EntityStore> ent, Vector3d pos, Vector3f rot) {
        if (store == null || ent == null || pos == null) return;
        if (rot == null) rot = new Vector3f(0, 0, 0);

        try {
            store.addComponent(ent, Teleport.getComponentType(), buildTeleport(null, pos, rot));
        } catch (Throwable ignored) {}
    }

    public static void teleport(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> ent, Vector3d pos, Vector3f rot) {
        if (commandBuffer == null || ent == null || pos == null) return;
        if (rot == null) rot = new Vector3f(0, 0, 0);

        try {
            commandBuffer.addComponent(ent, Teleport.getComponentType(), buildTeleport(null, pos, rot));
        } catch (Throwable ignored) {}
    }

    private static Teleport buildTeleport(World world, Vector3d pos, Vector3f rot) {
        try {
            return (world != null)
                    ? Teleport.createForPlayer(world, pos, rot)
                    : Teleport.createForPlayer(pos, rot);
        } catch (Throwable ignored) {
            // Fallback: emulate createForPlayer (yaw-only body, full head rotation)
            Vector3f bodyRot = new Vector3f(0f, rot.getY(), 0f);
            Teleport tp = (world != null) ? new Teleport(world, pos, bodyRot) : new Teleport(pos, bodyRot);
            try { tp.setHeadRotation(rot.clone()); } catch (Throwable ignored2) {}
            return tp;
        }
    }
}

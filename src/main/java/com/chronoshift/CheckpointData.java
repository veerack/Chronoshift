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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class CheckpointData {

    @SerializedName("uuid")
    private final String playerUuid;

    @SerializedName("hasCharm")
    private boolean hasCharm;

    @SerializedName("checkpoint")
    private Checkpoint checkpoint;

    public CheckpointData(String playerUuid) {
        this.playerUuid = playerUuid;
        this.hasCharm = false;
        this.checkpoint = null;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public boolean hasCharm() {
        return hasCharm;
    }

    public void setHasCharm(boolean hasCharm) {
        this.hasCharm = hasCharm;
    }

    public Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static CheckpointData fromJson(String json) {
        return new Gson().fromJson(json, CheckpointData.class);
    }

    public static class Checkpoint {
        @SerializedName("world")
        private final String world;

        @SerializedName("x")
        private final double x;

        @SerializedName("y")
        private final double y;

        @SerializedName("z")
        private final double z;

        @SerializedName("yaw")
        private final float yaw;

        @SerializedName("pitch")
        private final float pitch;

        @SerializedName("health")
        private final float health;

        @SerializedName("inventory")
        private final String inventoryData;

        @SerializedName("worldTime")
        private final long worldTime;

        public Checkpoint(String world, double x, double y, double z, float yaw, float pitch,
                         float health, String inventoryData, long worldTime) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.health = health;
            this.inventoryData = inventoryData;
            this.worldTime = worldTime;
        }

        public String getWorld() {
            return world;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public float getHealth() {
            return health;
        }

        public String getInventoryData() {
            return inventoryData;
        }

        public long getWorldTime() {
            return worldTime;
        }
    }
}

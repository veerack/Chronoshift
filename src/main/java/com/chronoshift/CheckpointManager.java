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

import com.chronoshift.utils.Log;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class CheckpointManager {

    private final File dataFolder;
    private final File checkpointsFile;
    private final Map<String, CheckpointData> playerCheckpoints;

    public CheckpointManager(File dataFolder) {
        this.dataFolder = dataFolder;
        this.checkpointsFile = new File(dataFolder, "checkpoints.json");
        this.playerCheckpoints = new HashMap<>();
    }

    public void loadCheckpoints() {
        if (!checkpointsFile.exists()) {
            Log.info("No existing checkpoints file found. Starting fresh.");
            return;
        }

        try {
            String content = Files.readString(checkpointsFile.toPath());
            if (content.trim().isEmpty()) {
                Log.info("Checkpoints file is empty. Starting fresh.");
                return;
            }

            String[] entries = content.split("\\}\\s*\\{");
            for (String entry : entries) {
                entry = entry.replaceAll("[\\[\\]]", "").trim();
                if (entry.isEmpty()) continue;

                try {
                    if (!entry.startsWith("{")) entry = "{" + entry;
                    if (!entry.endsWith("}")) entry = entry + "}";

                    CheckpointData data = CheckpointData.fromJson(entry);
                    playerCheckpoints.put(data.getPlayerUuid(), data);
                } catch (Exception e) {
                    Log.warn("Failed to parse checkpoint entry: " + entry);
                }
            }

            Log.info("Loaded " + playerCheckpoints.size() + " player checkpoints.");
        } catch (IOException e) {
            Log.error("Failed to load checkpoints: " + e.getMessage());
        }
    }

    public void saveCheckpoints() {
        try {
            StringBuilder json = new StringBuilder("[\n");

            int index = 0;
            for (CheckpointData data : playerCheckpoints.values()) {
                if (index > 0) {
                    json.append(",\n");
                }
                json.append("  ").append(data.toJson());
                index++;
            }

            json.append("\n]");

            Files.writeString(checkpointsFile.toPath(), json.toString());
            Log.info("Saved " + playerCheckpoints.size() + " player checkpoints.");
        } catch (IOException e) {
            Log.error("Failed to save checkpoints: " + e.getMessage());
        }
    }

    public CheckpointData getCheckpointData(String playerUuid) {
        return playerCheckpoints.computeIfAbsent(playerUuid, CheckpointData::new);
    }

    public void hasCheckpointData(String playerUuid) {
        playerCheckpoints.computeIfAbsent(playerUuid, CheckpointData::new);
    }

    public void removeCharm(String playerUuid) {
        CheckpointData data = playerCheckpoints.get(playerUuid);
        if (data != null) {
            data.setHasCharm(false);
            data.setCheckpoint(null);
        }
    }
}

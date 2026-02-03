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
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import com.chronoshift.utils.Log;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class ChronoshiftPlugin extends JavaPlugin {

    private CheckpointManager checkpointManager;

    public ChronoshiftPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getCodecRegistry(Interaction.CODEC).register("chronoshift.ChronoshiftInteraction",
                ChronoshiftInteraction.class, ChronoshiftInteraction.CODEC);
    }

    @Override
    protected void start() {
        Log.setDefault(getLogger());

        File dataFolder = new File("plugins/Chronoshift");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        checkpointManager = new CheckpointManager(dataFolder);
        checkpointManager.loadCheckpoints();

        ChronoshiftEffects effects = new ChronoshiftEffects();

        try {
            ChronoshiftActivationSystem activationSystem = new ChronoshiftActivationSystem(checkpointManager, effects);
            getEntityStoreRegistry().registerSystem(activationSystem);
            Log.info("Chronoshift activation system registered!");
        } catch (Throwable t) {
            Log.error("Could not register activation system: " + t.getMessage());
        }

        try {
            ChronoshiftDropProtectionSystem dropProtection = new ChronoshiftDropProtectionSystem(checkpointManager);
            getEntityStoreRegistry().registerSystem(dropProtection);
            Log.info("Chronoshift drop protection system registered!");
        } catch (Throwable t) {
            Log.error("Could not register drop protection system: " + t.getMessage());
        }

        try {
            ChronoshiftInventoryLockSystem inventoryLock = new ChronoshiftInventoryLockSystem(checkpointManager);
            getEntityStoreRegistry().registerSystem(inventoryLock);
            Log.info("Chronoshift inventory lock system registered!");
        } catch (Throwable t) {
            Log.error("Could not register inventory lock system: " + t.getMessage());
        }

        Object registry = getEventRegistry();
        EventSniffer sniffer = new EventSniffer();
        sniffer.registerAll(registry);

        try {
            ChronoshiftCommand tcCommand = new ChronoshiftCommand(checkpointManager);
            getCommandRegistry().registerCommand(tcCommand);
            Log.info("Chronoshift /tc command registered!");
        } catch (Throwable t) {
            Log.error("Could not register /tc command: " + t.getMessage());
        }

        PlayerDeathListener deathListener = new PlayerDeathListener(checkpointManager);
        deathListener.register(registry);

        PlayerInventoryListener inventoryListener = new PlayerInventoryListener(checkpointManager);
        inventoryListener.register(registry);

        registerPlayerReady(registry);

        Log.info("Chronoshift plugin loaded!");
    }

    private void registerPlayerReady(Object registry) {
        try {
            Class<?> eventClass = Class.forName("com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent");
            Method m = registry.getClass().getMethod("registerGlobal", Class.class, Consumer.class);
            m.invoke(registry, eventClass, (Consumer<Object>) this::onPlayerReady);
        } catch (Throwable t) {
            System.err.println("[Chronoshift] Could not register PlayerReadyEvent: " + t.getMessage());
        }
    }

    private void onPlayerReady(Object event) {
        try {
            PlayerRef pr = null;
            try { pr = (PlayerRef) invokeMethod(event, "getPlayerRef"); } catch (Throwable ignored) {}

            if (pr != null && pr.isValid()) {
                EventTitleUtil.showEventTitleToPlayer(
                        pr,
                        Message.raw("CHRONOSHIFT MOD").color(Color.MAGENTA),
                        Message.raw("Try: /item give Chronoshift").color(Color.WHITE),
                        true
                );
            }
        } catch (Throwable t) {
            System.err.println("[Chronoshift] Error in onPlayerReady: " + t.getMessage());
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

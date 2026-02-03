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

public class Log {
    private static Object logger;

    public static void setDefault(Object log) {
        logger = log;
    }

    public static void info(String message) {
        if (logger != null) {
            try {
                logger.getClass().getMethod("info", String.class).invoke(logger, message);
            } catch (Exception e) {
                System.out.println("[INFO] " + message);
            }
        } else {
            System.out.println("[INFO] " + message);
        }
    }

    public static void error(String message) {
        if (logger != null) {
            try {
                logger.getClass().getMethod("error", String.class).invoke(logger, message);
            } catch (Exception e) {
                System.err.println("[ERROR] " + message);
            }
        } else {
            System.err.println("[ERROR] " + message);
        }
    }

    public static void warning(String message) {
        if (logger != null) {
            try {
                logger.getClass().getMethod("warn", String.class).invoke(logger, message);
            } catch (Exception e) {
                System.out.println("[WARN] " + message);
            }
        } else {
            System.out.println("[WARN] " + message);
        }
    }

    public static void warn(String message) {
        warning(message);
    }

    public static void debug(String message) {
        info("[DEBUG] " + message);
    }
}

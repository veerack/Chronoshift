package com.chronoshift.utils;

import com.hypixel.hytale.server.core.Message;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses & color/style codes into Message objects.
 * Supports:
 * - Rainbow: &r{...} or &rWord
 * - Gradients: &g#hex1#hex2{text} or &gcolor1#color2{text}
 * - Preset gradients: &gred{...}, &gblue{...}, &ggold{...}, etc.
 * - Color codes do NOT reset styles; use &r (standalone) to reset.
 */
public final class ChatColors {

    private ChatColors() {}

    public static Message parse(String text) {
        return parse(text, Color.WHITE);
    }

    public static Message parse(String text, Color baseColor) {
        if (text == null) {
            return Message.raw("").color(baseColor != null ? baseColor : Color.WHITE);
        }

        List<Message> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        Color color = (baseColor != null) ? baseColor : Color.WHITE;
        boolean bold = false;
        boolean italic = false;
        boolean monospace = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));

                // Gradient: &g#hex1#hex2{text} or &gcolor1#color2{text} or &gpreset{text}
                if (code == 'g') {
                    int braceIdx = text.indexOf('{', i + 2);
                    if (braceIdx != -1) {
                        int endIdx = text.indexOf('}', braceIdx + 1);
                        if (endIdx != -1) {
                            flush(parts, current, color, bold, italic, monospace);

                            String gradientSpec = text.substring(i + 2, braceIdx);
                            String gradientText = text.substring(braceIdx + 1, endIdx);

                            if (!gradientText.isEmpty()) {
                                applyGradient(parts, gradientSpec, gradientText, bold, italic, monospace);
                            }

                            i = endIdx;
                            continue;
                        }
                    }
                }

                // Rainbow: &r{...} or &rWord
                if (code == 'r') {
                    if (i + 2 < text.length() && text.charAt(i + 2) == '{') {
                        int end = text.indexOf('}', i + 3);
                        if (end != -1) {
                            flush(parts, current, color, bold, italic, monospace);
                            String inner = text.substring(i + 3, end);
                            appendRainbow(parts, inner, bold, italic, monospace);
                            i = end;
                            continue;
                        }
                    }

                    if (i + 2 >= text.length()) {
                        flush(parts, current, color, bold, italic, monospace);
                        color = Color.WHITE;
                        bold = false;
                        italic = false;
                        monospace = false;
                        i++;
                        continue;
                    }

                    char next = text.charAt(i + 2);
                    if (Character.isWhitespace(next) || next == '&') {
                        flush(parts, current, color, bold, italic, monospace);
                        color = Color.WHITE;
                        bold = false;
                        italic = false;
                        monospace = false;
                        i++;
                        continue;
                    }

                    // rainbow for the next token (until whitespace or next &)
                    int end = i + 2;
                    while (end < text.length()) {
                        char ch = text.charAt(end);
                        if (Character.isWhitespace(ch) || ch == '&') break;
                        end++;
                    }
                    if (end > i + 2) {
                        flush(parts, current, color, bold, italic, monospace);
                        String inner = text.substring(i + 2, end);
                        appendRainbow(parts, inner, bold, italic, monospace);
                        i = end - 1;
                        continue;
                    }
                }
                if (code == '&') {
                    current.append('&');
                    i++;
                    continue;
                }
                if (isStyleCode(code)) {
                    flush(parts, current, color, bold, italic, monospace);
                    if (code == 'l') bold = true;
                    else if (code == 'o') italic = true;
                    else if (code == 'm') monospace = true;
                    else if (code == 'r') {
                        color = Color.WHITE;
                        bold = false;
                        italic = false;
                        monospace = false;
                    }
                    i++;
                    continue;
                }
                Color next = colorFromCode(code);
                if (next != null) {
                    flush(parts, current, color, bold, italic, monospace);
                    color = next;
                    i++;
                    continue;
                }
            }
            current.append(c);
        }

        flush(parts, current, color, bold, italic, monospace);

        if (parts.isEmpty()) {
            return Message.raw("").color(color);
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return Message.join(parts.toArray(new Message[0]));
    }

    private static void flush(List<Message> parts, StringBuilder current, Color color,
                              boolean bold, boolean italic, boolean monospace) {
        if (current.length() == 0) return;
        Message msg = Message.raw(current.toString()).color(color);
        if (bold) msg = msg.bold(true);
        if (italic) msg = msg.italic(true);
        if (monospace) msg = msg.monospace(true);
        parts.add(msg);
        current.setLength(0);
    }

    private static void appendRainbow(List<Message> parts, String text,
                                      boolean bold, boolean italic, boolean monospace) {
        if (text == null || text.isEmpty()) return;
        Color[] colors = rainbowColors();
        int idx = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Color c = colors[idx % colors.length];
            Message msg = Message.raw(String.valueOf(ch)).color(c);
            if (bold) msg = msg.bold(true);
            if (italic) msg = msg.italic(true);
            if (monospace) msg = msg.monospace(true);
            parts.add(msg);
            idx++;
        }
    }

    private static Color colorFromCode(char code) {
        return switch (code) {
            case '0' -> new Color(0x00, 0x00, 0x00);
            case '1' -> new Color(0x00, 0x00, 0xAA);
            case '2' -> new Color(0x00, 0xAA, 0x00);
            case '3' -> new Color(0x00, 0xAA, 0xAA);
            case '4' -> new Color(0xAA, 0x00, 0x00);
            case '5' -> new Color(0xAA, 0x00, 0xAA);
            case '6' -> new Color(0xFF, 0xAA, 0x00);
            case '7' -> new Color(0xAA, 0xAA, 0xAA);
            case '8' -> new Color(0x55, 0x55, 0x55);
            case '9' -> new Color(0x55, 0x55, 0xFF);
            case 'a' -> new Color(0x55, 0xFF, 0x55);
            case 'b' -> new Color(0x55, 0xFF, 0xFF);
            case 'c' -> new Color(0xFF, 0x55, 0x55);
            case 'd' -> new Color(0xFF, 0x55, 0xFF);
            case 'e' -> new Color(0xFF, 0xFF, 0x55);
            case 'f' -> new Color(0xFF, 0xFF, 0xFF);
            case 'r' -> Color.WHITE;
            default -> null;
        };
    }

    private static boolean isStyleCode(char code) {
        return code == 'l' || code == 'o' || code == 'm' || code == 'r';
    }

    private static Color[] rainbowColors() {
        return new Color[] {
                new Color(0xFF, 0x55, 0x55),
                new Color(0xFF, 0xAA, 0x00),
                new Color(0xFF, 0xFF, 0x55),
                new Color(0x55, 0xFF, 0x55),
                new Color(0x55, 0xFF, 0xFF),
                new Color(0x55, 0x55, 0xFF),
                new Color(0xFF, 0x55, 0xFF)
        };
    }

    /**
     * Applies a gradient effect to text.
     * Gradient spec formats:
     * - #hex1#hex2 - Custom two-color gradient (e.g., #ff0000#0000ff)
     * - #hex1#hex2#hex3 - Multi-color gradient
     * - color1#color2 - Named colors separated by # (e.g., red#blue)
     * - preset - Named preset (red, blue, gold, green, purple, orange, cyan, magenta)
     */
    private static void applyGradient(List<Message> parts, String spec, String text,
                                     boolean bold, boolean italic, boolean monospace) {
        if (text == null || text.isEmpty()) return;

        Color[] colors = parseGradientSpec(spec);
        if (colors == null || colors.length == 0) {
            // Fallback to plain text
            Message msg = Message.raw(text).color(Color.WHITE);
            if (bold) msg = msg.bold(true);
            if (italic) msg = msg.italic(true);
            if (monospace) msg = msg.monospace(true);
            parts.add(msg);
            return;
        }

        if (colors.length == 1) {
            // Single color
            Message msg = Message.raw(text).color(colors[0]);
            if (bold) msg = msg.bold(true);
            if (italic) msg = msg.italic(true);
            if (monospace) msg = msg.monospace(true);
            parts.add(msg);
            return;
        }

        // Interpolate gradient for each character
        int len = text.length();
        for (int i = 0; i < len; i++) {
            float position = (len > 1) ? (float) i / (len - 1) : 0.5f;
            Color c = interpolateGradient(colors, position);
            Message msg = Message.raw(String.valueOf(text.charAt(i))).color(c);
            if (bold) msg = msg.bold(true);
            if (italic) msg = msg.italic(true);
            if (monospace) msg = msg.monospace(true);
            parts.add(msg);
        }
    }

    /**
     * Parses gradient specification and returns array of colors.
     */
    private static Color[] parseGradientSpec(String spec) {
        if (spec == null || spec.isEmpty()) return null;

        spec = spec.trim().toLowerCase();

        // Check for preset gradients
        if (!spec.contains("#")) {
            Color[] preset = getPresetGradient(spec);
            if (preset != null) return preset;
        }

        // Parse colors separated by #
        String[] parts = spec.split("#");
        List<Color> colors = new ArrayList<>();

        for (String part : parts) {
            if (part.isEmpty()) continue;

            // Try hex color first
            if (part.startsWith("#")) part = part.substring(1);

            if (part.length() == 6) {
                try {
                    int rgb = Integer.parseInt(part, 16);
                    colors.add(new Color(rgb));
                    continue;
                } catch (NumberFormatException ignored) {}
            }

            // Try named color
            Color named = colorFromName(part);
            if (named != null) {
                colors.add(named);
            }
        }

        return colors.isEmpty() ? null : colors.toArray(new Color[0]);
    }

    /**
     * Gets preset gradient colors by name.
     */
    private static Color[] getPresetGradient(String name) {
        return switch (name) {
            case "red" -> new Color[] {new Color(0xFF, 0x00, 0x00), new Color(0xFF, 0x80, 0x80)};
            case "blue" -> new Color[] {new Color(0x00, 0x80, 0xFF), new Color(0x00, 0xFF, 0xFF)};
            case "green" -> new Color[] {new Color(0x00, 0xFF, 0x00), new Color(0x80, 0xFF, 0x80)};
            case "gold" -> new Color[] {new Color(0xFF, 0xD7, 0x00), new Color(0xFF, 0xFF, 0x00)};
            case "purple" -> new Color[] {new Color(0x80, 0x00, 0xFF), new Color(0xFF, 0x00, 0xFF)};
            case "orange" -> new Color[] {new Color(0xFF, 0x80, 0x00), new Color(0xFF, 0xFF, 0x80)};
            case "cyan" -> new Color[] {new Color(0x00, 0xFF, 0xFF), new Color(0x80, 0xFF, 0xFF)};
            case "magenta" -> new Color[] {new Color(0xFF, 0x00, 0xFF), new Color(0xFF, 0x80, 0xFF)};
            case "sunset" -> new Color[] {new Color(0xFF, 0x00, 0x00), new Color(0xFF, 0xFF, 0x00)};
            case "ocean" -> new Color[] {new Color(0x00, 0x00, 0xFF), new Color(0x00, 0xFF, 0xFF)};
            case "forest" -> new Color[] {new Color(0x00, 0x80, 0x00), new Color(0x00, 0xFF, 0x00)};
            case "fire" -> new Color[] {new Color(0xFF, 0x00, 0x00), new Color(0xFF, 0xFF, 0x00), new Color(0xFF, 0x80, 0x00)};
            case "ice" -> new Color[] {new Color(0x00, 0x80, 0xFF), new Color(0x00, 0xFF, 0xFF), new Color(0xFF, 0xFF, 0xFF)};
            default -> null;
        };
    }

    /**
     * Gets a color by name (for gradient parsing).
     */
    private static Color colorFromName(String name) {
        if (name == null || name.isEmpty()) return null;

        return switch (name) {
            case "0", "black" -> new Color(0x00, 0x00, 0x00);
            case "1", "darkblue" -> new Color(0x00, 0x00, 0xAA);
            case "2", "darkgreen" -> new Color(0x00, 0xAA, 0x00);
            case "3", "darkaqua" -> new Color(0x00, 0xAA, 0xAA);
            case "4", "darkred" -> new Color(0xAA, 0x00, 0x00);
            case "5", "darkpurple" -> new Color(0xAA, 0x00, 0xAA);
            case "6", "gold" -> new Color(0xFF, 0xAA, 0x00);
            case "7", "gray" -> new Color(0xAA, 0xAA, 0xAA);
            case "8", "darkgray" -> new Color(0x55, 0x55, 0x55);
            case "9", "blue" -> new Color(0x55, 0x55, 0xFF);
            case "a", "green" -> new Color(0x55, 0xFF, 0x55);
            case "b", "aqua" -> new Color(0x55, 0xFF, 0xFF);
            case "c", "red" -> new Color(0xFF, 0x55, 0x55);
            case "d", "lightpurple" -> new Color(0xFF, 0x55, 0xFF);
            case "e", "yellow" -> new Color(0xFF, 0xFF, 0x55);
            case "f", "white" -> new Color(0xFF, 0xFF, 0xFF);
            default -> null;
        };
    }

    /**
     * Interpolates between multiple gradient colors at a given position (0.0 to 1.0).
     */
    private static Color interpolateGradient(Color[] colors, float position) {
        if (colors == null || colors.length == 0) return Color.WHITE;
        if (colors.length == 1) return colors[0];

        // Clamp position
        position = Math.max(0.0f, Math.min(1.0f, position));

        // Find which segment we're in
        float segmentSize = 1.0f / (colors.length - 1);
        int segment = (int) (position / segmentSize);

        // Clamp to last segment
        if (segment >= colors.length - 1) {
            segment = colors.length - 2;
        }

        // Calculate local position within segment
        float localPos = (position - (segment * segmentSize)) / segmentSize;

        Color c1 = colors[segment];
        Color c2 = colors[segment + 1];

        // Linear interpolation
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * localPos);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * localPos);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * localPos);

        return new Color(r, g, b);
    }
}

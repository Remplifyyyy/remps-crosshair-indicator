package com.customcrosshair;

import static com.customcrosshair.CrosshairConfig.CENTER;
import static com.customcrosshair.CrosshairConfig.GRID;

/** Ready-made shapes you can load into the editor and then tweak. */
public enum Presets {
    PLUS("Plus (vanilla)"),
    GAP_PLUS("Gap plus"),
    GAP_PLUS_DOT("Gap plus + dot"),
    DOT("Dot"),
    BIG_DOT("Big dot"),
    CROSS("X"),
    CIRCLE("Circle"),
    CIRCLE_DOT("Circle + dot"),
    T_SHAPE("T shape"),
    SQUARE("Square"),
    CHEVRON("Chevron"),
    EMPTY("Empty");

    public final String label;
    Presets(String label) { this.label = label; }

    public Presets next() { return values()[(ordinal() + 1) % values().length]; }

    public boolean[] make() {
        boolean[] g = new boolean[GRID * GRID];
        int c = CENTER;
        switch (this) {
            case PLUS -> { for (int i = 3; i <= 11; i++) { on(g, c, i); on(g, i, c); } }
            case GAP_PLUS, GAP_PLUS_DOT -> {
                for (int i = 2; i <= 5; i++) { on(g, c, i); on(g, i, c); on(g, c, GRID - 1 - i); on(g, GRID - 1 - i, c); }
                if (this == GAP_PLUS_DOT) on(g, c, c);
            }
            case DOT -> on(g, c, c);
            case BIG_DOT -> { for (int x = c - 1; x <= c + 1; x++) for (int y = c - 1; y <= c + 1; y++) on(g, x, y); }
            case CROSS -> { for (int i = 3; i <= 11; i++) { on(g, i, i); on(g, i, GRID - 1 - i); } }
            case CIRCLE, CIRCLE_DOT -> {
                for (int x = 0; x < GRID; x++) for (int y = 0; y < GRID; y++) {
                    double d = Math.sqrt((x - c) * (x - c) + (y - c) * (y - c));
                    if (d >= 4.5 && d < 5.5) on(g, x, y);
                }
                if (this == CIRCLE_DOT) on(g, c, c);
            }
            case T_SHAPE -> { for (int i = 3; i <= 11; i++) on(g, i, c); for (int i = c; i <= 11; i++) on(g, c, i); }
            case SQUARE -> { for (int i = 4; i <= 10; i++) { on(g, i, 4); on(g, i, 10); on(g, 4, i); on(g, 10, i); } }
            case CHEVRON -> { for (int i = 0; i <= 4; i++) { on(g, c - i, c + i); on(g, c + i, c + i); } }
            case EMPTY -> { }
        }
        return g;
    }

    private static void on(boolean[] g, int x, int y) {
        if (x >= 0 && y >= 0 && x < GRID && y < GRID) g[y * GRID + x] = true;
    }
}

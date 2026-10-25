package com.customcrosshair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Everything the mod remembers, saved to config/customcrosshair.json. */
public class CrosshairConfig {
    public static final int GRID = 15;          // crosshair canvas is 15x15 pixels (same as vanilla)
    public static final int CENTER = GRID / 2;  // index 7
    public static final float MIN_SCALE = 0.01f, MAX_SCALE = 5.0f;   // 1% - 500%

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("customcrosshair.json");

    public enum TargetMode {
        ALL("All entities"), LIVING("Living mobs"), HOSTILE("Hostile mobs"), PLAYERS("Players only");
        public final String label;
        TargetMode(String label) { this.label = label; }
        public TargetMode next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public enum HitboxTargets {
        PLAYERS("Players only"), LIVING("Players + mobs"), ALL("All entities");
        public final String label;
        HitboxTargets(String label) { this.label = label; }
        public HitboxTargets next() { return values()[(ordinal() + 1) % values().length]; }
    }

    /** A drawing you saved yourself from the editor. */
    public static class SavedPreset {
        public String name = "Preset";
        public boolean[] pixels = new boolean[GRID * GRID];
        public SavedPreset() { }
        public SavedPreset(String name, boolean[] pixels) { this.name = name; this.pixels = pixels.clone(); }
    }

    /** One crosshair look: a drawing, colour, size and outline. */
    public static class Profile {
        public boolean[] pixels = new boolean[GRID * GRID];
        public int color = 0xFFFFFFFF;          // ARGB
        public float scale = 1.0f;              // size multiplier: 1.0 = 100% = vanilla size (0.01 - 5.0)
        public boolean outline = false;
        public int outlineColor = 0xFF000000;
        public boolean vanillaStyle = false;    // vanilla see-through/inverted look instead of solid colour

        public boolean get(int x, int y) { return pixels[y * GRID + x]; }
        public void set(int x, int y, boolean v) {
            if (x >= 0 && y >= 0 && x < GRID && y < GRID) pixels[y * GRID + x] = v;
        }
        public Profile copy() {
            Profile p = new Profile();
            p.pixels = pixels.clone();
            p.color = color; p.scale = scale; p.outline = outline; p.outlineColor = outlineColor; p.vanillaStyle = vanillaStyle;
            return p;
        }
        void fix() {
            if (pixels == null || pixels.length != GRID * GRID) pixels = Presets.PLUS.make();
            if (Float.isNaN(scale)) scale = 1.0f;
            scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
        }
    }

    public java.util.List<SavedPreset> savedPresets = new java.util.ArrayList<>();

    public Profile normal = defaultNormal();
    public Profile target = defaultTarget();
    public boolean targetEnabled = true;
    public boolean targetUsesNormalShape = true;   // "default crosshair but red"
    public TargetMode targetMode = TargetMode.ALL;
    public boolean hideInThirdPerson = true;

    // hitboxes tab
    public HitboxTargets hitboxTargets = HitboxTargets.PLAYERS;
    public int hitboxColor = 0xFFFF3030;        // the entity you're looking at (and can hit)
    public int hitboxOutColor = 0xFFFFFFFF;     // every other entity
    public boolean hitboxShowOutOfRange = true; // show boxes on entities you're not looking at
    public boolean vanillaHitboxDirection = true; // F3+B: show the blue direction line and red eye-height line
    public boolean showToggleFeedback = true;   // "Hitboxes: ON/OFF" message when pressing the toggle key
    public boolean hitboxThroughWalls = false;
    public float hitboxWidth = 2.0f;

    private static Profile defaultNormal() {
        Profile p = new Profile();
        p.pixels = Presets.PLUS.make();
        p.vanillaStyle = true;
        return p;
    }

    private static Profile defaultTarget() {
        Profile p = new Profile();
        p.pixels = Presets.PLUS.make();
        p.color = 0xFFFF3030;
        return p;
    }

    // ---------------------------------------------------------------- saving / loading

    private static CrosshairConfig instance;

    public static CrosshairConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        CrosshairConfig cfg = null;
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                cfg = GSON.fromJson(r, CrosshairConfig.class);
            } catch (Exception e) {
                CustomCrosshairClient.LOGGER.warn("Could not read crosshair config, using defaults", e);
            }
        }
        if (cfg == null) cfg = new CrosshairConfig();
        if (cfg.normal == null) cfg.normal = defaultNormal();
        if (cfg.target == null) cfg.target = defaultTarget();
        if (cfg.targetMode == null) cfg.targetMode = TargetMode.ALL;
        if (cfg.hitboxTargets == null) cfg.hitboxTargets = HitboxTargets.PLAYERS;
        if (!(cfg.hitboxWidth >= 1f)) cfg.hitboxWidth = 2.0f;
        if (cfg.savedPresets == null) cfg.savedPresets = new java.util.ArrayList<>();
        cfg.savedPresets.removeIf(p -> p == null || p.pixels == null || p.pixels.length != GRID * GRID);
        for (SavedPreset p : cfg.savedPresets) if (p.name == null) p.name = "Preset";
        cfg.normal.fix();
        cfg.target.fix();
        instance = cfg;
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(get(), w);
            }
        } catch (Exception e) {
            CustomCrosshairClient.LOGGER.warn("Could not save crosshair config", e);
        }
    }
}

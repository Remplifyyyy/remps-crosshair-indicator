# Remplify Crosshair Indicator (Fabric, Minecraft Java 1.21.11)

Client-side mod. Needs Fabric Loader and Fabric API for 1.21.11.

## Features
- **Editor menu** opened with a keybind (default **K**, rebind in *Options → Controls → Key Binds → Remplify Crosshair Indicator → "Open Crosshair Editor"*).
- **Draw your own crosshair** on a 15×15 pixel canvas: left-click draws, right-click erases, with optional symmetry (mirror L/R, U/D, 4-way, 8-way).
- **12 presets** (vanilla plus, gap plus, dot, X, circle, T, square, chevron…), plus Clear and Invert.
- **Saved crosshairs**: press "Saved" to name and save your own drawings, then load or delete them any time.
- **Colour**: RGB + opacity sliders and 16 quick swatches.
- **Size**: 1%–500% of the vanilla crosshair, with a slider and a box to type an exact percentage.
- **Outline** toggle (black border so it shows up on any background).
- **Entity indicator**: a second crosshair look used while you're aiming at an entity.
  - Targets: all entities / living mobs / hostile mobs / players only.
  - Shape: "Same as normal" (e.g. your normal crosshair, just red — the default) or "Own drawing" to draw a totally different one.
  - Its own colour, size and outline.
- **Hitboxes tab**: restyles Minecraft's own F3+B hitboxes. The box of the entity you're looking at (and can hit) changes colour, like the crosshair indicator. On/off is the same switch as F3+B: use F3+B, the mod's "Toggle Hitboxes" key (default **H**) or the ON/OFF button — they all control the same thing. Choose players only / players + mobs / all entities. Options for line width, see-through-walls, hiding the blue direction line and red eye line on vanilla F3+B hitboxes, and turning the ON/OFF message off ("Show feedback").
- Live preview, settings saved to `config/customcrosshair.json`.

## Building the .jar
**Option A – GitHub (no setup on your PC):** upload this folder to a new GitHub repo. The included workflow builds it automatically; open the *Actions* tab → latest run → download **remplify-crosshair-indicator-jar**. Use the jar *without* `-sources` in its name.

**Option B – on your PC:** install JDK 21 and Gradle 9.2+, then in this folder run `gradle build`. The jar appears in `build/libs/`.

Or open the folder in IntelliJ IDEA (with the Minecraft Development plugin) and run the `build` Gradle task.

## Installing
Put the jar and Fabric API into `.minecraft/mods`, launch the Fabric 1.21.11 profile.

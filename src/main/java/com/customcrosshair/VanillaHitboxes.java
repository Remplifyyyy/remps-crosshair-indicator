package com.customcrosshair;

import net.minecraft.client.MinecraftClient;
import com.customcrosshair.mixin.MinecraftClientAccessor;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

/**
 * Ties the mod's hitboxes to Minecraft's own F3+B hitboxes.
 * On/off is the same switch F3+B uses, and the mod restyles those boxes
 * (colour for the entity you're looking at, line width, extra lines, filters).
 */
public final class VanillaHitboxes {
    private VanillaHitboxes() { }

    private static Identifier entryId;

    /** The id of the "entity hitboxes" debug option that F3+B toggles. */
    private static Identifier id() {
        if (entryId == null) {
            for (Identifier key : DebugHudEntries.getEntries().keySet()) {
                if (key.getPath().contains("hitbox")) { entryId = key; break; }
            }
            if (entryId == null) entryId = Identifier.ofVanilla("entity_hitboxes");
        }
        return entryId;
    }

    private static DebugHudProfile entries() {
        return ((MinecraftClientAccessor) MinecraftClient.getInstance()).customcrosshair$getDebugEntries();
    }

    public static boolean isOn() {
        DebugHudProfile list = entries();
        return list != null && list.isEntryVisible(id());
    }

    /** Same as pressing F3+B. */
    public static void toggle() {
        DebugHudProfile list = entries();
        if (list != null) list.toggleVisibility(id());
    }

    /**
     * Called instead of Minecraft's hitbox drawing for each entity.
     * Runs inside Minecraft's own drawing pass, so the boxes appear exactly where F3+B's would.
     */
    public static void draw(Entity e, float tick) {
        CrosshairConfig cfg = CrosshairConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = mc.player;
        if (me == null) return;

        switch (cfg.hitboxTargets) {
            case PLAYERS -> { if (!(e instanceof PlayerEntity)) return; }
            case LIVING -> { if (!(e instanceof LivingEntity)) return; }
            default -> { }
        }

        // Minecraft only reports an entity here when it's under your crosshair AND within hit range.
        boolean targeted = mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() == e;
        if (!targeted && !cfg.hitboxShowOutOfRange) return;

        Vec3d pos = e.getLerpedPos(tick);
        Box box = e.getBoundingBox().offset(pos.subtract(e.getEntityPos()));

        int color = targeted ? cfg.hitboxColor : cfg.hitboxOutColor;
        show(GizmoDrawing.box(box, DrawStyle.stroked(color, cfg.hitboxWidth)), cfg);

        if (cfg.vanillaHitboxDirection) {
            double eyeY = pos.y + e.getStandingEyeHeight();
            // red eye-height line
            show(GizmoDrawing.box(new Box(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ),
                    DrawStyle.stroked(0xFFFF0000)), cfg);
            // blue looking-direction line
            Vec3d eye = new Vec3d(pos.x, eyeY, pos.z);
            show(GizmoDrawing.arrow(eye, eye.add(e.getRotationVec(tick).multiply(2.0)), 0xFF0000FF), cfg);
        }
    }

    private static void show(VisibilityConfigurable gizmo, CrosshairConfig cfg) {
        if (cfg.hitboxThroughWalls) gizmo.ignoreOcclusion();
    }
}

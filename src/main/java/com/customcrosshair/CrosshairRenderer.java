package com.customcrosshair;

import com.customcrosshair.CrosshairConfig.Profile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static com.customcrosshair.CrosshairConfig.GRID;

public final class CrosshairRenderer {
    private CrosshairRenderer() { }

    /** Plain white sprite; drawn with the vanilla crosshair pipeline it gives the vanilla inverted look. */
    private static final Identifier WHITE = Identifier.of("customcrosshair", "hud/white");

    /** Draws the in-game crosshair in the middle of the screen. */
    public static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        CrosshairConfig cfg = CrosshairConfig.get();
        if (cfg.hideInThirdPerson && !mc.options.getPerspective().isFirstPerson()) return;

        boolean targeting = cfg.targetEnabled && isLookingAtTarget(mc, cfg);
        Profile look = targeting ? cfg.target : cfg.normal;
        boolean[] shape = (targeting && cfg.targetUsesNormalShape) ? cfg.normal.pixels : look.pixels;

        draw(ctx, shape, look, ctx.getScaledWindowWidth() / 2f, ctx.getScaledWindowHeight() / 2f, look.scale);
    }

    public static boolean isLookingAtTarget(MinecraftClient mc, CrosshairConfig cfg) {
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY || !(hit instanceof EntityHitResult ehr)) return false;
        Entity e = ehr.getEntity();
        if (e == null) return false;
        return switch (cfg.targetMode) {
            case ALL -> true;
            case LIVING -> e instanceof LivingEntity;
            case HOSTILE -> e instanceof Monster;
            case PLAYERS -> e instanceof PlayerEntity;
        };
    }

    /**
     * Draws a crosshair drawing centred on (cx, cy) in GUI coordinates.
     * Works in real screen pixels so decimal sizes (e.g. 1.3x) are possible.
     * At size 1.0 one drawn pixel = one GUI pixel, exactly like vanilla.
     */
    public static void draw(DrawContext ctx, boolean[] shape, Profile look, float cx, float cy, float size) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float gui = mc.getWindow().getFramebufferWidth() / (float) ctx.getScaledWindowWidth(); // GUI scale
        if (!(gui > 0)) gui = 1f;

        int cellPx = Math.max(1, Math.round(size * gui));        // real pixels per drawn pixel
        int total = cellPx * GRID;
        int left = Math.round(cx * gui) - total / 2;
        int top = Math.round(cy * gui) - total / 2;
        int border = Math.max(1, Math.round(gui / 2f));

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(1f / gui, 1f / gui);

        if (look.outline) {
            for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) {
                if (!shape[y * GRID + x]) continue;
                int px = left + x * cellPx, py = top + y * cellPx;
                ctx.fill(px - border, py - border, px + cellPx + border, py + cellPx + border, look.outlineColor);
            }
        }
        for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) {
            if (!shape[y * GRID + x]) continue;
            int px = left + x * cellPx, py = top + y * cellPx;
            if (look.vanillaStyle) {
                ctx.drawGuiTexture(RenderPipelines.CROSSHAIR, WHITE, px, py, cellPx, cellPx);
            } else {
                ctx.fill(px, py, px + cellPx, py + cellPx, look.color);
            }
        }

        ctx.getMatrices().popMatrix();
    }
}

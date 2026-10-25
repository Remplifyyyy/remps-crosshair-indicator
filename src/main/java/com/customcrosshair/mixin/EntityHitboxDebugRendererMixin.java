package com.customcrosshair.mixin;

import com.customcrosshair.VanillaHitboxes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces how Minecraft draws each F3+B hitbox with the mod's styled version
 * (colours, width, optional red/blue lines). F3+B still turns them on and off.
 */
@Mixin(targets = "net.minecraft.client.render.debug.EntityHitboxDebugRenderer")
public abstract class EntityHitboxDebugRendererMixin {

    @Inject(method = "drawHitbox(Lnet/minecraft/entity/Entity;FZ)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void customcrosshair$drawHitbox(Entity entity, float tickProgress, boolean inLocalServer, CallbackInfo ci) {
        ci.cancel();
        // In singleplayer Minecraft can also draw a second copy using the built-in server's entity.
        // Only draw the client's own copy so there are no doubled boxes.
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null && mc.world.getEntityById(entity.getId()) == entity) {
            VanillaHitboxes.draw(entity, tickProgress);
        }
    }
}

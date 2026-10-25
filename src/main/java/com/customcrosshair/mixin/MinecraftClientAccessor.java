package com.customcrosshair.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the debug-options list (where F3+B's on/off switch lives), whether or not it's public. */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("debugHudEntryList")
    DebugHudProfile customcrosshair$getDebugEntries();
}

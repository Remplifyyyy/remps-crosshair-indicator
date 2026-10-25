package com.customcrosshair;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomCrosshairClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("remplify-crosshair-indicator");

    private static KeyBinding openEditor;
    private static KeyBinding toggleHitboxes;

    @Override
    public void onInitializeClient() {
        CrosshairConfig.load();

        // Both keys appear in Options > Controls > Key Binds under "Remplify Crosshair Indicator".
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("customcrosshair", "main"));
        openEditor = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.customcrosshair.open", GLFW.GLFW_KEY_K, category));
        toggleHitboxes = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.customcrosshair.hitboxes", GLFW.GLFW_KEY_H, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditor.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new CrosshairEditorScreen(null));
            }
            while (toggleHitboxes.wasPressed()) {
                // exactly the same switch as F3+B
                VanillaHitboxes.toggle();
                boolean on = VanillaHitboxes.isOn();
                if (CrosshairConfig.get().showToggleFeedback) client.inGameHud.setOverlayMessage(Text.literal("Hitboxes: ")
                        .append(on
                                ? Text.literal("ON").formatted(Formatting.GREEN)
                                : Text.literal("OFF").formatted(Formatting.RED)), false);
            }
        });

        // Swap the vanilla crosshair for ours.
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR,
                original -> (context, tickCounter) -> CrosshairRenderer.renderHud(context));
    }
}

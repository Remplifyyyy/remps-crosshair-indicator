package com.customcrosshair;

import com.customcrosshair.CrosshairConfig.Profile;
import com.customcrosshair.CrosshairConfig.SavedPreset;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

import static com.customcrosshair.CrosshairConfig.GRID;

/** Save the current drawing under a name, and load or delete your saved drawings. */
public class SavedPresetsScreen extends Screen {
    private static final int ROW = 22;
    private static int page = 0;

    private final Screen parent;
    private final Profile drawing;   // the drawing being edited in the editor

    private TextFieldWidget nameField;
    private String typedName = "";
    private int pendingDelete = -1;

    private int left, listTop, perPage;
    private static final int LIST_W = 300;

    public SavedPresetsScreen(Screen parent, Profile drawing) {
        super(Text.literal("Saved crosshairs"));
        this.parent = parent;
        this.drawing = drawing;
    }

    private List<SavedPreset> presets() { return CrosshairConfig.get().savedPresets; }

    @Override
    protected void init() {
        left = (width - LIST_W) / 2;
        int y = 24;

        // ---- save current drawing
        nameField = new TextFieldWidget(textRenderer, left, y, LIST_W - 104, 20, Text.literal("Name"));
        nameField.setMaxLength(24);
        nameField.setPlaceholder(Text.literal("Name your crosshair..."));
        nameField.setText(typedName);
        nameField.setChangedListener(t -> typedName = t);
        addDrawableChild(nameField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Save current"), b -> saveCurrent())
                .dimensions(left + LIST_W - 100, y, 100, 20).build());

        // ---- list of saved drawings
        listTop = y + 30;
        perPage = Math.max(1, (height - listTop - 34) / ROW);
        int pages = Math.max(1, (presets().size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * perPage;
        for (int i = start; i < Math.min(presets().size(), start + perPage); i++) {
            final int index = i;
            int ry = listTop + (i - start) * ROW;
            addDrawableChild(ButtonWidget.builder(Text.literal("Load"), b -> load(index))
                    .dimensions(left + LIST_W - 104, ry, 50, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal(pendingDelete == index ? "Sure?" : "Delete"), b -> delete(index))
                    .dimensions(left + LIST_W - 50, ry, 50, 20).build());
        }

        // ---- page buttons + back
        int by = height - 26;
        ButtonWidget prev = ButtonWidget.builder(Text.literal("<"), b -> { page--; pendingDelete = -1; clearAndInit(); })
                .dimensions(left, by, 20, 20).build();
        ButtonWidget next = ButtonWidget.builder(Text.literal(">"), b -> { page++; pendingDelete = -1; clearAndInit(); })
                .dimensions(left + 84, by, 20, 20).build();
        prev.active = page > 0;
        next.active = page < pages - 1;
        addDrawableChild(prev);
        addDrawableChild(next);
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
                .dimensions(left + LIST_W - 100, by, 100, 20).build());
    }

    private void saveCurrent() {
        String name = typedName.trim();
        if (name.isEmpty()) name = "Crosshair " + (presets().size() + 1);
        // same name = overwrite that one
        for (SavedPreset p : presets()) {
            if (p.name.equalsIgnoreCase(name)) {
                p.pixels = drawing.pixels.clone();
                finishSave();
                return;
            }
        }
        presets().add(new SavedPreset(name, drawing.pixels));
        page = (presets().size() - 1) / perPage;   // jump to where it was added
        finishSave();
    }

    private void finishSave() {
        typedName = "";
        pendingDelete = -1;
        CrosshairConfig.save();
        clearAndInit();
    }

    private void load(int index) {
        if (index < 0 || index >= presets().size()) return;
        drawing.pixels = presets().get(index).pixels.clone();
        CrosshairConfig.save();
        close();
    }

    private void delete(int index) {
        if (pendingDelete != index) {          // first click asks "Sure?"
            pendingDelete = index;
            clearAndInit();
            return;
        }
        if (index >= 0 && index < presets().size()) presets().remove(index);
        pendingDelete = -1;
        CrosshairConfig.save();
        clearAndInit();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFFFF);

        List<SavedPreset> list = presets();
        if (list.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("No saved crosshairs yet - name one and press Save current."),
                    width / 2, listTop + 6, 0xFFAAAAAA);
        }
        int start = page * perPage;
        for (int i = start; i < Math.min(list.size(), start + perPage); i++) {
            SavedPreset p = list.get(i);
            int ry = listTop + (i - start) * ROW;
            // small preview of the drawing
            int tx = left, ty = ry + 2;
            ctx.fill(tx - 1, ty - 1, tx + GRID + 1, ty + GRID + 1, 0xFF000000);
            ctx.fill(tx, ty, tx + GRID, ty + GRID, 0xFF333333);
            for (int cy = 0; cy < GRID; cy++) for (int cx = 0; cx < GRID; cx++) {
                if (p.pixels[cy * GRID + cx]) ctx.fill(tx + cx, ty + cy, tx + cx + 1, ty + cy + 1, 0xFFFFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal(p.name), left + 22, ry + 6, 0xFFFFFFFF);
        }

        int pages = Math.max(1, (list.size() + perPage - 1) / perPage);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal((page + 1) + "/" + pages), left + 52, height - 20, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        CrosshairConfig.save();
        if (client != null) client.setScreen(parent);
    }
}

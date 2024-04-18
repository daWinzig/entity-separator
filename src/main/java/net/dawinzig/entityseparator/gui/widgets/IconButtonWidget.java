package net.dawinzig.entityseparator.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconButtonWidget extends ButtonWidget implements Drawable, Selectable {
    private final Identifier texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int offsetX;
    private final int offsetY;

    public IconButtonWidget(int width, int height, Identifier texture, int textureWidth, int textureHeight, PressAction onPress, Text narration) {
        super(0, 0, width, height, Text.empty(), onPress, (textSupplier) -> MutableText.of(narration.getContent()));
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.offsetX = (width - textureWidth) / 2;
        this.offsetY = (height - textureHeight) / 2;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.drawGuiTexture(this.texture, this.getX() + this.offsetX, this.getY() + this.offsetY,
                this.textureWidth, this.textureHeight);
    }
}

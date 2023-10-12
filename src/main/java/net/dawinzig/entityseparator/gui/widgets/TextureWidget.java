package net.dawinzig.entityseparator.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.util.Identifier;

public class TextureWidget extends EmptyWidget implements Drawable {
    private final Identifier texture;
    private final Identifier hoveredTexture;

    public TextureWidget(Identifier texture, Identifier hoveredTexture, int width, int height) {
        super(width, height);
        this.texture = texture;
        this.hoveredTexture = hoveredTexture;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= (double)this.getY() && mouseY <= (double)(this.getY()+this.getHeight()) &&
                mouseX >= (double)this.getX() && mouseX <= (double)(this.getX()+this.getWidth());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.isMouseOver(mouseX, mouseY))
            context.drawTexture(this.texture, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        else
            context.drawTexture(this.hoveredTexture, this.getX(), this.getY(), 0, 0,
                    this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }
}

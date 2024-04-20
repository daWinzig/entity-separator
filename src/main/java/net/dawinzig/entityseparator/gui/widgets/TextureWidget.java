package net.dawinzig.entityseparator.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.resources.ResourceLocation;

public class TextureWidget extends SpacerElement implements Renderable {
    private final ResourceLocation texture;
    private final ResourceLocation hoveredTexture;

    public TextureWidget(ResourceLocation texture, ResourceLocation hoveredTexture, int width, int height) {
        super(width, height);
        this.texture = texture;
        this.hoveredTexture = hoveredTexture;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= (double)this.getY() && mouseY <= (double)(this.getY()+this.getHeight()) &&
                mouseX >= (double)this.getX() && mouseX <= (double)(this.getX()+this.getWidth());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!this.isMouseOver(mouseX, mouseY))
            context.blitSprite(this.texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        else
            context.blitSprite(this.hoveredTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}

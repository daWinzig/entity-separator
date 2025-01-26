package net.dawinzig.entityseparator.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class IconButtonWidget extends Button implements Renderable, NarratableEntry {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int offsetX;
    private final int offsetY;

    public IconButtonWidget(int width, int height, ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress, Component narration) {
        super(0, 0, width, height, Component.empty(), onPress, (textSupplier) -> MutableComponent.create(narration.getContents()));
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.offsetX = (width - textureWidth) / 2;
        this.offsetY = (height - textureHeight) / 2;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.blitSprite(RenderType::guiTextured, this.texture, this.getX() + this.offsetX, this.getY() + this.offsetY,
                this.textureWidth, this.textureHeight);
    }
}

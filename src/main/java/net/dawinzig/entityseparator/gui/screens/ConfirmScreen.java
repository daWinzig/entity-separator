package net.dawinzig.entityseparator.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
    private final Screen parent;
    private final Consumer<Choice> SELECTION_CALLBACK;
    private final ButtonWidget doneButton;
    private final ButtonWidget acceptButton;
    private final ButtonWidget declineButton;

    public ConfirmScreen(Screen parent, Text title, Consumer<Choice> selectionCallback) {
        super(title);
        this.SELECTION_CALLBACK = selectionCallback;
        this.parent = parent;

        this.acceptButton = ButtonWidget.builder(ScreenTexts.YES, (button) -> {
            this.close();
                    this.SELECTION_CALLBACK.accept(Choice.YES);
                }
        ).width(210).build();
        this.declineButton = ButtonWidget.builder(ScreenTexts.NO, (button) -> {
            this.close();
                    this.SELECTION_CALLBACK.accept(Choice.NO);
                }
        ).width(210).build();
        this.doneButton = ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> this.close()).build();
    }

    @Override
    protected void init() {
        this.acceptButton.setX(this.width / 2 - this.acceptButton.getWidth() / 2);
        this.acceptButton.setY(50);
        this.addDrawableChild(this.acceptButton);

        this.declineButton.setX(this.width / 2 - this.declineButton.getWidth() / 2);
        this.declineButton.setY(78);
        this.addDrawableChild(this.declineButton);

        this.doneButton.setX(this.width / 2 - this.doneButton.getWidth() / 2);
        this.doneButton.setY(this.height - 29);
        this.addDrawableChild(this.doneButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 16777215);
    }

    @SuppressWarnings("unused")
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }
    @SuppressWarnings("unused")
    public void renderBackground(DrawContext context) {
        this.renderBackgroundTexture(context);
    }

        @Override
    public void close() {
        Objects.requireNonNull(client).setScreen(this.parent);
    }

    public enum Choice {
        YES, NO
    }
}
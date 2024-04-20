package net.dawinzig.entityseparator.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
    private final Screen parent;
    private final Consumer<Choice> SELECTION_CALLBACK;
    private final Button doneButton;
    private final Button acceptButton;
    private final Button declineButton;

    public ConfirmScreen(Screen parent, Component title, Consumer<Choice> selectionCallback) {
        super(title);
        this.SELECTION_CALLBACK = selectionCallback;
        this.parent = parent;

        this.acceptButton = Button.builder(CommonComponents.GUI_YES, (button) -> {
            this.onClose();
                    this.SELECTION_CALLBACK.accept(Choice.YES);
                }
        ).width(210).build();
        this.declineButton = Button.builder(CommonComponents.GUI_NO, (button) -> {
            this.onClose();
                    this.SELECTION_CALLBACK.accept(Choice.NO);
                }
        ).width(210).build();
        this.doneButton = Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose()).build();
    }

    @Override
    protected void init() {
        this.acceptButton.setX(this.width / 2 - this.acceptButton.getWidth() / 2);
        this.acceptButton.setY(50);
        this.addRenderableWidget(this.acceptButton);

        this.declineButton.setX(this.width / 2 - this.declineButton.getWidth() / 2);
        this.declineButton.setY(78);
        this.addRenderableWidget(this.declineButton);

        this.doneButton.setX(this.width / 2 - this.doneButton.getWidth() / 2);
        this.doneButton.setY(this.height - 29);
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 14, 16777215);
    }

        @Override
    public void onClose() {
        Objects.requireNonNull(minecraft).setScreen(this.parent);
    }

    public enum Choice {
        YES, NO
    }
}
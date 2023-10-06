package net.dawinzig.entityseparator.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
    private static final int SPACING = 8;
    private static final int BUTTON_WIDTH = 210;
    private final Screen parent;
    private final Consumer<Choice> SELECTION_CALLBACK;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public ConfirmScreen(Screen parent, Text title, Consumer<Choice> selectionCallback) {
        super(title);
        this.SELECTION_CALLBACK = selectionCallback;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addHeader(new TextWidget(this.getTitle(), this.textRenderer));
        DirectionalLayoutWidget directionalLayoutWidget =
                (this.layout.addBody(DirectionalLayoutWidget.vertical())).spacing(SPACING);
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();

        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.YES, (button) ->
                this.SELECTION_CALLBACK.accept(Choice.YES)
        ).width(BUTTON_WIDTH).build());
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.NO, (button) ->
                this.SELECTION_CALLBACK.accept(Choice.NO)
        ).width(BUTTON_WIDTH).build());

        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> this.close()).build());

        this.layout.refreshPositions();
        this.layout.forEachChild(this::addDrawableChild);
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    @Override
    public void close() {
        Objects.requireNonNull(client).setScreen(this.parent);
    }

    public enum Choice {
        YES, NO
    }
}
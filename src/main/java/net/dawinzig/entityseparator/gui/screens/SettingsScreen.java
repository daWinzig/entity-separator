package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Random;

public class SettingsScreen extends Screen {
    private static final Text TITLE = Text.translatable("entityseparator.settings.title");
    private static final Text RELOAD_LABEL = Text.translatable("entityseparator.button.reload");
    private static final Identifier RELOAD_ID_SHORT = new Identifier("entityseparator", "reload");
    private final Screen parent;
    private final TextIconButtonWidget reloadButton;
    private final ListWidget optionsList;

    protected SettingsScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;

        reloadButton = TextIconButtonWidget.builder(
                        RELOAD_LABEL, (button) -> {

                            this.reset();
                            assert this.client != null;
                            this.client.getToastManager().add(new MessageToast(
                                    this.client,
                                    Text.translatable("entityseparator.toast.reload", TITLE),
                                    MessageToast.Level.INFO
                            ));}, true)
                .texture(RELOAD_ID_SHORT, 16, 16)
                .dimension(20, 20).build();
        reloadButton.setTooltip(Tooltip.of(RELOAD_LABEL));

        this.optionsList = new ListWidget(this, MinecraftClient.getInstance());
        this.reset();
    }

    private void reset() {
        int r = new Random().nextInt(20);
        assert MinecraftClient.getInstance().player != null;
        int g = new Random((int) MinecraftClient.getInstance().mouse.getX()).nextInt(20);
        optionsList.clear();
        optionsList.addHeader(Text.of("Mirror, mirror on the wall..."), Text.of("How do this look?"));
        optionsList.addEntry(
                Text.of("I do nothing!"), Text.of("or do I..?"),
                r, r, 0, 20,
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                entry -> {
                    entry.focusOn(entry.children().get(0));
                    entry.reset();
                },
                entry -> { if (r == g) entry.setValid(false); },
                entry -> {}
        );
        boolean x = new Random().nextBoolean();
        optionsList.addEntry(
                Text.of("Hellooo!"), null, x, !x,
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                entry -> {
                    entry.focusOn(entry.children().get(0));
                    entry.reset();
                }, entry -> {}, entry -> {}
        );
        optionsList.addEntry(
                Text.of("Do you want to say something?!"), Text.of("pleeeesssseeee..."),
                "", "...",
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                entry -> {
                    entry.focusOn(entry.children().get(0));
                    entry.reset();
                }, entry -> {}, entry -> {}
        );
    }

    @Override
    protected void init() {
        reloadButton.setX(3);
        reloadButton.setY(9);
        this.addDrawableChild(reloadButton);

        optionsList.update();
        this.addSelectableChild(this.optionsList);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) ->
            Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.optionsList.save();
            Objects.requireNonNull(this.client).setScreen(this.parent);
        }).dimensions(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.optionsList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 16777215);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    @Override
    public void close() {
        if (optionsList.hasChanged())
            Objects.requireNonNull(client).setScreen(new ConfirmScreen(this,
                    Text.translatable("entityseparator.confirmsave.title"),
                    choice -> {
                        if (choice == ConfirmScreen.Choice.YES)
                            this.optionsList.save();
                        Objects.requireNonNull(this.client).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.client).setScreen(this.parent);
    }
}

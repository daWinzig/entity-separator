package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.EntitySeparator;
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

public class RulesScreen extends Screen {
    private static final Text TITLE = Text.translatable("entityseparator.list.title");
    private static final Text RELOAD_LABEL = Text.translatable("entityseparator.button.reload");
    private static final Text OPEN_LABEL = Text.translatable("entityseparator.button.open");
    private static final Text ADD_LABEL = Text.translatable("entityseparator.list.button.add");
    private static final Text SETTINGS_LABEL = Text.translatable("entityseparator.list.button.options");
    private static final Identifier RELOAD_ID_SHORT = new Identifier("entityseparator", "reload");
    private static final Identifier OPEN_ID_SHORT = new Identifier("entityseparator", "folder");
    private static final Identifier ADD_ID_SHORT = new Identifier("entityseparator", "add");
    private static final Identifier SETTINGS_ID_SHORT = new Identifier("entityseparator", "options");

    private final Screen parent;
    private final ListWidget rulesList;
    private final ButtonWidget reloadButton;
    private final ButtonWidget openButton;
    private final ButtonWidget addButton;
    private final ButtonWidget optionsButton;
    private boolean resetNextInit = false;

    public RulesScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.client = MinecraftClient.getInstance();

        this.rulesList = new ListWidget(this, this.client);
        this.reset();

        reloadButton = TextIconButtonWidget.builder(
                        RELOAD_LABEL, (button) -> {
                            EntitySeparator.RULES.clear();
                            EntitySeparator.CONFIG.loadAllRules();
                            this.reset();
                            assert this.client != null;
                            this.client.getToastManager().add(new MessageToast(
                                    this.client,
                                    Text.translatable("entityseparator.toast.reload", TITLE),
                                    MessageToast.Level.INFO
                            ));
                        }, true)
                .texture(RELOAD_ID_SHORT, 16, 16)
                .dimension(20, 20).build();
        reloadButton.setTooltip(Tooltip.of(RELOAD_LABEL));

        openButton = TextIconButtonWidget.builder(OPEN_LABEL, (button) -> EntitySeparator.CONFIG.openRulesFolder(),true)
                .texture(OPEN_ID_SHORT, 16, 16)
                .dimension(20, 20).build();
        openButton.setTooltip(Tooltip.of(OPEN_LABEL));

        addButton = TextIconButtonWidget.builder(
                        ADD_LABEL,
                        (button) -> {
                            Objects.requireNonNull(client).setScreen(new EditScreen(this));
                            this.resetNextInit = true;
                        },
                        true)
                .texture(ADD_ID_SHORT, 16, 16)
                .dimension(20, 20).build();
        addButton.setTooltip(Tooltip.of(ADD_LABEL));

        optionsButton = TextIconButtonWidget.builder(
                        SETTINGS_LABEL,
                        button -> Objects.requireNonNull(client).setScreen(new SettingsScreen(this)),
                        true)
                .texture(SETTINGS_ID_SHORT, 16, 16)
                .dimension(20, 20).build();
        optionsButton.setTooltip(Tooltip.of(SETTINGS_LABEL));
    }

    private void reset() {
        rulesList.clear();
        EntitySeparator.RULES.forEach((path, rule) ->
            rulesList.addEntry(
                Text.of(rule.getName()), Text.of(path.toString()),
                rule.isEnabled(), false,
                ListWidget.FunctionEnable.ENABLED,
                new Identifier("entityseparator", "edit"),
                Text.translatable("entityseparator.button.editordelete"),
                Text.translatable("entityseparator.button.editordelete.narrator"),
                entry -> {
                    Objects.requireNonNull(this.client).setScreen(new EditScreen(this, path, rule));
                    this.resetNextInit = true;
                },
                entry -> {},
                entry -> rule.setEnabled(entry.getValue())
            )
        );
    }

    @Override
    protected void init() {
        if (resetNextInit) {
            reset();
            resetNextInit = false;
        }

        reloadButton.setX(3);
        reloadButton.setY(9);
        this.addDrawableChild(reloadButton);

        openButton.setX(26);
        openButton.setY(9);
        this.addDrawableChild(openButton);

        addButton.setX(this.width - 46);
        addButton.setY(9);
        this.addDrawableChild(addButton);

        optionsButton.setX(this.width - 23);
        optionsButton.setY(9);
        this.addDrawableChild(optionsButton);

        rulesList.update();
        this.addSelectableChild(this.rulesList);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close())
                .dimensions(this.width / 2 - 75, this.height - 29, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.rulesList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 16777215);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    @Override
    public void close() {
        Objects.requireNonNull(this.client).setScreen(this.parent);
    }
}

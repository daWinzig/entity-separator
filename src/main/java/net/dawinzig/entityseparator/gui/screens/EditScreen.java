package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.EntitySeparator;
import net.dawinzig.entityseparator.config.Rule;
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

import java.nio.file.Path;
import java.util.Objects;

public class EditScreen extends Screen {
    private static final Text TITLE_NEW = Text.translatable("entityseparator.add.title");
    private static final Text TITLE_EDIT = Text.translatable("entityseparator.edit.title");
    private static final Text DELETE_LABEL = Text.translatable("entityseparator.button.delete");
    private static final Identifier DELETE_ID_SHORT = new Identifier("entityseparator", "delete");
    private final Screen parent;
    private final Path path;
    private final Rule rule;
    private final ButtonWidget deleteButton;
    private final ListWidget listWidget;
    private final ButtonWidget doneButton;
    private final ButtonWidget cancelButton;

    protected EditScreen(Screen parent) {
        this(parent, null, new Rule());
    }

    protected EditScreen(Screen parent, Path path, Rule rule) {
        super(path != null
                ? TITLE_EDIT.copy().append(Text.of(" (%s)".formatted(path)))
                : TITLE_NEW);
        this.parent = parent;
        this.path = path;
        this.rule = rule;
        this.client = MinecraftClient.getInstance();

        if (path != null) {
            deleteButton = TextIconButtonWidget.builder(
                            DELETE_LABEL,
                            button -> Objects.requireNonNull(client).setScreen(new ConfirmScreen(this,
                                    Text.translatable("entityseparator.confirm.delete.title"),
                                    choice -> {
                                        if (choice == ConfirmScreen.Choice.YES) {
                                            if (EntitySeparator.CONFIG.deleteRule(this.path)) {
                                                this.client.getToastManager().add(new MessageToast(
                                                        this.client,
                                                        Text.translatable("entityseparator.toast.delete.success"),
                                                        MessageToast.Level.SUCCESS
                                                ));
                                                Objects.requireNonNull(this.client).setScreen(this.parent);
                                            } else {
                                                this.client.getToastManager().add(new MessageToast(
                                                        this.client,
                                                        Text.translatable("entityseparator.toast.delete.failed"),
                                                        MessageToast.Level.ERROR
                                                ));
                                                Objects.requireNonNull(this.client).setScreen(this);
                                            }
                                        }
                                    })),
                            true)
                    .texture(DELETE_ID_SHORT, 16, 16)
                    .dimension(20, 20).build();
            deleteButton.setTooltip(Tooltip.of(DELETE_LABEL));
        } else deleteButton = null;

        this.listWidget = new ListWidget(this, this.client);

        this.cancelButton = ButtonWidget.builder(ScreenTexts.CANCEL, (button) ->
                Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build();

        this.doneButton = ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.exit())
                .dimensions(this.width / 2 + 5, this.height - 29, 150, 20).build();

        listWidget.addEntry(
                Text.translatable("entityseparator.rule.name"), null,
                rule.getName(), rule.getName(),
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setName(entry.getValue()),
                entry -> {
                    entry.setValid(!entry.getValue().isEmpty());
                    this.updateDoneEnabled();
                }
        );
        String entityTypes = rule.getEntityTypes();
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.entities"), null,
                entityTypes, entityTypes,
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setEntityTypes(entry.getValue()),
                entry -> {
                    entry.setValid(Rule.isValidEntityTypes(entry.getValue()));
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.path"), null,
                rule.getPath(), rule.getPath(),
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setPath(entry.getValue()),
                entry -> {
                    entry.setValid(Rule.isValidPath(entry.getValue()));
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.compare"),
                Text.translatable("entityseparator.rule.compare.tooltip"),
                rule.getCompare(), rule.getCompare(),
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setCompare(entry.getValue()),
                entry -> {
                    entry.setValid(Rule.isValidCompare(entry.getValue()));
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.pattern"), null,
                rule.getLabelPattern(), rule.getLabelPattern(),
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setLabelPattern(entry.getValue()),
                entry -> {
                    entry.setValid(!entry.getValue().isEmpty());
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.distance"), null,
                rule.getMaxDistance(), rule.getMaxDistance(), 1, 128,
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setMaxDistance(entry.getValue()),
                entry -> this.updateDoneEnabled()
        );
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.texture"),
                Text.translatable("entityseparator.rule.texture.tooltip"),
                rule.getTexture(), rule.getTexture(),
                ListWidget.FunctionEnable.ON_CHANGED,
                new Identifier("entityseparator", "reset"),
                Text.translatable("entityseparator.button.reset"),
                Text.translatable("entityseparator.button.reset.narrator"),
                ListWidget.Entry::reset,
                entry -> this.rule.setTexture(entry.getValue()),
                entry -> this.updateDoneEnabled()
        );
    }

    @Override
    protected void init() {
        if (path != null) {
            deleteButton.setX(this.width - 23);
            deleteButton.setY(9);
            this.addDrawableChild(deleteButton);
        }

        listWidget.update();
        this.addSelectableChild(this.listWidget);

        this.cancelButton.setX(this.width / 2 - 155);
        this.cancelButton.setY(this.height - 29);
        this.addDrawableChild(this.cancelButton);

        this.doneButton.setX(this.width / 2 + 5);
        this.doneButton.setY(this.height - 29);
        this.addDrawableChild(this.doneButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.listWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 16777215);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    private void updateDoneEnabled() {
        for (int i = 0; i < listWidget.children().size(); i++) {
            if (!listWidget.children().get(i).isValid()) {
                this.doneButton.active = false;
                return;
            }
        }
        this.doneButton.active = true;
    }

    @Override
    public void close() {
        if (listWidget.hasChanged())
            Objects.requireNonNull(client).setScreen(new ConfirmScreen(this,
                    Text.translatable("entityseparator.confirmsave.title"),
                    choice -> {
                        if (choice == ConfirmScreen.Choice.YES) {
                            this.exit();
                        }
                        else
                            Objects.requireNonNull(this.client).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.client).setScreen(this.parent);
    }

    private void exit() {
        this.listWidget.save();
        Path path = this.path;
        if (path == null) {
            path = EntitySeparator.CONFIG.getAvailiableRelRulePath(Path.of(""), this.rule.getName());
            EntitySeparator.RULES.put(path, this.rule);
        }
        if (EntitySeparator.CONFIG.saveRule(path, this.rule))
            Objects.requireNonNull(this.client).setScreen(this.parent);
    }
}

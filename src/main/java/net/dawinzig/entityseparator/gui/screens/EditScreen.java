package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.config.Rule;
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
    private final RulesScreen parent;
    private final Rule rule;
    private final Rule defaultRule;
    private final Path path;
    private final ButtonWidget deleteButton;
    private final ListWidget listWidget;
    private final ButtonWidget doneButton;
    private final ButtonWidget cancelButton;
    private final boolean isNew;

    protected EditScreen(RulesScreen parent) {
        this(parent, null, null, null);
    }
    protected EditScreen(RulesScreen parent, Rule rule) {
        this(parent, rule, null, null);
    }
    protected EditScreen(RulesScreen parent, Rule rule, Rule defaultRule) {
        this(parent, rule, defaultRule, null);
    }
    protected EditScreen(RulesScreen parent, Rule rule, Rule defaultRule, Path path) {
        super(path != null ? TITLE_EDIT.copy().append(" (%s)".formatted(path.toString())) : TITLE_NEW);
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        if (rule == null) {
            this.rule = new Rule();
            this.isNew = true;
        } else {
            this.rule = rule;
            this.isNew = false;
        }
        if (defaultRule == null) {
            this.defaultRule = this.rule.copy();
        } else {
            this.defaultRule = defaultRule;
        }
        this.path = path;

        if (!this.isNew) {
            this.deleteButton = TextIconButtonWidget.builder(
                    DELETE_LABEL, button -> {
                            if (this.path != null) {
                                parent.setPendingDelete(this.path);
                            } else {
                                parent.removePendingCreation(rule);
                            }
                            Objects.requireNonNull(this.client).setScreen(this.parent);
                        }, true)
                    .texture(DELETE_ID_SHORT, 16, 16)
                    .dimension(20, 20).build();
            deleteButton.setTooltip(Tooltip.of(DELETE_LABEL));
        } else this.deleteButton = null;

        this.listWidget = new ListWidget(this, this.client);

        this.cancelButton = ButtonWidget.builder(ScreenTexts.CANCEL, (button) ->
                Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build();

        this.doneButton = ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.save())
                .dimensions(this.width / 2 + 5, this.height - 29, 150, 20).build();

        listWidget.addEntry(
                Text.translatable("entityseparator.rule.name"), null,
                this.rule.getName(), this.defaultRule.getName(),
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
        listWidget.addEntry(
                Text.translatable("entityseparator.rule.entities"), null,
                this.rule.getEntityTypes(), this.defaultRule.getEntityTypes(),
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
                this.rule.getPath(), this.defaultRule.getPath(),
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
                this.rule.getCompare(), this.defaultRule.getCompare(),
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
                this.rule.getLabelPattern(), this.defaultRule.getLabelPattern(),
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
                this.rule.getMaxDistance(), this.defaultRule.getMaxDistance(), 1, 128,
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
                this.rule.getTexture(), this.defaultRule.getTexture(),
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
        if (deleteButton != null) {
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
                            this.save();
                        }
                        else
                            Objects.requireNonNull(this.client).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.client).setScreen(this.parent);
    }

    private void save() {
        this.listWidget.save();
        if (this.path != null) {
            if (this.rule.compare(this.defaultRule)) this.parent.removePendingUpdate(this.path);
            else this.parent.setPendingUpdate(this.path, this.rule);
        }
        else if (this.isNew) this.parent.setPendingCreation(this.rule);
        parent.reload();
        Objects.requireNonNull(this.client).setScreen(this.parent);
    }
}

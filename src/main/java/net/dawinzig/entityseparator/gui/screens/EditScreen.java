package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.Resources;
import net.dawinzig.entityseparator.config.Rule;
import net.dawinzig.entityseparator.gui.widgets.IconButtonWidget;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.nio.file.Path;
import java.util.Objects;

public class EditScreen extends Screen {
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
    protected EditScreen(RulesScreen parent, Rule rule, Rule defaultRule, Path path) {
        super(path != null
                ? Resources.Translation.insert(Resources.Translation.TITLE_EDIT, path)
                : Resources.Translation.TITLE_NEW);
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
            this.deleteButton = new IconButtonWidget(
                    20 , 20, Resources.IDShort.DELETE, 16, 16,
                    button -> {
                        if (this.path != null) {
                            parent.setPendingDelete(this.path);
                        } else {
                            parent.removePendingCreation(rule);
                        }
                        Objects.requireNonNull(this.client).setScreen(this.parent);
                    }, Resources.Translation.BUTTON_DELETE);
            deleteButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_DELETE));
        } else this.deleteButton = null;

        this.listWidget = new ListWidget(this, this.client);

        this.cancelButton = ButtonWidget.builder(Resources.Translation.BUTTON_CANCEL, (button) ->
                Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build();

        this.doneButton = ButtonWidget.builder(Resources.Translation.BUTTON_DONE, (button) -> this.save())
                .dimensions(this.width / 2 + 5, this.height - 29, 150, 20).build();

        listWidget.addEntry(
                Resources.Translation.RULE_NAME, null,
                this.rule.getName(), this.defaultRule.getName(),
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
                ListWidget.Entry::reset,
                entry -> this.rule.setName(entry.getValue()),
                entry -> {
                    entry.setValid(!entry.getValue().isEmpty());
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Resources.Translation.RULE_ENTITIES, null,
                this.rule.getEntityTypes(), this.defaultRule.getEntityTypes(),
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
                ListWidget.Entry::reset,
                entry -> this.rule.setEntityTypes(entry.getValue()),
                entry -> {
                    entry.setValid(Rule.isValidEntityTypes(entry.getValue()));
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Resources.Translation.RULE_PATH, null,
                this.rule.getPath(), this.defaultRule.getPath(),
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
                ListWidget.Entry::reset,
                entry -> this.rule.setPath(entry.getValue()),
                entry -> {
                    entry.setValid(Rule.isValidPath(entry.getValue()));
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Resources.Translation.RULE_COMPARE,
                Resources.Translation.RULE_COMPARE_TOOLTIP,
                this.rule.getCompare(), this.defaultRule.getCompare(),
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
                ListWidget.Entry::reset,
                entry -> this.rule.setCompare(entry.getValue()),
                entry -> {
                    entry.setValid(Rule.isValidCompare(entry.getValue()));
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Resources.Translation.RULE_PATTERN, null,
                this.rule.getLabelPattern(), this.defaultRule.getLabelPattern(),
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
                ListWidget.Entry::reset,
                entry -> this.rule.setLabelPattern(entry.getValue()),
                entry -> {
                    entry.setValid(!entry.getValue().isEmpty());
                    this.updateDoneEnabled();
                }
        );
        listWidget.addEntry(
                Resources.Translation.RULE_DISTANCE, null,
                this.rule.getMaxDistance(), this.defaultRule.getMaxDistance(), 1, 128,
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
                ListWidget.Entry::reset,
                entry -> this.rule.setMaxDistance(entry.getValue()),
                entry -> this.updateDoneEnabled()
        );
        listWidget.addEntry(
                Resources.Translation.RULE_TEXTURE,
                Resources.Translation.RULE_TEXTURE_TOOLTIP,
                this.rule.getTexture(), this.defaultRule.getTexture(),
                ListWidget.FunctionEnable.ON_CHANGED,
                Resources.IDShort.RESET,
                Resources.Translation.BUTTON_RESET,
                Resources.Translation.BUTTON_RESET,
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

    @SuppressWarnings("unused")
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }
    @SuppressWarnings("unused")
    public void renderBackground(DrawContext context) {
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
                    Resources.Translation.CONFIRM_SAVE_TITLE,
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

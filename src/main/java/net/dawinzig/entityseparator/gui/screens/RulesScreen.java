package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.Resources;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Rule;
import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.*;

public class RulesScreen extends Screen {
    private final Screen parent;
    private final ListWidget rulesList;
    private final Set<Path> pendingDelete;
    private final Map<Rule, Boolean> pendingCreation;
    private final Map<Path, Rule> pendingUpdate;
    private final ButtonWidget reloadButton;
    private final ButtonWidget openButton;
    private final ButtonWidget addButton;
    private final ButtonWidget optionsButton;

    public RulesScreen(Screen parent) {
        super(Resources.Translation.TITLE_RULES);
        this.parent = parent;
        this.client = MinecraftClient.getInstance();

        this.rulesList = new ListWidget(this, this.client);
        this.pendingDelete = new TreeSet<>();
        this.pendingCreation = new LinkedHashMap<>();
        this.pendingUpdate = new HashMap<>();
        this.reload();

        reloadButton = TextIconButtonWidget.builder(
                        Resources.Translation.BUTTON_RELOAD, (button) -> {
                            Config.RULES.clear();
                            Config.IO.loadAllRules();
                            this.reload();
                            assert this.client != null;
                            this.client.getToastManager().add(new MessageToast(
                                    this.client,
                                    Resources.Translation.insert(Resources.Translation.TOAST_RELOAD, Resources.Translation.TITLE_RULES),
                                    MessageToast.Level.INFO
                            ));
                        }, true)
                .texture(Resources.IDShort.RELOAD, 16, 16)
                .dimension(20, 20).build();
        reloadButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_RELOAD));

        openButton = TextIconButtonWidget.builder(Resources.Translation.BUTTON_OPEN, (button) -> Config.IO.openRulesFolder(),true)
                .texture(Resources.IDShort.FOLDER, 16, 16)
                .dimension(20, 20).build();
        openButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_OPEN));

        addButton = TextIconButtonWidget.builder(
                        Resources.Translation.BUTTON_NEW,
                        (button) -> Objects.requireNonNull(client).setScreen(new EditScreen(this)),
                        true)
                .texture(Resources.IDShort.ADD, 16, 16)
                .dimension(20, 20).build();
        addButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_NEW));

        optionsButton = TextIconButtonWidget.builder(
                        Resources.Translation.BUTTON_OPTIONS,
                        button -> Objects.requireNonNull(client).setScreen(new SettingsScreen(this)),
                        true)
                .texture(Resources.IDShort.OPTIONS, 16, 16)
                .dimension(20, 20).build();
        optionsButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_OPTIONS));
    }

    protected void reload() {
        this.rulesList.clear();
        this.rulesList.addHeader(Resources.Translation.RULES_CATEGORY_ON_DISK, Resources.Translation.RULES_CATEGORY_ON_DISK_TOOLTIP);
        Config.RULES.forEach((path, rule) -> {
            if (this.pendingDelete.contains(path)) return;
            Rule usedRule;
            String name;
            if (this.pendingUpdate.containsKey(path)) {
                usedRule = this.pendingUpdate.get(path);
                name = "*" + usedRule.getName();
            } else if (Config.SAVE_FAILED.contains(path)) {
                usedRule = rule.copy();
                name = "[!] " + usedRule.getName();
            } else {
                usedRule = rule.copy();
                name = usedRule.getName();
            }
            this.rulesList.addEntry(
                    Text.of(name), Text.of(path.toString()),
                    Config.isRuleEnabled(path), false,
                    ListWidget.FunctionEnable.ENABLED,
                    Resources.IDShort.EDIT,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE_NARRATOR,
                    entry -> Objects.requireNonNull(this.client).setScreen(new EditScreen(this, usedRule, rule, path)),
                    entry -> {},
                    entry -> Config.setRuleEnabled(path, entry.getValue())
            );
        });
        this.rulesList.addHeader(Resources.Translation.RULES_CATEGORY_CREATED, Resources.Translation.RULES_CATEGORY_CREATED_TOOLTIP);
        this.pendingCreation.keySet().forEach(rule ->
            this.rulesList.addEntry(
                    Text.of(rule.getName()), Text.of(""),
                    this.pendingCreation.get(rule), false,
                    ListWidget.FunctionEnable.ENABLED,
                    Resources.IDShort.EDIT,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE_NARRATOR,
                    entry -> Objects.requireNonNull(this.client).setScreen(new EditScreen(this, rule)),
                    entry -> {},
                    entry -> this.pendingCreation.put(rule, entry.getValue())
            )
        );
        this.rulesList.addHeader(Resources.Translation.RULES_CATEGORY_DELETED, Resources.Translation.RULES_CATEGORY_DELETED_TOOLTIP);
        this.pendingDelete.forEach(path -> {
            if (!Config.RULES.containsKey(path)) return;
            Rule rule = Config.RULES.get(path);
            this.rulesList.addEntry(
                    Text.of(rule.getName()), Text.of(path.toString()),
                    ListWidget.FunctionEnable.ENABLED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESTORE,
                    Resources.Translation.BUTTON_RESTORE,
                    entry -> {
                        this.pendingDelete.remove(path);
                        this.reload();
                    }
            );
        });
    }

    public void setPendingCreation(Rule rule) {
        this.pendingCreation.put(rule, false);
    }
    public void removePendingCreation(Rule rule) {
        this.pendingCreation.remove(rule);
    }
    public void setPendingDelete(Path path) {
        this.pendingDelete.add(path);
    }
    public void setPendingUpdate(Path path, Rule rule) {
        this.pendingUpdate.put(path, rule);
    }
    public void removePendingUpdate(Path path) {
        this.pendingUpdate.remove(path);
    }

    @Override
    protected void init() {
        reload();

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

        this.addDrawableChild(ButtonWidget.builder(Resources.Translation.BUTTON_CANCEL, (button) ->
                Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Resources.Translation.BUTTON_SAVE_EXIT, (button) -> this.save()
        ).dimensions(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
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
        if (this.rulesList.hasChanged() || !this.pendingDelete.isEmpty() || !this.pendingCreation.isEmpty() ||
                !this.pendingUpdate.isEmpty() || !Config.SAVE_FAILED.isEmpty())
            Objects.requireNonNull(client).setScreen(new ConfirmScreen(this,
                    Resources.Translation.CONFIRM_SAVE_TITLE,
                    choice -> {
                        if (choice == ConfirmScreen.Choice.YES) this.save();
                        else Objects.requireNonNull(this.client).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.client).setScreen(this.parent);}

    private void save() {
        boolean success = true;

        this.rulesList.save();

        for (Path path : Config.SAVE_FAILED) {
            if (this.pendingUpdate.containsKey(path) || this.pendingDelete.contains(path)) continue;
            Config.RULES.put(path, Config.RULES.get(path));
            if (!Config.IO.saveRule(path, Config.RULES.get(path))) {
                success = false;
                Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                        this.client,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                        MessageToast.Level.ERROR
                ));
            }
        }

        for (Map.Entry<Path, Rule> entry : this.pendingUpdate.entrySet()) {
            Path path = entry.getKey();
            Rule rule = entry.getValue();
            if (!this.pendingDelete.contains(path)) {
                Config.RULES.put(path, rule);
                if (!Config.IO.saveRule(path, rule)) {
                    success = false;
                    Config.SAVE_FAILED.add(path);
                    Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                            this.client,
                            Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                            MessageToast.Level.ERROR
                    ));
                }
            }
        }

        this.pendingDelete.forEach(Config.IO::deleteRule);

        for (Map.Entry<Rule, Boolean> entry : this.pendingCreation.entrySet()) {
            Rule rule = entry.getKey();
            Path path = Config.IO.getAvailiableRelRulePath(Path.of(""), rule.getName());
            Config.RULES.put(path, rule);
            Config.setRuleEnabled(path, entry.getValue());
            if (!Config.IO.saveRule(path, rule)) {
                success = false;
                Config.SAVE_FAILED.add(path);
                Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                        this.client,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                        MessageToast.Level.ERROR
                ));
            }
        }

        if (this.rulesList.hasChanged() || !this.pendingDelete.isEmpty() || !this.pendingCreation.isEmpty())
            Config.IO.saveEnabled();

        if (success) Objects.requireNonNull(this.client).setScreen(this.parent);
        else {
            this.pendingUpdate.clear();
            this.pendingDelete.clear();
            this.pendingCreation.clear();
            this.reload();
        }
    }
}

package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.Resources;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Rule;
import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.dawinzig.entityseparator.gui.widgets.IconButtonWidget;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RulesScreen extends Screen {
    private final Screen parent;
    private final ListWidget rulesList;
    private final Set<Path> pendingDelete;
    private final Map<Rule, Boolean> pendingCreation;
    private final Map<Path, Rule> pendingUpdate;
    private final Map<Path, Boolean> rulesEnabled;
    private final Button reloadButton;
    private final Button openButton;
    private final Button addButton;
    private final Button optionsButton;

    public RulesScreen(Screen parent) {
        super(Resources.Translation.TITLE_RULES);
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();

        this.rulesList = new ListWidget(this, this.minecraft);
        this.rulesEnabled = new LinkedHashMap<>();
        this.rulesEnabled.putAll(Config.ENABLED);
        this.pendingDelete = new TreeSet<>();
        this.pendingCreation = new LinkedHashMap<>();
        this.pendingUpdate = new HashMap<>();
        this.reload();

        reloadButton = new IconButtonWidget(20, 20, Resources.IDShort.RELOAD, 16, 16,
                (button) -> {
                    Config.RULES.clear();
                    Config.IO.loadAllRules();
                    this.reload();
                    assert this.minecraft != null;
                    this.minecraft.getToastManager().addToast(new MessageToast(
                            this.minecraft,
                            Resources.Translation.insert(Resources.Translation.TOAST_RELOAD, Resources.Translation.TITLE_RULES),
                            MessageToast.Level.INFO
                    ));
                }, Resources.Translation.BUTTON_RELOAD);
        reloadButton.setTooltip(Tooltip.create(Resources.Translation.BUTTON_RELOAD));

        openButton = new IconButtonWidget(20, 20, Resources.IDShort.FOLDER, 16, 16,
                (button) -> Config.IO.openRulesFolder(), Resources.Translation.BUTTON_OPEN);
        openButton.setTooltip(Tooltip.create(Resources.Translation.BUTTON_OPEN));

        addButton = new IconButtonWidget(20, 20, Resources.IDShort.ADD, 16, 16,
                (button) -> Objects.requireNonNull(minecraft).setScreen(new EditScreen(this)),
                Resources.Translation.BUTTON_NEW);
        addButton.setTooltip(Tooltip.create(Resources.Translation.BUTTON_NEW));

        optionsButton = new IconButtonWidget(20, 20, Resources.IDShort.OPTIONS, 16, 16,
                (button) -> Objects.requireNonNull(minecraft).setScreen(new SettingsScreen(this)),
                Resources.Translation.BUTTON_OPTIONS);
        optionsButton.setTooltip(Tooltip.create(Resources.Translation.BUTTON_OPTIONS));
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
                name = "§o" + usedRule.getName() + "*";
            } else if (Config.SAVE_FAILED.contains(path)) {
                usedRule = rule.copy();
                name = "§4[!]§r " + usedRule.getName();
            } else {
                usedRule = rule.copy();
                name = usedRule.getName();
            }
            this.rulesEnabled.putIfAbsent(path, false);
            this.rulesList.addEntry(
                    Component.nullToEmpty(name), Component.nullToEmpty(path.toString()),
                    this.rulesEnabled.getOrDefault(path, false), false,
                    ListWidget.FunctionEnable.ENABLED,
                    Resources.IDShort.EDIT,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE_NARRATOR,
                    entry -> Objects.requireNonNull(this.minecraft).setScreen(new EditScreen(this, usedRule, rule, path)),
                    entry -> Config.ENABLED.put(path, entry.getValue()),
                    entry -> rulesEnabled.put(path, entry.getValue())
            );
        });
        this.rulesList.addHeader(Resources.Translation.RULES_CATEGORY_CREATED, Resources.Translation.RULES_CATEGORY_CREATED_TOOLTIP);
        this.pendingCreation.keySet().forEach(rule ->
            this.rulesList.addEntry(
                    Component.nullToEmpty(rule.getName()), Component.nullToEmpty(""),
                    this.pendingCreation.get(rule), false,
                    ListWidget.FunctionEnable.ENABLED,
                    Resources.IDShort.EDIT,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE_NARRATOR,
                    entry -> Objects.requireNonNull(this.minecraft).setScreen(new EditScreen(this, rule)),
                    entry -> this.pendingCreation.put(rule, entry.getValue()),
                    entry -> {}
            )
        );
        this.rulesList.addHeader(Resources.Translation.RULES_CATEGORY_DELETED, Resources.Translation.RULES_CATEGORY_DELETED_TOOLTIP);
        this.pendingDelete.forEach(path -> {
            if (!Config.RULES.containsKey(path)) return;
            Rule rule = Config.RULES.get(path);
            this.rulesList.addEntry(
                    Component.nullToEmpty(rule.getName()), Component.nullToEmpty(path.toString()),
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
        reloadButton.setY(3);
        this.addRenderableWidget(reloadButton);

        openButton.setX(26);
        openButton.setY(3);
        this.addRenderableWidget(openButton);

        addButton.setX(this.width - 46);
        addButton.setY(3);
        this.addRenderableWidget(addButton);

        optionsButton.setX(this.width - 23);
        optionsButton.setY(3);
        this.addRenderableWidget(optionsButton);

        rulesList.update();
        this.addWidget(this.rulesList);

        this.addRenderableWidget(Button.builder(Resources.Translation.BUTTON_CANCEL, (button) ->
                Objects.requireNonNull(this.minecraft).setScreen(this.parent)
        ).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addRenderableWidget(Button.builder(Resources.Translation.BUTTON_SAVE_EXIT, (button) -> this.save()
        ).bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.rulesList.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 11, 16777215);
    }

    private boolean enableChanged() {
        for (Path path : Config.RULES.keySet()) {
            boolean a = Config.ENABLED.getOrDefault(path, false);
            boolean b = this.rulesEnabled.getOrDefault(path, a);
            if (a != b) return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        if (this.enableChanged() || !this.pendingDelete.isEmpty() || !this.pendingCreation.isEmpty() ||
                !this.pendingUpdate.isEmpty() || !Config.SAVE_FAILED.isEmpty())
            Objects.requireNonNull(minecraft).setScreen(new ConfirmScreen(this,
                    Resources.Translation.CONFIRM_SAVE_TITLE,
                    choice -> {
                        if (choice == ConfirmScreen.Choice.YES) this.save();
                        else Objects.requireNonNull(this.minecraft).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.minecraft).setScreen(this.parent);}

    private void save() {
        AtomicBoolean success = new AtomicBoolean(true);
        boolean enabledChanged = this.enableChanged() || !this.pendingDelete.isEmpty() || !this.pendingCreation.isEmpty();

        this.rulesList.save();

        Config.SAVE_FAILED.forEach(path -> {
            if (this.pendingUpdate.containsKey(path) || this.pendingDelete.contains(path)) return;
            Config.RULES.put(path, Config.RULES.get(path));
            if (!Config.IO.saveRule(path, Config.RULES.get(path))) {
                success.set(false);
                Objects.requireNonNull(this.minecraft).getToastManager().addToast(new MessageToast(
                        this.minecraft,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                        MessageToast.Level.ERROR
                ));
            }
        });

        this.pendingUpdate.forEach((path, rule) -> {
            if (!this.pendingDelete.contains(path)) {
                Config.RULES.put(path, rule);
                if (!Config.IO.saveRule(path, rule)) {
                    success.set(false);
                    Config.SAVE_FAILED.add(path);
                    Objects.requireNonNull(this.minecraft).getToastManager().addToast(new MessageToast(
                            this.minecraft,
                            Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                            MessageToast.Level.ERROR
                    ));
                }
            }
        });

        this.pendingDelete.forEach(Config.IO::deleteRule);

        this.pendingCreation.forEach((rule, value) -> {
            Path path = Config.IO.getAvailiableRelRulePath(Path.of(""), rule.getName());
            Config.RULES.put(path, rule);
            Config.ENABLED.put(path, value);
            if (!Config.IO.saveRule(path, rule)) {
                success.set(false);
                Config.SAVE_FAILED.add(path);
                Objects.requireNonNull(this.minecraft).getToastManager().addToast(new MessageToast(
                        this.minecraft,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                        MessageToast.Level.ERROR
                ));
            }
        });

        if (enabledChanged) Config.IO.saveEnabled();

        if (success.get()) Objects.requireNonNull(this.minecraft).setScreen(this.parent);
        else {
            this.pendingUpdate.clear();
            this.pendingDelete.clear();
            this.pendingCreation.clear();
            this.reload();
        }
    }
}

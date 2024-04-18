package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.Resources;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Rule;
import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.dawinzig.entityseparator.gui.widgets.IconButtonWidget;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.dawinzig.entityseparator.gui.widgets.TextureWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

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
    private final ButtonWidget reloadButton;
    private final ButtonWidget openButton;
    private final ButtonWidget addButton;
    private final ButtonWidget optionsButton;

    //TEMP MobVote2023
    private final TextureWidget armadillo;
    private final TextureWidget crab;
    private final TextureWidget penguin;

    public RulesScreen(Screen parent) {
        super(Resources.Translation.TITLE_RULES);
        this.parent = parent;
        this.client = MinecraftClient.getInstance();

        this.rulesList = new ListWidget(this, this.client);
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
                    assert this.client != null;
                    this.client.getToastManager().add(new MessageToast(
                            this.client,
                            Resources.Translation.insert(Resources.Translation.TOAST_RELOAD, Resources.Translation.TITLE_RULES),
                            MessageToast.Level.INFO
                    ));
                }, Resources.Translation.BUTTON_RELOAD);
        reloadButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_RELOAD));

        openButton = new IconButtonWidget(20, 20, Resources.IDShort.FOLDER, 16, 16,
                (button) -> Config.IO.openRulesFolder(), Resources.Translation.BUTTON_OPEN);
        openButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_OPEN));

        addButton = new IconButtonWidget(20, 20, Resources.IDShort.ADD, 16, 16,
                (button) -> Objects.requireNonNull(client).setScreen(new EditScreen(this)),
                Resources.Translation.BUTTON_NEW);
        addButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_NEW));

        optionsButton = new IconButtonWidget(20, 20, Resources.IDShort.OPTIONS, 16, 16,
                (button) -> Objects.requireNonNull(client).setScreen(new SettingsScreen(this)),
                Resources.Translation.BUTTON_OPTIONS);
        optionsButton.setTooltip(Tooltip.of(Resources.Translation.BUTTON_OPTIONS));

        //TEMP MobVote2023
        this.armadillo = new TextureWidget(Resources.IDShort.ARMADILLO, Resources.IDShort.ARMADILLO_SURPRISED, 32, 32);
        this.crab = new TextureWidget(Resources.IDShort.CRAB, Resources.IDShort.CRAB_WAVE, 36, 36);
        this.penguin = new TextureWidget(Resources.IDShort.PENGUIN, Resources.IDShort.PENGUIN_HAPPY, 32, 32);
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
                    Text.of(name), Text.of(path.toString()),
                    this.rulesEnabled.getOrDefault(path, false), false,
                    ListWidget.FunctionEnable.ENABLED,
                    Resources.IDShort.EDIT,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE,
                    Resources.Translation.BUTTON_EDIT_OR_DELETE_NARRATOR,
                    entry -> Objects.requireNonNull(this.client).setScreen(new EditScreen(this, usedRule, rule, path)),
                    entry -> Config.ENABLED.put(path, entry.getValue()),
                    entry -> rulesEnabled.put(path, entry.getValue())
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
                    entry -> this.pendingCreation.put(rule, entry.getValue()),
                    entry -> {}
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
        reloadButton.setY(3);
        this.addDrawableChild(reloadButton);

        openButton.setX(26);
        openButton.setY(3);
        this.addDrawableChild(openButton);

        addButton.setX(this.width - 46);
        addButton.setY(3);
        this.addDrawableChild(addButton);

        optionsButton.setX(this.width - 23);
        optionsButton.setY(3);
        this.addDrawableChild(optionsButton);

        rulesList.update();
        this.addSelectableChild(this.rulesList);

        this.addDrawableChild(ButtonWidget.builder(Resources.Translation.BUTTON_CANCEL, (button) ->
                Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Resources.Translation.BUTTON_SAVE_EXIT, (button) -> this.save()
        ).dimensions(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());

        //TEMP MobVote2023
        this.armadillo.setX(this.width - 76);
        this.armadillo.setY(0);
        this.crab.setX(-3);
        this.crab.setY(36);
        this.penguin.setX(this.width - this.penguin.getWidth());
        this.penguin.setY(this.height - this.penguin.getHeight());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.rulesList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 11, 16777215);

        //TEMP MobVote2023
        if (Config.OPTIONS.getValueOrDefault(false, "easter_eggs")) {
            if ((System.currentTimeMillis() / 1000L) < 1697994000) {
                this.armadillo.render(context, mouseX, mouseY, delta);
                this.crab.render(context, mouseX, mouseY, delta);
                this.penguin.render(context, mouseX, mouseY, delta);
            }
        }
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
    public void close() {
        if (this.enableChanged() || !this.pendingDelete.isEmpty() || !this.pendingCreation.isEmpty() ||
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
        AtomicBoolean success = new AtomicBoolean(true);
        boolean enabledChanged = this.enableChanged() || !this.pendingDelete.isEmpty() || !this.pendingCreation.isEmpty();

        this.rulesList.save();

        Config.SAVE_FAILED.forEach(path -> {
            if (this.pendingUpdate.containsKey(path) || this.pendingDelete.contains(path)) return;
            Config.RULES.put(path, Config.RULES.get(path));
            if (!Config.IO.saveRule(path, Config.RULES.get(path))) {
                success.set(false);
                Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                        this.client,
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
                    Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                            this.client,
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
                Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                        this.client,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, path),
                        MessageToast.Level.ERROR
                ));
            }
        });

        if (enabledChanged) Config.IO.saveEnabled();

        if (success.get()) Objects.requireNonNull(this.client).setScreen(this.parent);
        else {
            this.pendingUpdate.clear();
            this.pendingDelete.clear();
            this.pendingCreation.clear();
            this.reload();
        }
    }
}

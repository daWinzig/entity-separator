package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.Resources;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Option;
import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private final ListWidget optionsList;

    protected SettingsScreen(Screen parent) {
        super(Resources.Translation.TITLE_OPTIONS);
        this.parent = parent;

        this.optionsList = new ListWidget(this, Minecraft.getInstance());
        this.getOptions();
    }

    private void getOptions() {
        this.getOptions(Config.OPTIONS, new ArrayList<>());
    }
    private void getOptions(Option.Category category, List<String> path) {
        category.foreach((key, option) -> {
            if (option.isShownInGUI()) {
                List<String> newPath = new ArrayList<>(path);
                newPath.add(key);
                this.addOption(option, newPath.toArray(new String[0]));
                if (option instanceof Option.Category) this.getOptions((Option.Category) option, newPath);
            }
        });
    }
    private void addOption(Option<?> option, String[] path) {
        if (option instanceof Option.Category) {
            this.optionsList.addHeader(option.getDisplayName(), option.getTooltip());
        } else if (option instanceof Option.Bool) {
            this.optionsList.addEntry(
                    option.getDisplayName(), option.getTooltip(),
                    ((Option.Bool) option).getValue(), ((Option.Bool) option).getDefaultValue(),
                    ListWidget.FunctionEnable.ON_CHANGED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESET,
                    Resources.Translation.BUTTON_RESET,
                    entry -> {
                        entry.setFocused(entry.children().get(0));
                        entry.reset();
                    },
                    entry -> Config.OPTIONS.getBool(path).setValue(entry.getValue()),
                    entry -> {}
            );
        } else if (option instanceof Option.Str) {
            this.optionsList.addEntry(
                    option.getDisplayName(), option.getTooltip(),
                    ((Option.Str) option).getValue(), ((Option.Str) option).getDefaultValue(),
                    ListWidget.FunctionEnable.ON_CHANGED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESET,
                    Resources.Translation.BUTTON_RESET,
                    entry -> {
                        entry.setFocused(entry.children().get(0));
                        entry.reset();
                    },
                    entry -> Config.OPTIONS.getStr(path).setValue(entry.getValue()),
                    entry -> {}
            );
        } else if (option instanceof Option.Int) {
            this.optionsList.addEntry(
                    option.getDisplayName(), option.getTooltip(),
                    ((Option.Int) option).getValue(), ((Option.Int) option).getDefaultValue(),
                    ((Option.Int) option).getMin(), ((Option.Int) option).getMax(),
                    ListWidget.FunctionEnable.ON_CHANGED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESET,
                    Resources.Translation.BUTTON_RESET,
                    entry -> {
                        entry.setFocused(entry.children().get(0));
                        entry.reset();
                    },
                    entry -> Config.OPTIONS.getInt(path).setValue(entry.getValue()),
                    entry -> {}
            );
        }
    }

    @Override
    protected void init() {
        optionsList.update();
        this.addWidget(this.optionsList);

        this.addRenderableWidget(Button.builder(Resources.Translation.BUTTON_CANCEL, (button) ->
            Objects.requireNonNull(this.minecraft).setScreen(this.parent)
        ).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addRenderableWidget(Button.builder(Resources.Translation.BUTTON_SAVE_EXIT, (button) -> this.save()
        ).bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.optionsList.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 11, 16777215);
    }

    @Override
    public void onClose() {
        if (optionsList.hasChanged())
            Objects.requireNonNull(minecraft).setScreen(new ConfirmScreen(this,
                    Resources.Translation.CONFIRM_SAVE_TITLE,
                    choice -> {
                        if (choice == ConfirmScreen.Choice.YES) this.save();
                        else Objects.requireNonNull(this.minecraft).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.minecraft).setScreen(this.parent);
    }

    public void save() {
        if (this.optionsList.hasChanged()) {
            this.optionsList.save();
            if (!Config.IO.saveConfig())
                Objects.requireNonNull(this.minecraft).getToasts().addToast(new MessageToast(
                        this.minecraft,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, "config"),
                        MessageToast.Level.ERROR
                ));
        }
        Objects.requireNonNull(this.minecraft).setScreen(this.parent);
    }
}

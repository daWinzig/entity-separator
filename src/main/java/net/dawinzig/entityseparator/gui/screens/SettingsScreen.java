package net.dawinzig.entityseparator.gui.screens;

import net.dawinzig.entityseparator.Resources;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Option;
import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.dawinzig.entityseparator.gui.widgets.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private final ListWidget optionsList;

    protected SettingsScreen(Screen parent) {
        super(Resources.Translation.TITLE_OPTIONS);
        this.parent = parent;

        this.optionsList = new ListWidget(this, MinecraftClient.getInstance());
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
            this.optionsList.addHeader(Text.of(option.getDisplayName()), Text.of(option.getTooltip()));
        } else if (option instanceof Option.Bool) {
            this.optionsList.addEntry(
                    Text.of(option.getDisplayName()), Text.of(option.getTooltip()),
                    ((Option.Bool) option).getValue(), ((Option.Bool) option).getDefaultValue(),
                    ListWidget.FunctionEnable.ON_CHANGED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESET,
                    Resources.Translation.BUTTON_RESET,
                    entry -> {
                        entry.focusOn(entry.children().get(0));
                        entry.reset();
                    },
                    entry -> Config.OPTIONS.getBool(path).setValue(entry.getValue()),
                    entry -> {}
            );
        } else if (option instanceof Option.Str) {
            this.optionsList.addEntry(
                    Text.of(option.getDisplayName()), Text.of(option.getTooltip()),
                    ((Option.Str) option).getValue(), ((Option.Str) option).getDefaultValue(),
                    ListWidget.FunctionEnable.ON_CHANGED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESET,
                    Resources.Translation.BUTTON_RESET,
                    entry -> {
                        entry.focusOn(entry.children().get(0));
                        entry.reset();
                    },
                    entry -> Config.OPTIONS.getStr(path).setValue(entry.getValue()),
                    entry -> {}
            );
        } else if (option instanceof Option.Int) {
            this.optionsList.addEntry(
                    Text.of(option.getDisplayName()), Text.of(option.getTooltip()),
                    ((Option.Int) option).getValue(), ((Option.Int) option).getDefaultValue(),
                    ((Option.Int) option).getMin(), ((Option.Int) option).getMax(),
                    ListWidget.FunctionEnable.ON_CHANGED,
                    Resources.IDShort.RESET,
                    Resources.Translation.BUTTON_RESET,
                    Resources.Translation.BUTTON_RESET,
                    entry -> {
                        entry.focusOn(entry.children().get(0));
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
        this.addSelectableChild(this.optionsList);

        this.addDrawableChild(ButtonWidget.builder(Resources.Translation.BUTTON_CANCEL, (button) ->
            Objects.requireNonNull(this.client).setScreen(this.parent)
        ).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Resources.Translation.BUTTON_SAVE_EXIT, (button) -> this.save()
        ).dimensions(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
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
                    Resources.Translation.CONFIRM_SAVE_TITLE,
                    choice -> {
                        if (choice == ConfirmScreen.Choice.YES) this.save();
                        else Objects.requireNonNull(this.client).setScreen(this.parent);
                    }));
        else
            Objects.requireNonNull(this.client).setScreen(this.parent);
    }

    public void save() {
        if (this.optionsList.hasChanged()) {
            this.optionsList.save();
            if (!Config.IO.saveConfig())
                Objects.requireNonNull(this.client).getToastManager().add(new MessageToast(
                        this.client,
                        Resources.Translation.insert(Resources.Translation.TOAST_SAVE_FAILED, "config"),
                        MessageToast.Level.ERROR
                ));
        }
        Objects.requireNonNull(this.client).setScreen(this.parent);
    }
}

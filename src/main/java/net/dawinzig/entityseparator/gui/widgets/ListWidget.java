package net.dawinzig.entityseparator.gui.widgets;

import com.google.common.collect.ImmutableList;
import net.dawinzig.entityseparator.Resources;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ListWidget extends ContainerObjectSelectionList<ListWidget.Entry<?>> {
    final Screen parent;

    public ListWidget(Screen parent, Minecraft client) {
        super(client, parent.width, parent.height - 72, 32, 21);
        this.parent = parent;
    }

    public void addEntry(Component entryName, Component tooltipText, Boolean initialValue, Boolean defaultValue,
                         FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip,
                         Component functionNarration, Consumer<Entry<Boolean>> functionConsumer,
                         Consumer<Entry<Boolean>> saveConsumer, Consumer<Entry<Boolean>> changeConsumer) {
        super.addEntry(new BooleanEntry(entryName, tooltipText, initialValue, defaultValue, functionEnable,
                functionIcon, functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer));
    }

    public void addEntry(Component entryName, Component tooltipText, int initialValue, int defaultValue, int min, int max,
                         FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip,
                         Component functionNarration, Consumer<Entry<Integer>> functionConsumer,
                         Consumer<Entry<Integer>> saveConsumer, Consumer<Entry<Integer>> changeConsumer) {
        super.addEntry(new IntEntry(entryName, tooltipText, initialValue, defaultValue, min, max, functionEnable,
                functionIcon, functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer));
    }

    public void addEntry(Component entryName, Component tooltipText, String initialValue, String defaultValue,
                         FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip,
                         Component functionNarration, Consumer<Entry<String>> functionConsumer,
                         Consumer<Entry<String>> saveConsumer, Consumer<Entry<String>> changeConsumer) {
        super.addEntry(new StringEntry(entryName, tooltipText, initialValue, defaultValue, functionEnable,
                functionIcon, functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer));
    }

    public void addEntry(Component entryName, Component tooltipText, FunctionEnable functionEnable, ResourceLocation identifier,
                         Component functionTooltip, Component functionNarration, Consumer<Entry<Boolean>> functionConsumer) {
        super.addEntry(new PlainEntry(entryName, tooltipText, functionEnable, identifier, functionTooltip,
                functionNarration, functionConsumer));
    }

    public void addHeader(Component title, Component tooltipText) {
        super.addEntry(new CategoryEntry(title, tooltipText));
    }

    public void save() {
        this.children().forEach(Entry::save);
    }

    public boolean hasChanged() {
        for (int i = 0; i < children().size(); i++) {
            if (children().get(i).hasChanged()) return true;
        }
        return false;
    }

    @Override
    public int getRowWidth() {
        return parent.width - 32;
    }

    @Override
    public int getRowLeft() {
        return 16;
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 7));
    }

    @Override
    protected int getScrollbarPosition() {
        return parent.width - 9;
    }

    public void update() {
        this.setSize(parent.width, parent.height - 72);
        if (this.getScrollAmount() > this.getMaxScroll()) this.setScrollAmount(this.getMaxScroll());
    }

    public void clear() {
        this.children().clear();
    }

    @Environment(EnvType.CLIENT)
    public class StringEntry extends Entry<String> {
        private final EditBox textField;

        StringEntry(Component entryName, Component tooltipText, String initialValue, String defaultValue,
                    FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip,
                    Component functionNarration, Consumer<Entry<String>> functionConsumer,
                    Consumer<Entry<String>> saveConsumer, Consumer<Entry<String>> changeConsumer) {
            super(ListWidget.this, entryName, tooltipText, initialValue, defaultValue, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer);

            this.textField = new EditBox(ListWidget.this.minecraft.font, 0, 0, this.mainWidth - 2, 18, entryName);
            this.textField.setMaxLength(Integer.MAX_VALUE);
            this.textField.setValue(value);
            this.textField.moveCursorToStart(false);
            this.textField.setResponder(s -> {
                this.value = s;
                this.update(false);
            });

            this.update();
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            this.textField.setX(ListWidget.this.getRowRight() - 23 - this.mainWidth + 1);
            this.textField.setY(y + 1);
            this.textField.render(context, mouseX, mouseY, tickDelta);
        }

        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.textField, this.functionButton);
        }
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.textField, this.functionButton);
        }

        @Override
        protected void update() {
            this.update(true);
        }
        private  void update(boolean includeTextField) {
            if (includeTextField)
                this.textField.setValue(this.value);

            super.update();
        }
    }

    @Environment(EnvType.CLIENT)
    public class IntEntry extends Entry<Integer> {
        private final IntSlider slider;

        IntEntry(Component entryName, Component tooltipText, int initialValue, int defaultValue, int min, int max,
                 FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip,
                 Component functionNarration, Consumer<Entry<Integer>> functionConsumer,
                 Consumer<Entry<Integer>> saveConsumer, Consumer<Entry<Integer>> changeConsumer) {
            super(ListWidget.this, entryName, tooltipText, initialValue, defaultValue, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer);

            this.slider = new IntSlider(0, 0, this.mainWidth, 20, this.value, min, max);

            this.update();
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            this.slider.setX(ListWidget.this.getRowRight() - 23 - this.mainWidth);
            this.slider.setY(y);
            this.slider.render(context, mouseX, mouseY, tickDelta);
        }

        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.slider, this.functionButton);
        }
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.slider, this.functionButton);
        }

        @Override
        protected void update() {
            this.slider.setValue(this.value);

            super.update();
        }

        protected class IntSlider extends AbstractSliderButton {
            private final int min;
            private final int max;

            public IntSlider(int x, int y, int width, int height, int value, int min, int max) {
                super(x, y, width, height, Component.empty(), min);

                this.min = min;
                this.max = max;

                this.setValue(value);
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.nullToEmpty(String.valueOf(IntEntry.this.getValue())));
            }

            @Override
            protected void applyValue() {
                IntEntry.this.value = this.min + (int) ((this.max - this.min) * this.value);
                IntEntry.this.update();
            }

            protected void setValue(int value) {
                this.value = (double) (value - this.min) / (this.max - this.min);
                this.updateMessage();
            }

            @Override
            protected @NotNull MutableComponent createNarrationMessage() {
                return MutableComponent.create(Resources.Translation.insert(Resources.Translation.SLIDER_NARRATOR,
                        IntEntry.this.entryName, this.getMessage()).getContents());
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public class BooleanEntry extends Entry<Boolean> {
        private final Button toggleButton;
        private MutableComponent toggleNarration;

        BooleanEntry(Component entryName, Component tooltipText, boolean initialValue, boolean defaultValue,
                     FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip,
                     Component functionNarration, Consumer<Entry<Boolean>> functionConsumer,
                     Consumer<Entry<Boolean>> saveConsumer, Consumer<Entry<Boolean>> changeConsumer) {
            super(ListWidget.this, entryName, tooltipText, initialValue, defaultValue, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer);

            this.toggleButton = Button.builder(this.entryName, button -> {
                this.value = !this.value;
                this.update();
            }).bounds(0, 0, this.mainWidth, 20).createNarration(
                    textSupplier -> toggleNarration
            ).build();

            this.update();
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            this.toggleButton.setX(ListWidget.this.getRowRight() - 23 - this.mainWidth);
            this.toggleButton.setY(y);
            this.toggleButton.render(context, mouseX, mouseY, tickDelta);
        }

        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.toggleButton, this.functionButton);
        }

        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.toggleButton, this.functionButton);
        }

        @Override
        protected void update() {
            if (this.value)
                this.toggleButton.setMessage(((MutableComponent) CommonComponents.OPTION_ON).withStyle(ChatFormatting.GREEN));
            else
                this.toggleButton.setMessage(((MutableComponent) CommonComponents.OPTION_OFF).withStyle(ChatFormatting.RED));

            this.toggleNarration = MutableComponent.create(Resources.Translation.insert(Resources.Translation.TOGGLE_NARRATOR,
                    this.entryName, this.toggleButton.getMessage()).getContents());

            super.update();
        }
    }

    @Environment(EnvType.CLIENT)
    public class PlainEntry extends Entry<Boolean> {
        PlainEntry(Component entryName, Component tooltipText, FunctionEnable functionEnable, ResourceLocation functionIcon,
                   Component functionTooltip, Component functionNarration, Consumer<Entry<Boolean>> functionConsumer) {
            super(ListWidget.this, entryName, tooltipText, true, true, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, entry -> {}, entry -> {});

            this.update();
        }

        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.functionButton);
        }

        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.functionButton);
        }
    }

    @Environment(EnvType.CLIENT)
    public class CategoryEntry extends Entry<Boolean> {
        CategoryEntry(Component entryName, Component tooltipText) {
            super(ListWidget.this, entryName, tooltipText, true, true, cat -> {}, cat -> {});
        }

        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of();
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            context.hLine(x+3, x+entryWidth/2-this.label.getWidth()/2-6, y+entryHeight/2+1, FastColor.ARGB32.color(255,128,128,128));
            context.hLine(x+entryWidth/2+this.label.getWidth()/2+6, x+entryWidth-3, y+entryHeight/2+1, FastColor.ARGB32.color(255,128,128,128));
        }

        @Override
        protected void highlight(GuiGraphics context, int y, int x, int entryWidth, int entryHeight, int color) {}
    }

    public enum FunctionEnable {
        ENABLED, ON_CHANGED, DISABLED
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry<T> extends ContainerObjectSelectionList.Entry<Entry<?>> {
        final int mainWidth = 200;

        private final ListWidget parent;
        final StringWidget label;
        final Component entryName;
        final T initialValue;
        final T defaultValue;
        final Button functionButton;
        final Component functionTooltip;
        final FunctionEnable functionEnable;
        final Consumer<Entry<T>> saveConsumer;
        final Consumer<Entry<T>> changeConsumer;

        private boolean valid = true;
        T value;

        protected Entry(ListWidget parent, Component entryName, Component tooltipText, T initialValue, T defaultValue,
                        FunctionEnable functionEnable, ResourceLocation functionIcon, Component functionTooltip, Component functionNarration,
                        Consumer<Entry<T>> functionConsumer, Consumer<Entry<T>> saveConsumer, Consumer<Entry<T>> changeConsumer) {
            this.parent = parent;
            this.entryName = entryName;
            this.initialValue = initialValue;
            this.defaultValue = defaultValue;
            this.functionTooltip = functionTooltip;
            this.functionEnable = functionEnable;
            this.saveConsumer = saveConsumer;
            this.changeConsumer = changeConsumer;
            this.value = initialValue;

            this.label = new StringWidget(entryName, parent.minecraft.font);
            label.setTooltip(Tooltip.create(tooltipText != null ? tooltipText : Component.nullToEmpty("")));

            this.functionButton = new IconButtonWidget(20, 20, functionIcon, 16, 16,
                    button -> functionConsumer.accept(this), entryName.copy().append(" ").append(functionNarration));

            if (this.functionEnable == FunctionEnable.ENABLED)
                this.functionButton.setTooltip(Tooltip.create(this.functionTooltip, Component.nullToEmpty("")));
            else if (this.functionEnable == FunctionEnable.DISABLED)
                this.functionButton.active = false;
        }
        protected Entry(ListWidget parent, Component entryName, Component tooltipText, T initialValue, T defaultValue,
                        Consumer<Entry<T>> saveConsumer, Consumer<Entry<T>> changeConsumer) {
            this.parent = parent;
            this.entryName = entryName;
            this.initialValue = initialValue;
            this.defaultValue = defaultValue;
            this.functionTooltip = null;
            this.functionEnable = null;
            this.saveConsumer = saveConsumer;
            this.changeConsumer = changeConsumer;
            this.value = initialValue;

            this.label = new StringWidget(entryName, parent.minecraft.font);
            this.label.setTooltip(tooltipText != null ? Tooltip.create(tooltipText) : null);

            this.functionButton = null;
        }

        public T getValue() {
            return this.value;
        }

        public boolean hasChanged() {
            return !this.initialValue.equals(this.value);
        }

        public boolean isDefault() {
            return this.defaultValue.equals(this.value);
        }

        public void reset() {
            this.value = this.defaultValue;
            this.update();
        }

        public void save() {
            this.saveConsumer.accept(this);
        }

        protected void update() {
            if (this.functionButton != null && this.functionEnable == FunctionEnable.ON_CHANGED) {
                this.functionButton.active = !this.isDefault();
                this.functionButton.setTooltip(Tooltip.create(isDefault() ? Component.nullToEmpty("") : this.functionTooltip, Component.nullToEmpty("")));
            }

            this.changeConsumer.accept(this);
        }

        protected void highlight(GuiGraphics context, int y, int x, int entryWidth, int entryHeight, int color) {
            context.renderOutline(x - 1, y - 1, entryWidth + 2, entryHeight + 5, color);
        }

        public void setValid(boolean valid) { this.valid = valid; }
        public boolean isValid() { return this.valid; }

        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.isMouseOver(mouseX, mouseY))
                this.highlight(context, y, x, entryWidth, entryHeight, -12303292);

            int labelX = x + 4;
            if (this.functionButton == null)  labelX = x + entryWidth/2 - this.label.getWidth()/2;

            this.label.setX(labelX);
            this.label.setY(y + entryHeight/2 - 2);

            if (label.getTooltip() != null && !label.getTooltip().toCharSequence(parent.minecraft).isEmpty())
                context.hLine(
                        labelX, labelX+this.label.getWidth(), y+entryHeight/2+7,
                        FastColor.ARGB32.color(70, 255,255,255));

            if (!this.valid) {
                this.label.setColor(-65530);
                this.highlight(context, y, x, entryWidth, entryHeight, -65530);
            }
            else this.label.setColor(-1);

            this.label.render(context, mouseX, mouseY, tickDelta);

            if (this.functionButton != null) {
                this.functionButton.setX(this.parent.getRowRight() - 20);
                this.functionButton.setY(y);
                this.functionButton.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}

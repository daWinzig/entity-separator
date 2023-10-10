package net.dawinzig.entityseparator.gui.widgets;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.function.Consumer;

public class ListWidget extends ElementListWidget<ListWidget.Entry<?>> {
    final Screen parent;

    public ListWidget(Screen parent, MinecraftClient client) {
        super(client, parent.width, parent.height, 32, parent.height - 32, 21);
        this.parent = parent;
    }

    public void addEntry(Text entryName, Text tooltipText, Boolean initialValue, Boolean defaultValue,
                         FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip,
                         Text functionNarration, Consumer<Entry<Boolean>> functionConsumer,
                         Consumer<Entry<Boolean>> saveConsumer, Consumer<Entry<Boolean>> changeConsumer) {
        super.addEntry(new BooleanEntry(entryName, tooltipText, initialValue, defaultValue, functionEnable,
                functionIcon, functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer));
    }

    public void addEntry(Text entryName, Text tooltipText, int initialValue, int defaultValue, int min, int max,
                         FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip,
                         Text functionNarration, Consumer<Entry<Integer>> functionConsumer,
                         Consumer<Entry<Integer>> saveConsumer, Consumer<Entry<Integer>> changeConsumer) {
        super.addEntry(new IntEntry(entryName, tooltipText, initialValue, defaultValue, min, max, functionEnable,
                functionIcon, functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer));
    }

    public void addEntry(Text entryName, Text tooltipText, String initialValue, String defaultValue,
                         FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip,
                         Text functionNarration, Consumer<Entry<String>> functionConsumer,
                         Consumer<Entry<String>> saveConsumer, Consumer<Entry<String>> changeConsumer) {
        super.addEntry(new StringEntry(entryName, tooltipText, initialValue, defaultValue, functionEnable,
                functionIcon, functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer));
    }

    public void addEntry(Text entryName, Text tooltipText, FunctionEnable functionEnable, Identifier identifier,
                         Text functionTooltip, Text functionNarration, Consumer<Entry<Boolean>> functionConsumer) {
        super.addEntry(new PlainEntry(entryName, tooltipText, functionEnable, identifier, functionTooltip,
                functionNarration, functionConsumer));
    }

    public void addHeader(Text title, Text tooltipText) {
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
    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 7));
    }

    @Override
    protected int getScrollbarPositionX() {
        return parent.width - 9;
    }

    public void update() {
        this.updateSize(parent.width, parent.height, 32, parent.height - 32);
        if (this.getScrollAmount() > this.getMaxScroll()) this.setScrollAmount(this.getMaxScroll());
    }

    public void clear() {
        this.children().clear();
    }

    @Environment(EnvType.CLIENT)
    public class StringEntry extends ListWidget.Entry<String> {
        private final TextFieldWidget textField;

        StringEntry(Text entryName, Text tooltipText, String initialValue, String defaultValue,
                    FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip,
                    Text functionNarration, Consumer<Entry<String>> functionConsumer,
                    Consumer<Entry<String>> saveConsumer, Consumer<Entry<String>> changeConsumer) {
            super(ListWidget.this, entryName, tooltipText, initialValue, defaultValue, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer);

            this.textField = new TextFieldWidget(ListWidget.this.client.textRenderer, this.mainWidth - 2, 18, entryName);
            this.textField.setMaxLength(Integer.MAX_VALUE);
            this.textField.setText(value);
            this.textField.setCursorToStart(false);
            this.textField.setChangedListener(s -> {
                this.value = s;
                this.update(false);
            });

            this.update();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            this.textField.setX(ListWidget.this.getRowRight() - 23 - this.mainWidth + 1);
            this.textField.setY(y + 1);
            this.textField.render(context, mouseX, mouseY, tickDelta);
        }

        public List<? extends Element> children() {
            return ImmutableList.of(this.textField, this.functionButton);
        }
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.textField, this.functionButton);
        }

        @Override
        protected void update() {
            this.update(true);
        }
        private  void update(boolean includeTextField) {
            if (includeTextField)
                this.textField.setText(this.value);

            super.update();
        }
    }

    @Environment(EnvType.CLIENT)
    public class IntEntry extends ListWidget.Entry<Integer> {
        private final IntSlider slider;

        IntEntry(Text entryName, Text tooltipText, int initialValue, int defaultValue, int min, int max,
                 FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip,
                 Text functionNarration, Consumer<Entry<Integer>> functionConsumer,
                 Consumer<Entry<Integer>> saveConsumer, Consumer<Entry<Integer>> changeConsumer) {
            super(ListWidget.this, entryName, tooltipText, initialValue, defaultValue, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer);

            this.slider = new IntSlider(0, 0, this.mainWidth, 20, this.value, min, max);

            this.update();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            this.slider.setX(ListWidget.this.getRowRight() - 23 - this.mainWidth);
            this.slider.setY(y);
            this.slider.render(context, mouseX, mouseY, tickDelta);
        }

        public List<? extends Element> children() {
            return ImmutableList.of(this.slider, this.functionButton);
        }
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.slider, this.functionButton);
        }

        @Override
        protected void update() {
            this.slider.setValue(this.value);

            super.update();
        }

        protected class IntSlider extends SliderWidget {
            private final int min;
            private final int max;

            public IntSlider(int x, int y, int width, int height, int value, int min, int max) {
                super(x, y, width, height, Text.empty(), min);

                this.min = min;
                this.max = max;

                this.setValue(value);
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Text.of(String.valueOf(IntEntry.this.getValue())));
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
            protected MutableText getNarrationMessage() {
                return Text.translatable("entityseparator.slider.narrator", IntEntry.this.entryName, this.getMessage());
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public class BooleanEntry extends ListWidget.Entry<Boolean> {
        private final ButtonWidget toggleButton;
        private MutableText toggleNarration;

        BooleanEntry(Text entryName, Text tooltipText, boolean initialValue, boolean defaultValue,
                     FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip,
                     Text functionNarration, Consumer<Entry<Boolean>> functionConsumer,
                     Consumer<Entry<Boolean>> saveConsumer, Consumer<Entry<Boolean>> changeConsumer) {
            super(ListWidget.this, entryName, tooltipText, initialValue, defaultValue, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, saveConsumer, changeConsumer);

            this.toggleButton = ButtonWidget.builder(this.entryName, button -> {
                this.value = !this.value;
                this.update();
            }).dimensions(0, 0, this.mainWidth, 20).narrationSupplier(
                    textSupplier -> toggleNarration
            ).build();

            this.update();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            this.toggleButton.setX(ListWidget.this.getRowRight() - 23 - this.mainWidth);
            this.toggleButton.setY(y);
            this.toggleButton.render(context, mouseX, mouseY, tickDelta);
        }

        public List<? extends Element> children() {
            return ImmutableList.of(this.toggleButton, this.functionButton);
        }

        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.toggleButton, this.functionButton);
        }

        @Override
        protected void update() {
            if (this.value)
                this.toggleButton.setMessage(((MutableText) ScreenTexts.ON).formatted(Formatting.GREEN));
            else
                this.toggleButton.setMessage(((MutableText) ScreenTexts.OFF).formatted(Formatting.RED));

            this.toggleNarration = Text.translatable("entityseparator.toggle.narrator", this.entryName, this.toggleButton.getMessage());

            super.update();
        }
    }

    @Environment(EnvType.CLIENT)
    public class PlainEntry extends ListWidget.Entry<Boolean> {
        PlainEntry(Text entryName, Text tooltipText, FunctionEnable functionEnable, Identifier functionIcon,
                   Text functionTooltip, Text functionNarration, Consumer<Entry<Boolean>> functionConsumer) {
            super(ListWidget.this, entryName, tooltipText, true, true, functionEnable, functionIcon,
                    functionTooltip, functionNarration, functionConsumer, entry -> {}, entry -> {});

            this.update();
        }

        public List<? extends Element> children() {
            return ImmutableList.of(this.functionButton);
        }

        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.functionButton);
        }
    }

    @Environment(EnvType.CLIENT)
    public class CategoryEntry extends ListWidget.Entry<Boolean> {
        CategoryEntry(Text entryName, Text tooltipText) {
            super(ListWidget.this, entryName, tooltipText, true, true, cat -> {}, cat -> {});
        }

        public List<? extends Element> children() {
            return ImmutableList.of();
        }
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            context.drawHorizontalLine(x+3, x+entryWidth/2-this.label.getWidth()/2-6, y+entryHeight/2+1, Colors.LIGHT_GRAY);
            context.drawHorizontalLine(x+entryWidth/2+this.label.getWidth()/2+6, x+entryWidth-3, y+entryHeight/2+1, Colors.LIGHT_GRAY);
        }

        @Override
        protected void highlight(DrawContext context, int y, int x, int entryWidth, int entryHeight, int color) {}
    }

    public enum FunctionEnable {
        ENABLED, ON_CHANGED, DISABLED
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry<T> extends ElementListWidget.Entry<Entry<?>> {
        final int mainWidth = 200;

        private final ListWidget parent;
        final TextWidget label;
        final Text entryName;
        final T initialValue;
        final T defaultValue;
        final ButtonWidget functionButton;
        final Text functionTooltip;
        final FunctionEnable functionEnable;
        final Consumer<Entry<T>> saveConsumer;
        final Consumer<Entry<T>> changeConsumer;

        private boolean valid = true;
        T value;

        protected Entry(ListWidget parent, Text entryName, Text tooltipText, T initialValue, T defaultValue,
                        FunctionEnable functionEnable, Identifier functionIcon, Text functionTooltip, Text functionNarration,
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

            this.label = new TextWidget(entryName, parent.client.textRenderer);
            label.setTooltip(Tooltip.of(tooltipText != null ? tooltipText : Text.of("")));

            this.functionButton = TextIconButtonWidget.builder(
                            entryName.copy().append(" ").append(functionNarration),
                            button -> functionConsumer.accept(this), true)
                    .texture(functionIcon, 16, 16)
                    .dimension(20, 20).build();

            if (this.functionEnable == FunctionEnable.ENABLED)
                this.functionButton.setTooltip(Tooltip.of(this.functionTooltip, Text.of("")));
            else if (this.functionEnable == FunctionEnable.DISABLED)
                this.functionButton.active = false;
        }
        protected Entry(ListWidget parent, Text entryName, Text tooltipText, T initialValue, T defaultValue,
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

            this.label = new TextWidget(entryName, parent.client.textRenderer);
            this.label.setTooltip(tooltipText != null ? Tooltip.of(tooltipText) : null);

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
                this.functionButton.setTooltip(Tooltip.of(isDefault() ? Text.of("") : this.functionTooltip, Text.of("")));
            }

            this.changeConsumer.accept(this);
        }

        protected void highlight(DrawContext context, int y, int x, int entryWidth, int entryHeight, int color) {
            context.drawBorder(x - 1, y - 1, entryWidth + 2, entryHeight + 5, color);
        }

        public void setValid(boolean valid) { this.valid = valid; }
        public boolean isValid() { return this.valid; }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.isMouseOver(mouseX, mouseY))
                this.highlight(context, y, x, entryWidth, entryHeight, -12303292);

            int labelX = x + 4;
            if (this.functionButton == null)  labelX = x + entryWidth/2 - this.label.getWidth()/2;

            this.label.setX(labelX);
            this.label.setY(y + entryHeight/2 - 2);

            if (label.getTooltip() != null && !label.getTooltip().getLines(parent.client).isEmpty())
                context.drawHorizontalLine(
                        labelX, labelX+this.label.getWidth(), y+entryHeight/2+7,
                        ColorHelper.Argb.getArgb(70, 255,255,255));

            if (!this.valid) {
                this.label.setTextColor(-65530);
                this.highlight(context, y, x, entryWidth, entryHeight, -65530);
            }
            else this.label.setTextColor(-1);

            this.label.render(context, mouseX, mouseY, tickDelta);

            if (this.functionButton != null) {
                this.functionButton.setX(this.parent.getRowRight() - 20);
                this.functionButton.setY(y);
                this.functionButton.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}

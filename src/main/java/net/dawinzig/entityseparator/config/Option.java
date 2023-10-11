package net.dawinzig.entityseparator.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class Option<T> {
    private final String displayName;
    private final String tooltip;
    private final T defaultValue;
    private T value;

    Option(@Nullable String displayName, @Nullable String tooltip, @Nullable T value, @Nullable T defaultValue) {
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public boolean isShownInGUI() {
        return this.displayName != null;
    }
    public String getDisplayName() {
        return this.displayName == null ? "" : this.displayName;
    }
    public String getTooltip() {
        return this.tooltip == null ? "" : this.tooltip;
    }

    public T getValue() {
        return this.value;
    }
    public void setValue(T value) {
        this.value = value;
    }
    public T getDefaultValue() {
        return this.defaultValue;
    }

    public abstract JsonElement asJson();
    public abstract void setValueFromJson(JsonElement json);

    public static class Category extends Option<Boolean> {
        private final Map<String, Option<?>> children = new LinkedHashMap<>();
        private int depth = 0;

        Category(@Nullable String displayName, @Nullable String tooltip) {
            super(displayName, tooltip, true, true);
        }

        public void addChild(String key, Option<?> option) {
            children.put(key, option);
            if (option instanceof Category) ((Category) option).depth = this.depth + 1;
        }
        public int getDepth() {
            return this.depth;
        }

        public Category getOrCreateCategory(String... key) {
            if (key.length == 0) return this;
            if (!children.containsKey(key[0])) this.addChild(key[0], new Category(null, null));
            return ((Category) children.get(key[0])).getOrCreateCategory(Arrays.copyOfRange(key, 1, key.length));
        }

        public boolean getValueOrDefault(boolean defaultValue, String... key) {
            Bool option = getBool(key);
            return option != null ? option.getValue() : defaultValue;
        }
        public int getValueOrDefault(int defaultValue, String... key) {
            Int option = getInt(key);
            return option != null ? option.getValue() : defaultValue;
        }
        public String getValueOrDefault(String defaultValue, String... key) {
            Str option = getStr(key);
            return option != null ? option.getValue() : defaultValue;
        }

        public Bool getBool(String... key) {
            if (key.length == 0) return null;
            if (key.length == 1 && children.get(key[0]) instanceof Bool) return (Bool) children.get(key[0]);
            if (children.containsKey(key[0]) && children.get(key[0]) instanceof Category)
                return ((Category) children.get(key[0])).getBool(Arrays.copyOfRange(key, 1, key.length));
            return null;
        }
        public Int getInt(String... key) {
            if (key.length == 0) return null;
            if (key.length == 1 && children.get(key[0]) instanceof Int) return (Int) children.get(key[0]);
            if (children.containsKey(key[0]) && children.get(key[0]) instanceof Category)
                return ((Category) children.get(key[0])).getInt(Arrays.copyOfRange(key, 1, key.length));
            return null;
        }
        public Str getStr(String... key) {
            if (key.length == 0) return null;
            if (key.length == 1 && children.get(key[0]) instanceof Str) return (Str) children.get(key[0]);
            if (children.containsKey(key[0]) && children.get(key[0]) instanceof Category)
                return ((Category) children.get(key[0])).getStr(Arrays.copyOfRange(key, 1, key.length));
            return null;
        }

        public void remove(String... key) {
            if (key.length == 0) return;
            if (key.length == 1) children.remove(key[0]);
            if (children.containsKey(key[0]) && children.get(key[0]) instanceof Category)
                ((Category) children.get(key[0])).remove(Arrays.copyOfRange(key, 1, key.length));
        }

        public void foreach(BiConsumer<String, Option<?>> consumer) {
            children.forEach(consumer);
        }

        @Override
        public JsonElement asJson() {
            JsonObject json = new JsonObject();
            children.forEach((key, value) -> json.add(key, value.asJson()));
            return json;
        }
        @Override
        public void setValueFromJson(JsonElement json) {}
    }

    public static class Int extends Option<Integer> {
        private final int min;
        private final int max;

        Int(@Nullable String displayName, @Nullable String tooltip, int value, int defaultValue, int min, int max) {
            super(displayName, tooltip, value, defaultValue);

            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return this.min;
        }
        public int getMax() {
            return this.max;
        }

        @Override
        public JsonElement asJson() {
            return new JsonPrimitive(this.getValue());
        }
        @Override
        public void setValueFromJson(JsonElement json) {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                this.setValue(json.getAsNumber().intValue());
            }
        }
    }

    public static class Bool extends Option<Boolean> {
        Bool(@Nullable String displayName, @Nullable String tooltip, boolean value, boolean defaultValue) {
            super(displayName, tooltip, value, defaultValue);
        }

        @Override
        public JsonElement asJson() {
            return new JsonPrimitive(this.getValue());
        }
        @Override
        public void setValueFromJson(JsonElement json) {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
                this.setValue(json.getAsBoolean());
            }
        }
    }

    public static class Str extends Option<String> {
        Str(@Nullable String displayName, @Nullable String tooltip, String value, String defaultValue) {
            super(displayName, tooltip, value, defaultValue);
        }

        @Override
        public JsonElement asJson() {
            return new JsonPrimitive(this.getValue());
        }
        @Override
        public void setValueFromJson(JsonElement json) {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                this.setValue(json.getAsString());
            }
        }
    }
}

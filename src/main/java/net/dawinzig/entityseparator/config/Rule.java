package net.dawinzig.entityseparator.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    private String name = "New Rule";
    private final HashSet<EntityType<?>> entityTypes = new HashSet<>();
    private NbtPathArgument.NbtPath path = null;
    private boolean inverted = false;
    private boolean compareMode = false;
    private final HashSet<Tag> compare = new HashSet<>();
    private int maxDistance = 32;
    private String labelPattern = "&6Entity matched!";
    private String texture = "";

    public Rule() {
        this.entityTypes.add(EntityType.GOAT);
        this.setPath("");
    }
    public Rule(CompoundTag nbtCompound) throws IllegalArgumentException {
        this.name = nbtCompound.getString("name");
        nbtCompound.getList("entity_types", Tag.TAG_STRING).forEach(id -> {
            Optional<EntityType<?>> entityType = EntityType.byString((id.getAsString()));
            entityType.ifPresent(this.entityTypes::add);
        });
        this.setPath(nbtCompound.getString("path"));
        this.maxDistance = nbtCompound.getInt("distance");
        this.labelPattern = nbtCompound.getString("pattern");
        if (nbtCompound.contains("compare")) {
            this.compare.addAll(((ListTag) Objects.requireNonNull(nbtCompound.get("compare"))));
            this.compareMode = true;
        }
        this.inverted = nbtCompound.getBoolean("inverted");
        if (nbtCompound.contains("texture"))
            this.texture = nbtCompound.getString("texture");

        if (!this.isValid())
            throw new IllegalArgumentException();
    }

    public CompoundTag asNbt() {
        CompoundTag nbtCompound = new CompoundTag();

        nbtCompound.putString("name", this.name);
        ListTag entityTypes = new ListTag();
        this.entityTypes.forEach(entityType -> entityTypes.add(StringTag.valueOf(EntityType.getKey(entityType).toString())));
        nbtCompound.put("entity_types", entityTypes);
        nbtCompound.putString("path", this.getPath());
        nbtCompound.putInt("distance", this.maxDistance);
        nbtCompound.putString("pattern", this.labelPattern);
        if (this.isCompareMode()) {
            ListTag nbtList = new ListTag();
            nbtList.addAll(this.compare);
            nbtCompound.put("compare", nbtList);
        }
        nbtCompound.putBoolean("inverted", this.inverted);
        if (!Objects.equals(this.texture, ""))
            nbtCompound.putString("texture", this.texture);

        return nbtCompound;
    }

    public boolean shouldAddNameTag(CompoundTag nbt, double d) {
        if (d <= this.maxDistance * this.maxDistance) {
            return this.matchNbt(nbt);
        }
        return false;
    }
    public boolean matchNbt(CompoundTag nbt) {
        Tag value;
        boolean result = false;
        try {
            value = path.get(nbt).get(0);
            if (this.compareMode) {
                for (Tag element : this.compare) {
                    if (value.equals(element)) {
                        result = true;
                        break;
                    }
                }
            } else result = true;
        } catch (CommandSyntaxException ignored) {}
        return this.inverted != result;
    }

    public boolean isValid() {
        return !Objects.equals(name, "") && !entityTypes.isEmpty() && path != null &&
                (!compareMode || !compare.isEmpty()) && !Objects.equals(labelPattern, "");
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean containsEntityType(EntityType<?> entityType) {
        return entityTypes.contains(entityType);
    }
    public String getEntityTypes() {
        String[] arr = new String[this.entityTypes.size()];
        Iterator<EntityType<?>> itt = this.entityTypes.iterator();
        for (int i = 0; i < this.entityTypes.size(); i++) {
            arr[i] = EntityType.getKey(itt.next()).toString();
        }
        return String.join("; ", arr);
    }
    public void setEntityTypes(String entityTypes) {
        this.entityTypes.clear();

        String[] arr = entityTypes.split(";");
        for (String s : arr) {
            ResourceLocation identifier = ResourceLocation.tryParse(s.trim());
            if (identifier != null) {
                Optional<EntityType<?>> entityType = EntityType.byString(identifier.toString());
                entityType.ifPresent(this.entityTypes::add);
            }
        }
    }
    public static boolean isValidEntityTypes(String entityTypes) {
        String[] arr = entityTypes.split(";");
        for (String s : arr) {
            ResourceLocation identifier = ResourceLocation.tryParse(s.trim());
            if (identifier != null) {
                Optional<EntityType<?>> entityType = EntityType.byString(identifier.toString());
                if (entityType.isEmpty())
                    return false;
            }
        }
        return true;
    }

    public boolean isInverted() {
        return this.inverted;
    }
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public String getPath() {
        return this.path.toString();
    }
    public void setPath(String path) {
        try {
            this.path = new NbtPathArgument().parse(new StringReader(path));
        } catch (CommandSyntaxException|StringIndexOutOfBoundsException ignored) {}
    }
    public static boolean isValidPath(String path) {
        try {
            new NbtPathArgument().parse(new StringReader(path));
        } catch (CommandSyntaxException|StringIndexOutOfBoundsException ignored) {
            return false;
        }
        return true;
    }

    public boolean isCompareMode() {
        return this.compareMode;
    }
    public String getCompare() {
        if (this.compare.isEmpty()) return "";
        String[] compares = new String[this.compare.size()];
        int i = 0;
        for (Tag nbtElement : this.compare) {
            compares[i] = NbtUtils.prettyPrint(nbtElement, true);
            i++;
        }
        return String.join("; ", compares);
    }
    public void setCompare(String comparesString) {
        if (Objects.equals(comparesString, "")) {
            this.compareMode = false;
            this.compare.clear();
            return;
        }
        this.compare.clear();
        String[] compares = comparesString.split(";");
        CompoundTag nbtCompound;
        for (String compare : compares) {
            try {
                nbtCompound = NbtUtils.snbtToStructure("{\"root\":" + compare + "}");
            } catch (CommandSyntaxException ignored) {
                continue;
            }
            this.compare.add(nbtCompound.get("root"));
        }
        this.compareMode = true;
    }
    public static boolean isValidCompare(String comparesString) {
        if (Objects.equals(comparesString, "")) return true;
        String[] compares = comparesString.split(";");
        for (String compare : compares) {
            try {
                NbtUtils.snbtToStructure("{\"root\":" + compare + "}");
            } catch (CommandSyntaxException ignored) {
                return false;
            }
        }
        return true;
    }

    public int getMaxDistance() {
        return maxDistance;
    }
    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public Component getLabel(CompoundTag nbt) {
        String labelText = this.labelPattern;

        Matcher placeholder = Pattern.compile("\\{([^{]*)}").matcher(labelText);
        while (placeholder.find()) {
            String replacement = "&c[Error]&r";
            try {
                replacement = new NbtPathArgument().parse(new StringReader(placeholder.group(1))).get(nbt).get(0).getAsString();
            } catch (CommandSyntaxException|StringIndexOutOfBoundsException ignored) {}
            labelText = labelText.replaceAll("\\{" + placeholder.group(1) + "}", replacement);
        }

        return Component.literal(labelText.replace('&', '§'));
    }
    public String getLabelPattern() {
        return labelPattern;
    }
    public void setLabelPattern(String pattern) {
        this.labelPattern = pattern;
    }

    public boolean hasTexture() {
        return !Objects.equals(this.texture, "");
    }
    public String getTexture() {
        return this.texture;
    }
    public void setTexture(String texture) {
        this.texture = texture;
    }

    public Rule copy() {
        Rule rule = new Rule();
        rule.name = this.name;
        rule.entityTypes.clear();
        rule.entityTypes.addAll(this.entityTypes);
        rule.path = this.path;
        rule.compareMode = this.compareMode;
        rule.inverted = this.inverted;
        rule.setCompare(this.getCompare());
        rule.maxDistance = this.maxDistance;
        rule.labelPattern = this.labelPattern;
        rule.texture = this.texture;
        return rule;
    }

    public boolean compare(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof Rule)) return false;

        if (!Objects.equals(this.name, ((Rule) other).name)) return false;
        if (!Objects.equals(this.entityTypes, ((Rule) other).entityTypes)) return false;
        if (!Objects.equals(this.getPath(), ((Rule) other).getPath())) return false;
        if (!Objects.equals(this.compareMode, ((Rule) other).compareMode)) return false;
        if (!Objects.equals(this.compare, ((Rule) other).compare)) return false;
        if (!Objects.equals(this.inverted, ((Rule) other).inverted)) return false;
        if (!Objects.equals(this.maxDistance, ((Rule) other).maxDistance)) return false;
        if (!Objects.equals(this.labelPattern, ((Rule) other).labelPattern)) return false;
        return Objects.equals(this.texture, ((Rule) other).texture);
    }
}

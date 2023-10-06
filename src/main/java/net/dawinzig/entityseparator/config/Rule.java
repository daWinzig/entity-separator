package net.dawinzig.entityseparator.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    private boolean enabled = false;
    private String name = "New Rule";
    private final HashSet<EntityType<?>> entityTypes = new HashSet<>();
    private NbtPathArgumentType.NbtPath path = null;
    private boolean compareMode = false;
    private final HashSet<NbtElement> compare = new HashSet<>();
    private int maxDistance = 32;
    private String labelPatternWithoutName = "&6Entity matched!";
    private String labelPatternWithName = "&6{name} matched!";
    private String texture = "";

    public Rule() {
        this.entityTypes.add(EntityType.GOAT);
        this.setPath("");
    }
    public Rule(NbtCompound nbtCompound) throws IllegalArgumentException {
        this.name = nbtCompound.getString("name");
        nbtCompound.getList("entity_types", NbtElement.STRING_TYPE).forEach(id -> {
            Optional<EntityType<?>> entityType = EntityType.get((id.asString()));
            entityType.ifPresent(this.entityTypes::add);
        });
        this.setPath(nbtCompound.getString("path"));
        this.maxDistance = nbtCompound.getInt("distance");
        this.labelPatternWithoutName = nbtCompound.getString("pattern");
        this.labelPatternWithName = nbtCompound.getString("pattern_name");
        if (nbtCompound.contains("compare")) {
            this.compare.addAll(((NbtList) Objects.requireNonNull(nbtCompound.get("compare"))));
            this.compareMode = true;
        }
        if (nbtCompound.contains("texture"))
            this.texture = nbtCompound.getString("texture");

        if (!this.isValid())
            throw new IllegalArgumentException();
    }

    public NbtCompound asNbt() {
        NbtCompound nbtCompound = new NbtCompound();

        nbtCompound.putString("name", this.name);
        NbtList entityTypes = new NbtList();
        this.entityTypes.forEach(entityType -> entityTypes.add(NbtString.of(EntityType.getId(entityType).toString())));
        nbtCompound.put("entity_types", entityTypes);
        nbtCompound.putString("path", this.getPath());
        nbtCompound.putInt("distance", this.maxDistance);
        nbtCompound.putString("pattern", this.labelPatternWithoutName);
        nbtCompound.putString("pattern_name", this.labelPatternWithName);
        if (this.isCompareMode()) {
            NbtList nbtList = new NbtList();
            nbtList.addAll(this.compare);
            nbtCompound.put("compare", nbtList);
        }
        if (!Objects.equals(this.texture, ""))
            nbtCompound.putString("texture", this.texture);

        return nbtCompound;
    }

    public boolean shouldRenderNameTag(NbtCompound nbt, double d) {
        if (d <= this.maxDistance * this.maxDistance) {
            return this.matchNbt(nbt);
        }
        return false;
    }
    public boolean matchNbt(NbtCompound nbt) {
        NbtElement value;
        try {
            value = path.get(nbt).get(0);
        } catch (CommandSyntaxException ignored) { return false; }
        if (this.compareMode) {
            for (NbtElement element : this.compare) {
                if (value == element) return true;
            }
        } else return true;
        return false;
    }

    public boolean isValid() {
        return !Objects.equals(name, "") && entityTypes.size() > 0 && path != null &&
                (!compareMode || !compare.isEmpty()) && !Objects.equals(labelPatternWithName, "") &&
                !Objects.equals(labelPatternWithoutName, "");
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
            arr[i] = EntityType.getId(itt.next()).toString();
        }
        return String.join("; ", arr);
    }
    public void setEntityTypes(String entityTypes) {
        this.entityTypes.clear();

        String[] arr = entityTypes.split(";");
        for (String s : arr) {
            Identifier identifier = Identifier.tryParse(s.trim());
            if (identifier != null) {
                Optional<EntityType<?>> entityType = EntityType.get(identifier.toString());
                entityType.ifPresent(this.entityTypes::add);
            }
        }
    }
    public static boolean isValidEntityTypes(String entityTypes) {
        String[] arr = entityTypes.split(";");
        for (String s : arr) {
            Identifier identifier = Identifier.tryParse(s.trim());
            if (identifier != null) {
                Optional<EntityType<?>> entityType = EntityType.get(identifier.toString());
                if (entityType.isEmpty())
                    return false;
            }
        }
        return true;
    }

    public String getPath() {
        return this.path.toString();
    }
    public boolean setPath(String path) {
        try {
            this.path = new NbtPathArgumentType().parse(new StringReader(path));
        } catch (CommandSyntaxException|StringIndexOutOfBoundsException ignored) {
            return false;
        }
        return true;
    }
    public static boolean isValidPath(String path) {
        try {
            new NbtPathArgumentType().parse(new StringReader(path));
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
        for (NbtElement nbtElement : this.compare) {
            compares[i] = NbtHelper.toFormattedString(nbtElement, true);
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
        NbtCompound nbtCompound;
        for (String compare : compares) {
            try {
                nbtCompound = NbtHelper.fromNbtProviderString("{\"root\":" + compare + "}");
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
                NbtHelper.fromNbtProviderString("{\"root\":" + compare + "}");
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

    public Text getLabel(Entity entity, NbtCompound nbt) {
        String labelText;

        if (entity.hasCustomName())
            labelText = this.labelPatternWithName.replace("{name}", Objects.requireNonNull(entity.getCustomName()).getString());
        else labelText = this.labelPatternWithoutName;

        Matcher placeholder = Pattern.compile("\\{([^{]*)}").matcher(labelText);
        while (placeholder.find()) {
            String replacement = "&c[Error]&r";
            try {
                replacement = new NbtPathArgumentType().parse(new StringReader(placeholder.group(1))).get(nbt).get(0).asString();
            } catch (CommandSyntaxException|StringIndexOutOfBoundsException ignored) {}
            labelText = labelText.replaceAll("\\{" + placeholder.group(1) + "}", replacement);
        }

        return Text.literal(labelText.replace('&', '§'));
    }
    public String getLabelPattern() {
        return labelPatternWithoutName;
    }
    public void setLabelPattern(String pattern) {
        this.labelPatternWithoutName = pattern;
    }
    public String getLabelPatternWithName() {
        return labelPatternWithName;
    }
    public void setLabelPatternWithName(String pattern) {
        this.labelPatternWithName = pattern;
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
}

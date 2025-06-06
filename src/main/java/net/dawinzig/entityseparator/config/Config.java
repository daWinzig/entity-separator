package net.dawinzig.entityseparator.config;

import com.google.gson.*;
import net.dawinzig.entityseparator.EntitySeparator;
import net.dawinzig.entityseparator.Resources;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class Config {
    public static final Config IO = new Config("entityseparator");
    public static final Map<Path, Rule> RULES = new TreeMap<>();
    public static final Set<Path> SAVE_FAILED = new HashSet<>();
    public static final Option.Category OPTIONS = new Option.Category(null, null);
    public static final HashMap<Path, Boolean> ENABLED = new HashMap<>();

    private static final String configFileName = "config.json";
    private static final String enabledFileName = "rules.json";
    private static final String rulesFolderName = "rules";
    private static final String ruleFileExtension = ".nbt";
    private static final String defaultRulesFolderName = "default";

    private final Path basePath;
    private final Path configFile;
    private final Path enabledFile;
    private final Path rulesPath;

    public Config(String baseDirectory) {
        this.basePath = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), baseDirectory);
        this.configFile = this.basePath.resolve(Config.configFileName);
        this.enabledFile = this.basePath.resolve(Config.enabledFileName);
        this.rulesPath = this.basePath.resolve(rulesFolderName);
    }

    private File getFolder(Path path) {
        File folder = path.toFile();
        if (!folder.exists())
            if (folder.mkdirs())
                EntitySeparator.LOGGER.info("created directory: {}", folder);
        return folder;
    }
    private File getFile(Path path, boolean createEmptyIfMissing) throws IOException {
        File folder = getFolder(path.getParent());
        File file = folder.toPath().resolve(path.getFileName()).toFile();
        if (createEmptyIfMissing && file.createNewFile())
            EntitySeparator.LOGGER.info("created file: {}", file);
        return file;
    }

    public boolean saveJson(File file, JsonElement json) {
        Path relPath = this.basePath.relativize(file.toPath());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String content = gson.toJson(json);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to save JSON: {}", relPath, e);
            return false;
        }
        EntitySeparator.LOGGER.info("Saved JSON: {}", relPath);
        return true;
    }
    public JsonElement loadJson(Path path) {
        Path relPath = this.basePath.relativize(path);
        JsonElement element;
        try {
            String content = Files.readString(this.getFile(path, true).toPath());
            element = JsonParser.parseString(content);
        } catch (IOException | JsonSyntaxException e) {
            EntitySeparator.LOGGER.error("Failed to load JSON: {}", relPath, e);
            return null;
        }
        EntitySeparator.LOGGER.info("Loaded JSON: {}", relPath);
        return element;
    }

    public boolean saveConfig() {
        return saveJson(this.configFile.toFile(), Config.OPTIONS.asJson());
    }
    public void loadConfig() {
        Config.generateDefaultConfig();

        JsonElement element = this.loadJson(configFile);

        if (element instanceof JsonObject) {
            Config.setOptionsIfPresent(Config.OPTIONS, (JsonObject) element);
            EntitySeparator.LOGGER.info("Apply changes to config!");
        }

        this.saveConfig();
    }
    private static void generateDefaultConfig() {
        Option.Category generalCategory = new Option.Category(Resources.Translation.OPTION_CATEGORY_GENERAL, null);
        Config.OPTIONS.addChild("general", generalCategory);
        generalCategory.addChild("regenerate", new Option.Bool(
                Resources.Translation.OPTION_GENERAL_REGENERATE, Resources.Translation.OPTION_GENERAL_REGENERATE_TOOLTIP, true, true));
        Option.Category copyCategory = new Option.Category(Resources.Translation.OPTION_CATEGORY_COPY, Resources.Translation.OPTION_CATEGORY_COPY_TOOLTIP);
        Config.OPTIONS.addChild("copy", copyCategory);
        copyCategory.addChild("active", new Option.Bool(
                Resources.Translation.OPTION_COPY_ACTIVE, null, true, true));
        copyCategory.addChild("tool", new Option.Str(
                Resources.Translation.OPTION_COPY_TOOL, Resources.Translation.OPTION_COPY_TOOL_TOOLTIP, "minecraft:diamond", "minecraft:diamond"));
////        TEMP hidden while none implemented
//        Option.Category easterEggCategory = new Option.Category(Resources.Translation.OPTION_CATEGORY_EASTEREGG, null);
//        Config.OPTIONS.addChild("easteregg", easterEggCategory);
//        easterEggCategory.addChild("active", new Option.Bool(
//                Resources.Translation.OPTION_EASTEREGG_ACTIVE, null, true, true));
    }
    private static void setOptionsIfPresent(Option.Category category, JsonObject json) {
        category.foreach((key, option) -> {
            if (json.has(key)) {
                if (option instanceof Option.Category && json.get(key).isJsonObject()) {
                    setOptionsIfPresent((Option.Category) option, json.getAsJsonObject(key));
                }
                else {
                    option.setValueFromJson(json.get(key));
                }
            }
        });
    }

    public void saveEnabled() {
        JsonObject json = new JsonObject();
        Config.ENABLED.forEach(((path, state) -> json.addProperty(Config.pathToString(path), state)));
        this.saveJson(this.enabledFile.toFile(), json);
    }
    public void loadEnabled() {
        JsonElement element = this.loadJson(this.enabledFile);

        if (!(element instanceof JsonObject))
            element = new JsonObject();

        for (Path relPath : Config.RULES.keySet()) {
            JsonElement child = ((JsonObject) element).get(Config.pathToString(relPath));
            if (child != null && child.isJsonPrimitive() && child.getAsJsonPrimitive().isBoolean())
                Config.ENABLED.put(relPath, child.getAsBoolean());
            else
                Config.ENABLED.put(relPath, false);
        }

        EntitySeparator.LOGGER.info("Apply changes to enabled rules!");
        this.saveEnabled();
    }

    public void openRulesFolder() {
        Util.getPlatform().openFile(getFolder(rulesPath));
    }

    public boolean saveRule(Path relPath, Rule rule) {
        try {
            NbtIo.writeCompressed(rule.asNbt(), this.getFile(rulesPath.resolve(relPath), false).toPath());
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to save rule: {}", relPath, e);
            return false;
        }
        Config.SAVE_FAILED.remove(relPath);
        EntitySeparator.LOGGER.info("Saved rule: {}", relPath);
        return true;
    }
    public Path getAvailiableRelRulePath(Path offset, String name) {
        getFolder(rulesPath);
        try {
            String fileName = name.toLowerCase(Locale.ROOT).replace(' ', '_');
            fileName = FileUtil.findAvailableName(this.rulesPath.resolve(offset), fileName, Config.ruleFileExtension);
            return offset.resolve(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String pathToString(Path path) {
        List<String> parts = new ArrayList<>();
        path.forEach(part -> parts.add(part.toString()));
        return String.join("/", parts);
    }

    public void loadAllRules() {
        if (Config.OPTIONS.getValueOrDefault(false, "regenerate"))
            this.generateDefaultRulesIfMissing();

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**"+Config.ruleFileExtension);
        try (Stream<Path> paths = Files.walk(Path.of(getFolder(rulesPath).toURI()))) {
            paths.forEach(path -> {
                if (pathMatcher.matches(path)) {
                    File file = path.toFile();
                    if (file.isFile()) loadRule(rulesPath.relativize(path), file);
                }
            });
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to load rules", e);
        }
    }

    private void loadRule(Path relPath, File file) {
        try {
            Config.RULES.put(relPath, new Rule(NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap())));
        } catch (IOException | IllegalArgumentException e) {
            EntitySeparator.LOGGER.warn("Failed to load rule: {}", relPath, e);
        }
        Config.SAVE_FAILED.remove(relPath);
        EntitySeparator.LOGGER.info("Loaded rule: {}", relPath);
    }

    public void deleteRule(Path relPath) {
        try {
            Files.delete(rulesPath.resolve(relPath));
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to delete rule: {}", relPath, e);
        }
        Config.SAVE_FAILED.remove(relPath);
        Config.RULES.remove(relPath);
        Config.OPTIONS.remove("rules", pathToString(relPath));
        EntitySeparator.LOGGER.info("Deleted rule: {}", relPath);
    }

    public void generateDefaultRulesIfMissing() {
        for (DefaultRules defaultRule : DefaultRules.values()) {
            File file;
            try {
                file = this.getFile(this.rulesPath.resolve(defaultRule.relPath), false);
            } catch (IOException e) {
                EntitySeparator.LOGGER.error("Failed to generate default rule: {}", defaultRule.relPath, e);
                continue;
            }
            if (!file.isFile())
                this.saveRule(defaultRule.relPath, defaultRule.rule);
        }
    }

    public enum DefaultRules {
        SCREAMING_GOAT("screaming_goats", DefaultRules.getScreamingGoatRule()),
        PANDA_GENES("panda_genes", DefaultRules.getPandaGenesRule()),
        LLAMA_STRENGTH("llama_strength", DefaultRules.getLlamaStrengthRule());

        public final Path relPath;
        public final Rule rule;

        DefaultRules(String fileName, Rule rule) {
            this.relPath = Path.of(Config.defaultRulesFolderName).resolve(fileName + Config.ruleFileExtension);
            this.rule = rule;
        }

        private static Rule getScreamingGoatRule() {
            Rule rule = new Rule();
            rule.setName("Screaming Goat");
            rule.setEntityTypes("minecraft:goat");
            rule.setPath("IsScreamingGoat");
            rule.setCompare("1b");
            rule.setMaxDistance(32);
            rule.setLabelPattern("&6Screaming Goat");
            rule.setTexture("entityseparator:textures/entity/goat/screaming_goat.png");
            return rule;
        }
        public static Rule getPandaGenesRule() {
            Rule rule = new Rule();
            rule.setName("Panda Genes");
            rule.setEntityTypes("minecraft:panda");
            rule.setPath("");
            rule.setCompare("");
            rule.setMaxDistance(8);
            rule.setLabelPattern("&6({MainGene}, {HiddenGene})");
            return rule;
        }
        public static Rule getLlamaStrengthRule() {
            Rule rule = new Rule();
            rule.setName("Llama Strength");
            rule.setEntityTypes("minecraft:llama");
            rule.setPath("Strength");
            rule.setCompare("4; 5");
            rule.setMaxDistance(32);
            rule.setLabelPattern("&6Strength: {Strength}");
            return rule;
        }
    }
}

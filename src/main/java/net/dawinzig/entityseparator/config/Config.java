package net.dawinzig.entityseparator.config;

import com.google.gson.*;
import net.dawinzig.entityseparator.EntitySeparator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;

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

    private static final String configFileName = "config.json";
    private static final String rulesFolderName = "rules";
    private static final String ruleFileExtension = ".nbt";
    private static final String defaultRulesFolderName = "default";

    private final Path configFile;
    private final Path rulesPath;

    public Config(String baseDirectory) {
        Path basePath = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), baseDirectory);
        this.configFile = basePath.resolve(Config.configFileName);
        this.rulesPath = basePath.resolve(rulesFolderName);
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

    public boolean saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String content = gson.toJson(Config.OPTIONS.asJson());
        try (FileWriter fileWriter = new FileWriter(this.configFile.toFile())) {
            fileWriter.write(content);
            EntitySeparator.LOGGER.info("Saved config!");
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to save config", e);
            return false;
        }
        return true;
    }

    public void loadConfig() {
        this.generateDefaultConfig();

        JsonElement element;
        try {
            String content = Files.readString(this.getFile(configFile, true).toPath());
            element = JsonParser.parseString(content);
        } catch (IOException | JsonSyntaxException e) {
            EntitySeparator.LOGGER.error("Failed to load config file", e);
            return;
        }

        if (!(element instanceof JsonObject))
            element = new JsonObject();

        this.setOptionsIfPresent(Config.OPTIONS, (JsonObject) element);
        EntitySeparator.LOGGER.info("Loaded config!");
        this.saveConfig();
    }

    private void generateDefaultConfig() {
        Option.Category enabled = new Option.Category(null, null);
        Config.OPTIONS.addChild("rules", enabled);
        Config.RULES.forEach((relPath, rule) ->
            enabled.addChild(pathToString(relPath), new Option.Bool(null, null, false, false)));

        Option.Category hideDefault = new Option.Category("Hide Default Rules", null);
        Config.OPTIONS.addChild("hideDefault", hideDefault);
        for (DefaultRules defaultRule : DefaultRules.values()) {
            hideDefault.addChild(pathToString(defaultRule.relPath), new Option.Bool(
                    defaultRule.rule.getName(), defaultRule.relPath.toString(), false, false));
        }

//        Option.Category test_tt = new Option.Category("Hello World", "this is a test with tooltips");
//        Config.OPTIONS.addChild("test_tt", test_tt);
//        test_tt.addChild("bool", new Option.Bool("Bool Test", "boolean with tooltip", false, false));
//        test_tt.addChild("str", new Option.Str("Str Test", "string with tooltip", "Content", "Content"));
//        test_tt.addChild("int", new Option.Int("Int Test", "integer with tooltip", 4, 4, 0, 10));
//
//        Option.Category test = new Option.Category("Hello World", null);
//        Config.OPTIONS.addChild("test", test);
//        test.addChild("bool", new Option.Bool("Bool Test", null, false, false));
//        test.addChild("str", new Option.Str("Str Test", null, "Content", "Content"));
//        test.addChild("int", new Option.Int("Int Test", null, 4, 4, 0, 10));
    }

    private void setOptionsIfPresent(Option.Category category, JsonObject json) {
        category.foreach((key, option) -> {
            if (json.has(key)) {
                if (option instanceof Option.Category && json.get(key).isJsonObject())
                    setOptionsIfPresent((Option.Category) option, json.getAsJsonObject(key));
                option.setValueFromJson(json.get(key));
            }
        });
    }

    public void openRulesFolder() {
        Util.getOperatingSystem().open(getFolder(rulesPath));
    }

    public boolean saveRule(Path relPath, Rule rule) {
        try {
            NbtIo.writeCompressed(rule.asNbt(), this.getFile(rulesPath.resolve(relPath), false));
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
            fileName = PathUtil.getNextUniqueName(this.rulesPath.resolve(offset), fileName, Config.ruleFileExtension);
            fileName = fileName.replace(" ", "");
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

    public static boolean isRuleEnabled(Path relPath) {
        Option.Bool ruleOption = Config.OPTIONS.getBool("rules", pathToString(relPath));
        if (ruleOption == null) return false;
        return ruleOption.getValue();
    }
    public static void setRuleEnabled(Path relPath, boolean value) {
        Option.Bool ruleOption = Config.OPTIONS.getBool("rules", pathToString(relPath));
        if (ruleOption == null) {
            Config.OPTIONS.getOrCreateCategory("rules").addChild(
                    pathToString(relPath), new Option.Bool(null, null, value, false));
            return;
        }
        ruleOption.setValue(value);
    }

    public void loadAllRules() {
        Config.IO.generateDefaultRulesIfMissing();
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
            Config.RULES.put(relPath, new Rule(NbtIo.readCompressed(file)));
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

package net.dawinzig.entityseparator.config;

import net.dawinzig.entityseparator.EntitySeparator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.stream.Stream;

public class Config {
    private static final String fileExtension = ".nbt";
    private static final String rulesFolderName = "rules";
    private static final String defaultRulesFolderName = "default";

    private final Path basePath;
    private final Path rulesPath;

    public Config(String baseDirectory) {
        this.basePath = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), baseDirectory);
        this.rulesPath = this.basePath.resolve(rulesFolderName);
    }

    private File getFolder(Path path) {
        File folder = path.toFile();
        if (!folder.exists())
            if (folder.mkdirs())
                EntitySeparator.LOGGER.info("created directory: {}", folder);
        return folder;
    }
    private File getFile(Path path) {
        File folder = getFolder(path.getParent());
        return folder.toPath().resolve(path.getFileName()).toFile();
    }

    public void openRulesFolder() {
        Util.getOperatingSystem().open(getFolder(rulesPath));
    }

    public boolean saveRule(Path relPath, Rule rule) {
        try {
            NbtIo.writeCompressed(rule.asNbt(), this.getFile(rulesPath.resolve(relPath)));
            EntitySeparator.LOGGER.info("Saved rule: {}", relPath);
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to save rule: {}", relPath, e);
            return false;
        }
        return true;
    }
    public Path getAvailiableRelRulePath(Path offset, String name) {
        getFolder(rulesPath);
        try {
            String fileName = name.toLowerCase(Locale.ROOT).replace(' ', '_');
            fileName = PathUtil.getNextUniqueName(this.rulesPath.resolve(offset), fileName, Config.fileExtension);
            fileName = fileName.replace(" ", "");
            return offset.resolve(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadAllRules() {
        EntitySeparator.CONFIG.generateDefaultRulesIfMissing();
        try (Stream<Path> paths = Files.walk(Path.of(getFolder(rulesPath).toURI()))) {
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**"+Config.fileExtension);
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
            EntitySeparator.RULES.put(relPath, new Rule(NbtIo.readCompressed(file)));
            EntitySeparator.LOGGER.info("Loaded rule: {}", relPath);
        } catch (IOException | IllegalArgumentException e) {
            EntitySeparator.LOGGER.warn("Failed to load rule: {}", relPath, e);
        }
    }

    public boolean deleteRule(Path relPath) {
        try {
            Files.delete(rulesPath.resolve(relPath));
            EntitySeparator.RULES.remove(relPath);
            EntitySeparator.LOGGER.info("Deleted rule: {}", relPath);
            return true;
        } catch (IOException e) {
            EntitySeparator.LOGGER.error("Failed to delete rule: {}", relPath, e);
            return false;
        }
    }

    public void generateDefaultRulesIfMissing() {
        for (DefaultRules defaultRule : DefaultRules.values()) {
            if (!this.getFile(this.rulesPath.resolve(defaultRule.relPath)).isFile()) {
                this.saveRule(defaultRule.relPath, defaultRule.rule);
            }
        }
    }

    public enum DefaultRules {
        SCREAMING_GOAT("screaming_goats", DefaultRules.getScreamingGoatRule()),
        PANDA_GENES("panda_genes", DefaultRules.getPandaGenesRule()),
        LLAMA_STRENGTH("llama_strength", DefaultRules.getLlamaStrengthRule());

        public final Path relPath;
        public final Rule rule;

        DefaultRules(String fileName, Rule rule) {
            this.relPath = Path.of(Config.defaultRulesFolderName).resolve(fileName + Config.fileExtension);
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

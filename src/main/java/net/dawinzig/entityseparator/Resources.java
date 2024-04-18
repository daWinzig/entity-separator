package net.dawinzig.entityseparator;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class Resources {
    public static String MOD_ID = "entityseparator";
    public abstract static class Translation {
        // toasts
        public static final Text TOAST_RELOAD = Text.translatable(Resources.MOD_ID + ".toast.reload");
        public static final Text TOAST_SAVE_FAILED = Text.translatable(Resources.MOD_ID + ".toast.save.failed");

        // widgets
        public static final Text SLIDER_NARRATOR = Text.translatable(Resources.MOD_ID + ".slider.narrator");
        public static final Text TOGGLE_NARRATOR = Text.translatable(Resources.MOD_ID + ".toggle.narrator");

        // buttons
        public static final Text BUTTON_SAVE_EXIT = Text.translatable(Resources.MOD_ID + ".button.save.exit");
        public static final Text BUTTON_DONE = Text.translatable(Resources.MOD_ID + ".button.done");
        public static final Text BUTTON_CANCEL = Text.translatable(Resources.MOD_ID + ".button.cancel");
        public static final Text BUTTON_OPTIONS = Text.translatable(Resources.MOD_ID + ".button.options");
        public static final Text BUTTON_EDIT_OR_DELETE = Text.translatable(Resources.MOD_ID + ".button.edit_or_delete");
        public static final Text BUTTON_EDIT_OR_DELETE_NARRATOR = Text.translatable(Resources.MOD_ID + ".button.edit_or_delete.narrator");
        public static final Text BUTTON_RELOAD = Text.translatable(Resources.MOD_ID + ".button.reload");
        public static final Text BUTTON_RESET = Text.translatable(Resources.MOD_ID + ".button.reset");
        public static final Text BUTTON_NEW = Text.translatable(Resources.MOD_ID + ".button.new");
        public static final Text BUTTON_DELETE = Text.translatable(Resources.MOD_ID + ".button.delete");
        public static final Text BUTTON_RESTORE = Text.translatable(Resources.MOD_ID + ".button.restore");
        public static final Text BUTTON_OPEN = Text.translatable(Resources.MOD_ID + ".button.open");

        // edit screen
        public static final Text TITLE_NEW = Text.translatable(Resources.MOD_ID + ".title.add");
        public static final Text TITLE_EDIT = Text.translatable(Resources.MOD_ID + ".title.edit");
        // rule elements
        public static final Text RULE_NAME = Text.translatable(Resources.MOD_ID + ".rule.name");
        public static final Text RULE_ENTITIES = Text.translatable(Resources.MOD_ID + ".rule.entities");
        public static final Text RULE_PATH = Text.translatable(Resources.MOD_ID + ".rule.path");
        public static final Text RULE_COMPARE = Text.translatable(Resources.MOD_ID + ".rule.compare");
        public static final Text RULE_COMPARE_TOOLTIP = Text.translatable(Resources.MOD_ID + ".rule.compare.tooltip");
        public static final Text RULE_PATTERN = Text.translatable(Resources.MOD_ID + ".rule.pattern");
        public static final Text RULE_INVERTED = Text.translatable(Resources.MOD_ID + ".rule.inverted");
        public static final Text RULE_DISTANCE = Text.translatable(Resources.MOD_ID + ".rule.distance");
        public static final Text RULE_TEXTURE = Text.translatable(Resources.MOD_ID + ".rule.texture");
        public static final Text RULE_TEXTURE_TOOLTIP = Text.translatable(Resources.MOD_ID + ".rule.texture.tooltip");

        // rules screen
        public static final Text TITLE_RULES = Text.translatable(Resources.MOD_ID + ".title.rules");
        public static final Text RULES_CATEGORY_ON_DISK = Text.translatable(Resources.MOD_ID + ".rules.category.on_disk");
        public static final Text RULES_CATEGORY_ON_DISK_TOOLTIP = Text.translatable(Resources.MOD_ID + ".rules.category.on_disk.tooltip");
        public static final Text RULES_CATEGORY_CREATED = Text.translatable(Resources.MOD_ID + ".rules.category.created");
        public static final Text RULES_CATEGORY_CREATED_TOOLTIP = Text.translatable(Resources.MOD_ID + ".rules.category.created.tooltip");
        public static final Text RULES_CATEGORY_DELETED = Text.translatable(Resources.MOD_ID + ".rules.category.deleted");
        public static final Text RULES_CATEGORY_DELETED_TOOLTIP = Text.translatable(Resources.MOD_ID + ".rules.category.deleted.tooltip");

        // options screen
        public static final Text TITLE_OPTIONS = Text.translatable(Resources.MOD_ID + ".title.options");
        public static final Text OPTION_REGENERATE = Text.translatable(Resources.MOD_ID + ".option.regenerate");
        public static final Text OPTION_REGENERATE_TOOLTIP = Text.translatable(Resources.MOD_ID + ".option.regenerate.tooltip");
//        TEMP hidden while none implemented
//        public static final Text OPTION_EASTER_EGGS = Text.translatable(Resources.MOD_ID + ".option.easter_eggs");

        // confirm screen
        public static final Text CONFIRM_SAVE_TITLE = Text.translatable(Resources.MOD_ID + ".confirm.save.title");

        public static Text insert(Text main, Object... args) {
            for (int i = 0; i < args.length; i++)
                if (args[i] instanceof Text) args[i] = ((Text) args[i]).getString();

            return Text.of(main.getString().formatted(args));
        }
    }
    public abstract static class IDShort {
        // buttons
        public static final Identifier RESET = new Identifier(Resources.MOD_ID, "reset");
        public static final Identifier DELETE = new Identifier(Resources.MOD_ID, "delete");
        public static final Identifier RELOAD = new Identifier(Resources.MOD_ID, "reload");
        public static final Identifier ADD = new Identifier(Resources.MOD_ID, "add");
        public static final Identifier OPTIONS = new Identifier(Resources.MOD_ID, "options");
        public static final Identifier FOLDER = new Identifier(Resources.MOD_ID, "folder");
        public static final Identifier EDIT = new Identifier(Resources.MOD_ID, "edit");
        // toast
        public static final Identifier TOAST_BACKGROUND = new Identifier("toast/advancement");
    }
}

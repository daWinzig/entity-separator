package net.dawinzig.entityseparator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class Resources {
    public static String MOD_ID = "entityseparator";
    public abstract static class Translation {
        // toasts
        public static final Component TOAST_RELOAD = Component.translatable(Resources.MOD_ID + ".toast.reload");
        public static final Component TOAST_SAVE_FAILED = Component.translatable(Resources.MOD_ID + ".toast.save.failed");

        // widgets
        public static final Component SLIDER_NARRATOR = Component.translatable(Resources.MOD_ID + ".slider.narrator");
        public static final Component TOGGLE_NARRATOR = Component.translatable(Resources.MOD_ID + ".toggle.narrator");

        // buttons
        public static final Component BUTTON_SAVE_EXIT = Component.translatable(Resources.MOD_ID + ".button.save.exit");
        public static final Component BUTTON_DONE = Component.translatable(Resources.MOD_ID + ".button.done");
        public static final Component BUTTON_CANCEL = Component.translatable(Resources.MOD_ID + ".button.cancel");
        public static final Component BUTTON_OPTIONS = Component.translatable(Resources.MOD_ID + ".button.options");
        public static final Component BUTTON_EDIT_OR_DELETE = Component.translatable(Resources.MOD_ID + ".button.edit_or_delete");
        public static final Component BUTTON_EDIT_OR_DELETE_NARRATOR = Component.translatable(Resources.MOD_ID + ".button.edit_or_delete.narrator");
        public static final Component BUTTON_RELOAD = Component.translatable(Resources.MOD_ID + ".button.reload");
        public static final Component BUTTON_RESET = Component.translatable(Resources.MOD_ID + ".button.reset");
        public static final Component BUTTON_NEW = Component.translatable(Resources.MOD_ID + ".button.new");
        public static final Component BUTTON_DELETE = Component.translatable(Resources.MOD_ID + ".button.delete");
        public static final Component BUTTON_RESTORE = Component.translatable(Resources.MOD_ID + ".button.restore");
        public static final Component BUTTON_OPEN = Component.translatable(Resources.MOD_ID + ".button.open");

        // edit screen
        public static final Component TITLE_NEW = Component.translatable(Resources.MOD_ID + ".title.add");
        public static final Component TITLE_EDIT = Component.translatable(Resources.MOD_ID + ".title.edit");
        // rule elements
        public static final Component RULE_NAME = Component.translatable(Resources.MOD_ID + ".rule.name");
        public static final Component RULE_ENTITIES = Component.translatable(Resources.MOD_ID + ".rule.entities");
        public static final Component RULE_PATH = Component.translatable(Resources.MOD_ID + ".rule.path");
        public static final Component RULE_COMPARE = Component.translatable(Resources.MOD_ID + ".rule.compare");
        public static final Component RULE_COMPARE_TOOLTIP = Component.translatable(Resources.MOD_ID + ".rule.compare.tooltip");
        public static final Component RULE_PATTERN = Component.translatable(Resources.MOD_ID + ".rule.pattern");
        public static final Component RULE_INVERTED = Component.translatable(Resources.MOD_ID + ".rule.inverted");
        public static final Component RULE_DISTANCE = Component.translatable(Resources.MOD_ID + ".rule.distance");
        public static final Component RULE_TEXTURE = Component.translatable(Resources.MOD_ID + ".rule.texture");
        public static final Component RULE_TEXTURE_TOOLTIP = Component.translatable(Resources.MOD_ID + ".rule.texture.tooltip");

        // rules screen
        public static final Component TITLE_RULES = Component.translatable(Resources.MOD_ID + ".title.rules");
        public static final Component RULES_CATEGORY_ON_DISK = Component.translatable(Resources.MOD_ID + ".rules.category.on_disk");
        public static final Component RULES_CATEGORY_ON_DISK_TOOLTIP = Component.translatable(Resources.MOD_ID + ".rules.category.on_disk.tooltip");
        public static final Component RULES_CATEGORY_CREATED = Component.translatable(Resources.MOD_ID + ".rules.category.created");
        public static final Component RULES_CATEGORY_CREATED_TOOLTIP = Component.translatable(Resources.MOD_ID + ".rules.category.created.tooltip");
        public static final Component RULES_CATEGORY_DELETED = Component.translatable(Resources.MOD_ID + ".rules.category.deleted");
        public static final Component RULES_CATEGORY_DELETED_TOOLTIP = Component.translatable(Resources.MOD_ID + ".rules.category.deleted.tooltip");

        // options screen
        public static final Component TITLE_OPTIONS = Component.translatable(Resources.MOD_ID + ".title.options");
        public static final Component OPTION_REGENERATE = Component.translatable(Resources.MOD_ID + ".option.regenerate");
        public static final Component OPTION_REGENERATE_TOOLTIP = Component.translatable(Resources.MOD_ID + ".option.regenerate.tooltip");
//        TEMP hidden while none implemented
//        public static final Text OPTION_EASTER_EGGS = Text.translatable(Resources.MOD_ID + ".option.easter_eggs");

        // confirm screen
        public static final Component CONFIRM_SAVE_TITLE = Component.translatable(Resources.MOD_ID + ".confirm.save.title");

        public static Component insert(Component main, Object... args) {
            for (int i = 0; i < args.length; i++)
                if (args[i] instanceof Component) args[i] = ((Component) args[i]).getString();

            return Component.nullToEmpty(main.getString().formatted(args));
        }
    }
    public abstract static class IDShort {
        // buttons
        public static final ResourceLocation RESET = new ResourceLocation(Resources.MOD_ID, "reset");
        public static final ResourceLocation DELETE = new ResourceLocation(Resources.MOD_ID, "delete");
        public static final ResourceLocation RELOAD = new ResourceLocation(Resources.MOD_ID, "reload");
        public static final ResourceLocation ADD = new ResourceLocation(Resources.MOD_ID, "add");
        public static final ResourceLocation OPTIONS = new ResourceLocation(Resources.MOD_ID, "options");
        public static final ResourceLocation FOLDER = new ResourceLocation(Resources.MOD_ID, "folder");
        public static final ResourceLocation EDIT = new ResourceLocation(Resources.MOD_ID, "edit");
        // toast
        public static final ResourceLocation TOAST_BACKGROUND = new ResourceLocation("toast/advancement");
    }
}

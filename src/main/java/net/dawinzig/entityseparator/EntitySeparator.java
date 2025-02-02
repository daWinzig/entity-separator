package net.dawinzig.entityseparator;

import com.mojang.blaze3d.platform.ClipboardManager;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.gui.screens.RulesScreen;
import net.dawinzig.entityseparator.gui.toasts.MessageToast;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EntitySeparator implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("entityseparator");
	private static KeyMapping menuKeyBinding;
	private static KeyMapping toolKeyBinding;
	private static final ClipboardManager clipboard = new ClipboardManager();

	private UUID lastCopyTarget = null;
	private long lastCopyTime = 0;
	private boolean toolToggleWasDown = false;

	@Override
	public void onInitializeClient() {
		menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"entityseparator.key.menu",
				GLFW.GLFW_KEY_LEFT_ALT,
				"entityseparator.category.main"
		));
		toolKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"entityseparator.key.tool",
				-1,
				"entityseparator.category.main"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (menuKeyBinding.consumeClick()) {
				Minecraft.getInstance().setScreen(new RulesScreen(Minecraft.getInstance().screen));
			}

			if (toolKeyBinding.isDown() && !toolToggleWasDown) {
				boolean state = !Config.OPTIONS.getBool("copy","active").getValue();
				Config.OPTIONS.getBool("copy","active").setValue(state);
				Config.IO.saveConfig();
				toolToggleWasDown = true;
				// feedback
				Minecraft.getInstance().getToastManager().addToast(new MessageToast(
						Minecraft.getInstance(),
						Resources.Translation.insert(Resources.Translation.TOAST_TOOL_TOGGLE, state ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
						MessageToast.Level.INFO
				));
			} else if (!toolKeyBinding.isDown() && toolToggleWasDown) {
				toolToggleWasDown = false;
			}
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			// check active
			if (!Config.OPTIONS.getBool("copy","active").getValue())
				return InteractionResult.PASS;
			// check right tool item
			if (!(player.isLocalPlayer() && player.getItemInHand(hand).is(BuiltInRegistries.ITEM.getValue(
					ResourceLocation.parse(Config.OPTIONS.getValueOrDefault("minecraft:diamond", "copy", "tool"))))))
				return InteractionResult.PASS;
			// limit to once per second if target stays the same
			if (entity.getUUID() == lastCopyTarget && System.currentTimeMillis() - lastCopyTime < 1000)
				return InteractionResult.PASS;
			// copy snbt
			String snbt = NbtUtils.structureToSnbt(entity.saveWithoutId(new CompoundTag()));
			clipboard.setClipboard(snbt.length(),snbt);
			// Player feedback
			player.swing(hand);
			Minecraft.getInstance().getToastManager().addToast(new MessageToast(
					Minecraft.getInstance(),
					Resources.Translation.TOAST_COPIED,
					MessageToast.Level.INFO
			));
			// set restraints
			lastCopyTarget = entity.getUUID();
			lastCopyTime = System.currentTimeMillis();
			// cancel click event
			return InteractionResult.FAIL;
        });

		Config.IO.loadConfig();
		Config.IO.loadAllRules();
		Config.IO.loadEnabled();
	}
}

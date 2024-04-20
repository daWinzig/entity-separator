package net.dawinzig.entityseparator;

import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.gui.screens.RulesScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitySeparator implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("entityseparator");
	private static KeyMapping keyBinding;

	@Override
	public void onInitializeClient() {
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"entityseparator.key.menu",
				GLFW.GLFW_KEY_LEFT_ALT,
				"entityseparator.category.main"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.consumeClick()) {
				Minecraft.getInstance().setScreen(new RulesScreen(Minecraft.getInstance().screen));
			}
		});

		Config.IO.loadConfig();
		Config.IO.loadAllRules();
		Config.IO.loadEnabled();
	}
}

package net.dawinzig.entityseparator;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.dawinzig.entityseparator.gui.screens.RulesScreen;
import net.minecraft.client.MinecraftClient;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new RulesScreen(MinecraftClient.getInstance().currentScreen);
    }
}

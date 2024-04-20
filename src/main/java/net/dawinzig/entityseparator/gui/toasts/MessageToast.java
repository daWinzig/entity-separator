package net.dawinzig.entityseparator.gui.toasts;

import net.dawinzig.entityseparator.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import java.util.Objects;

public class MessageToast implements Toast {
    private final Minecraft client;
    private final Component text;
    private final Level level;

    public MessageToast(Minecraft client, Component text, Level level) {
        this.client = client;
        this.text = text;
        this.level = level;
    }

    @Override
    public int width() {
        return client.font.width(text) + 27;
    }

    @Override
    public Visibility render(GuiGraphics context, ToastComponent manager, long startTime) {
        context.blitSprite(Resources.IDShort.TOAST_BACKGROUND, 0, 0, this.width(), this.height());
        context.vLine(4, 3, this.height()-4, level.color);
        context.drawString(client.font, text, 15, 12, 16777215, false);
        int showTime = 2000;
        return startTime > showTime ? Visibility.HIDE : Visibility.SHOW;
    }

    public enum Level {
        INFO(FastColor.ARGB32.color(255,200,200,200)),
        ERROR(FastColor.ARGB32.color(255,255,50,50));

        public final int color;

        Level(Integer color) {
            this.color = Objects.requireNonNullElse(color, 11184810);
        }
    }
}

package net.dawinzig.entityseparator.gui.toasts;

import net.dawinzig.entityseparator.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.Objects;

public class MessageToast implements Toast {
    private final MinecraftClient client;
    private final Text text;
    private final Level level;

    public MessageToast(MinecraftClient client, Text text, Level level) {
        this.client = client;
        this.text = text;
        this.level = level;
    }

    @Override
    public int getWidth() {
        return client.textRenderer.getWidth(text) + 27;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(Resources.IDShort.TOAST_BACKGROUND, 0, 0, this.getWidth(), this.getHeight());
        context.drawVerticalLine(4, 3, this.getHeight()-4, level.color);
        context.drawText(client.textRenderer, text, 15, 12, 16777215, false);
        int showTime = 2000;
        return startTime > showTime ? Visibility.HIDE : Visibility.SHOW;
    }

    public enum Level {
        INFO(ColorHelper.Argb.getArgb(255,200,200,200)),
        ERROR(ColorHelper.Argb.getArgb(255,255,50,50));

        public final int color;

        Level(Integer color) {
            this.color = Objects.requireNonNullElse(color, 11184810);
        }
    }
}

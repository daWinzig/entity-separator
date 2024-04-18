package net.dawinzig.entityseparator.mixin;

import net.dawinzig.entityseparator.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin<T extends Entity> {
	@Shadow protected abstract void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta);
	@Shadow protected abstract boolean hasLabel(T entity);

	@ModifyVariable(method = "renderLabelIfPresent", at = @At("STORE"), ordinal = 0)
	private double d(double x) {
		return 0.0;
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		double d = MinecraftClient.getInstance().gameRenderer.getCamera().getPos()
				.squaredDistanceTo(entity.getPos());
		NbtCompound nbt = entity.writeNbt(new NbtCompound());

        List<Text> nameTagText = new ArrayList<>();
		Config.RULES.forEach((path, rule) -> {
			if (Config.ENABLED.getOrDefault(path, false) && rule.containsEntityType(entity.getType())) {
                if (rule.shouldAddNameTag(nbt, d)) {
                    nameTagText.add(rule.getLabel(nbt));
                    ci.cancel();
                }
			}
		});

		if (!nameTagText.isEmpty()) {
			if (this.hasLabel(entity))
				nameTagText.add(0, entity.getCustomName());

			matrices.push();
			for (int i = nameTagText.size() - 1; i >= 0; i--) {
				this.renderLabelIfPresent(entity, nameTagText.get(i), matrices, vertexConsumers, light, tickDelta);
				matrices.translate(0F, 0.25F, 0F);
			}
			matrices.pop();
		}

		if (d > 4096.0) ci.cancel();
	}
}
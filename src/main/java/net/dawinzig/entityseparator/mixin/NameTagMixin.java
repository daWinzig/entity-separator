package net.dawinzig.entityseparator.mixin;

import net.dawinzig.entityseparator.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin<T extends Entity> {
	@Shadow protected abstract void renderNameTag(T entity, Component text, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float tickDelta);
	@Shadow protected abstract boolean shouldShowName(T entity);

	@SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "renderNameTag", at = @At("STORE"), ordinal = 0)
	private double d(double x) {
		return 0.0;
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(T entity, float yaw, float tickDelta, PoseStack matrices,
					   MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
		double d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
				.distanceToSqr(entity.position());
		CompoundTag nbt = entity.saveWithoutId(new CompoundTag());

        List<Component> nameTagText = new ArrayList<>();
		Config.RULES.forEach((path, rule) -> {
			if (Config.ENABLED.getOrDefault(path, false) && rule.containsEntityType(entity.getType())) {
                if (rule.shouldAddNameTag(nbt, d)) {
                    nameTagText.add(rule.getLabel(nbt));
                    ci.cancel();
                }
			}
		});

		if (!nameTagText.isEmpty()) {
			if (this.shouldShowName(entity))
				nameTagText.addFirst(entity.getCustomName());

			matrices.pushPose();
			for (int i = nameTagText.size() - 1; i >= 0; i--) {
				this.renderNameTag(entity, nameTagText.get(i), matrices, vertexConsumers, light, tickDelta);
				matrices.translate(0F, 0.25F, 0F);
			}
			matrices.popPose();
		}

		if (d > 4096.0) ci.cancel();
	}
}
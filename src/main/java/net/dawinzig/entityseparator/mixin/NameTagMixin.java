package net.dawinzig.entityseparator.mixin;

import net.dawinzig.entityseparator.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin<T extends Entity, S extends EntityRenderState> {
	@Shadow protected abstract void renderNameTag(S entityRenderState, Component text, PoseStack poseStack, MultiBufferSource multiBufferSource, int light);
	@Shadow protected abstract boolean shouldShowName(T entity, double d);

	@Unique
	private T currentEntity = null;
	@Inject(at = @At("HEAD"), method = "affectedByCulling")
	private void getNameTag(T entity, CallbackInfoReturnable<Component> cir) {
		currentEntity = entity;
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(S entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
		T entity = currentEntity;
		if (entity == null) {
			return;
		}

		double d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(entity.position());

		// minecraft crashes if you try to read nbt from an arrow with an empty pickupItemStack (stuck in a player)
		if (entity instanceof AbstractArrow && ((AbstractArrow) entity).getPickupItemStackOrigin().isEmpty()) {
			ci.cancel();
			return;
		}

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
			if (this.shouldShowName(entity, d))
				nameTagText.addFirst(entity.getCustomName());

			poseStack.pushPose();
			for (int i = nameTagText.size() - 1; i >= 0; i--) {
				this.renderNameTag(entityRenderState, nameTagText.get(i), poseStack, multiBufferSource, light);
				poseStack.translate(0F, 0.25F, 0F);
			}
			poseStack.popPose();
		}

		if (d > 4096.0) ci.cancel();
	}
}
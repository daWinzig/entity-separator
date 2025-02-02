package net.dawinzig.entityseparator.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dawinzig.entityseparator.config.Config;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Unique
    Entity current = null;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V")
    private void renderInject(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EntityRenderer<? super Entity,EntityRenderState> entityRenderer, CallbackInfo ci) {
        current = entity;
    }

    @Redirect(method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void renderRenderRedirect(EntityRenderer<? super Entity,EntityRenderState> entityRenderer, EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        double d = entityRenderState.distanceToCameraSq;

        // minecraft crashes if you try to read nbt from an arrow with an empty pickupItemStack (stuck in a player)
        if (!(current instanceof AbstractArrow && ((AbstractArrow) current).getPickupItemStackOrigin().isEmpty())) {
            CompoundTag nbt = current.saveWithoutId(new CompoundTag());

            List<Component> nameTagText = new ArrayList<>();
            Config.RULES.forEach((path, rule) -> {
                if (Config.ENABLED.getOrDefault(path, false) && rule.containsEntityType(current.getType())) {
                    if (rule.shouldAddNameTag(nbt, d)) {
                        nameTagText.add(rule.getLabel(nbt));
                    }
                }
            });

            if (!nameTagText.isEmpty() && d < 4096) {
                poseStack.pushPose();
                if (((EntityRendererInvoker) entityRenderer).invokeShouldShowName(current, d)) {
                    poseStack.translate(0F, 0.25F, 0F);
                } else if (entityRenderState.nameTagAttachment == null) {
                    entityRenderState.nameTagAttachment = current.getAttachments().get(EntityAttachment.NAME_TAG, 0, 0);
                }
                for (int i = nameTagText.size() - 1; i >= 0; i--) {
                    ((EntityRendererInvoker) entityRenderer).invokeRenderNameTag(entityRenderState, nameTagText.get(i), poseStack, multiBufferSource, light);
                    poseStack.translate(0F, 0.25F, 0F);
                }
                poseStack.popPose();
            }
        }

        entityRenderer.render(entityRenderState, poseStack, multiBufferSource, light);
    }
}

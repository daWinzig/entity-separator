package net.dawinzig.entityseparator.mixin;

import net.dawinzig.entityseparator.EntitySeparator;
import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Rule;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(LivingEntityRenderer.class)
public abstract class TextureMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Unique
    private T currentEntity = null;

    @Inject(at = @At("HEAD"), method = "shouldShowName*")
    private void shouldShowNameX(T entity, double d, CallbackInfoReturnable<Boolean> cir) {
        currentEntity = entity;
    }

    @Redirect(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation getTextureLocation(LivingEntityRenderer<T, S, M> instance, S livingEntityRenderState) {
        EntitySeparator.LOGGER.info(String.valueOf(currentEntity));
        if (currentEntity != null) {
            for (Rule rule : Config.RULES.values()) {
                if (rule.containsEntityType(currentEntity.getType()) && rule.hasTexture()) {
                    CompoundTag nbt = currentEntity.saveWithoutId(new CompoundTag());
                    if (rule.matchNbt(nbt))
                        return ResourceLocation.tryParse(rule.getTexture());
                }
            }
        }
        return instance.getTextureLocation(livingEntityRenderState);
    }
}

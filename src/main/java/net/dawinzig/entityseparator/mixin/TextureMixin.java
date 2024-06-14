package net.dawinzig.entityseparator.mixin;

import net.dawinzig.entityseparator.config.Config;
import net.dawinzig.entityseparator.config.Rule;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(LivingEntityRenderer.class)
public abstract class TextureMixin<T extends LivingEntity> {
    @SuppressWarnings("unchecked")
    @Redirect(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation getTextureLocation(LivingEntityRenderer<T, EntityModel<T>> instance, Entity entity) {
        for(Rule rule : Config.RULES.values()) {
            if (rule.containsEntityType(entity.getType()) && rule.hasTexture()) {
                CompoundTag nbt = entity.saveWithoutId(new CompoundTag());
                if (rule.matchNbt(nbt))
                    return ResourceLocation.tryParse(rule.getTexture());
            }
        }
        return instance.getTextureLocation((T) entity);
    }
}

package net.dawinzig.entityseparator.mixin;

import net.dawinzig.entityseparator.EntitySeparator;
import net.dawinzig.entityseparator.config.Rule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(LivingEntityRenderer.class)
public abstract class TextureMixin<T extends LivingEntity> {
    @SuppressWarnings("unchecked")
    @Redirect(method = "getRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;"))
    private Identifier redirectExampleMethod(LivingEntityRenderer<T, EntityModel<T>> instance, Entity entity) {
        for(Rule rule : EntitySeparator.RULES.values()) {
            if (rule.containsEntityType(entity.getType()) && rule.hasTexture()) {
                NbtCompound nbt = entity.writeNbt(new NbtCompound());
                if (rule.matchNbt(nbt))
                    return new Identifier(rule.getTexture());
            }
        }
        return instance.getTexture((T) entity);
    }
}

package net.dawinzig.entityseparator.mixin;

import net.minecraft.client.render.entity.GoatEntityRenderer;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(GoatEntityRenderer.class)
public abstract class GoatTextureMixin {
    @Unique
    private static final Identifier TEXTURE_SCREAMING = new Identifier("goatseparator:textures/entity/goat/screaming_goat.png");

    @Inject(at = @At("HEAD"), method = "getTexture*", cancellable = true)
    public Identifier getTexture(GoatEntity goatEntity, CallbackInfoReturnable<Identifier> cir) {
        if (goatEntity.isScreaming()) {
            cir.setReturnValue(TEXTURE_SCREAMING);
        }
        return null;
    }
}

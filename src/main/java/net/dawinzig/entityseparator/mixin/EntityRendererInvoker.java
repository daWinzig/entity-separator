package net.dawinzig.entityseparator.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererInvoker {
    @Invoker("renderNameTag")
    void invokeRenderNameTag(EntityRenderState entityRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int light);

    @Invoker("shouldShowName")
    boolean invokeShouldShowName(Entity entity, double d);
}

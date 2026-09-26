package com.elesey3510.powerprojector;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SpotlightRenderer implements BlockEntityRenderer<SpotlightBlockEntity> {

    public SpotlightRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SpotlightBlockEntity be, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Передаем точное время между тиками (долю кадра)
        be.frameUpdate(partialTick);
    }
}
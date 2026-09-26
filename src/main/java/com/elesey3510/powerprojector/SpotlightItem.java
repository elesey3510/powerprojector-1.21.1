package com.elesey3510.powerprojector;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class SpotlightItem extends BlockItem {

    public SpotlightItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        if (Screen.hasShiftDown()) {
            // Раздел 1: Что это такое (Summary в стиле Create)
            tooltip.add(Component.translatable("tooltip.powerprojector.spotlight.summary")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.empty());

            // Раздел 2: Условие и поведение (Как в Create)
            tooltip.add(Component.translatable("tooltip.powerprojector.spotlight.condition")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(" → ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("tooltip.powerprojector.spotlight.behaviour")
                            .withStyle(ChatFormatting.GOLD)));
        } else {
            // Серая подсказка в стиле Create: «Зажмите Shift»
            tooltip.add(Component.translatable("tooltip.powerprojector.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
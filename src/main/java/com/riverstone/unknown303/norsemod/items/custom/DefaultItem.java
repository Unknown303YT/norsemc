package com.riverstone.unknown303.norsemod.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultItem extends Item {
    String name;
    int lines;
    public DefaultItem(String name, int lines, Properties properties) {
        super(properties);
        this.name = name;
        this.lines = lines;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (this.lines == 0) {
            return;
        }
        for (int i = 1; i <= this.lines; i++) {
            pTooltipComponents.add(Component.translatable("item.norsemc." + name + ".tooltip.line" + i));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}

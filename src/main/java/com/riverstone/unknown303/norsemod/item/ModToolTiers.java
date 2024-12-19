package com.riverstone.unknown303.norsemod.item;

import com.riverstone.unknown303.norsemod.NorseMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public class ModToolTiers {
    public static final Tier GODLY = TierSortingRegistry.registerTier(
            new ForgeTier(20, 1999999999, 15.0F, 7.5F, 0,
                    null, () -> Ingredient.EMPTY),
            new ResourceLocation(NorseMod.MOD_ID, "godly"), List.of(Tiers.NETHERITE), List.of());
}

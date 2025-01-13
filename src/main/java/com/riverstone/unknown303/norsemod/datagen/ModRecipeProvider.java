package com.riverstone.unknown303.norsemod.datagen;

import com.riverstone.unknown303.norsemod.NorseMod;
import com.riverstone.unknown303.norsemod.block.ModBlocks;
import com.riverstone.unknown303.norsemod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        oreBlockItem(pWriter, ModBlocks.BONE_STEEL_BLOCK, ModItems.BONE_STEEL);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pWriter, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pWriter, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pWriter, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pWriter, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pWriter, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                            pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pWriter,  NorseMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }

    protected static void oreBlockItem(Consumer<FinishedRecipe> pWriter, RegistryObject<Block> oreBlock, RegistryObject<Item> oreItem) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, oreItem.get(), 9)
                .requires(oreBlock.get()).group(oreItem.getId().getPath())
                .unlockedBy(getHasName(oreBlock.get()), has(oreBlock.get()))
                .save(pWriter, new ResourceLocation(NorseMod.MOD_ID,
                        oreItem.getId().getPath() + "_from_" + oreBlock.getId().getPath()));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, oreBlock.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', oreItem.get())
                .group(oreBlock.getId().getPath()).unlockedBy(getHasName(oreItem.get()), has(oreItem.get()))
                .save(pWriter);
    }
}

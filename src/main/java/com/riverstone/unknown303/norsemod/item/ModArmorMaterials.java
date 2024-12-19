package com.riverstone.unknown303.norsemod.item;

import com.riverstone.unknown303.errorlib.api.misc.CustomArmorMaterial;
import com.riverstone.unknown303.norsemod.NorseMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.Lazy;

import java.util.function.Supplier;

public enum ModArmorMaterials implements CustomArmorMaterial {
    BONE_STEEL("bone_steel", 46, new int[]{ 5, 11, 9, 5 }, 25,
            Lazy.of(() -> SoundEvents.ARMOR_EQUIP_NETHERITE), 4.0F, 0.2F, () -> Ingredient.EMPTY);

    private final ResourceLocation armorTypeId;
    private final int durabilityMultiplier;
    private final int[] protection;
    private final int enchantability;
    private final Lazy<SoundEvent> equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] BASE_DURABILITY = new int[]{ 11, 16, 15, 13};

    ModArmorMaterials(String armorTypeId, int durabilityMultiplier, int[] protection, int enchantability,
                      Lazy<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this(new ResourceLocation(NorseMod.MOD_ID, armorTypeId), durabilityMultiplier, protection, enchantability,
                equipSound, toughness, knockbackResistance, repairIngredient);
    }

    ModArmorMaterials(ResourceLocation armorTypeId, int durabilityMultiplier, int[] protection, int enchantability,
                      Lazy<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.armorTypeId = armorTypeId;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protection = protection;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public ResourceLocation getId() {
        return this.armorTypeId;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.ordinal()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.protection[type.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound.get();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}

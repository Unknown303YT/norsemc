package com.riverstone.unknown303.norsemod.item;

import com.riverstone.unknown303.norsemod.NorseMod;
import com.riverstone.unknown303.norsemod.item.custom.MjolnirItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NorseMod.MOD_ID);

    public static final RegistryObject<Item> MJOLNIR = ITEMS.register("mjolnir",
            () -> new MjolnirItem(ModToolTiers.GODLY, new Item.Properties().stacksTo(1)));

//    public static final RegistryObject<Item> BONE_STEEL_HORSE_ARMOR =
//            ModHelpers.HORSE_ARMOR_HELPER.registerHorseArmor(ModArmorMaterials.BONE_STEEL, new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

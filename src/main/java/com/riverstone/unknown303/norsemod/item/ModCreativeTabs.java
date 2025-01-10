package com.riverstone.unknown303.norsemod.item;

import com.riverstone.unknown303.errorlib.api.misc.MiscUtil;
import com.riverstone.unknown303.norsemod.NorseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NorseMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> NORSE_TAB = CREATIVE_TABS.register("norse_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MJOLNIR.get()))
                    .title(MiscUtil.createTranslatableComponent("creative_tab",
                            new ResourceLocation(NorseMod.MOD_ID, "norse_tab")))
                    .displayItems(((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.BONE_STEEL.get());
                        pOutput.accept(ModItems.MJOLNIR.get());
//                        pOutput.accept(ModItems.BONE_STEEL_HORSE_ARMOR.get());
                    })).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}

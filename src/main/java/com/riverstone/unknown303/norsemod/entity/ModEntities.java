package com.riverstone.unknown303.norsemod.entity;

import com.riverstone.unknown303.norsemod.NorseMod;
import com.riverstone.unknown303.norsemod.entity.custom.MjolnirProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NorseMod.MOD_ID);

    public static final RegistryObject<EntityType<MjolnirProjectileEntity>> MJOLNIR_PROJECTILE =
            ENTITY_TYPES.register("mjolnir_projectile", () -> EntityType.Builder.<MjolnirProjectileEntity>of(MjolnirProjectileEntity::new, MobCategory.MISC)
                    .sized(0.75F, 1.125F).build("mjolnir_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

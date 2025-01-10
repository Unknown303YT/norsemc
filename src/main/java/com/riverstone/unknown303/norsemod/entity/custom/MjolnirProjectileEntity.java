package com.riverstone.unknown303.norsemod.entity.custom;

import com.riverstone.unknown303.errorlib.api.misc.MiscUtil;
import com.riverstone.unknown303.norsemod.NorseMod;
import com.riverstone.unknown303.norsemod.entity.ModEntities;
import com.riverstone.unknown303.norsemod.item.ModItems;
import com.riverstone.unknown303.norsemod.item.custom.MjolnirItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Random;
import java.util.List;

public class MjolnirProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS;
    private boolean shouldReturn = false;
    private boolean canExplode = true;
    private AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;

    public MjolnirProjectileEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public MjolnirProjectileEntity(Level pLevel) {
        super(ModEntities.MJOLNIR_PROJECTILE.get(), pLevel);
    }

    public MjolnirProjectileEntity(Level pLevel, LivingEntity livingEntity) {
        super(ModEntities.MJOLNIR_PROJECTILE.get(), livingEntity, pLevel);

        if (livingEntity instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
        }
    }


    @Override
    protected Item getDefaultItem() {
        return ModItems.MJOLNIR.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        Entity owner = this.getOwner();
        if (!this.level().isClientSide()) {
            if (this.canExplode) {
                this.level().broadcastEntityEvent(this, ((byte) 3));
                Vec3 location = pResult.getLocation();
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()), true);
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()),true);
                if (this.getOwner() != null) {
                    this.level().explode(owner, location.x, location.y, location.z, 5F, Level.ExplosionInteraction.MOB);
                } else {
                    this.level().explode(this, location.x, location.y, location.z, 5F, Level.ExplosionInteraction.MOB);
                }
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()), true);
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()), true);

                this.canExplode = false;
            }
        }
        this.shouldReturn = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity owner = this.getOwner();
        Entity enemy = pResult.getEntity();
        if (!this.level().isClientSide()) {
            if (enemy != owner) {
                if (owner instanceof Player) {
                    enemy.hurt(owner.damageSources().playerAttack((Player) owner), ((MjolnirItem) ModItems.MJOLNIR.get()).getDamage());
                } else {
                    assert owner != null;
                    enemy.hurt(owner.damageSources().mobAttack((LivingEntity) owner), ((MjolnirItem) ModItems.MJOLNIR.get()).getDamage());
                }
            }
            if (this.canExplode) {
                this.level().broadcastEntityEvent(this, ((byte) 3));
                Vec3 location = pResult.getLocation();
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()), true);
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()),true);
                if (this.getOwner() != null) {
                    this.level().explode(owner, location.x, location.y, location.z, 5F, Level.ExplosionInteraction.MOB);
                } else {
                    this.level().explode(this, location.x, location.y, location.z, 5F, Level.ExplosionInteraction.MOB);
                }
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()), true);
                spawnEntity(this.level(), location, new LightningBolt(EntityType.LIGHTNING_BOLT, this.level()), true);

                this.canExplode = false;
            }
        }
        this.shouldReturn = true;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        if (this.shouldReturn) {
            if (this.getOwner() != null) {
                Entity owner = this.getOwner();
                this.setNoPhysics(true);
                Vec3 destination = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + destination.y * 0.015 * 3, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double movementFactor = 0.05 * 3;
                this.setDeltaMovement(this.getDeltaMovement().add(destination.normalize().scale(movementFactor)));
            } else {
                this.setDeltaMovement(0, 0, 0);
                for (Player player : this.level().players()) {
                    player.displayClientMessage(NorseMod.combineComponent((MutableComponent) MiscUtil.createTranslatableComponent("msg",
                            new ResourceLocation(NorseMod.MOD_ID, "mjolnir_gone")), List.of(Component.literal(String.valueOf(getX() + getY() + getZ())))), false);
                }
                this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(ModItems.MJOLNIR.get(), 1)));
                this.discard();
            }
        } else {
            if (isTooFar()) {
                this.shouldReturn = true;
            }
        }
        super.tick();
    }

    private boolean isTooFar() {
        if (getOwner() != null) {
            Entity owner = getOwner();
            return this.getX() > owner.getX() + 250 || this.getX() < owner.getX() - 250 ||
                    this.getY() > owner.getY() + 250 || this.getY() < owner.getY() - 250 ||
                    this.getZ() > owner.getZ() + 250 || this.getZ() < owner.getZ() - 250 ||
                    this.getY() >= 200;
        } else {
            return true;
        }
    }

    private void spawnEntity(Level pLevel, Vec3 pPos, Entity pEntity, boolean doesDeviate) {
        if (doesDeviate) {
            Random random = new Random();
            pEntity.setPos(pPos.x + random.nextInt(2), pPos.y + random.nextInt(2),
                    pPos.z + random.nextInt(2));
        }
        pLevel.addFreshEntity(pEntity);
    }

    private void setNoPhysics(boolean pNoPhysics) {
        this.noPhysics = pNoPhysics;
        this.setFlag(2, pNoPhysics);
    }

    private void setFlag(int pId, boolean pValue) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (pValue) {
            this.entityData.set(ID_FLAGS, (byte) (b0 | pId));
        } else {
            this.entityData.set(ID_FLAGS, (byte) (b0 & ~pId));
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ID_FLAGS, (byte) 0);
        super.defineSynchedData();
    }

    protected ItemStack getPickupItem() {
        return new ItemStack(this.getDefaultItem(), 1).copy();
    }

    protected boolean tryPickup(Player pPlayer) {
        if (pPlayer.equals(this.getOwner())) {
            switch (this.pickup) {
                case ALLOWED -> {
                    if (!pPlayer.isCreative()) {
                        return pPlayer.getInventory().add(this.getPickupItem());
                    } else {
                        return true;
                    }
                }
                case CREATIVE_ONLY -> {
                    return pPlayer.getAbilities().instabuild;
                }
                default -> {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public void playerTouch(Player pEntity) {
        if (!this.level().isClientSide && this.shouldReturn && this.tryPickup(pEntity)) {
            this.discard();
        }
    }

    private boolean canExplode(Entity entity) {
        return this.canExplode && entity != getOwner();
    }

    static {
        ID_FLAGS = SynchedEntityData.defineId(MjolnirProjectileEntity.class, EntityDataSerializers.BYTE);
    }
}

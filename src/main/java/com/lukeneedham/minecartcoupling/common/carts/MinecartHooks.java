/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.carts;

import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsDao;
import com.lukeneedham.minecartcoupling.common.carts.coupling.ICouplingsDao;
import com.lukeneedham.minecartcoupling.common.packet.ClientServerCommunication;
import com.lukeneedham.minecartcoupling.common.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.common.IMinecartCollisionHandler;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public enum MinecartHooks implements IMinecartCollisionHandler, IWorldEventListener {
    INSTANCE;
    private static final float OPTIMAL_DISTANCE = 1.28f;
    private static final float COEF_SPRING = 0.2f;
    private static final float COEF_SPRING_PLAYER = 0.5f;
    private static final float COEF_RESTITUTION = 0.2f;
    private static final float COEF_DAMPING = 0.4f;
    private static final float CART_LENGTH = 1.22f;
    private static final float CART_WIDTH = 0.98f;
    private static final float COLLISION_EXPANSION = 0.2f;

    private static final boolean areCartsSolid = false;
    private static final boolean cartsCollideWithItems = false;

    public boolean canMount(EntityMinecart cart) {
        return cart.getEntityData().getInteger("MountPrevention") <= 0;
    }

    @Override
    public void onEntityCollision(EntityMinecart cart, Entity other) {
        if (Game.isClient(cart.world) || cart.isPassenger(other) || !other.isEntityAlive() || !cart.isEntityAlive())
            return;

        ICouplingsDao lm = CouplingsDao.SERVER_INSTANCE;
        EntityMinecart coupledCartA = lm.getCoupledCartA(cart);
        if (coupledCartA != null && (coupledCartA == other || coupledCartA.isPassenger(other)))
            return;
        EntityMinecart coupledCartB = lm.getCoupledCartB(cart);
        if (coupledCartB != null && (coupledCartB == other || coupledCartB.isPassenger(other)))
            return;

        boolean isLiving = other instanceof EntityLivingBase;
        boolean isPlayer = other instanceof EntityPlayer;

        if (isPlayer && ((EntityPlayer) other).isSpectator()) {
            return;
        }

        if (isLiving && !isPlayer && cart.canBeRidden() && !(other instanceof EntityIronGolem)
                && cart.motionX * cart.motionX + cart.motionZ * cart.motionZ > 0.001D
                && !cart.isBeingRidden() && !other.isRiding()) {
            if (canMount(cart))
                other.startRiding(cart);
        }

        Vec2D cartPos = new Vec2D(cart.posX, cart.posZ);
        Vec2D otherPos = new Vec2D(other.posX, other.posZ);

        Vec2D unit = Vec2D.subtract(otherPos, cartPos);
        unit.normalize();

        double distance = cart.getDistance(other);
        double depth = distance - OPTIMAL_DISTANCE;

        double forceX = 0;
        double forceZ = 0;

        if (depth < 0) {
            double spring = isPlayer ? COEF_SPRING_PLAYER : COEF_SPRING;
            double penaltyX = spring * depth * unit.getX();
            double penaltyZ = spring * depth * unit.getY();

            forceX += penaltyX;
            forceZ += penaltyZ;

            if (!isPlayer) {
                double impulseX = unit.getX();
                double impulseZ = unit.getY();
                impulseX *= -(1.0 + COEF_RESTITUTION);
                impulseZ *= -(1.0 + COEF_RESTITUTION);

                Vec2D cartVel = new Vec2D(cart.motionX, cart.motionZ);
                Vec2D otherVel = new Vec2D(other.motionX, other.motionZ);

                double dot = Vec2D.subtract(otherVel, cartVel).dotProduct(unit);

                impulseX *= dot;
                impulseZ *= dot;
                impulseX *= 0.5;
                impulseZ *= 0.5;

                forceX -= impulseX;
                forceZ -= impulseZ;
            }
        }

        if (other instanceof EntityMinecart) {
            EntityMinecart otherCart = (EntityMinecart) other;
            if (!cart.isPoweredCart() || otherCart.isPoweredCart()) {
                cart.addVelocity(forceX, 0, forceZ);
            }
            if (!otherCart.isPoweredCart() || cart.isPoweredCart()) {
                other.addVelocity(-forceX, 0, -forceZ);
            }
        } else {
            Vec2D cartVel = new Vec2D(cart.motionX + forceX, cart.motionZ + forceZ);
            Vec2D otherVel = new Vec2D(other.motionX - forceX, other.motionZ - forceZ);

            double dot = Vec2D.subtract(otherVel, cartVel).dotProduct(unit);

            double dampX = COEF_DAMPING * dot * unit.getX();
            double dampZ = COEF_DAMPING * dot * unit.getY();

            forceX += dampX;
            forceZ += dampZ;

            if (!isPlayer) {
                other.addVelocity(-forceX, 0.0D, -forceZ);
            }
            cart.addVelocity(forceX, 0, forceZ);
        }
    }

    @Override
    public @Nullable AxisAlignedBB getCollisionBox(EntityMinecart cart, Entity other) {
        if (other instanceof EntityItem && cartsCollideWithItems) {
            return other.getEntityBoundingBox().grow(-0.01);
        }
        return other.canBePushed() ? other.getEntityBoundingBox().grow(-COLLISION_EXPANSION) : null;
    }

    @Override
    public AxisAlignedBB getMinecartCollisionBox(EntityMinecart cart) {
        double yaw = Math.toRadians(cart.rotationYaw);
        double diff = ((CART_LENGTH - CART_WIDTH) / 2.0) + MinecartHooks.COLLISION_EXPANSION;
        double x = diff * Math.abs(Math.cos(yaw));
        double z = diff * Math.abs(Math.sin(yaw));
        return cart.getEntityBoundingBox().grow(x, MinecartHooks.COLLISION_EXPANSION, z);
    }

    @Override
    public @Nullable AxisAlignedBB getBoundingBox(EntityMinecart cart) {
        if (cart == null || cart.isDead) {
            return null;
        }
        if (areCartsSolid) {
            return cart.getEntityBoundingBox();
        }
        return null;
    }

    private void land(EntityMinecart cart) {
        cart.getEntityData().setInteger("Launched", 0);
        cart.setMaxSpeedAirLateral(EntityMinecart.defaultMaxSpeedAirLateral);
        cart.setMaxSpeedAirVertical(EntityMinecart.defaultMaxSpeedAirVertical);
        cart.setDragAir(EntityMinecart.defaultDragAir);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onMinecartUpdate(MinecartUpdateEvent event) {
        EntityMinecart cart = event.getMinecart();
        NBTTagCompound data = cart.getEntityData();

        // Fix flip
        float distance = MathTools.getDistanceBetweenAngles(cart.rotationYaw, cart.prevRotationYaw);
        float cutoff = 120F;
        if (distance < -cutoff || distance >= cutoff) {
            cart.rotationYaw += 180.0F;

            try {
                Field isInReverseField = EntityMinecart.class.getDeclaredField("isInReverse");
                isInReverseField.setAccessible(true);
                boolean isInReverse = isInReverseField.getBoolean(cart);
                isInReverseField.setBoolean(cart, !isInReverse);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            cart.rotationYaw = cart.rotationYaw % 360.0F;
        }

        Block block = WorldPlugin.getBlock(cart.world, event.getPos());
        int launched = data.getInteger("Launched");
        if (TrackTools.isRailBlock(block)) {
            cart.fallDistance = 0;
            if (cart.isBeingRidden())
                cart.getPassengers().forEach(p -> p.fallDistance = 0);
            if (launched > 1)
                land(cart);
        } else if (launched == 1) {
            data.setInteger("Launched", 2);
            cart.setCanUseRail(true);
        } else if (launched > 1 && (cart.onGround || cart.isInsideOfMaterial(Material.CIRCUITS)))
            land(cart);

        int mountPrevention = data.getInteger("MountPrevention");
        if (mountPrevention > 0) {
            mountPrevention--;
            data.setInteger("MountPrevention", mountPrevention);
        }

        cart.motionX = Math.copySign(Math.min(Math.abs(cart.motionX), 9.5), cart.motionX);
        cart.motionY = Math.copySign(Math.min(Math.abs(cart.motionY), 9.5), cart.motionY);
        cart.motionZ = Math.copySign(Math.min(Math.abs(cart.motionZ), 9.5), cart.motionZ);
    }

    @SubscribeEvent
    public void onWorldCreate(WorldEvent.Load event) {
        if (Game.isHost(event.getWorld())) {
            event.getWorld().addEventListener(this);
        }
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        // Fix couplings for killed carts
        // Unloaded entities are not "isDead"
        if (Game.isHost(entityIn.world) && !entityIn.isEntityAlive() && entityIn instanceof EntityMinecart) {
            // We only mark Trains for deletion here, this event seems to be called from outside the server thread.
            Train.get(((EntityMinecart) entityIn)).ifPresent(Train::kill);
            EntityMinecart minecart = (EntityMinecart) entityIn;
            CouplingsDao.SERVER_INSTANCE.breakCouplings(minecart);
            ClientServerCommunication.sendAllCouplingsBrokenUpdate(minecart.getEntityId());
        }
    }

    @Override
    public void notifyBlockUpdate(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState oldState, @NotNull IBlockState newState, int flags) {
    }

    @Override
    public void notifyLightSet(@NotNull BlockPos pos) {
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, @NotNull SoundEvent soundIn, @NotNull SoundCategory category, double x, double y, double z, float volume, float pitch) {
    }

    @Override
    public void playRecord(@NotNull SoundEvent soundIn, @NotNull BlockPos pos) {
    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, @NotNull int... parameters) {
    }

    @Override
    public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, @NotNull int... parameters) {
    }

    @Override
    public void onEntityAdded(@NotNull Entity entityIn) {
    }

    @Override
    public void broadcastSound(int soundID, @NotNull BlockPos pos, int data) {
    }

    @Override
    public void playEvent(@NotNull EntityPlayer player, int type, @NotNull BlockPos blockPosIn, int data) {
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, @NotNull BlockPos pos, int progress) {
    }

}

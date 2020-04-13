/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public final class CartTools {

    /**
     * Returns a minecart from a persistent UUID. Only returns carts from the same world.
     *
     * @param id Cart's persistent UUID
     * @return EntityMinecart
     */
    public static @Nullable EntityMinecart getCartFromUUID(@Nullable World world, @Nullable UUID id) {
        if (world == null || id == null)
            return null;
        if (world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(id);
            if (entity instanceof EntityMinecart && entity.isEntityAlive()) {
                return (EntityMinecart) entity;
            }
        } else {
            // for performance reasons
            //noinspection Convert2streamapi
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityMinecart && entity.isEntityAlive() && entity.getPersistentID().equals(id))
                    return (EntityMinecart) entity;
            }
        }
        return null;
    }

}

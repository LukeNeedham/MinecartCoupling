/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019

 This work (the API) is licensed under the "MIT" License,
 see LICENSE.md for details.
 -----------------------------------------------------------------------------*/

package com.lukeneedham.minecartcoupling.common.carts;

import net.minecraft.entity.item.EntityMinecart;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * The LinkageManager contains all the functions needed to link and interact
 * with linked carts.
 * <p/>
 * Each cart can up to two links. They are called Link A and Link B.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface ICouplingManager {

    /**
     * The default max distance at which carts can be linked, divided by 2.
     */
    float COUPLABLE_DISTANCE = 1.25f;
    /**
     * The default distance at which linked carts are maintained, divided by 2.
     */
    float OPTIMAL_DISTANCE = 0.665f;

    /**
     * Creates a link between two carts, but only if there is nothing preventing
     * such a link.
     *
     * @return True if the link succeeded.
     */
    default boolean createLink(EntityMinecart cart1, EntityMinecart cart2) {
        return false;
    }

    default boolean hasFreeLink(EntityMinecart cart) {
        return false;
    }

    /**
     * Returns the cart linked to Link A or null if nothing is currently
     * occupying Link A.
     *
     * @param cart The cart for which to get the link
     * @return The linked cart or null
     */
    default @Nullable
    EntityMinecart getLinkedCartA(EntityMinecart cart) {
        return null;
    }

    /**
     * Returns the cart linked to Link B or null if nothing is currently
     * occupying Link B.
     *
     * @param cart The cart for which to get the link
     * @return The linked cart or null
     */
    default @Nullable
    EntityMinecart getLinkedCartB(EntityMinecart cart) {
        return null;
    }

    /**
     * Returns true if the two carts are linked to each other.
     *
     * @return True if linked
     */
    default boolean areLinked(EntityMinecart cart1, EntityMinecart cart2) {
        return false;
    }

    /**
     * Breaks a link between two carts, if any link exists.
     */
    default void breakCoupling(EntityMinecart cart1, EntityMinecart cart2) {
    }

    /**
     * Breaks all links the cart has.
     */
    default void breakLinks(EntityMinecart cart) {
    }

    /**
     * Counts how many carts are in the train.
     *
     * @param cart Any cart in the train
     * @return The number of carts in the train
     */
    @SuppressWarnings("unused")
    default int countCartsInTrain(EntityMinecart cart) {
        return 0;
    }

    /**
     * Returns a Stream which will iterate over every cart in the provided cart's train.
     * <p>
     * There is no guarantee of order.
     * <p>
     * If called on the client, it will only contain the passed cart object.
     * There is no linkage information on the client.
     */
    default Stream<EntityMinecart> streamTrain(EntityMinecart cart) {
        return Stream.empty();
    }

}

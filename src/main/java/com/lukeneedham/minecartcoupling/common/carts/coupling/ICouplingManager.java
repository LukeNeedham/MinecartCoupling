/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019

 This work (the API) is licensed under the "MIT" License,
 see LICENSE.md for details.
 -----------------------------------------------------------------------------*/

package com.lukeneedham.minecartcoupling.common.carts.coupling;

import net.minecraft.entity.item.EntityMinecart;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Contains all the functions needed to couple and interact with coupled carts.
 * <p/>
 * Each cart can up to two couplings: A and B.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface ICouplingManager {

    /**
     * The default max distance at which carts can be coupled, divided by 2.
     */
    float COUPLABLE_DISTANCE = 1.25f;
    /**
     * The default distance at which coupled carts are maintained, divided by 2.
     */
    float OPTIMAL_DISTANCE = 0.665f;

    /**
     * Creates a coupling between two carts, but only if there is nothing preventing
     * such a coupling.
     *
     * @return true if the coupling succeeded.
     */
    default boolean createCoupling(EntityMinecart cart1, EntityMinecart cart2) {
        return false;
    }

    default boolean hasFreeCoupling(EntityMinecart cart) {
        return false;
    }

    /**
     * Returns the cart coupled to LiConk A or null if nothing is currently
     * occupying coupling A.
     *
     * @param cart The cart for which to get the coupling
     * @return The coupled cart or null
     */
    default @Nullable
    EntityMinecart getCoupledCartA(EntityMinecart cart) {
        return null;
    }

    /**
     * Returns the cart coupled to coupling B or null if nothing is currently
     * occupying coupling B.
     *
     * @param cart The cart for which to get the coupling
     * @return The coupled cart or null
     */
    default @Nullable
    EntityMinecart getCoupledCartB(EntityMinecart cart) {
        return null;
    }

    /**
     * Returns true if the two carts are coupled to each other.
     *
     * @return True if coupled
     */
    default boolean areCoupled(EntityMinecart cart1, EntityMinecart cart2) {
        return false;
    }

    /**
     * Breaks a coupling between two carts, if any coupling exists.
     */
    default void breakCoupling(EntityMinecart cart1, EntityMinecart cart2) {
    }

    /**
     * Breaks all couplings the cart has.
     */
    default void breakCouplings(EntityMinecart cart) {
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
     * There is no coupling information on the client.
     */
    default Stream<EntityMinecart> streamTrain(EntityMinecart cart) {
        return Stream.empty();
    }

}

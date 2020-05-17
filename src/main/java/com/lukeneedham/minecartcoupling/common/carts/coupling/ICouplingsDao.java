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
public interface ICouplingsDao {

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
    boolean createCoupling(EntityMinecart cart1, EntityMinecart cart2);

    /**
     * Breaks a coupling between two carts, if any coupling exists.
     */
    void breakCoupling(EntityMinecart cart1, EntityMinecart cart2);

    /**
     * Breaks all couplings the cart has.
     */
    void breakCouplings(EntityMinecart cart);

    boolean hasFreeCoupling(EntityMinecart cart);

    /**
     * Returns the cart coupled to LiConk A or null if nothing is currently
     * occupying coupling A.
     *
     * @param cart The cart for which to get the coupling
     * @return The coupled cart or null
     */
    @Nullable
    EntityMinecart getCoupledCartA(EntityMinecart cart);

    /**
     * Returns the cart coupled to coupling B or null if nothing is currently
     * occupying coupling B.
     *
     * @param cart The cart for which to get the coupling
     * @return The coupled cart or null
     */
    @Nullable
    EntityMinecart getCoupledCartB(EntityMinecart cart);

    /**
     * Returns true if the two carts are coupled to each other.
     *
     * @return True if coupled
     */
    boolean areCoupled(EntityMinecart cart1, EntityMinecart cart2);

    /**
     * Counts how many carts are in the train.
     *
     * @param cart Any cart in the train
     * @return The number of carts in the train
     */
    @SuppressWarnings("unused")
    int countCartsInTrain(EntityMinecart cart);

    /**
     * Returns a Stream which will iterate over every cart in the provided cart's train.
     * <p>
     * There is no guarantee of order.
     * <p>
     * If called on the client, it will only contain the passed cart object.
     * There is no coupling information on the client.
     */
    Stream<EntityMinecart> streamTrain(EntityMinecart cart);

}

/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.carts.coupling;

import com.lukeneedham.minecartcoupling.common.carts.Train;
import com.lukeneedham.minecartcoupling.common.util.CartTools;
import com.lukeneedham.minecartcoupling.common.util.Game;
import com.lukeneedham.minecartcoupling.common.util.MathTools;
import net.minecraft.entity.item.EntityMinecart;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * Contains all the functions needed to couple and interact with coupled carts.
 * <p/>
 * One concept of import is that of the Coupling Id. Every cart is given a unique
 * identifier by this class the first time it encounters the cart.
 * <p/>
 * This identifier is stored in the entity's NBT data between world loads so
 * that couplings are persistent rather than transitory.
 * <p/>
 * Couplings are also stored in NBT data as an Integer value that contains the
 * Coupling Id of the cart it is coupled to.
 * <p/>
 * Generally you can ignore most of this and use the functions that don't
 * require or return Coupling Ids.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public enum CouplingManager implements ICouplingManager {
    INSTANCE;

    public static void printDebug(String msg, Object... args) {
        Game.log().msg(Level.DEBUG, msg, args);
    }

    /**
     * Returns the coupling id of the cart and adds the cart the coupling cache.
     *
     * @param cart The EntityMinecart
     * @return The coupling id
     */
    public UUID getCouplingId(EntityMinecart cart) {
        return cart.getPersistentID();
    }

    /**
     * Returns the square of the max distance two carts can be and still be couplable.
     *
     * @return The square of the couplable distance
     */
    private float getCouplableDistanceSq() {
        float dist = 2 * COUPLABLE_DISTANCE;
        return dist * dist;
    }

    /**
     * Returns true if there is nothing preventing the two carts from being coupled.
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return true if can be coupled
     */
    private boolean canCoupleCarts(EntityMinecart cart1, EntityMinecart cart2) {
        if (cart1 == cart2)
            return false;

        if (!hasFreeCoupling(cart1) || !hasFreeCoupling(cart2))
            return false;

        if (areCoupled(cart1, cart2))
            return false;

        if (cart1.getDistanceSq(cart2) > getCouplableDistanceSq())
            return false;

        return !Train.areInSameTrain(cart1, cart2);
    }

    /**
     * Creates a coupling between two carts, but only if there is nothing preventing
     * such a coupling.
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return True if the coupling succeeded.
     */
    @Override
    public boolean createCoupling(EntityMinecart cart1, EntityMinecart cart2) {
        if (canCoupleCarts(cart1, cart2)) {
            setCouplingUnidirectional(cart1, cart2);
            setCouplingUnidirectional(cart2, cart1);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasFreeCoupling(EntityMinecart cart) {
        return Arrays.stream(CouplingType.VALUES).anyMatch(couplingType -> hasFreeCouple(cart, couplingType));
    }

    public boolean hasFreeCouple(EntityMinecart cart, CouplingType type) {
        return MathTools.isNil(getCoupling(cart, type));
    }

    private boolean setCouplingUnidirectional(EntityMinecart from, EntityMinecart to) {
        for (CouplingType couplingType : CouplingType.VALUES) {
            if (hasFreeCouple(from, couplingType)) {
                setCouplingUnidirectional(from, to, couplingType);
                return true;
            }
        }
        return false;
    }

    // Note: returns a nil uuid (0) if the coupling does not exist
    public UUID getCoupling(EntityMinecart cart, CouplingType couplingType) {
        long high = cart.getEntityData().getLong(couplingType.tagHigh);
        long low = cart.getEntityData().getLong(couplingType.tagLow);
        return new UUID(high, low);
    }

    public UUID getCouplingA(EntityMinecart cart) {
        return getCoupling(cart, CouplingType.COUPLING_A);
    }

    public UUID getCouplingB(EntityMinecart cart) {
        return getCoupling(cart, CouplingType.COUPLING_B);
    }

    private void setCouplingUnidirectional(EntityMinecart source, EntityMinecart target, CouplingType couplingType) {
        UUID id = getCouplingId(target);
        source.getEntityData().setLong(couplingType.tagHigh, id.getMostSignificantBits());
        source.getEntityData().setLong(couplingType.tagLow, id.getLeastSignificantBits());
    }

    /**
     * Returns the cart coupled to CouplingType A or null if nothing is currently
     * occupying CouplingType A.
     *
     * @param cart The cart for which to get the coupling
     * @return The coupled cart or null
     */
    @Override
    public @Nullable
    EntityMinecart getCoupledCartA(EntityMinecart cart) {
        return getCoupledCart(cart, CouplingType.COUPLING_A);
    }

    /**
     * Returns the cart coupled to CoupleType B or null if nothing is currently
     * occupying CoupleType B.
     *
     * @param cart The cart for which to get the coupling
     * @return The coupled cart or null
     */
    @Override
    public @Nullable
    EntityMinecart getCoupledCartB(EntityMinecart cart) {
        return getCoupledCart(cart, CouplingType.COUPLING_B);
    }

    public @Nullable
    EntityMinecart getCoupledCart(EntityMinecart cart, CouplingType type) {
        return CartTools.getCartFromUUID(cart.world, getCoupling(cart, type));
    }

    /**
     * Returns true if the two carts are coupled directly to each other.
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return True if coupled
     */
    @Override
    public boolean areCoupled(EntityMinecart cart1, EntityMinecart cart2) {
        return areCoupled(cart1, cart2, true);
    }

    /**
     * Returns true if the two carts are coupled directly to each other.
     *
     * @param cart1  First Cart
     * @param cart2  Second Cart
     * @param strict true if both carts should have coupling data pointing to the other cart,
     *               false if its ok if only one cart has the data (this is technically an invalid state, but its been known to happen)
     * @return true if coupled
     */
    public boolean areCoupled(EntityMinecart cart1, EntityMinecart cart2, boolean strict) {
        if (cart1 == cart2)
            return false;

        UUID id1 = getCouplingId(cart1);
        UUID id2 = getCouplingId(cart2);
        boolean cart1Coupled = id2.equals(getCouplingA(cart1)) || id2.equals(getCouplingB(cart1));
        boolean cart2Coupled = id1.equals(getCouplingA(cart2)) || id1.equals(getCouplingB(cart2));

        if (cart1Coupled != cart2Coupled) {
            Game.log().msg(Level.WARN,
                    "Coupling discrepancy between carts " +
                            getCouplingId(cart1) +
                            "(" + cart1.getDisplayName() + ") and " +
                            getCouplingId(cart2) +
                            "(" + cart2.getDisplayName() + "): The first cart reports " +
                            cart1Coupled + " for coupled while the second one reports " + cart2Coupled + "!"
            );
        }

        if (strict) {
            return cart1Coupled && cart2Coupled;
        } else {
            return cart1Coupled || cart2Coupled;
        }
    }

    /**
     * Repairs an asymmetrical coupling between carts
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return true if the repair was successful.
     */
    public boolean repairCoupling(EntityMinecart cart1, EntityMinecart cart2) {
        boolean repaired = repairCouplingUnidirectional(cart1, cart2) && repairCouplingUnidirectional(cart2, cart1);
        if (repaired)
            Train.repairTrain(cart1, cart2);
        else
            breakCoupling(cart1, cart2);
        return repaired;
    }

    private boolean repairCouplingUnidirectional(EntityMinecart from, EntityMinecart to) {
        UUID coupling = getCouplingId(to);

        return coupling.equals(getCouplingA(from)) || coupling.equals(getCouplingB(from)) || setCouplingUnidirectional(from, to);
    }

    @Override
    public void breakCoupling(EntityMinecart one, EntityMinecart two) {
        CouplingType couplingOne = getCouplingType(one, two);
        CouplingType couplingTwo = getCouplingType(two, one);

        breakCouplingInternal(one, two, couplingOne, couplingTwo);
    }

    @Override
    public void breakCouplings(EntityMinecart cart) {
        breakCouplingA(cart);
        breakCouplingB(cart);
    }

    /**
     * Break only coupling A.
     *
     * @param cart Cart
     */
    private void breakCouplingA(EntityMinecart cart) {
        EntityMinecart other = getCoupledCartA(cart);
        if (other == null) {
            return;
        }

        CouplingType otherCoupling = getCouplingType(other, cart);
        breakCouplingInternal(cart, other, CouplingType.COUPLING_A, otherCoupling);
    }

    /**
     * Break only coupling B.
     *
     * @param cart Cart
     */
    private void breakCouplingB(EntityMinecart cart) {
        EntityMinecart other = getCoupledCartB(cart);
        if (other == null) {
            return;
        }

        CouplingType otherCoupling = getCouplingType(other, cart);
        breakCouplingInternal(cart, other, CouplingType.COUPLING_B, otherCoupling);
    }

    /**
     * Breaks a bidirectional coupling with all the arguments given.
     * <p>
     * This has the most argument and tries to prevent a recursion.
     *
     * @param one     One of the carts given
     * @param two     The cart, given or calculated via a coupling
     * @param couplingOne The coupling from one, given or calculated
     * @param couplingTwo The coupling from two, calculated
     */
    private void breakCouplingInternal(EntityMinecart one, EntityMinecart two, @Nullable CouplingType couplingOne, @Nullable CouplingType couplingTwo) {
        if ((couplingOne == null) != (couplingTwo == null)) {
            Game.log().msg(Level.WARN,
                    "Coupling discrepancy between carts " +
                            getCouplingId(one) +
                            "(" + one.getDisplayName() + ") and " +
                            getCouplingId(two) +
                            "(" + two.getDisplayName() + "): The first cart reports " +
                            (couplingOne == null) + " for coupled while the second one reports " + (couplingTwo == null) + "!"
            );
        }

        if (couplingOne != null) {
            breakCouplingUnidirectional(one, two, couplingOne);
        }
        if (couplingTwo != null) {
            breakCouplingUnidirectional(two, one, couplingTwo);
        }
    }

    private @Nullable
    CouplingType getCouplingType(EntityMinecart from, EntityMinecart to) {
        UUID coupleTo = getCouplingId(to);
        return Arrays.stream(CouplingType.VALUES)
                .filter(coupling -> coupleTo.equals(getCoupling(from, coupling)))
                .findFirst().orElse(null);
    }

    private void breakCouplingUnidirectional(EntityMinecart cart, EntityMinecart other, CouplingType couplingType) {
        removeCouplingTags(cart, couplingType);

        printDebug("Cart {0}({1}) unidirectionally uncoupled {2}({3}) at ({4}).", getCouplingId(cart), cart.getDisplayName(), getCouplingId(other), other, couplingType.name());
    }

    private void removeCouplingTags(EntityMinecart cart, CouplingType couplingType) {
        cart.getEntityData().removeTag(couplingType.tagHigh);
        cart.getEntityData().removeTag(couplingType.tagLow);
    }

    /**
     * Counts how many carts are in the train.
     *
     * @param cart Any cart in the train
     * @return The number of carts in the train
     */
    @Override
    public int countCartsInTrain(EntityMinecart cart) {
        return Train.get(cart).map(Train::size).orElse(1);
    }

    @Override
    public Stream<EntityMinecart> streamTrain(EntityMinecart cart) {
        return Train.streamCarts(cart);
    }
}

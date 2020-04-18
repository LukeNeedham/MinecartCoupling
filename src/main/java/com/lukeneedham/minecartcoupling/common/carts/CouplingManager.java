/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.carts;

import com.lukeneedham.minecartcoupling.common.util.CartTools;
import com.lukeneedham.minecartcoupling.common.util.Game;
import com.lukeneedham.minecartcoupling.common.util.MathTools;
import net.minecraft.entity.item.EntityMinecart;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * The LinkageManager contains all the functions needed to link and interacted
 * with linked carts.
 * <p/>
 * One concept if import is that of the Linkage Id. Every cart is given a unique
 * identifier by the LinkageManager the first time it encounters the cart.
 * <p/>
 * This identifier is stored in the entity's NBT data between world loads so
 * that links are persistent rather than transitory.
 * <p/>
 * Links are also stored in NBT data as an Integer value that contains the
 * Linkage Id of the cart it is linked to.
 * <p/>
 * Generally you can ignore most of this and use the functions that don't
 * require or return Linkage Ids.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public enum CouplingManager implements ICouplingManager {
    INSTANCE;

    public static final String LINK_A_HIGH = "rcLinkAHigh";
    public static final String LINK_A_LOW = "rcLinkALow";
    public static final String LINK_B_HIGH = "rcLinkBHigh";
    public static final String LINK_B_LOW = "rcLinkBLow";

    public static void printDebug(String msg, Object... args) {
        Game.log().msg(Level.DEBUG, msg, args);
    }

    /**
     * Returns the coupler id of the cart and adds the cart the coupler cache.
     *
     * @param cart The EntityMinecart
     * @return The coupler id
     */
    public UUID getCouplerId(EntityMinecart cart) {
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
     * Returns true if there is nothing preventing the two carts from being
     * linked.
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return True if can be linked
     */
    private boolean canLinkCarts(EntityMinecart cart1, EntityMinecart cart2) {
        if (cart1 == cart2)
            return false;

        if (!hasFreeLink(cart1) || !hasFreeLink(cart2))
            return false;

        if (areLinked(cart1, cart2))
            return false;

        if (cart1.getDistanceSq(cart2) > getCouplableDistanceSq())
            return false;

        return !Train.areInSameTrain(cart1, cart2);
    }

    /**
     * Creates a link between two carts, but only if there is nothing preventing
     * such a link.
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return True if the link succeeded.
     */
    @Override
    public boolean createLink(EntityMinecart cart1, EntityMinecart cart2) {
        if (canLinkCarts(cart1, cart2)) {
            setLinkUnidirectional(cart1, cart2);
            setLinkUnidirectional(cart2, cart1);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasFreeLink(EntityMinecart cart) {
        return Arrays.stream(CouplingType.VALUES).anyMatch(link -> hasFreeLink(cart, link));
    }

    public boolean hasFreeLink(EntityMinecart cart, CouplingType type) {
        return MathTools.isNil(getLink(cart, type));
    }

    private boolean setLinkUnidirectional(EntityMinecart from, EntityMinecart to) {
        for (CouplingType link : CouplingType.VALUES) {
            if (hasFreeLink(from, link)) {
                setLinkUnidirectional(from, to, link);
                return true;
            }
        }
        return false;
    }

    // Note: returns a nil uuid (0) if the link does not exist
    public UUID getLink(EntityMinecart cart, CouplingType couplingType) {
        long high = cart.getEntityData().getLong(couplingType.tagHigh);
        long low = cart.getEntityData().getLong(couplingType.tagLow);
        return new UUID(high, low);
    }

    public UUID getLinkA(EntityMinecart cart) {
        return getLink(cart, CouplingType.LINK_A);
    }

    public UUID getLinkB(EntityMinecart cart) {
        return getLink(cart, CouplingType.LINK_B);
    }

    private void setLinkUnidirectional(EntityMinecart source, EntityMinecart target, CouplingType couplingType) {
        // hasFreeLink(source, linkType) checked
        UUID id = getCouplerId(target);
        source.getEntityData().setLong(couplingType.tagHigh, id.getMostSignificantBits());
        source.getEntityData().setLong(couplingType.tagLow, id.getLeastSignificantBits());
    }

    /**
     * Returns the cart linked to LinkType A or null if nothing is currently
     * occupying LinkType A.
     *
     * @param cart The cart for which to get the link
     * @return The linked cart or null
     */
    @Override
    public @Nullable
    EntityMinecart getLinkedCartA(EntityMinecart cart) {
        return getLinkedCart(cart, CouplingType.LINK_A);
    }

    /**
     * Returns the cart linked to LinkType B or null if nothing is currently
     * occupying LinkType B.
     *
     * @param cart The cart for which to get the link
     * @return The linked cart or null
     */
    @Override
    public @Nullable
    EntityMinecart getLinkedCartB(EntityMinecart cart) {
        return getLinkedCart(cart, CouplingType.LINK_B);
    }

    public @Nullable
    EntityMinecart getLinkedCart(EntityMinecart cart, CouplingType type) {
        return CartTools.getCartFromUUID(cart.world, getLink(cart, type));
    }

    /**
     * Returns true if the two carts are linked directly to each other.
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return True if linked
     */
    @Override
    public boolean areLinked(EntityMinecart cart1, EntityMinecart cart2) {
        return areLinked(cart1, cart2, true);
    }

    /**
     * Returns true if the two carts are linked directly to each other.
     *
     * @param cart1  First Cart
     * @param cart2  Second Cart
     * @param strict true if both carts should have linking data pointing to the other cart,
     *               false if its ok if only one cart has the data (this is technically an invalid state, but its been known to happen)
     * @return True if linked
     */
    public boolean areLinked(EntityMinecart cart1, EntityMinecart cart2, boolean strict) {
        if (cart1 == cart2)
            return false;

        UUID id1 = getCouplerId(cart1);
        UUID id2 = getCouplerId(cart2);
        boolean cart1Linked = id2.equals(getLinkA(cart1)) || id2.equals(getLinkB(cart1));
        boolean cart2Linked = id1.equals(getLinkA(cart2)) || id1.equals(getLinkB(cart2));

        if (cart1Linked != cart2Linked) {
            Game.log().msg(Level.WARN,
                    "Linking discrepancy between carts " +
                            getCouplerId(cart1) +
                            "(" + cart1.getDisplayName() + ") and " +
                            getCouplerId(cart2) +
                            "(" + cart2.getDisplayName() + "): The first cart reports " +
                            cart1Linked + " for linked while the second one reports " + cart2Linked + "!"
            );
        }

        if (strict) {
            return cart1Linked && cart2Linked;
        } else {
            return cart1Linked || cart2Linked;
        }
    }

    /**
     * Repairs an asymmetrical link between carts
     *
     * @param cart1 First Cart
     * @param cart2 Second Cart
     * @return true if the repair was successful.
     */
    public boolean repairLink(EntityMinecart cart1, EntityMinecart cart2) {
        boolean repaired = repairLinkUnidirectional(cart1, cart2) && repairLinkUnidirectional(cart2, cart1);
        if (repaired)
            Train.repairTrain(cart1, cart2);
        else
            breakCoupling(cart1, cart2);
        return repaired;
    }

    private boolean repairLinkUnidirectional(EntityMinecart from, EntityMinecart to) {
        UUID link = getCouplerId(to);

        return link.equals(getLinkA(from)) || link.equals(getLinkB(from)) || setLinkUnidirectional(from, to);
    }

    @Override
    public void breakCoupling(EntityMinecart one, EntityMinecart two) {
        CouplingType linkOne = getLinkType(one, two);
        CouplingType linkTwo = getLinkType(two, one);

        breakLinkInternal(one, two, linkOne, linkTwo);
    }

    @Override
    public void breakLinks(EntityMinecart cart) {
        breakLinkA(cart);
        breakLinkB(cart);
    }

    /**
     * Break only link A.
     *
     * @param cart Cart
     */
    private void breakLinkA(EntityMinecart cart) {
        EntityMinecart other = getLinkedCartA(cart);
        if (other == null) {
            return;
        }

        CouplingType otherLink = getLinkType(other, cart);
        breakLinkInternal(cart, other, CouplingType.LINK_A, otherLink);
    }

    /**
     * Break only link B.
     *
     * @param cart Cart
     */
    private void breakLinkB(EntityMinecart cart) {
        EntityMinecart other = getLinkedCartB(cart);
        if (other == null) {
            return;
        }

        CouplingType otherLink = getLinkType(other, cart);
        breakLinkInternal(cart, other, CouplingType.LINK_B, otherLink);
    }

    /**
     * Breaks a bidirectional link with all the arguments given.
     * <p>
     * This has the most argument and tries to prevent a recursion.
     *
     * @param one     One of the carts given
     * @param two     The cart, given or calculated via a link
     * @param linkOne The link from one, given or calculated
     * @param linkTwo The link from two, calculated
     */
    private void breakLinkInternal(EntityMinecart one, EntityMinecart two, @Nullable CouplingType linkOne, @Nullable CouplingType linkTwo) {
        if ((linkOne == null) != (linkTwo == null)) {
            Game.log().msg(Level.WARN,
                    "Linking discrepancy between carts " +
                            getCouplerId(one) +
                            "(" + one.getDisplayName() + ") and " +
                            getCouplerId(two) +
                            "(" + two.getDisplayName() + "): The first cart reports " +
                            (linkOne == null) + " for linked while the second one reports " + (linkTwo == null) + "!"
            );
        }

        if (linkOne != null) {
            breakLinkUnidirectional(one, two, linkOne);
        }
        if (linkTwo != null) {
            breakLinkUnidirectional(two, one, linkTwo);
        }
    }

    private @Nullable
    CouplingType getLinkType(EntityMinecart from, EntityMinecart to) {
        UUID linkTo = getCouplerId(to);
        return Arrays.stream(CouplingType.VALUES)
                .filter(link -> linkTo.equals(getLink(from, link)))
                .findFirst().orElse(null);
    }

    private void breakLinkUnidirectional(EntityMinecart cart, EntityMinecart other, CouplingType couplingType) {
        removeLinkTags(cart, couplingType);

        printDebug("Cart {0}({1}) unidirectionally unlinked {2}({3}) at ({4}).", getCouplerId(cart), cart.getDisplayName(), getCouplerId(other), other, couplingType.name());
    }

    private void removeLinkTags(EntityMinecart cart, CouplingType couplingType) {
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

    public enum CouplingType {
        LINK_A(LINK_A_HIGH, LINK_A_LOW),
        LINK_B(LINK_B_HIGH, LINK_B_LOW);
        public static final CouplingType[] VALUES = values();
        public final String tagHigh;
        public final String tagLow;

        CouplingType(String tagHigh, String tagLow) {
            this.tagHigh = tagHigh;
            this.tagLow = tagLow;
        }
    }
}

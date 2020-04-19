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
import com.lukeneedham.minecartcoupling.common.util.Vec2D;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CouplingHandler {
    public static final String COUPLING_A_TIMER = "couplingA_timer";
    public static final String COUPLING_B_TIMER = "couplingB_timer";
    public static final double COUPLED_DRAG = 0.95;
    public static final float MAX_DISTANCE = 8F;
    private static final float STIFFNESS = 0.7F;
    private static final float DAMPING = 0.4F;
    private static final float FORCE_LIMITER = 6F;
    private static CouplingHandler instance;

    private CouplingHandler() {
    }

    public static CouplingHandler getInstance() {
        if (instance == null)
            instance = new CouplingHandler();
        return instance;
    }

    /**
     * Returns the optimal distance between two coupled carts that should ideally be maintained at all times.
     *
     * @return The optimal distance
     */
    private float getOptimalDistance() {
        return 2 * ICouplingManager.OPTIMAL_DISTANCE;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean canCartBeAdjustedBy(EntityMinecart cart1, EntityMinecart cart2) {
        return cart1 != cart2;
    }

    /**
     * This is where the physics magic actually gets performed. It uses Spring
     * Forces and Damping Forces to maintain a fixed distance between carts.
     *
     * @param cart1 EntityMinecart
     * @param cart2 EntityMinecart
     */
    protected void adjustVelocity(EntityMinecart cart1, EntityMinecart cart2, CouplingType couplingType) {
        String timer = COUPLING_A_TIMER;
        if (couplingType == CouplingType.COUPLING_B)
            timer = COUPLING_B_TIMER;
        if (cart1.world.provider.getDimension() != cart2.world.provider.getDimension()) {
            short count = cart1.getEntityData().getShort(timer);
            count++;
            if (count > 200) {
                CouplingManager.INSTANCE.breakCoupling(cart1, cart2);
                CouplingManager.printDebug("Reason For Broken Coupling: Carts in different dimensions.");
            }
            cart1.getEntityData().setShort(timer, count);
            return;
        }
        cart1.getEntityData().setShort(timer, (short) 0);

        double dist = cart1.getDistance(cart2);
        if (dist > MAX_DISTANCE) {
            CouplingManager.INSTANCE.breakCoupling(cart1, cart2);
            CouplingManager.printDebug("Reason For Broken Coupling: Max distance exceeded.");
            return;
        }

        boolean adj1 = canCartBeAdjustedBy(cart1, cart2);
        boolean adj2 = canCartBeAdjustedBy(cart2, cart1);

        Vec2D cart1Pos = new Vec2D(cart1);
        Vec2D cart2Pos = new Vec2D(cart2);

        Vec2D unit = Vec2D.unit(cart2Pos, cart1Pos);

        // Energy transfer

//        double transX = TRANSFER * (cart2.motionX - cart1.motionX);
//        double transZ = TRANSFER * (cart2.motionZ - cart1.motionZ);
//
//        transX = limitForce(transX);
//        transZ = limitForce(transZ);
//
//        if(adj1) {
//            cart1.motionX += transX;
//            cart1.motionZ += transZ;
//        }
//
//        if(adj2) {
//            cart2.motionX -= transX;
//            cart2.motionZ -= transZ;
//        }

        // Spring force

        float optDist = getOptimalDistance();
        double stretch = dist - optDist;
//        stretch = Math.max(0.0, stretch);
//        if(Math.abs(stretch) > 0.5) {
//            stretch *= 2;
//        }

        double stiffness = STIFFNESS;
        double springX = stiffness * stretch * unit.getX();
        double springZ = stiffness * stretch * unit.getY();

        springX = limitForce(springX);
        springZ = limitForce(springZ);

        if (adj1) {
            cart1.motionX += springX;
            cart1.motionZ += springZ;
        }

        if (adj2) {
            cart2.motionX -= springX;
            cart2.motionZ -= springZ;
        }

        // Damping

        Vec2D cart1Vel = new Vec2D(cart1.motionX, cart1.motionZ);
        Vec2D cart2Vel = new Vec2D(cart2.motionX, cart2.motionZ);

        double dot = Vec2D.subtract(cart2Vel, cart1Vel).dotProduct(unit);

        double damping = DAMPING;
        double dampX = damping * dot * unit.getX();
        double dampZ = damping * dot * unit.getY();

        dampX = limitForce(dampX);
        dampZ = limitForce(dampZ);

        if (adj1) {
            cart1.motionX += dampX;
            cart1.motionZ += dampZ;
        }

        if (adj2) {
            cart2.motionX -= dampX;
            cart2.motionZ -= dampZ;
        }
    }

    private double limitForce(double force) {
        return Math.copySign(Math.min(Math.abs(force), FORCE_LIMITER), force);
    }

    /**
     * This function inspects the couplings and determines if any physics
     * adjustments need to be made.
     *
     * @param cart EntityMinecart
     */
    private void adjustCart(EntityMinecart cart) {
        if (isLaunched(cart))
            return;

        boolean coupledA = adjustCoupledCart(cart, CouplingType.COUPLING_A);
        boolean coupledB = adjustCoupledCart(cart, CouplingType.COUPLING_B);
        boolean coupled = coupledA || coupledB;

        // Drag
        if (coupled) {
            cart.motionX *= COUPLED_DRAG;
            cart.motionZ *= COUPLED_DRAG;
        }

        // Speed & End Drag
        Train.get(cart).ifPresent(train -> {
            if (train.isTrainEnd(cart)) {
                train.refreshMaxSpeed();
            }
        });

    }

    private boolean adjustCoupledCart(EntityMinecart cart, CouplingType couplingType) {
        boolean coupled = false;
        CouplingManager lm = CouplingManager.INSTANCE;
        EntityMinecart coupledCart = lm.getCoupledCart(cart, couplingType);
        if (coupledCart != null) {
            // sanity check to ensure couplings are consistent
            if (!lm.areCoupled(cart, coupledCart)) {
                lm.repairCoupling(cart, coupledCart);
            }
            if (!isLaunched(coupledCart) && !isOnElevator()) {
                coupled = true;
                adjustVelocity(cart, coupledCart, couplingType);
            }
        }
        return coupled;
    }

    /**
     * This is our entry point, its triggered once per tick per cart.
     *
     * @param event MinecartUpdateEvent
     */
    @SubscribeEvent
    public void onMinecartUpdate(MinecartUpdateEvent event) {
        EntityMinecart cart = event.getMinecart();

        adjustCart(cart);
    }

    public boolean isLaunched(EntityMinecart cart) {
        int launched = cart.getEntityData().getInteger("Launched");
        return launched > 0;
    }

    public boolean isOnElevator() {
        return false;
    }
}

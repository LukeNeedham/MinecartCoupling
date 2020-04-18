/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package com.lukeneedham.minecartcoupling.common.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by CovertJaguar on 10/30/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public final class MathTools {

    public static final UUID NIL_UUID = new UUID(0, 0);
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();

    public static boolean isNil(UUID uuid) {
        return NIL_UUID.equals(uuid);
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        angle1 = normalizeAngle(angle1);
        angle2 = normalizeAngle(angle2);
        return normalizeAngle(angle1 - angle2);
    }

    public static float normalizeAngle(float angle) {
        while (angle < -180F) angle += 360F;
        while (angle > 180F) angle -= 360F;
        return angle;
    }
}

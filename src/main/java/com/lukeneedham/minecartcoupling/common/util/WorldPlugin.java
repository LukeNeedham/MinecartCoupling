/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class WorldPlugin {

    public static IBlockState getBlockState(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    public static Block getBlock(IBlockAccess world, BlockPos pos) {
        return getBlockState(world, pos).getBlock();
    }
}

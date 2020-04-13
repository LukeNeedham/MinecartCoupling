/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
@SuppressWarnings({"WeakerAccess"})
public final class TrackTools {
    public static final int TRAIN_LOCKDOWN_DELAY = 200;

    public static boolean isRailBlockAt(IBlockAccess world, BlockPos pos) {
        return isRailBlock(WorldPlugin.getBlock(world, pos));
    }

    public static boolean isRailBlock(@Nullable Block block) {
        return block instanceof BlockRailBase;
    }

    public static EnumRailDirection getTrackDirection(IBlockAccess world, BlockPos pos, IBlockState state) {
        return getTrackDirection(world, pos, state, null);
    }

    public static EnumRailDirection getTrackDirection(IBlockAccess world, BlockPos pos) {
        return getTrackDirection(world, pos, (EntityMinecart) null);
    }

    public static EnumRailDirection getTrackDirection(IBlockAccess world, BlockPos pos, @Nullable EntityMinecart cart) {
        IBlockState state = WorldPlugin.getBlockState(world, pos);
        return getTrackDirection(world, pos, state, cart);
    }

    public static EnumRailDirection getTrackDirection(IBlockAccess world, BlockPos pos, IBlockState state, @Nullable EntityMinecart cart) {
        if (state.getBlock() instanceof BlockRailBase)
            return ((BlockRailBase) state.getBlock()).getRailDirection(world, pos, state, cart);
        throw new IllegalArgumentException("Block was not a track");
    }

    public static EnumRailDirection getTrackDirectionRaw(IBlockAccess world, BlockPos pos) {
        IBlockState state = WorldPlugin.getBlockState(world, pos);
        return getTrackDirectionRaw(state);
    }

    public static EnumRailDirection getTrackDirectionRaw(IBlockState state) {
        IProperty<EnumRailDirection> prop = getRailDirectionProperty(state.getBlock());
        return state.getValue(prop);
    }

    public static IProperty<EnumRailDirection> getRailDirectionProperty(Block block) {
        if (block instanceof BlockRailBase)
            return ((BlockRailBase) block).getShapeProperty();
        throw new IllegalArgumentException("Block was not a track");
    }

    public static boolean setTrackDirection(World world, BlockPos pos, EnumRailDirection wanted) {
        IBlockState state = world.getBlockState(pos);
        IProperty<EnumRailDirection> prop = getRailDirectionProperty(state.getBlock());
        if (prop.getAllowedValues().contains(wanted)) {
            state = state.withProperty(prop, wanted);
            return world.setBlockState(pos, state);
        }
        return false;
    }

    //    public static boolean isTrackAt(IBlockAccess world, BlockPos pos, TrackKits track, Block block) {
//        return isTrackSpecAt(world, pos, track.getTrackKit(), block);
//    }
//
//    public static boolean isTrackAt(IBlockAccess world, BlockPos pos, TrackKits track) {
//        return isTrackSpecAt(world, pos, track.getTrackKit());
//    }

//    public static boolean isTrackSpecAt(IBlockAccess world, BlockPos pos, TrackKit trackKit, Block block) {
//        if (!RailcraftBlocks.TRACK.isEqual(block))
//            return false;
//        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
//        return isTrackSpec(tile, trackKit);
//    }

//    public static boolean isTrackSpecAt(IBlockAccess world, BlockPos pos, TrackKit trackKit) {
//        return isTrackSpecAt(world, pos, trackKit, WorldPlugin.getBlock(world, pos));
//    }

//    public static boolean isTrackSpec(TileEntity tile, TrackKit trackKit) {
//        return (tile instanceof TileTrackOutfitted) && ((TileTrackOutfitted) tile).getTrackKitInstance().getTrackKit() == trackKit;
//    }

//    public static boolean isTrackClassAt(IBlockAccess world, BlockPos pos, Class<? extends ITrackKitInstance> trackClass, Block block) {
//        if (!RailcraftBlocks.TRACK.isEqual(block))
//            return false;
//        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
//        return isTrackClass(tile, trackClass);
//    }

//    public static boolean isTrackClassAt(IBlockAccess world, BlockPos pos, Class<? extends ITrackKitInstance> trackClass) {
//        return isTrackClassAt(world, pos, trackClass, WorldPlugin.getBlock(world, pos));
//    }

//    public static boolean isTrackClass(TileEntity tile, Class<? extends ITrackKitInstance> trackClass) {
//        return (tile instanceof TileTrackOutfitted) && trackClass.isAssignableFrom(((TileTrackOutfitted) tile).getTrackKitInstance().getClass());
//    }

    public static void traverseConnectedTracks(World world, BlockPos pos, BiFunction<World, BlockPos, Boolean> action) {
        _traverseConnectedTracks(world, pos, action, new HashSet<>());
    }

    private static void _traverseConnectedTracks(World world, BlockPos pos, BiFunction<World, BlockPos, Boolean> action, Set<BlockPos> visited) {
        visited.add(pos);
        if (!action.apply(world, pos))
            return;
        getConnectedTracks(world, pos).stream().filter(p -> !visited.contains(p)).forEach(p -> _traverseConnectedTracks(world, p, action, visited));
    }

    public static Set<BlockPos> getConnectedTracks(IBlockAccess world, BlockPos pos) {
        Set<BlockPos> connectedTracks;
        EnumRailDirection shape;
        if (isRailBlockAt(world, pos))
            shape = getTrackDirectionRaw(world, pos);
        else
            shape = EnumRailDirection.NORTH_SOUTH;
        connectedTracks = Arrays.stream(EnumFacing.HORIZONTALS)
                .map(side -> getTrackConnectedTrackAt(world, pos.offset(side), shape))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return connectedTracks;
    }

    public static @Nullable BlockPos getTrackConnectedTrackAt(IBlockAccess world, BlockPos pos, EnumRailDirection shape) {
        if (isRailBlockAt(world, pos))
            return pos;
        BlockPos up = pos.up();
        if (shape.isAscending() && isRailBlockAt(world, up))
            return up;
        BlockPos down = pos.down();
        if (isRailBlockAt(world, down) && getTrackDirectionRaw(world, down).isAscending())
            return down;
        return null;
    }

//    public static Optional<TileTrackOutfitted> placeTrack(TrackKit track, World world, BlockPos pos, BlockRailBase.EnumRailDirection direction) {
//        BlockTrackOutfitted block = (BlockTrackOutfitted) RailcraftBlocks.TRACK.block();
//        TileTrackOutfitted tile = null;
//        if (block != null) {
//            WorldPlugin.setBlockState(world, pos, TrackToolsAPI.makeTrackState(block, direction));
//            tile = TrackTileFactory.makeTrackTile(track);
//            world.setTileEntity(pos, tile);
//        }
//        //noinspection ConstantConditions
//        return Optional.ofNullable(tile);
//    }

    public static EnumRailDirection getAxisAlignedDirection(Axis axis) {
        switch (axis) {
            case X:
                return EnumRailDirection.EAST_WEST;
            case Z:
                return EnumRailDirection.NORTH_SOUTH;
            default:
                throw new IllegalArgumentException("No corresponding direction for other axes");
        }
    }

    public static EnumRailDirection getAxisAlignedDirection(EnumFacing facing) {
        return getAxisAlignedDirection(facing.getAxis());
    }

    private TrackTools() {
    }

}

/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package com.lukeneedham.minecartcoupling.common.utils;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by CovertJaguar on 8/28/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class Streams {
    /**
     * Helper function to use when casting Streams.
     *
     * Works as both a mapper and a filter.
     *
     * Put it in a {@link Stream#flatMap(Function)} call.
     */
    public static <T, U> Function<T, Stream<U>> toType(Class<U> clazz) {
        return t -> clazz.isInstance(t) ? Stream.of(clazz.cast(t)) : Stream.empty();
    }
}

package com.lukeneedham.minecartcoupling.common.carts.coupling;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javax.annotation.Nullable;

public enum CouplingsInProgressDao {
    CLIENT_INSTANCE, SERVER_INSTANCE;

    /**
     * PlayerId to MinecartId
     */
    public BiMap<Integer, Integer> couplingInProgressMap = HashBiMap.create();

    /**
     * @return the player current making a coupling involving minecart param, or null if this minecart is not involved with any couplings in progress
     */
    public @Nullable
    Integer getPlayerCouplingMinecart(int minecartId) {
        return couplingInProgressMap.inverse().get(minecartId);
    }
}

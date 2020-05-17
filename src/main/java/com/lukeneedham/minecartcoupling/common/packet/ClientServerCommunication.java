package com.lukeneedham.minecartcoupling.common.packet;

import com.lukeneedham.minecartcoupling.Mod;
import com.lukeneedham.minecartcoupling.common.packet.couplingprogress.CouplingProgressState;
import com.lukeneedham.minecartcoupling.common.packet.couplingprogress.CouplingProgressStateMessage;
import com.lukeneedham.minecartcoupling.common.packet.couplingupdate.CouplingUpdate;
import com.lukeneedham.minecartcoupling.common.packet.couplingupdate.CouplingUpdateMessage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class ClientServerCommunication {
    public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(Mod.MOD_ID);

    public static void sendCouplingProgressState(int stateType, int cartId, int playerId) {
        channel.sendToAll(
                new CouplingProgressStateMessage(CouplingProgressState.from(stateType, cartId, playerId))
        );
    }

    public static void sendCouplingCreationUpdate(int cart1Id, int cart2Id) {
        channel.sendToAll(new CouplingUpdateMessage(new CouplingUpdate.Created(cart1Id, cart2Id)));
    }

    public static void sendCouplingBrokenUpdate(int cart1Id, int cart2Id) {
        channel.sendToAll(new CouplingUpdateMessage(new CouplingUpdate.Broken(cart1Id, cart2Id)));
    }

    public static void sendAllCouplingsBrokenUpdate(int cartId) {
        channel.sendToAll(new CouplingUpdateMessage(new CouplingUpdate.AllBroken(cartId)));
    }
}

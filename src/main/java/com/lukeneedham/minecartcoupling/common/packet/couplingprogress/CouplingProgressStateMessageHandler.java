package com.lukeneedham.minecartcoupling.common.packet.couplingprogress;

import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsInProgressDao;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client side */
public class CouplingProgressStateMessageHandler implements IMessageHandler<CouplingProgressStateMessage, IMessage> {

    @Override
    public IMessage onMessage(CouplingProgressStateMessage message, MessageContext ctx) {
        CouplingProgressState state = message.state;
        if (state.type == CouplingProgressState.Started.TYPE) {
            CouplingsInProgressDao.CLIENT_INSTANCE.couplingInProgressMap.put(state.playerId, state.cartId);
        } else {
            CouplingsInProgressDao.CLIENT_INSTANCE.couplingInProgressMap.remove(state.playerId);
        }

        return null;
    }
}
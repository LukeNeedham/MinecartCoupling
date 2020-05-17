package com.lukeneedham.minecartcoupling.common.packet.couplingprogress;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CouplingProgressStateMessage implements IMessage {

    public CouplingProgressState state;

    public CouplingProgressStateMessage() {
    }

    public CouplingProgressStateMessage(CouplingProgressState state) {
        this.state = state;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int type = buf.readInt();
        int cartId = buf.readInt();
        int playerId = buf.readInt();
        state = CouplingProgressState.from(type, cartId, playerId);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(state.type);
        buf.writeInt(state.cartId);
        buf.writeInt(state.playerId);
    }
}
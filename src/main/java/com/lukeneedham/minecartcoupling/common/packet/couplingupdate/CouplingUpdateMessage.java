package com.lukeneedham.minecartcoupling.common.packet.couplingupdate;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CouplingUpdateMessage implements IMessage {

    public CouplingUpdate update;

    public CouplingUpdateMessage() {
    }

    public CouplingUpdateMessage(CouplingUpdate update) {
        this.update = update;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int type = buf.readInt();
        switch (type) {
            case CouplingUpdate.Created.TYPE: {
                int cart1Id = buf.readInt();
                int cart2Id = buf.readInt();
                update = new CouplingUpdate.Created(cart1Id, cart2Id);
                break;
            }
            case CouplingUpdate.Broken.TYPE: {
                int cart1Id = buf.readInt();
                int cart2Id = buf.readInt();
                update = new CouplingUpdate.Broken(cart1Id, cart2Id);
                break;
            }
            case CouplingUpdate.AllBroken.TYPE: {
                int cartId = buf.readInt();
                update = new CouplingUpdate.AllBroken(cartId);
                break;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(update.type);
        switch (update.type) {
            case CouplingUpdate.Created.TYPE: {
                CouplingUpdate.Created createdUpdate = (CouplingUpdate.Created) update;
                buf.writeInt(createdUpdate.cart1Id);
                buf.writeInt(createdUpdate.cart2Id);
                break;
            }
            case CouplingUpdate.Broken.TYPE: {
                CouplingUpdate.Broken brokenUpdate = (CouplingUpdate.Broken) update;
                buf.writeInt(brokenUpdate.cart1Id);
                buf.writeInt(brokenUpdate.cart2Id);
                break;
            }
            case CouplingUpdate.AllBroken.TYPE: {
                CouplingUpdate.AllBroken allBrokenUpdate = (CouplingUpdate.AllBroken) update;
                buf.writeInt(allBrokenUpdate.cartId);
                break;
            }
        }
    }
}
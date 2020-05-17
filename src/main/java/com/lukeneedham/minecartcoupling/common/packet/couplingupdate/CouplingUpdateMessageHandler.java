package com.lukeneedham.minecartcoupling.common.packet.couplingupdate;

import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsDao;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client side
 */
public class CouplingUpdateMessageHandler implements IMessageHandler<CouplingUpdateMessage, IMessage> {

    @Override
    public IMessage onMessage(CouplingUpdateMessage message, MessageContext ctx) {
        CouplingUpdate update = message.update;
        switch (update.type) {
            case CouplingUpdate.Created.TYPE: {
                CouplingUpdate.Created createdUpdate = (CouplingUpdate.Created) update;
                EntityMinecart cart1 = getMinecartFromId(createdUpdate.cart1Id);
                EntityMinecart cart2 = getMinecartFromId(createdUpdate.cart2Id);
                CouplingsDao.CLIENT_INSTANCE.createCoupling(cart1, cart2);
                break;
            }
            case CouplingUpdate.Broken.TYPE: {
                CouplingUpdate.Broken brokenUpdate = (CouplingUpdate.Broken) update;
                EntityMinecart cart1 = getMinecartFromId(brokenUpdate.cart1Id);
                EntityMinecart cart2 = getMinecartFromId(brokenUpdate.cart2Id);
                CouplingsDao.CLIENT_INSTANCE.breakCoupling(cart1, cart2);
                break;
            }
            case CouplingUpdate.AllBroken.TYPE: {
                CouplingUpdate.AllBroken allBrokenUpdate = (CouplingUpdate.AllBroken) update;
                EntityMinecart cart = getMinecartFromId(allBrokenUpdate.cartId);
                CouplingsDao.CLIENT_INSTANCE.breakCouplings(cart);
                break;
            }
        }

        return null;
    }

    private EntityMinecart getMinecartFromId(int entityId) {
        return (EntityMinecart) Minecraft.getMinecraft().world.getEntityByID(entityId);
    }
}
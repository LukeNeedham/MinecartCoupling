package com.lukeneedham.minecartcoupling.common.carts;

import com.google.common.collect.BiMap;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsDao;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsInProgressDao;
import com.lukeneedham.minecartcoupling.common.packet.ClientServerCommunication;
import com.lukeneedham.minecartcoupling.common.packet.couplingprogress.CouplingProgressState;
import com.lukeneedham.minecartcoupling.common.util.Game;
import com.lukeneedham.minecartcoupling.common.util.InvTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber()
public class MinecartInteractHandler {

    @SubscribeEvent
    public static void onMinecartHit(AttackEntityEvent event) {

        if (Game.isClient(event.getEntity().world)) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof EntityMinecart)) {
            return;
        }

        EntityMinecart minecart = (EntityMinecart) target;
        CouplingsDao lm = CouplingsDao.SERVER_INSTANCE;

        EntityMinecart coupledCartA = lm.getCoupledCartA(minecart);
        EntityMinecart coupledCartB = lm.getCoupledCartB(minecart);

        if (coupledCartA != null) {
            lm.breakCoupling(minecart, coupledCartA);
            ClientServerCommunication.sendCouplingBrokenUpdate(minecart.getEntityId(), coupledCartA.getEntityId());
            minecart.dropItem(Items.STRING, 1);
        }
        if (coupledCartB != null) {
            lm.breakCoupling(minecart, coupledCartB);
            ClientServerCommunication.sendCouplingBrokenUpdate(minecart.getEntityId(), coupledCartB.getEntityId());
            minecart.dropItem(Items.STRING, 1);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(MinecartInteractEvent event) {
        if (Game.isClient(event.getPlayer().world)) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        if (item != Items.STRING) {
            return;
        }
        event.setCanceled(true);
        EntityMinecart minecart = event.getMinecart();
        coupleCart(event.getPlayer(), itemStack, minecart);
    }

    private static void coupleCart(EntityPlayer player, ItemStack stringStack, EntityMinecart cart) {
        BiMap<Integer, Integer> couplingInProgressMap = CouplingsInProgressDao.SERVER_INSTANCE.couplingInProgressMap;
        @Nullable Integer lastId = couplingInProgressMap.remove(player.getEntityId());

        EntityMinecart last;
        if (lastId == null) {
            last = null;
        } else {
            last = (EntityMinecart) cart.world.getEntityByID(lastId);
        }

        int stateType = -1;
        if (last == null || !last.isEntityAlive()) {
            couplingInProgressMap.put(player.getEntityId(), cart.getEntityId());

            stateType = CouplingProgressState.Started.TYPE;
        } else {
            CouplingsDao lm = CouplingsDao.SERVER_INSTANCE;
            boolean used;
            if (lm.areCoupled(cart, last, false)) {
                lm.breakCoupling(cart, last);
                ClientServerCommunication.sendCouplingBrokenUpdate(cart.getEntityId(), last.getEntityId());
                used = true;
                stateType = CouplingProgressState.Broken.TYPE;
                cart.dropItem(Items.STRING, 1);
            } else {
                used = lm.createCoupling(last, cart);
                ClientServerCommunication.sendCouplingCreationUpdate(last.getEntityId(), cart.getEntityId());
                if (used) {
                    stateType = CouplingProgressState.Created.TYPE;
                    InvTools.depleteItem(stringStack);
                }
            }
            if (!used) {
                stateType = CouplingProgressState.Failed.TYPE;
            }
        }

        ClientServerCommunication.sendCouplingProgressState(stateType, cart.getEntityId(), player.getEntityId());
    }

}

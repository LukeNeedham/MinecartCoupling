package com.lukeneedham.minecartcoupling.common.carts;

import com.google.common.collect.MapMaker;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingManager;
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

import java.util.Map;

@Mod.EventBusSubscriber()
public class MinecartInteractHandler {

    private static final Map<EntityPlayer, EntityMinecart> couplingInProgessMap = new MapMaker().weakKeys().weakValues().makeMap();

    @SubscribeEvent
    public static void onMinecartHit(AttackEntityEvent event) {

//        if (!event.getEntity().world.isRemote) {
//            return;
//        }

        Entity target = event.getTarget();
        if (!(target instanceof EntityMinecart)) {
            return;
        }
        EntityMinecart minecart = (EntityMinecart) target;
        CouplingManager lm = CouplingManager.INSTANCE;

        EntityMinecart coupledCartA = lm.getCoupledCartA(minecart);
        EntityMinecart coupledCartB = lm.getCoupledCartB(minecart);

        if (coupledCartA != null) {
            lm.breakCoupling(minecart, coupledCartA);
            minecart.dropItem(Items.STRING, 1);
        }
        if (coupledCartB != null) {
            lm.breakCoupling(minecart, coupledCartB);
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

    // TODO: Instead of printing in chat, indicate these states to the user graphically, with a string entity:
    // See net.minecraft.entity.EntityFishHook and net.minecraft.client.renderer.entity.RenderFish
    private static void coupleCart(EntityPlayer player, ItemStack stringStack, EntityMinecart cart) {
        EntityMinecart last = couplingInProgessMap.remove(player);

        if (last != null && last.isEntityAlive()) {
            CouplingManager lm = CouplingManager.INSTANCE;
            boolean used;
            if (lm.areCoupled(cart, last, false)) {
                lm.breakCoupling(cart, last);
                used = true;
                player.sendMessage(new TextComponentString("Coupling broken"));
                cart.dropItem(Items.STRING, 1);
            } else {
                used = lm.createCoupling(last, cart);
                if (used) {
                    player.sendMessage(new TextComponentString("Coupling created"));
                    InvTools.depleteItem(stringStack);
                }
            }
            if (!used) {
                player.sendMessage(new TextComponentString("Coupling failed"));
            }
        } else {
            couplingInProgessMap.put(player, cart);
            player.sendMessage(new TextComponentString("Coupling started"));
        }
    }

}

package com.lukeneedham.minecartcoupling.common.items;

import com.google.common.collect.MapMaker;
import com.lukeneedham.minecartcoupling.common.carts.LinkageManager;
import com.lukeneedham.minecartcoupling.common.utils.Game;
import com.lukeneedham.minecartcoupling.common.utils.InvTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

@Mod.EventBusSubscriber()
public class StringInteractMinecartHandler {

    private static final Map<EntityPlayer, EntityMinecart> linkMap = new MapMaker().weakKeys().weakValues().makeMap();

    @SubscribeEvent
    public static void onMinecartHit(AttackEntityEvent event) {
        if (!event.getEntity().world.isRemote) {
            return;
        }

        Entity target = event.getTarget();
        if (!(target instanceof EntityMinecart)) {
            return;
        }
        EntityMinecart minecart = (EntityMinecart) target;
        LinkageManager lm = LinkageManager.INSTANCE;

        EntityMinecart linkedCartA = lm.getLinkedCartA(minecart);
        EntityMinecart linkedCartB = lm.getLinkedCartB(minecart);

        if (linkedCartA != null) {
            lm.breakLink(minecart, linkedCartA);
            minecart.dropItem(Items.STRING, 1);
        }
        if (linkedCartB != null) {
            lm.breakLink(minecart, linkedCartB);
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
        linkCart(event.getPlayer(), event.getHand(), itemStack, minecart, item);
    }

    private static void linkCart(EntityPlayer player, EnumHand hand, ItemStack stack, EntityMinecart cart, Item stringItem) {
        EntityMinecart last = linkMap.remove(player);

        if (last != null && last.isEntityAlive()) {
            LinkageManager lm = LinkageManager.INSTANCE;
            boolean used;
            if (lm.areLinked(cart, last, false)) {
                lm.breakLink(cart, last);
                used = true;
                player.sendMessage(new TextComponentString("Coupling broken"));
                cart.dropItem(Items.STRING, 1);
            } else {
                used = lm.createLink(last, cart);
                if (used) {
                    player.sendMessage(new TextComponentString("Coupling created"));
                    InvTools.depleteItem(stack);
                }
            }
            if (!used) {
                player.sendMessage(new TextComponentString("Coupling failed"));
            }
        } else {
            linkMap.put(player, cart);
            player.sendMessage(new TextComponentString("Coupling started"));
        }
    }

}

package com.lukeneedham.minecartcoupling;

import com.lukeneedham.minecartcoupling.client.RenderCart;
import com.lukeneedham.minecartcoupling.common.carts.MinecartHooks;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingHandler;
import com.lukeneedham.minecartcoupling.common.packet.ClientServerCommunication;
import com.lukeneedham.minecartcoupling.common.packet.couplingprogress.CouplingProgressStateMessage;
import com.lukeneedham.minecartcoupling.common.packet.couplingprogress.CouplingProgressStateMessageHandler;
import com.lukeneedham.minecartcoupling.common.packet.couplingupdate.CouplingUpdateMessage;
import com.lukeneedham.minecartcoupling.common.packet.couplingupdate.CouplingUpdateMessageHandler;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@net.minecraftforge.fml.common.Mod(modid = Mod.MOD_ID, name = Mod.NAME, version = Mod.VERSION)
public class Mod {
    public static final String MOD_ID = "minecartcoupling";
    public static final String NAME = "Minecart Coupling";
    public static final String VERSION = "0.1.5";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(MinecartHooks.INSTANCE);
        MinecraftForge.EVENT_BUS.register(CouplingHandler.getInstance());

        ClientServerCommunication.channel.registerMessage(
                CouplingProgressStateMessageHandler.class,
                CouplingProgressStateMessage.class,
                1,
                Side.CLIENT
        );
        ClientServerCommunication.channel.registerMessage(
                CouplingUpdateMessageHandler.class,
                CouplingUpdateMessage.class,
                2,
                Side.CLIENT
        );

        EntityMinecart.setCollisionHandler(MinecartHooks.INSTANCE);

        if (event.getSide() == Side.CLIENT) {
            RenderingRegistry.registerEntityRenderingHandler(EntityMinecart.class, RenderCart::new);
        }
    }
}

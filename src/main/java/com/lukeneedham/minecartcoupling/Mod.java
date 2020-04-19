package com.lukeneedham.minecartcoupling;

import com.lukeneedham.minecartcoupling.common.carts.CouplingRenderer;
import com.lukeneedham.minecartcoupling.common.carts.MinecartHooks;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingHandler;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@net.minecraftforge.fml.common.Mod(modid = Mod.MOD_ID, name = Mod.NAME, version = Mod.VERSION)
public class Mod {
    public static final String MOD_ID = "minecartcoupling";
    public static final String NAME = "Minecart Coupling";
    public static final String VERSION = "0.1.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(MinecartHooks.INSTANCE);
        MinecraftForge.EVENT_BUS.register(CouplingHandler.getInstance());

        EntityMinecart.setCollisionHandler(MinecartHooks.INSTANCE);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CouplingRenderer());
    }
}

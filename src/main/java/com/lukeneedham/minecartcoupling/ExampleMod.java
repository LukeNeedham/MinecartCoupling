package com.lukeneedham.minecartcoupling;

import com.lukeneedham.minecartcoupling.common.carts.LinkageHandler;
import com.lukeneedham.minecartcoupling.common.carts.MinecartHooks;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ExampleMod.MODID, name = ExampleMod.NAME, version = ExampleMod.VERSION)
public class ExampleMod {
    public static final String MODID = "minecartcoupling";
    public static final String NAME = "Minecart Coupling";
    public static final String VERSION = "0.1.0";


    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(MinecartHooks.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LinkageHandler.getInstance());

        EntityMinecart.setCollisionHandler(MinecartHooks.INSTANCE);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // some example code
    }
}

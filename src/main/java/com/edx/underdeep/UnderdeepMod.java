package com.edx.underdeep;

import com.edx.underdeep.registry.ModBlocks;
import com.edx.underdeep.registry.ModItems;
import com.edx.underdeep.world.UnderdeepWorldgen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod(UnderdeepMod.MODID)
public class UnderdeepMod {
    public static final String MODID = "underdeep";

    public UnderdeepMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        MinecraftForge.EVENT_BUS.register(new UnderdeepWorldgen());
    }
}

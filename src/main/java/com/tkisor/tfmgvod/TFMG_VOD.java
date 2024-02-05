package com.tkisor.tfmgvod;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TFMG_VOD.MOD_ID)
public class TFMG_VOD {
    public static final String MOD_ID = "tfmgvod";

    public static final Logger LOGGER = LogManager.getLogger();

    public TFMG_VOD() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    }

}

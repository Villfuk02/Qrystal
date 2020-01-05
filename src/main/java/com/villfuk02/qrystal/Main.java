package com.villfuk02.qrystal;

import com.villfuk02.qrystal.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Main.MODID, name = Main.NAME, version = Main.VERSION)
public class Main
{
    public static final String MODID = "qrystal";
    public static final String NAME = "Qrystal";
    public static final String VERSION = "@VERSION@";
    
    public static final String MOD_ADDR = "com.villfuk02.qrystal";
    public static final String CL_PROXY = MOD_ADDR + ".proxy.ClientProxy";
    public static final String CM_PROXY = MOD_ADDR + ".proxy.CommonProxy";

    private static Logger logger;
    
    @Mod.Instance
    public static Main instance;
    @SidedProxy(clientSide = CL_PROXY, serverSide = CM_PROXY)
    public static CommonProxy proxy;
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    
    }

    
}

package com.villfuk02.qrystal;

import com.villfuk02.qrystal.proxy.CommonProxy;
import com.villfuk02.qrystal.world.ModWorldGen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Main.MODID, name = Main.NAME, version = Main.VERSION)
public class Main {
    public static final String MODID = "qrystal";
    public static final String NAME = "Qrystal";
    public static final String VERSION = "@VERSION@";
    
    public static final String MOD_ADDR = "com.villfuk02.qrystal";
    public static final String CL_PROXY = MOD_ADDR + ".proxy.ClientProxy";
    public static final String CM_PROXY = MOD_ADDR + ".proxy.CommonProxy";
    
    public enum EnumColor {
        QLEAR("qlear", 0), QERI("qeri", 1), QAWA("qawa", 2), QINI("qini", 3), QITAE("qitae", 4), QOID("qoid", 5), QONDO("qondo", 6), QALB("qalb", 7);
        
        private final String name;
        private final int meta;
        
        private EnumColor(String name, int meta) {
            this.name = name;
            this.meta = meta;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public int getMeta() {
            return meta;
        }
        
        public static EnumColor fromMeta(int meta) {
            switch(meta) {
                default:
                    return QLEAR;
                case 1:
                    return QERI;
                case 2:
                    return QAWA;
                case 3:
                    return QINI;
                case 4:
                    return QITAE;
                case 5:
                    return QOID;
                case 6:
                    return QONDO;
                case 7:
                    return QALB;
            }
        }
    }
    
    private static Logger logger;
    
    @Mod.Instance
    public static Main instance;
    @SidedProxy(clientSide = CL_PROXY, serverSide = CM_PROXY)
    public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        GameRegistry.registerWorldGenerator(new ModWorldGen(), 3);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    
    }
    
    
}

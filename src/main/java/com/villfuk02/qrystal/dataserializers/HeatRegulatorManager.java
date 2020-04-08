package com.villfuk02.qrystal.dataserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.villfuk02.qrystal.Main;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class HeatRegulatorManager extends JsonReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    
    public static Map<ResourceLocation, HeatRegulatorInfo> heat_regulators = new HashMap<>();
    
    public static class HeatRegulatorInfo {
        public final int temperature;
        public final int time;
        public final boolean hasContainer;
        public final ResourceLocation container;
        
        public HeatRegulatorInfo(int temperature, int time) {
            this.time = time;
            this.temperature = temperature;
            hasContainer = false;
            container = new ResourceLocation("");
        }
        
        public HeatRegulatorInfo(int temperature, int time, ResourceLocation container) {
            this.time = time;
            this.temperature = temperature;
            hasContainer = true;
            this.container = container;
        }
    }
    
    public HeatRegulatorManager() {
        super(GSON, "qrystal_heat_regulators");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        for(Map.Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if(resourcelocation.getPath().startsWith("_"))
                continue;
            
            try {
                if(!net.minecraftforge.common.crafting.CraftingHelper.processConditions(entry.getValue(), "conditions")) {
                    Main.LOGGER.info("Skipping loading heat regulator {} as it's conditions were not met", resourcelocation);
                    continue;
                }
                deserialize(resourcelocation, entry.getValue());
            } catch(IllegalArgumentException | JsonParseException jsonparseexception) {
                Main.LOGGER.error("Parsing error loading heat regulator {}", resourcelocation, jsonparseexception);
            }
        }
        Main.LOGGER.info("Loaded {} heat regulators", heat_regulators.size());
    }
    
    public static void deserialize(ResourceLocation id, JsonObject json) {
        String name = JSONUtils.getString(json, "item", "");
        int temperature = JSONUtils.getInt(json, "temperature", 0);
        int time = JSONUtils.getInt(json, "time", 1600);
        String container = JSONUtils.getString(json, "container", "");
        
        Main.LOGGER.info("Registering heat regulator " + name + " from data file " + id.toString());
        
        if(name.isEmpty())
            return;
        
        ResourceLocation rl = new ResourceLocation(name);
        if(heat_regulators.containsKey(rl))
            heat_regulators.remove(rl);
        if(container.isEmpty())
            heat_regulators.put(rl, new HeatRegulatorInfo(temperature, time));
        else
            heat_regulators.put(rl, new HeatRegulatorInfo(temperature, time, new ResourceLocation(container)));
    }
}

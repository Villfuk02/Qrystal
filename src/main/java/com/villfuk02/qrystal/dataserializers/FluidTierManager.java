package com.villfuk02.qrystal.dataserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.villfuk02.qrystal.Main;
import javafx.util.Pair;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FluidTierManager extends JsonReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    
    public static Map<ResourceLocation, Pair<Integer, Integer>> solvents = new HashMap<>();
    
    
    public FluidTierManager() {
        super(GSON, "qrystal_solvents");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        for(Map.Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if(resourcelocation.getPath().startsWith("_"))
                continue;
            
            try {
                if(!net.minecraftforge.common.crafting.CraftingHelper.processConditions(entry.getValue(), "conditions")) {
                    Main.LOGGER.info("Skipping loading solvent {} as it's conditions were not met", resourcelocation);
                    continue;
                }
                deserialize(resourcelocation, entry.getValue());
            } catch(IllegalArgumentException | JsonParseException jsonparseexception) {
                Main.LOGGER.error("Parsing error loading solvent {}", resourcelocation, jsonparseexception);
            }
        }
        Main.LOGGER.info("Loaded {} fluids as solvents", solvents.size());
    }
    
    public static void deserialize(ResourceLocation id, JsonObject json) {
        String name = JSONUtils.getString(json, "fluid", "");
        int tier = JSONUtils.getInt(json, "tier", -1);
        int priority = JSONUtils.getInt(json, "priority", 0);
        
        Main.LOGGER.info("Registering solvent " + name + " from data file " + id.toString());
        
        if(name.isEmpty())
            return;
        
        ResourceLocation rl = new ResourceLocation(name);
        if(solvents.containsKey(rl) && solvents.get(rl).getValue() < priority)
            solvents.remove(rl);
        
        solvents.put(rl, new Pair<>(tier, priority));
    }
}

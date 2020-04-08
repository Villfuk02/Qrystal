package com.villfuk02.qrystal.dataserializers;

import com.google.gson.*;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.JSONUtil;
import com.villfuk02.qrystal.util.MaterialInfo;
import com.villfuk02.qrystal.util.RecipeUtil;
import javafx.util.Pair;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MaterialManager extends JsonReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    
    public static List<String> material_names = new ArrayList<>();
    public static Map<String, MaterialInfo> materials = new HashMap<>();
    public static Map<ResourceLocation, MaterialInfo.Crushable> crushable = new HashMap<>();
    
    /*
            this.name = name;                    //Material ID
            this.replaceData = replaceData;      //Replace temp, locale, seed and colors? (empty entries will not replace existing ones)
            this.dataPriority = dataPriority;    //If replace = true, the version with higher priority is kept
            this.replaceIO = replaceIO;          //Replace crushable and outputs?
            this.IOPriority = IOPriority;        //If replace = true, the version with higher priority is kept
            this.locale = locale;                //Localisation string for the material name display
            this.temp = temp;                    //Temperature, keep it in range from -1000 to 2000, 0 is ambient, 1000 is lava, -100 is ice
            this.seed = seed;                    //Which material seed is used to grow this material, only accepts qrystal materials, "qlear" = none
            this.primaryColor = primaryColor;    //Primary color eg. "3EF20C"
            this.secondaryColor = secondaryColor;//Secondary color eg. "3EF20C"
            this.crushables = crushables;        //Items that can be crushed into material dust and their min and max value
            this.outputs = outputs;              //Items that can crystals cut into and their values
                                                 //Standard values are 23040 = nugget, 207360 = ingot, 1866240 = block
    */
    
    public MaterialManager() {
        super(GSON, "qrystal_materials");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        
        for(Map.Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if(resourcelocation.getPath().startsWith("_"))
                continue;
            
            try {
                if(!net.minecraftforge.common.crafting.CraftingHelper.processConditions(entry.getValue(), "conditions")) {
                    Main.LOGGER.info("Skipping loading material {} as it's conditions were not met", resourcelocation);
                    continue;
                }
                deserialize(resourcelocation, entry.getValue());
            } catch(IllegalArgumentException | JsonParseException jsonparseexception) {
                Main.LOGGER.error("Parsing error loading material {}", resourcelocation, jsonparseexception);
            }
        }
        Main.LOGGER.info("Loaded {} materials", material_names.size());
    }
    
    public static void deserialize(ResourceLocation id, JsonObject json) {
        String name = JSONUtils.getString(json, "name", "");
        boolean replaceData = JSONUtils.getBoolean(json, "replace_data", false);
        boolean replaceIO = JSONUtils.getBoolean(json, "replace_IO", false);
        int dataPriority = JSONUtils.getInt(json, "priority_data", 0);
        int IOPriority = JSONUtils.getInt(json, "priority_IO", 0);
        String lang = JSONUtils.getString(json, "locale", name);
        int temp = JSONUtils.getInt(json, "temp", 0);
        String seed = JSONUtils.getString(json, "seed", "qlear");
        String primaryColor = JSONUtils.getString(json, "color_primary", "FF00FF");
        String secondaryColor = JSONUtils.getString(json, "color_secondary", "000000");
        MaterialInfo.Crushable[] crushables;
        if(json.has("crushable")) {
            JsonArray inputArray = JSONUtils.getJsonArray(json, "crushable");
            crushables = StreamSupport.stream(inputArray.spliterator(), false)
                    .map(e -> new MaterialInfo.Crushable(JSONUtils.getString(e.getAsJsonObject(), "item", ""), JSONUtil.getLong(e.getAsJsonObject(), "min", 0),
                                                         JSONUtil.getLong(e.getAsJsonObject(), "max", 0)))
                    .toArray(MaterialInfo.Crushable[]::new);
        } else {
            crushables = new MaterialInfo.Crushable[0];
        }
        Pair<ResourceLocation, Long>[] outputs;
        if(json.has("outputs")) {
            JsonArray outputArray = JSONUtils.getJsonArray(json, "outputs");
            List<Pair<ResourceLocation, Long>> o = StreamSupport.stream(outputArray.spliterator(), false)
                    .map(e -> new javafx.util.Pair<>(new ResourceLocation(JSONUtils.getString(e.getAsJsonObject(), "item", "")),
                                                     JSONUtil.getLong(e.getAsJsonObject(), "value", 207360)))
                    .collect(Collectors.toList());
            outputs = o.toArray(new Pair[0]);
        } else {
            
            outputs = new Pair[0];
        }
        
        Main.LOGGER.info("Registering material " + name + " from data file " + id.toString());
        
        if(name.isEmpty())
            return;
        MaterialInfo m;
        if(material_names.contains(name)) {
            m = materials.get(name);
            if(replaceData) {
                if(dataPriority > m.dataPriority) {
                    m.dataPriority = dataPriority;
                    if(!lang.isEmpty())
                        m.lang = lang;
                    if(temp >= -1000 && temp <= 2000)
                        m.temp = temp;
                    if(!seed.isEmpty() && RecipeUtil.isQrystalMaterial(seed, true))
                        m.seed = CrystalUtil.Color.valueOf(seed);
                    if(!primaryColor.isEmpty() && !secondaryColor.isEmpty())
                        m.color = new javafx.util.Pair<>(Integer.parseInt(primaryColor, 16), Integer.parseInt(secondaryColor, 16));
                }
            }
            if(replaceIO) {
                if(IOPriority > m.IOPriority) {
                    m.IOPriority = IOPriority;
                    m.outputs = new HashMap<>();
                    for(ResourceLocation rl : crushable.keySet()) {
                        if(crushable.get(rl).material.equals(name))
                            crushable.remove(rl);
                    }
                }
            }
        } else {
            material_names.add(name);
            m = new MaterialInfo(name, lang);
            materials.put(name, m);
            m.dataPriority = dataPriority;
            m.temp = temp;
            m.seed = CrystalUtil.Color.fromString(seed);
            m.color = new javafx.util.Pair<>(Integer.parseInt(primaryColor, 16), Integer.parseInt(secondaryColor, 16));
        }
        
        if(m.outputs == null)
            m.outputs = new HashMap<>();
        for(Pair<ResourceLocation, Long> output : outputs) {
            m.outputs.putIfAbsent(output.getKey(), output.getValue());
        }
        for(MaterialInfo.Crushable value : crushables) {
            crushable.putIfAbsent(new ResourceLocation(value.material), new MaterialInfo.Crushable(name, value.min, value.max));
        }
    }
}

package com.villfuk02.qrystal.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;

public class JSONUtil {
    
    public static long getLong(JsonObject o, String k, long f) {
        return o.has(k) ? getLong(o.get(k), k) : f;
    }
    
    public static long getLong(JsonElement e, String k) {
        if(e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            return e.getAsLong();
        } else {
            throw new JsonSyntaxException("Expected " + k + " to be a Int, was " + JSONUtils.toString(e));
        }
    }
}

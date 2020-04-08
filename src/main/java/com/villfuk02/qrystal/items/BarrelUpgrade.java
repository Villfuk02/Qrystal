package com.villfuk02.qrystal.items;

public class BarrelUpgrade extends ItemBase {
    public final int level;
    public final String target;
    
    public BarrelUpgrade(int level, String target) {
        super("upgrade_" + level);
        this.level = level;
        this.target = target;
    }
}

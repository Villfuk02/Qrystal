package com.villfuk02.qrystal.tileentity;

public interface IBurnerEvaporator {
    int getHeatLeft();
    
    int getHeatTotal();
    
    int getSpeed();
    
    void restoreHeat();
}

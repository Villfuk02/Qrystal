package com.villfuk02.qrystal.tileentity;

public interface IPowerConsumer {
    void setPower(byte power);
    
    byte getPower();
    
    byte getRequiredPower();
    
    default String getPowerString() {
        return (getPower() >= getRequiredPower() ? getRequiredPower() : getPower()) + "/" + getRequiredPower();
    }
}

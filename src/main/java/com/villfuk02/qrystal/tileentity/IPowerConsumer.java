package com.villfuk02.qrystal.tileentity;

public interface IPowerConsumer {
    abstract void setPower(byte power);
    
    abstract byte getPower();
}

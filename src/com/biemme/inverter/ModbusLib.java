package com.biemme.inverter;

public class ModbusLib {
    public native static long openCom();
    public native static long ReadHoldingRegisters(int fd, int id, int address, int no_of_registers,int []holdingRegs);
    public native static long WriteMultipleRegisters(int fd, int id, int address, int no_of_registers,int []holdingRegs);
    public native static long closeCom(int fd);
    static{
            System.loadLibrary("com_biemme_inverter_ModbusLib");
    }
}

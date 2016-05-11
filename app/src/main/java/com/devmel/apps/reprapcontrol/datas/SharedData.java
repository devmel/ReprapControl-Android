package com.devmel.apps.reprapcontrol.datas;


import com.devmel.storage.android.UserPrefs;

import java.util.HashSet;
import java.util.Locale;

public class SharedData {
    private String portName = null;
    private int baudRate = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int parity = 0;
    private int steps = 0;
    private int laserPower = 255;

    public boolean connect = false;
    public String firmware = null;
    private double[] position = new double[]{0.0,0.0,0.0,0.0};
    private double[] temperature = new double[]{0.0,0.0,0.0,0.0};

    public boolean sdFilesRecord = false;
    public HashSet<String> sdFiles = new HashSet<>();
    private String sdFileSelected = null;
    private int[] sdStatus = new int[]{0,0};

    public boolean[] linkbusStatus = new boolean[]{false, false, false};

    public void setSDFileSelected(String filename){
        sdFileSelected = filename;
    }
    public String getSDFileSelected(){
        return sdFileSelected;
    }
    public void setTemperatureExtruder(double current) {
        temperature[0] = current;
    }
    public void setTemperatureExtruder(double current, double set) {
        temperature[0] = current;
        temperature[1] = set;
    }
    public double[] getTemperatureExtruder() {
        return new double[]{temperature[0], temperature[1]};
    }
    public void setTemperatureBed(double current) {
        temperature[2] = current;
    }
    public void setTemperatureBed(double current, double set) {
        temperature[2] = current;
        temperature[3] = set;
    }
    public double[] getTemperatureBed() {
        return new double[]{temperature[2], temperature[3]};
    }
    public void setPosition(double x, double y, double z, double e) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
        position[3] = e;
    }
    public double getPositionX(){
        return position[0];
    }
    public double getPositionY(){
        return position[1];
    }
    public double getPositionZ(){
        return position[2];
    }
    public double getPositionE(){
        return position[3];
    }

    public void setSDStatus(int position, int total) {
        if (total >= 0 && position >= 0 && position <= total) {
            sdStatus[0] = position;
            sdStatus[1] = total;
        }
    }
    public int getSDStatusPosition() {
        return sdStatus[0];
    }
    public int getSDStatusTotal() {
        return sdStatus[1];
    }


    public void setPortName(String name) {
        this.portName = name;
    }

    public String getPortName() {
        return this.portName;
    }

    public void setBaudRate(int baudRate){
        this.baudRate=baudRate;
    }
    public void setDataBits(int dataBits){
        this.dataBits=dataBits;
    }
    public void setStopbits(int stopBits){
        this.stopBits=stopBits;
    }
    public void setParity(int parity){
        this.parity=parity;
    }

    public String getSteps() {
        return String.format(Locale.US , "%.1f", ((this.steps * this.steps) / (100.0f * 100.0f)) * (100 - 0.1) + 0.1);
    }
    public int getStepsBarValue(){
        return this.steps;
    }
    public void setStepsBarValue(int value){
        this.steps = value;
    }
    public int getLaserpower(){
        return this.laserPower;
    }
    public void setLaserpower(int value){
        this.laserPower = value;
    }

    public int getBaudrate() {
        return this.baudRate;
    }

    public int getDatabits() {
        return this.dataBits;
    }

    public int getStopbits() {
        return this.stopBits;
    }

    public int getParity() {
        return this.parity;
    }

    public void load(UserPrefs userPrefs) {
        int baudrate = userPrefs.getInt("configBaudrate");
        if (baudrate >= 600 && baudrate <= 250000) {
            this.baudRate = baudrate;
        }
        int databits = userPrefs.getInt("configDatabits");
        if (databits >= 5 && databits <= 8) {
            this.dataBits = databits;
        }
        int stopbits = userPrefs.getInt("configStopbits");
        if (stopbits >= 1 && stopbits <= 2) {
            this.stopBits = stopbits;
        }
        int parity = userPrefs.getInt("configParity");
        if (parity >= 0 && parity <= 2) {
            this.parity = parity;
        }
        int step = userPrefs.getInt("configSteps");
        if (step >= 0 && step <= 100) {
            this.steps = step;
        }
        int laserPower = userPrefs.getInt("configLaserpower");
        if (laserPower >= 0 && laserPower <= 999999) {
            this.laserPower = laserPower;
        }
    }

    public void save(UserPrefs userPrefs) {
        userPrefs.saveInt("configBaudrate", baudRate);
        userPrefs.saveInt("configDatabits", dataBits);
        userPrefs.saveInt("configStopbits", stopBits);
        userPrefs.saveInt("configParity", parity);
        userPrefs.saveInt("configSteps", steps);
        userPrefs.saveInt("configLaserpower", laserPower);
    }
}
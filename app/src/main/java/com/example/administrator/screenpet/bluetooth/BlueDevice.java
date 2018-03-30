package com.example.administrator.screenpet.bluetooth;

/**
 * Created by asusz on 2018/3/29.
 */

public class BlueDevice {
    public String name;
    public String address;
    public int state;

    public BlueDevice() {
        name = "";
        address = "";
        state = 0;
    }

    public BlueDevice(String name, String address, int state) {
        this.name = name;
        this.address = address;
        this.state = state;
    }
}

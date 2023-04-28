package com.example.myntrafind;

import android.content.Context;
import android.os.Build;
import java.util.Locale;

public class DeviceInfo {
    private final String deviceName;
    private final String deviceModel;
    private final String deviceBrand;
    private final String deviceManufacturer;
    private final String deviceOsVersion;

    private final String deviceLanguage;

    public DeviceInfo(Context context) {
        deviceName = Build.MODEL;
        deviceModel = Build.MODEL;
        deviceBrand = Build.BRAND;
        deviceManufacturer = Build.MANUFACTURER;
        deviceOsVersion = Build.VERSION.RELEASE;

        deviceLanguage = Locale.getDefault().getLanguage();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public String getDeviceOsVersion() {
        return deviceOsVersion;
    }


    public String getDeviceLanguage() {
        return deviceLanguage;
    }

    public String getDeviceInfo() {

        return "Device name: " + deviceName +
                "\nModel: " + deviceModel +
                "\nBrand: " + deviceBrand +
                "\nManufacturer: " + deviceManufacturer +
                "\nOS version: " + deviceOsVersion +
                "\nLanguage: " + deviceLanguage;
    }
}

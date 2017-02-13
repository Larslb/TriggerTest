package com.example.larslb.triggertest;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by larslb on 27.01.2017.
 */

public class DeviceServices {

    private static final String TAG = DeviceServices.class.getSimpleName();
    public static HashMap<String,String> attributes = new HashMap<>();
    public static String GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static String GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";
    public static String ALERT_NOTIFICATION_SERVICE = "00001811-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String OUR_SERVICE = "0000f00d-1212-efde-1523-785fef13d123";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String ANALOG_ATTRIBUTE = "00001337-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT =  "00002a37-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_CHAR = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String OUR_SERVICE_NAME = "Our Service";


    static {
        attributes.put(OUR_SERVICE,OUR_SERVICE_NAME);
        attributes.put(GENERIC_ACCESS,"Generic Access");
        attributes.put(ALERT_NOTIFICATION_SERVICE,"Alert Notification Service");
        attributes.put(GENERIC_ATTRIBUTE,"Generic Attribute");
        attributes.put(DEVICE_INFORMATION,"Device Information");
        attributes.put(ANALOG_ATTRIBUTE,"Analog");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(HEART_RATE_SERVICE,"Heart Rate Service");
        attributes.put(BATTERY_SERVICE,"Battery Service");
        attributes.put(BATTERY_CHAR,"Battery Characterstic");
    }


    public static String lookup(String uuid,String defaultName) {
        String name = attributes.get(uuid);
        Log.d(TAG,"Name: " +name);
        return name == null ? defaultName : name;
    }
}

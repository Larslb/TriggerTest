package com.example.larslb.triggertest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BLEConnectService extends Service {
    private static final String TAG = BLEConnectService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mGatt;
    private final IBinder mBinder = new LocalBinder();
    private int mConnected = STATE_DISCONNECTED;
    private String mDeviceAddress;
    private BluetoothGattCharacteristic mCharacteristic;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.larslb.triggertest.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.larslb.triggertest.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.larslb.triggertest.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.larslb.triggertest.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.larslb.triggertest.EXTRA_DATA";

    public final static UUID UUID_TRIGGER_VALUE_MEASUREMENT = UUID.fromString(DeviceServices.ANALOG_ATTRIBUTE);

    public BLEConnectService() {
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                final Intent connectIntent = new Intent(ACTION_GATT_CONNECTED);
                broadCastUpdate(connectIntent);
                mGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                final Intent disconnectIntent = new Intent(ACTION_GATT_DISCONNECTED);
                broadCastUpdate(disconnectIntent);
                mConnected = STATE_DISCONNECTED;
                try {
                    mGatt.close();
                } catch (Exception e){
                    Log.d(TAG,"Close ignoring: " + e);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            Log.d(TAG,"onServicesDiscovered: Status Flag = " + status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                final Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED);
                broadCastUpdate(intent);
            }else {
                Log.i(TAG,"onServicesDiscovered recieved: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            Log.d(TAG,"onCharacteristicRead: Status Flag = " + status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                broadCastUpdate(ACTION_DATA_AVAILABLE,characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            broadCastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,int status){
            Log.d(TAG, "onDescriptorWrite --> Status: " + status);

        }
    };

    public void broadCastUpdate(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void broadCastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if(data != null && data.length >0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            int number = 0;
            for (byte bytechar : data){
                number = bytechar & 0xFF;

            }
            stringBuilder.append(number);
            intent.putExtra(EXTRA_DATA,stringBuilder.toString());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public int[] bytearray2intarray(byte[] barray){
        int[] iarray = new int[barray.length];
        int i = 0;
        for (byte b : barray){
            iarray[i++] = b & 0xff;
        }
        return iarray;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BLEConnectService getService() {
            return BLEConnectService.this;
        }
    }
    public boolean init(){
        if (mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null){
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        return !(mBluetoothAdapter == null);


    }

    public boolean connect(final String address){
        Log.d(TAG, "BluetoothAdapter: " + mBluetoothAdapter.getName());
        Log.d(TAG, "Adress: " + address);
        if (mBluetoothAdapter == null || address == null){
            Log.e(TAG,"Adapter or address null");
            return false;
        }
        if (mDeviceAddress != null && address.equals(mDeviceAddress)){
            if (mGatt.connect()){
                mConnected = STATE_CONNECTED;
                return true;
            }else{
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.d(TAG,"Device TO connect: " + device.getName());
        if (device == null){
            return false;
        }
        mGatt = device.connectGatt(this,false,mGattCallback);
        mDeviceAddress = address;
        mConnected = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mGatt == null){
            Log.w(TAG,"BluetoothAdapter not initialized");
            return;
        }
        mGatt.disconnect();
    }



    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mGatt == null){
            Log.w(TAG, "BluetoothAdapter not Initialized");
            return;
        }
        mGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled){
        if (mBluetoothAdapter == null || mGatt == null){
            Log.w(TAG, "BluetoothAdapter not Initialized");
            return;
        }
        mCharacteristic = characteristic;

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(DeviceServices.CLIENT_CHARACTERISTIC_CONFIG));
        Log.d(TAG,"BluetoothDescriptor " + descriptor.getUuid().toString());

        Log.d(TAG,"Characteristic UUID --> " + characteristic.getUuid().toString());


        mGatt.setCharacteristicNotification(characteristic,enabled);
        List<BluetoothDevice> connectedDevices = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (int i =0;i<connectedDevices.size();i++){
            Log.d(TAG,"ConnectedDevice: " + i + " --> "+ connectedDevices.get(i));
            Log.d(TAG,"mGATT Device: " + mGatt.getDevice());
        }

        if (UUID_TRIGGER_VALUE_MEASUREMENT.equals(characteristic.getUuid())){
            Log.d(TAG,"Descriptor: " + descriptor.getUuid().toString());
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = false;
                    while(!success){
                        if (mGatt.writeDescriptor(descriptor))
                            success = true;
                    }
                }
            }).start();
        }

    }

    public List<BluetoothGattService> getSupportedGattService() {
        if (mGatt == null) return null;
        return mGatt.getServices();
    }


}

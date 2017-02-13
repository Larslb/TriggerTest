package com.example.larslb.triggertest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import org.w3c.dom.Text;

import java.nio.channels.CancelledKeyException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


import static java.lang.StrictMath.toIntExact;
import static java.sql.Types.TIME;

public class DeviceManagerActivity extends AppCompatActivity {
    private final static String TAG = DeviceManagerActivity.class.getSimpleName();
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothGattService analogService;
    private BluetoothGattCharacteristic analogCharacteristic;

    private long mStartTime;
    Button DisconnectButton;
    Button ConnectButton;
    private TextView mConnectionState;
    private TextView mServiceName;
    private TextView mRawData;
    private int mCounter;
    private ListView mServiceList;
    private ArrayList<String> mServices;
    private ArrayAdapter<String> mArrayAdapter;
    private boolean mBound;
    private BLEConnectService mBLEConnectService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    ArrayList<HashMap<String, String>> mGattServiceData;

    ArrayList<String> mData;
    ArrayList<Integer> mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);
        mCounter = 0;

        mStartTime = 0;


        DisconnectButton = (Button) findViewById(R.id.disconnect_button);
        DisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBLEConnectService.disconnect();
            }
        });
        ConnectButton = (Button) findViewById(R.id.connect_button);
        ConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStart();
            }
        });

        mServices = new ArrayList<>();
        mServiceList = (ListView) findViewById(R.id.service_list);
        mServiceList.setOnItemClickListener(servicesListClickListener);

        mData = new ArrayList<>();
        mTime = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,mServices);
        mServiceList.setAdapter(mArrayAdapter);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceScanActivity.EXTRA_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceScanActivity.EXTRA_ADDRESS);
        //mServiceName = (TextView) findViewById(R.id.service_name);
        mConnectionState = (TextView) findViewById(R.id.connection_state);





    }

    private final BroadcastReceiver mGattUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Intent intent1 = intent;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (BLEConnectService.ACTION_GATT_CONNECTED.equals(action)){
                        updateConnection(R.string.disconnect);


                    }else if(BLEConnectService.ACTION_GATT_DISCONNECTED.equals(action)){
                        updateConnection(R.string.disconnected);

                    }
                    else if(BLEConnectService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){

                        lookupServices( mBLEConnectService.getSupportedGattService());
                    }
                    else if(BLEConnectService.ACTION_DATA_AVAILABLE.equals(action)){
                        if (mStartTime == 0){
                            mStartTime = SystemClock.uptimeMillis();
                        }
                        long time_now = SystemClock.uptimeMillis();
                        Log.d(TAG,"TIME START: " + mStartTime + "   TIME NOW: " + time_now );
                        //displayData(intent1.getStringExtra(BLEConnectService.EXTRA_DATA));
                        if (time_now - mStartTime < 5000) {
                            storeData(intent1.getStringExtra(BLEConnectService.EXTRA_DATA),time_now-mStartTime);
                        } else {
                            startGraphing();
                        }
                    }
                }
            });thread.start();
        }
    };


    public void startGraphingActivity(){
        mBLEConnectService.setCharacteristicNotification(mNotifyCharacteristic,false);
        Intent graphingintent = new Intent(this,GraphingActivity.class);
        graphingintent.putStringArrayListExtra("Data",mData);
        graphingintent.putIntegerArrayListExtra("Time",mTime);
        startActivity(graphingintent);
    }

    public void startGraphing(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startGraphingActivity();
            }
        });

    }



    private final ListView.OnItemClickListener servicesListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mGattCharacteristics != null) {
                final ArrayList<BluetoothGattCharacteristic> characteristics = mGattCharacteristics.get(position);
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    final int charaProp = characteristic.getProperties();
                    Log.d(TAG, "Properties: " + charaProp);

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                        if (mNotifyCharacteristic != null) {
                            mBLEConnectService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBLEConnectService.readCharacteristic(characteristic);

                    }

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBLEConnectService.setCharacteristicNotification(
                                characteristic, true);
                    }
                }
            }
        }
    };



    private void updateConnection(final int Id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(Id);
            }
        });
    }



    private void lookupServices(List<BluetoothGattService> supportedGattService) {
        if (supportedGattService == null) return;

        String unkownName = "Unkown service";
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        for (BluetoothGattService gattService : supportedGattService){

            mServices.add(DeviceServices.lookup(gattService.getUuid().toString(),unkownName));
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
            for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                characteristics.add(characteristic);

            }
            mGattCharacteristics.add(characteristics);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mArrayAdapter.notifyDataSetChanged();

                }
            });
        }

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBLEConnectService = ((BLEConnectService.LocalBinder) service).getService();
            if (!mBLEConnectService.init()){
                Log.e(TAG,"Init not complete");
            }
            mBound = true;
            mBLEConnectService.connect(mDeviceAddress);
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBLEConnectService = null;
            mBound = false;
        }
    };



    public void storeData(String data,long time){
        if (data != null){
            mData.add(data);
            int t = 0;
            try {
                t = (int) (long )time;
            } catch (Exception e){
                Log.e(TAG,"Exception Storage: " + e);
            }
            mTime.add(t);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, BLEConnectService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReciever, makeGattUpdateIntentFilter());

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEConnectService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEConnectService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEConnectService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEConnectService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"BOUNDED: " + mBound);
        if (mBound){
            unbindService(mServiceConnection);
            mBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReciever);
    }
}

package io.github.cleac.bluetoothletest.utils;

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
import android.util.Log;

import java.util.List;
import java.util.UUID;

import io.github.cleac.bluetoothletest.LocalStorage;
import io.github.cleac.bluetoothletest.SupportedGattAttributes;

/**
 * Created by cleac on 7/12/15.
 * Force, LLC (2015)
 */
public class BluetoothService extends Service {
    private final static String LOG_TAG = BluetoothService.class.getName();

    private final static String AUTHORITY = "com.forcemove.forceemotion.bt";
    private final int SERVICE_STATUS_ID = 1;

    private BluetoothGatt mBluetoothGatt;

    public final static String ACTION_GATT_CONNECTED = AUTHORITY + ".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = AUTHORITY + ".ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = AUTHORITY + ".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = AUTHORITY + ".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_DATA_AVAILABLE = AUTHORITY + ".ACTION_GATT_DATA_AVAILABLE";
    public final static String ACTION_GATT_EXTRA_DATA = AUTHORITY + ".ACTION_GATT_EXTRA_DATA";

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    /**
     *  Variable that defines interaction with Bluetooth device: gets data from and sends broadcasts
     *  notifications about new data; checks connection state and looks for Bluetooth services that
     *  can be discovered
     */

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction = null;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    intentAction = ACTION_GATT_CONNECTING;
                    mConnectionState = STATE_CONNECTING;
            }
            if(intentAction!=null)
                broadcastUpdate(intentAction);
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_DATA_AVAILABLE, characteristic);
            } else {
                Log.e(LOG_TAG,"onCharacteristicRead received : " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(LOG_TAG, "Characteristic got");
            if(characteristic.getUuid().equals(UUID.fromString(SupportedGattAttributes.Characteristics.Heartrate))) {
                int flag = characteristic.getProperties();
                int format;
                if((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                }
                final int heartrate = characteristic.getIntValue(format,1);
                Log.i(LOG_TAG, "Heartrate is " + heartrate);
            }
            broadcastUpdate(ACTION_GATT_DATA_AVAILABLE,characteristic);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                for(BluetoothGattService service:services) {
                    UUID uuid = service.getUuid();

                    if(uuid.equals(UUID.fromString(SupportedGattAttributes.Services.Heartrate))) {
                        final BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                                UUID.fromString(SupportedGattAttributes.Characteristics.Heartrate)
                        );
                        gatt.setCharacteristicNotification(characteristic,true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(SupportedGattAttributes.Client.Heartrate)
                        );
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                    } else {
                        Log.v(LOG_TAG, "Found unknown service with UUID: " + uuid.toString());
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            Log.v(LOG_TAG, "Found characteristic: " + characteristic.getUuid().toString());
                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(LOG_TAG, "onServicesDiscovered received: " + status);
            }
        }

        public void broadcastUpdate(String intentAction) {
        }

        public void broadcastUpdate(String intentAction,BluetoothGattCharacteristic characteristic) {
            Log.i(LOG_TAG,"Got data");
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"Service stopped");

        if(mBluetoothGatt==null)
            return;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private final IBinder mLocalBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mBluetoothGatt==null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(LocalStorage.getDeviceAddress(this));
            mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        } else {
            mBluetoothGatt.connect();
        }

        return START_STICKY;
    }

    public List<BluetoothGattService> getGattServices(){
        if(mBluetoothGatt!=null)
            return mBluetoothGatt.getServices();
        else
            return null;
    }



}

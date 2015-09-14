package io.github.cleac.bluetoothletest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import static android.bluetooth.BluetoothAdapter.LeScanCallback;

/**
 * Created by cleac on 7/13/15.
 */
public class BluetoothScanner {

    private static final String LOG_TAG = BluetoothScanner.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;

    private Runnable onScanningRestart;
    private Runnable onScanningStart;
    private Runnable onScanningStop;
    private LeScanCallback mLeScanCallback;

    private Context currentContext;

    private int mScanningTimeout;

    private Handler mHandler;
    private Runnable mRunnable;

    public BluetoothScanner(Context context) {
        currentContext = context;

        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mScanning = false;
        mScanningTimeout = 10000;
        mHandler = new Handler();

        if (!currentContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(currentContext, "Bluetooth Low Energy is not supported by device", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * @return whether bluetooth is available
     */
    public boolean isBluetoothAvailable() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }


    /**
     * function startScanning() - function that starts scanning for Bluetooth LE Devices
                                    runs onScanningRestart if scanning was in action
                                         onScanningStart if not
     * @return  whether the scanning was started
     */
    public boolean startScanning(boolean autoStop){
        if(!this.isBluetoothAvailable()) {
            Log.e(LOG_TAG, "No bluetooth device available. Exiting");
            mScanning = false;
        }
        else if(this.mLeScanCallback == null) {
            Log.e(LOG_TAG, "There is no action assigned when device is found. Exiting");
            mScanning = false;
        } else {
            if (mScanning) {
                if(onScanningRestart!=null)
                    onScanningRestart.run();
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else
                if(onScanningStart!=null)
                    onScanningStart.run();

            if(autoStop) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        stopScanning();
                    }
                };
                mHandler.postDelayed(mRunnable, mScanningTimeout);
            }

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mScanning = true;
            Log.v(LOG_TAG, "Started scanning");
        }
        return mScanning;
    }

    /**
     * function stopScanning() - stops scanning and runs onScanningStop
     */
    public void stopScanning() {
        if(mRunnable!=null)
            mHandler.removeCallbacks(mRunnable);
        if(onScanningStop!=null)
            onScanningStop.run();
        if(mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
            Log.v(LOG_TAG, "Stopped scanning");
        } else
            Log.w(LOG_TAG,"Scanning was not started");
    }

    /** Setters for handler methods **/

    public void setOnScanningStop(Runnable onScanningStop) {
        this.onScanningStop = onScanningStop;
    }

    public void setOnScanningStart(Runnable onScanningStart) {
        this.onScanningStart = onScanningStart;
    }

    public void setOnScanningRestart(Runnable onScanningRestart) {
        this.onScanningRestart = onScanningRestart;
    }

    public void setLeScanCallback(LeScanCallback mLeScanCallback) {
        this.mLeScanCallback = mLeScanCallback;
    }
}

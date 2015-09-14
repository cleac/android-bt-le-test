package io.github.cleac.bluetoothletest.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.cleac.bluetoothletest.R;
import io.github.cleac.bluetoothletest.ui.adapter.BluetoothListAdapter;

/**
 * Created by cleac on 9/14/15.
 */
public class BluetoothScanFragment extends Fragment {

    public static final String LOG_TAG = BluetoothScanFragment.class.getSimpleName();

    @Bind(R.id.list) RecyclerView recyclerView;
    BluetoothListAdapter bluetoothListAdapter;

    public static final int ENABLE_BLUETOOTH = 666;
    public static final int GET_LOCATION_PERMISSION = 666;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scan_bluetooth,container,false);
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ||
                !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(getContext(), "BLE is not supported by device", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        ButterKnife.bind(this,rootView);
        bluetoothListAdapter = new BluetoothListAdapter(getContext())
                .setNameFilter(null);
        if (Build.VERSION.SDK_INT >= 23 &&
                getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    GET_LOCATION_PERMISSION);
        }
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case GET_LOCATION_PERMISSION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothListAdapter.getBluetoothScanner().startScanning(false);
                } else {
                    Snackbar.make(getView().findViewById(R.id.root_bt_scan),
                            "Cannot get scanning results from bluetooth adapter",
                            Snackbar.LENGTH_LONG
                    ).show();
                }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(bluetoothListAdapter.getBluetoothScanner()!=null) {
            if (!bluetoothListAdapter.getBluetoothScanner().isBluetoothAvailable()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BLUETOOTH);
            } else {
                bluetoothListAdapter.getBluetoothScanner().startScanning(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(bluetoothListAdapter.getBluetoothScanner()!=null)
            bluetoothListAdapter.getBluetoothScanner().stopScanning();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH:
                if(resultCode == Activity.RESULT_OK) {
                    bluetoothListAdapter.getBluetoothScanner().startScanning(false);
                } else {
                    Snackbar.make(
                            getView().findViewById(R.id.root_bt_scan),
                            "Bluetooth device is disabled, can't scan",
                            Snackbar.LENGTH_LONG
                    ).show();
                }
        }
    }
}

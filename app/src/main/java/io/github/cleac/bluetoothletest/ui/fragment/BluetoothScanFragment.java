package io.github.cleac.bluetoothletest.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.cleac.bluetoothletest.LocalStorage;
import io.github.cleac.bluetoothletest.R;
import io.github.cleac.bluetoothletest.ui.adapter.BluetoothListAdapter;
import io.github.cleac.bluetoothletest.utils.BluetoothService;

/**
 * Created by cleac on 9/14/15.
 */
public class BluetoothScanFragment extends Fragment {

    public static final String LOG_TAG = BluetoothScanFragment.class.getSimpleName();

    @Bind(R.id.list)
    RecyclerView mDevicesRecyclerView;
    @Bind(R.id.layout_scanning)
    View mScanningLayout;
    BluetoothListAdapter bluetoothListAdapter;

    public static final int ENABLE_BLUETOOTH = 666;
    public static final int GET_LOCATION_PERMISSION = 666;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scan_bluetooth, container, false);
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ||
                !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(getActivity(), "BLE is not supported by device", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        ButterKnife.bind(this, rootView);
        bluetoothListAdapter = new BluetoothListAdapter(getActivity())
                .setNameFilter(null);
        bluetoothListAdapter.setOnItemClickedListener((device) -> {
            Toast.makeText(getActivity(),
                    "Saved address  " + device.getAddress(), Toast.LENGTH_SHORT).show();
            LocalStorage.saveDeviceAddress(getActivity(),device.getAddress());
            getActivity().stopService(new Intent(getActivity(),BluetoothService.class));
            getActivity().startService(new Intent(getActivity(), BluetoothService.class));
        });
        bluetoothListAdapter.setOnItemAddedListener((newCount -> mScanningLayout.setVisibility
                ((newCount == 0) ? View.VISIBLE : View.GONE)));
        mDevicesRecyclerView.setAdapter(bluetoothListAdapter);
        mDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GET_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    bluetoothListAdapter.getBluetoothScanner().startScanning(false);
                else
                    Snackbar.make(
                            getView().findViewById(R.id.root_bt_scan),
                            "Cannot get scanning results from bluetooth adapter",
                            Snackbar.LENGTH_LONG
                    ).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23 &&
                getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    GET_LOCATION_PERMISSION);
        if (!bluetoothListAdapter.getBluetoothScanner().isBluetoothAvailable())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BLUETOOTH);
        else
            bluetoothListAdapter.getBluetoothScanner().startScanning(false);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (bluetoothListAdapter.getBluetoothScanner() != null)
            bluetoothListAdapter.getBluetoothScanner().stopScanning();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
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

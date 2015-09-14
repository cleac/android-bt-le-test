package io.github.cleac.bluetoothletest.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.cleac.bluetoothletest.BluetoothScanner;
import io.github.cleac.bluetoothletest.R;

/**
 * Created by cleac on 9/14/15.
 */
public class BluetoothScanFragment extends Fragment {

    @Bind(R.id.list) RecyclerView recyclerView;
    BluetoothListAdapter bluetoothListAdapter;
    BluetoothScanner bluetoothScanner;

    public static final int ENABLE_BLUETOOTH=666;

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
        bluetoothListAdapter = new BluetoothListAdapter()
                .setNameFilter(null);

        bluetoothScanner = new BluetoothScanner(getContext());

        recyclerView.setAdapter(bluetoothListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bluetoothScanner.setLeScanCallback((device, rssi, scanRecord) ->
                        bluetoothListAdapter.appendDevice(device)
        );
        bluetoothScanner.setOnScanningRestart(bluetoothListAdapter::clearDevices);
        bluetoothScanner.setOnScanningStart(bluetoothListAdapter::clearDevices);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!bluetoothScanner.isBluetoothAvailable()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),ENABLE_BLUETOOTH);
        } else {
            bluetoothScanner.startScanning(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetoothScanner.stopScanning();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH:
                if(resultCode == Activity.RESULT_OK) {
                    bluetoothScanner.startScanning(false);
                } else {
                    Snackbar.make(
                            getView().findViewById(R.id.root_bt_scan),
                            "Bluetooth is disabled",
                            Snackbar.LENGTH_LONG
                    ).show();
                }
        }
    }

    private class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.ViewHolder> {

        private List<BluetoothDevice> mDevices;
        private String mFilter;

        public BluetoothListAdapter() {
            mDevices = new ArrayList<>();
            mFilter = null;
        }

        public BluetoothListAdapter setNameFilter(@Nullable String filter) {
            mFilter = filter;
            return this;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry_bt_scan_list,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final BluetoothDevice bluetoothDevice = mDevices.get(position);
            holder.name.setText(bluetoothDevice.getName());
            holder.address.setText(bluetoothDevice.getAddress());
        }

        public void appendDevice(BluetoothDevice device) {
            if(mFilter==null || Pattern.matches(mFilter,device.getName()) ) {
                mDevices.add(device);
                getActivity().runOnUiThread(this::notifyDataSetChanged);
            }
        }

        public void appendDevices(List<BluetoothDevice> devicesList) {
            mDevices.addAll(devicesList);
            getActivity().runOnUiThread(this::notifyDataSetChanged);
        }

        public void removeDevice(int position) {
            mDevices.remove(position);
            getActivity().runOnUiThread(this::notifyDataSetChanged);
        }

        public void clearDevices() {
            mDevices.clear();
            getActivity().runOnUiThread(this::notifyDataSetChanged);
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView address;

            public ViewHolder(View itemView) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.name);
                address = (TextView) itemView.findViewById(R.id.address);
            }
         }
    }
}

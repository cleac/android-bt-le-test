package io.github.cleac.bluetoothletest.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.github.cleac.bluetoothletest.R;
import io.github.cleac.bluetoothletest.utils.BluetoothScanner;

/**
 * Created by cleac on 9/14/15.
 */
public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.ViewHolder> {
    public static final String LOG_TAG = BluetoothListAdapter.class.getSimpleName();

    private List<BluetoothDevice> mDevices;
    private String mFilter;
    private BluetoothScanner bluetoothScanner;

    public BluetoothScanner getBluetoothScanner() {
        return bluetoothScanner;
    }

    public BluetoothListAdapter(Context context) {
        bluetoothScanner = new BluetoothScanner(context);
        bluetoothScanner.setLeScanCallback((device, rssi, scanRecord) -> appendDevice(device));
        bluetoothScanner.setOnScanningRestart(this::clearDevices);
        bluetoothScanner.setOnScanningStart(this::clearDevices);
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
        Log.d(LOG_TAG, "Found device " + device.getName());
        if(!mDevices.contains(device)
                || mFilter == null
                || Pattern.matches(mFilter, device.getName())) {
            mDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public void appendDevices(List<BluetoothDevice> devicesList) {
        mDevices.addAll(devicesList);
        notifyDataSetChanged();
    }

    public void removeDevice(int position) {
        mDevices.remove(position);
        notifyDataSetChanged();
    }

    public void clearDevices() {
        mDevices.clear();
        notifyDataSetChanged();
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

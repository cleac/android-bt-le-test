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

import butterknife.ButterKnife;
import io.github.cleac.bluetoothletest.R;
import io.github.cleac.bluetoothletest.utils.BluetoothScanner;

/**
 * Created by cleac on 9/14/15.
 */
public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.ViewHolder> {
    public static final String LOG_TAG = BluetoothListAdapter.class.getSimpleName();

    public interface OnItemClickedListener {
        void onItemClicked(BluetoothDevice device);
    }

    public interface OnItemAddedListener {
        void onItemAddedListener(int newCount);
    }

    private List<BluetoothDevice> mDevices;
    private String mFilter;
    private BluetoothScanner mBluetoothScanner;
    private OnItemClickedListener mOnItemClickedListener;
    private OnItemAddedListener mOnItemAddedListener;


    public BluetoothScanner getBluetoothScanner() {
        return mBluetoothScanner;
    }

    public BluetoothListAdapter(Context context) {
        mBluetoothScanner = new BluetoothScanner(context);
        mBluetoothScanner.setLeScanCallback((device, rssi, scanRecord) -> appendDevice(device));
        mBluetoothScanner.setOnScanningRestart(this::clearDevices);
        mBluetoothScanner.setOnScanningStart(this::clearDevices);
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
                .inflate(R.layout.entry_bt_scan_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final BluetoothDevice bluetoothDevice = mDevices.get(position);
        holder.name.setText(bluetoothDevice.getName());
        holder.address.setText(bluetoothDevice.getAddress());
        holder.rootLayout.setOnClickListener((v) -> {
            if (mOnItemClickedListener != null)
                mOnItemClickedListener.onItemClicked(mDevices.get(position));
        });
    }

    public void appendDevice(BluetoothDevice device) {
        if (!mDevices.contains(device)
                && (mFilter == null
                || Pattern.matches(mFilter, device.getName()))) {
            Log.d(LOG_TAG, "Found device " + device.getName());
            notifyItemInserted(mDevices.size());
            mDevices.add(device);
            if (mOnItemAddedListener != null)
                mOnItemAddedListener.onItemAddedListener(mDevices.size());
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
        View rootLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            name = ButterKnife.findById(itemView, R.id.name);
            address = ButterKnife.findById(itemView, R.id.address);
            rootLayout = ButterKnife.findById(itemView, R.id.root_layout);
            rootLayout.setClickable(true);
        }
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        mOnItemClickedListener = onItemClickedListener;
    }

    public void setOnItemAddedListener(OnItemAddedListener onItemAddedListener) {
        mOnItemAddedListener = onItemAddedListener;
    }

}

package io.github.cleac.bluetoothletest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by cleac on 7/13/15.
 */

public class LocalStorage {
    private static final String LOG_TAG = LocalStorage.class.getSimpleName();

    public static final String DEVICE_ADDRESS_KEY = "io.github.cleac.devaddress";

    public static void saveDeviceAddress(@NonNull Context context,@NonNull String address) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                DEVICE_ADDRESS_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_ADDRESS_KEY, address);
        editor.commit();
    }

    public static void removeDeviceAddress(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                DEVICE_ADDRESS_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(DEVICE_ADDRESS_KEY);
        editor.commit();
    }

    @Nullable
    public static String getDeviceAddress(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                DEVICE_ADDRESS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DEVICE_ADDRESS_KEY, null);
    }
}

package io.github.cleac.bluetoothletest;

/**
 * Created by cleac on 7/14/15.
 */
public class SupportedGattAttributes {

    public class Services {
        public static final String HID = "00001812-0000-1000-8000-00805F9B34FB";
        public static final String Battery = "0000180F-0000-1000-8000-00805F9B34FB";
        public static final String DeviceInfo = "0000180A-0000-1000-8000-00805F9B34FB";
        public static final String GenericAttributes = "00001801-0000-1000-8000-00805F9B34FB";
        public static final String Heartrate = "0000180D-0000-1000-8000-00805F9B34FB";
    }

    public class Characteristics {
        public static final String Heartrate = "00002A37-0000-1000-8000-00805F9B34FB";
    }

    public class Client {
        public static final String Heartrate = "00002902-0000-1000-8000-00805F9B34FB";
    }
}

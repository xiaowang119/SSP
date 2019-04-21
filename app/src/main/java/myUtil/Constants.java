package myUtil;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DEAL = 6;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    //为UI界面设置的陈述
    public static final int FROM_REFRESH_THREAD = 0;
    public static final int FROM_WRITE_THREAD = 1;
    public static final int FROM_STATUS_THREAD = 2;

}
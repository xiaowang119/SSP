package MyUtil;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * 是连接蓝牙音响是使用的
 * 用作初期调试
 */
public class MusicService {

    private BluetoothA2dp a2dp = null;
    private Context mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;

    public MusicService (Context context, BluetoothAdapter adapter, BluetoothDevice device){
        this.mBluetoothAdapter = adapter;
        this.mActivity = context;
        this.mDevice = device;
    }

    /**
     * 开始连接蓝牙设备
     */
    public void connectBlueDevices() {
        /**使用A2DP协议连接设备*/
        mBluetoothAdapter.getProfileProxy(mActivity, mProfileServiceListener, BluetoothProfile.A2DP);
    }

    /**
     * 连接蓝牙设备（通过监听蓝牙协议的服务，在连接服务的时候使用BluetoothA2dp协议）
     */
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {
            Looper.prepare();
            Toast.makeText(mActivity, "音响设备已断开！", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            try {
                if (profile == BluetoothProfile.HEADSET) {
                    //....

                } else if (profile == BluetoothProfile.A2DP) {
                    /**使用A2DP的协议连接蓝牙设备（使用了反射技术调用连接的方法）*/
                    a2dp = (BluetoothA2dp) proxy;
                    if (a2dp.getConnectionState(mDevice) != BluetoothProfile.STATE_CONNECTED) {
                        a2dp.getClass()
                                .getMethod("connect", BluetoothDevice.class)
                                .invoke(a2dp, mDevice);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

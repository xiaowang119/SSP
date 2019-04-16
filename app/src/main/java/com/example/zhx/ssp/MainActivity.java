package com.example.zhx.ssp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import myUtil.AutoPaire;
import myUtil.BluetoothService;
import myUtil.Constants;
import myUtil.MusicService;

public class MainActivity extends AppCompatActivity {

    public static int meterData[][] = new int[3][2];
    public static double listTableData[] = new double[200];
    public static boolean advance, later, isAddressFalse = true, isAbnormal = false;
    public static int mDeviceID = 0;
    private static int signal = 0;
    public static BluetoothDevice remoteDevice;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 10;
    public static final int REQUEST_DEVICE_ADDRESS_CODE = 1;

    private BluetoothAdapter bluetoothAdapter;
    private String deviceAddress;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    //private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothService mBluetoothService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (haveDeviceSelected()) {
            start();
        }else {
            Intent select = new Intent();
            select.setClass(MainActivity.this, BluetoothActivity.class);
            startActivityForResult(select, REQUEST_DEVICE_ADDRESS_CODE);
        }
    };

    private boolean haveDeviceSelected() {
        editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        pref = getSharedPreferences("data", MODE_PRIVATE);
        if (pref.getBoolean("selected", false)) {
            deviceAddress = pref.getString("deviceAddress", "");
            return true;
        } else {
            return false;
        }
    }

    private void initView() {
        Button mode1, mode2;

        mode1 = findViewById(R.id.mode1);
        mode2 = findViewById(R.id.mode2);

        mode1.setOnClickListener(new MyOnclickListener());
        mode2.setOnClickListener(new MyOnclickListener());
    }

    /*
     *蓝牙设备的初始化
     */
    public void start() {
        initView();

        //获取相应权限
        getPermission();
        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //静默开启蓝牙
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        // Initialize the BluetoothService to perform bluetooth connections
        mBluetoothService = new BluetoothService(mHandler, bluetoothAdapter);
        //给判断设备状态的变量设初值
        advance = true;
        later = false;

        //尝试对远程设备进行连接
        mHandler.post(ConnectThread);
    }

    //用于连接远程设备的线程
    private Runnable ConnectThread = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                connectToBluetooth(deviceAddress);
                if (signal > 0) {
                    showText("连接断开，正在尝试重连！");
                }
                signal++;
            }
            mHandler.postDelayed(this, 5000);
        }
    };


    //与尝试连接蓝牙的方法
    private void connectToBluetooth(String deviceAddress) {
        if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
            if(isAddressFalse) { //防止持续不断的显示
                showText("蓝牙地址无效");
            }
            isAddressFalse = false;
            signal = 0;
            return;
        }
        remoteDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        //如果已经连接则不再连接
        if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED)
            return;

        //判断是否已经完成配对
        if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            //进行配对操作
            pair(bluetoothAdapter, remoteDevice);
        }
        //进行连接操作
        if (remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            //showText("正在连接中...");
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            mBluetoothService.connect(remoteDevice);

            //通过判断蓝牙设备的不同类型来选择不同的连接方式
                /*if (remoteDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
                    new MusicService(MainActivity.this, bluetoothAdapter, remoteDevice).connectBlueDevices();
                }else {
                    mBluetoothService.connect(remoteDevice);
                }*/
        }
    }

    /**
     * 完成蓝牙配对功能
     * */
    public void pair(BluetoothAdapter adapter, BluetoothDevice device) {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        try {
            // 手机和蓝牙采集器配对
            AutoPaire.createBond(device.getClass(), device);
        } catch (Exception e) {
            e.printStackTrace();
            showText("配对失败");
            signal = 0;
        }
    }


    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {

        @Override

        public void handleMessage(Message msg) {
            switch (msg.what) {
                /*case Constants.MESSAGE_STATE_CHANGE:
                    String str;
                    int index;
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;*/
                case Constants.MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case Constants.MESSAGE_READ:
                    //czq
                    byte[] readBuf = (byte[]) msg.obj;
                    //对接收到的数据进行操作
                    showText("数据已收到！");
                    if (isLegal(readBuf, msg.arg1)) {
                        getMeterData(readBuf);
                        getListTableData(readBuf);
                        if (advance == later) {
                            advance = !later;
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // show the connected device's name
                    showText("Connected to " + remoteDevice.getName());
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    private class MyOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mode1 :
                    Intent mode_1 = new Intent();
                    mode_1.setClass(MainActivity.this ,Mode1.class);
                    startActivity(mode_1);
                    break;
                case R.id.mode2 :
                    Intent mode_2 = new Intent();
                    mode_2.setClass(MainActivity.this ,Mode2.class);
                    startActivity(mode_2);
                    break;
            }
        }
    }

    /**
     *  由于蓝牙所需要的权限包含Dangerous Permissions，
     *  所以我们需要在Java代码中进行动态授权处理：
     */
    private void getPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //检测当前app是否拥有某个权限
            int permissionCheck = 0;
            permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // 请求授权
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_BLUETOOTH_PERMISSION);// 自定义常量,任意整型
            } else {
                // 已经获得权限
            }
        }
    }

    private boolean hasAllPermissionGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void showText(String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    //将字节信息转化为多数协议传输信息的十六进制
    /*public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = (char) (v >>> 4);
            hexChars[j * 2 + 1] = (char)(v & 0x0F);
        }
        return new String(hexChars);
    }*/

    //判断数据包是否合法
    private boolean isLegal(byte[] msg, int length) {
        int index;
        if (length == 14) {
            byte sumCheck = 0;
            for (index = 0; index < 14; index++) {
                if (index < 2) {
                    //判断数据包包头是否合法
                    if (msg[index] != (byte)0xaa)
                        return false;
                } else if (index == 11){
                    //判断数据包校验和是否正常
                    if (msg[index] != sumCheck)
                        return false;
                } else if (index > 11) {
                    //判断数据包包尾是否合法
                    if (msg[index] != (byte)0xbb)
                        return false;
                }
                if (index >= 2)
                    sumCheck += (byte)(msg[index] & 0x0f);
            }
            return true;
        }
        return false;
    }

    //将接收到的数据包转化为虚拟表数据
    private void getMeterData(byte[] packet) {
        mDeviceID = (int) packet[3];
        int row, column, index = 4;
        for (row = 0; row < 3; row++) {
            for (column = 0; column < 2; column++) {
                meterData[row][column] = (int) packet[index++];
            }
        }
    }

    //将接收到的数据转化为参数列表的数据
    private void getListTableData(byte[] packet) {
        mDeviceID = (int) packet[3];
        double tmp = (packet[4]*256 + packet[5])/100.0 +0.1;
        if (mDeviceID > 0 && mDeviceID <= 200) {
            listTableData[mDeviceID - 1] = tmp;
        }
    }


    //当从临时窗口获取数据信息返回后执行此方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                deviceAddress = data.getStringExtra("deviceAddress");
                editor.putBoolean("selected", true);
                editor.putString("deviceAddress", deviceAddress);
                editor.commit();
                if (mBluetoothService != null) {
                    mBluetoothService.stop();
                    mBluetoothService = null;
                }
                isAddressFalse = true;
                start();
                break;
            case 2:
                break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_BLUETOOTH_PERMISSION:
                if (!hasAllPermissionGranted(grantResults)) {
                    showText("请先打开相应权限！");
                }
                break;
        }
    }

    //添加导航栏菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //对导航栏菜单的点击事件进行处理
    public boolean onOptionsItemSelected(MenuItem item) {
        mHandler.removeCallbacks(ConnectThread);
        Intent select = new Intent();
        select.setClass(MainActivity.this, BluetoothActivity.class);
        startActivityForResult(select, REQUEST_DEVICE_ADDRESS_CODE);
        return true;
    }
}

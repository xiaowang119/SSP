package myUtil;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zhx.ssp.BluetoothActivity;
import com.example.zhx.ssp.CarType;
import com.example.zhx.ssp.MainActivity;
import com.example.zhx.ssp.Mode2;
import com.example.zhx.ssp.R;


public class DataApplication extends Application {

    public static int meterData[][] = new int[3][2];
    public static double listTableData[] = new double[200];
    public static boolean advance, later, isAddressFalse = true, isAbnormal = false;
    public static int mDeviceID = 0;
    public static int signal = 0;
    public static BluetoothDevice remoteDevice;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 10;
    public static final int REQUEST_DEVICE_ADDRESS_CODE = 1;

    private BluetoothAdapter bluetoothAdapter;
    private String deviceAddress;
    public static BluetoothService mBluetoothService = null;
    public static DataCircle<Byte> dataCircle = new DataCircle<>(1024*1024);
    //StringBuilder stringBuilder = new StringBuilder();



    /*
     *蓝牙设备的初始化
     */
    public void start(BluetoothAdapter adapter, String address) {
        bluetoothAdapter = adapter;
        deviceAddress = address;
        mBluetoothService = new BluetoothService(mHandler, bluetoothAdapter);
        //给判断设备状态的变量设初值
        advance = true;
        later = false;
        //尝试对远程设备进行连接
        mHandler.post(ConnectThread);
        new MyThread(50, mHandler, Constants.MESSAGE_DEAL).start();
    }

    //用于连接远程设备的线程
    private Runnable ConnectThread = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                connectToBluetooth(deviceAddress);
                if (signal>0 & signal%5==0) {
                    showText("连接断开，正在尝试重连！");
                }
                signal++;
            }else {
                //测试写数据
                testWrite();
                Log.i("CZQ_test", "已发出一个包！");
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

        //boolean flag = true;
        int packLength;
        byte pack[];
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
                case Constants.MESSAGE_DEAL:
                    //Log.i("CZQ", "deal");
                    readData();
                    break;
                case Constants.MESSAGE_READ:
                    pack = (byte[]) msg.obj;
                    packLength = msg.arg1;
                    pushData(pack, packLength);
                    //Log.i("CZQ", bytesToHex(pack, packLength));
                    if (advance == later) {
                        advance = !later;
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // show the connected device's name
                    showText("Connected to " + remoteDevice.getName());
                    break;
            }
        }
    };



    //将下位机发来的数据加入到数据环中
    StringBuilder stringBuilder = new StringBuilder();
    private void pushData(byte[] pack, int packLength) {
        /*stringBuilder.append(packLength+"-");
        if(stringBuilder.length() > 50) {
            Log.i("CZQ", stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
        }*/
        //Log.i("CZQ", )
        if (!dataCircle.isFull()) {
            for(int i=0; i<packLength; i++) {
                if (!dataCircle.push(pack[i]))
                    break;
            }
        }
    }


    //开启一个轮询读取数据环的子线程
    private static int count = 0, previous = 0x00;
    private static byte tmpPack[] = new byte[14];
    private static boolean isStart = false;
    private void readData() {
        Byte tmp;
        if(!dataCircle.isEmpty()) {
            for (int i=0; i<14; i++) {
                if (isStart) {
                    if ((tmp=dataCircle.pull()) == null) {
                        break;
                    } else {
                        tmpPack[count++] = tmp;
                        if (count==14) {
                            sendData(tmpPack);
                            count = 2;
                            previous = 0x00;
                            isStart = false;
                            tmpPack = new byte[14];
                        }
                    }
                } else {
                    if ((tmp=dataCircle.pull()) == null) {
                        break;
                    } else {
                        if ((tmp&0xff)==0xaa && previous == 0xaa) {
                            //java中byte由补码来表示
                            tmpPack[0] = -86;  //0xAA
                            tmpPack[1] = -86;
                            isStart = true;
                        } else if((tmp&0xff)==0xaa) {
                            previous = tmp&0xff;
                        }
                    }
                }
            }
        } /*else {
            //getMeterData(new byte[14]);
        }*/
    }

    //从读取线程中读取到的数据发出来
    private void sendData(byte[] pack) {
        //Log.i("CZQ", bytesToHex(pack, 14));
        if (isLegal(pack)) {
            getMeterData(pack);
            mHandler.sendEmptyMessage(1);
        } else {
            Log.i("CZQ", bytesToHex(pack, 14));
        }
    }


    //判断数据包是否合法
    private boolean isLegal(byte[] pack) {
        int sumCheck = 0;
        for (int index = 13; index >= 0; index--) {
            if (index > 11) {
                //判断数据包包尾是否合法
                if ((pack[index] & 0xff) != 0xbb) {
                    Log.i("CZQ", "尾错误");
                    return false;
                }
            } else if (index<11 && index>2) {
                //sumCheck += (byte)(msg[index] & 0x0f);
                sumCheck += pack[index] & 0xff;
            } else if (index == 2) {
                sumCheck = sumCheck & 0xff;
                //判断数据包校验和是否正常
                if ((pack[11] & 0xff) != sumCheck) {
                    Log.i("CZQ", "校验错误");
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    //将接收到的数据包转化为虚拟表数据
    private void getMeterData(byte[] packet) {
        //mDeviceID = packet[3] & 0xff;
        int row, column, index = 4;
        for (row = 0; row < 3; row++) {
            for (column = 0; column < 2; column++) {
                meterData[row][column] = packet[index++] & 0xff;
            }
        }
        /*if ((packet[10] & 0xff) == 0) {
            meterData[0][0] = 0;
            meterData[0][1] = 0;
        }*/
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
                editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putBoolean("selected", true);
                editor.putString("deviceAddress", deviceAddress);
                editor.apply();
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


    //点击事件监听器
    private class MyOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mode1 :
                    Intent mode_1 = new Intent();
                    mode_1.setClass(MainActivity.this ,CarType.class);
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


    //一个测试发送数据的函数
    public void testWrite() {
        byte data[]  = new byte[8];
        for(int i = 0; i<8; i++) {
            data[i] = 0x0f;
        }
        mBluetoothService.write(data);
        //byte tmp = -0x7f;
    }




    //将byte转为对应字符串的函数
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public String bytesToHex(byte[] bytes, int length) {
        char[] buf = new char[length * 2];
        int index = 0;
        byte tmp;
        for(int i=0; i<length; i++) { // 利用位运算进行转换，可以看作方法一的变种
            tmp = bytes[i];
            buf[index++] = HEX_CHAR[tmp >>> 4 & 0x0f];
            buf[index++] = HEX_CHAR[tmp & 0x0f];
        }
        return new String(buf);
    }

}
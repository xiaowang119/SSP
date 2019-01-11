package com.example.zhx.ssp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.widget.AdapterView.INVALID_POSITION;
import static com.example.zhx.ssp.MainActivity.REQUEST_BLUETOOTH_PERMISSION;

public class BluetoothActivity extends AppCompatActivity {

    private ListView listView;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter arrayAdapter;
    private List<String> deviceMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        init();
    }

    private void init() {
        Button reFresh, sure;

        reFresh = (Button)findViewById(R.id.refresh);
        sure = (Button)findViewById(R.id.sure_select);
        listView = (ListView)findViewById(R.id.bluetooth_list);

        reFresh.setOnClickListener(new MyOnclickListener());
        sure.setOnClickListener(new MyOnclickListener());
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new MyOnItemSelectedListener());

        //设置广播接收过滤器
        IntentFilter filter = new IntentFilter();
        // 用BroadcastReceiver来取得搜索结果
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
        //获取相应权限
        getPermission();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //刷新设备列表
        reFreshDevice();
        //静默开启蓝牙
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    private void reFreshDevice() {
        //获取已经绑定的蓝牙设备
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        deviceMessage = new ArrayList<>();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                deviceMessage.add("设备名称：" + device.getName() + "\n" + device.getAddress());
            }
        }
        arrayAdapter = new ArrayAdapter<>(this,
                R.layout.bluetooth_device_list_item, R.id.device_item_text, deviceMessage);
        listView.setAdapter(arrayAdapter);

        //打开页面就自动进行一次搜索
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }


    /**
     * 定义蓝牙搜索广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //String name = device.getName() + "—" + device.getAddress();
                //Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();

                if (deviceMessage.contains("设备名称" + device.getName() + "\n" + device.getAddress()))
                    return;
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    //如果接收到未配对的设备，则加入到未配对集合中
                    deviceMessage.add("设备名称：" + device.getName() + "\n" + device.getAddress());
                    //更新适配器
                    arrayAdapter.notifyDataSetChanged();
                }

            }
        }
    };

    private class MyOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.refresh:
                    reFreshDevice();
                    break;
                case R.id.sure_select:
                    sendBack();
                    break;
            }
        }
    }

    private class MyOnItemSelectedListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listView.getChildAt(position).setSelected(true);
        }
    }

    //通过setResult为主界面传递信息
    private void sendBack() {
        int position = listView.getCheckedItemPosition();
        if (position != INVALID_POSITION) {
            TextView textView = (TextView) listView.getChildAt(position).findViewById(R.id.device_item_text);
            String message = textView.getText().toString();
            int index = message.indexOf('\n');
            String address = message.substring(index + 1);

            Intent msg = new Intent();
            msg.putExtra("deviceAddress", address);
            BluetoothActivity.this.setResult(1, msg);
            BluetoothActivity.this.finish();
        } else {
            showText("请选择设备！");
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

    private boolean hasAllPermissionGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void showText(String str) {
        Toast.makeText(BluetoothActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.isCheckable())
        {
            item.setCheckable(true);
        }
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent msg = new Intent();
                BluetoothActivity.this.setResult(2, msg);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

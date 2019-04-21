package com.example.zhx.ssp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import myUtil.BluetoothService;
import myUtil.DataApplication;
import myUtil.MyMessage;
import myUtil.TableAdapter;

public class MsgTable extends Activity {

    private long deviceID;
    private static int pageID = 1;
    private boolean avoidRepetition = false;
    private List<MyMessage> msgList;
    private TableAdapter msgAdapter;
    private ListView msgView;

    private byte[] packet = new byte[12];
    private Handler mHandler = new Handler();
    private DataApplication myApplication;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_msgtable);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_custom_title);

        myApplication = (DataApplication) getApplication();
        init();
    }

    private void init() {
        Intent lastIntent = getIntent();
        String deviceName = lastIntent.getStringExtra("deviceName");
        deviceID = lastIntent.getLongExtra("deviceID", 0L);

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle(deviceName + "参数列表");
        TextView headName = findViewById(R.id.header_text);
        headName.setText(deviceName);

        msgView = findViewById(R.id.msgView);
        msgList = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            msgList.add(new MyMessage());
        }

        msgAdapter = new TableAdapter(MsgTable.this, msgList);
        msgView.setAdapter(msgAdapter);

        setPacket();
        //tmpTest();
        //mHandler.postDelayed(writeThread, 100);
        mHandler.post(reFresh);
        mHandler.postDelayed(judgeState, 1000);
    }

    //定时刷新数据的线程
    private Runnable reFresh = new Runnable() {
        @Override
        public void run() {
            if (myApplication.mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                if (!myApplication.isAbnormal) {
                    setListTableData(true);
                }else {
                    setListTableData(false);
                }
            } else {
                setListTableData(false);
            }
            msgAdapter.notifyDataSetChanged();
            mHandler.postDelayed(reFresh, 100);
        }
    };

    //定时向下位机发送数据包的线程
    private Runnable writeThread = new Runnable() {
        @Override
        public void run() {
            //只有当有设备接入时才会发送数据包
            if (myApplication.mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                synchronized (MsgTable.this) {
                    packet[9] = getCheckSum();
                    myApplication.mBluetoothService.write(packet);
                }
            }
            mHandler.postDelayed(writeThread, 100);
        }
    };

    //根据收到数据包的时间间隔对设备的工作状态进行判断
    private Runnable judgeState = new Runnable() {
        @Override
        public void run() {
            if (myApplication.mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                if (myApplication.advance == myApplication.later && !avoidRepetition) {
                    myApplication.isAbnormal = true;
                    showText("设备状态异常！");
                    avoidRepetition = true;
                    //advance = !later;
                } else if (myApplication.advance != myApplication.later){
                    myApplication.isAbnormal = false;
                    myApplication.advance = myApplication.later;
                    avoidRepetition = false;
                }
            }else {
                myApplication.isAbnormal = false;
            }
            mHandler.postDelayed(judgeState, 1000);
        }
    };

    //设置数据包默认的部分内容
    private void setPacket() {
        int head = Integer.parseInt("aa", 16);
        int tail = Integer.parseInt("bb", 16);
        int count = Integer.parseInt("08", 16);
        int data = Integer.parseInt("55", 16);
        packet[0] = (byte)head;
        packet[1] = (byte)head;
        packet[2] = (byte)deviceID;
        packet[3] = (byte)count;
        packet[4] = (byte)data;
        packet[10] = (byte)tail;
        packet[11] = (byte)tail;
    }

    //得到当前数据包的校验和
    private byte getCheckSum() {
        byte checkSum = 0;
        for (int index = 2; index < 9; index++) {
            checkSum += (byte)(packet[index] & 0x0f);
        }
        return checkSum;
    }

    public void switchView(View view) {
        switch (view.getId()) {
            case R.id.page1:
                pageID = 1;
                break;
            case R.id.page2:
                pageID = 2;
                break;
            case R.id.page3:
                pageID = 3;
                break;
            case R.id.page4:
                pageID = 4;
                break;
            case R.id.page5:
                pageID = 5;
                break;
        }
    }

    private void setListTableData(boolean isGood) {
        if (isGood) {
            switch (pageID) {
                case 1:
                    for (int i = 0; i < 40; i++) {
                        msgList.get(i).setId(String.valueOf(i+1));
                        msgList.get(i).setDetection(String.valueOf(myApplication.listTableData[i]));
                    }
                    break;
                case 2:
                    for (int i = 40; i < 80; i++) {
                        msgList.get(i-40).setId(String.valueOf(i+1));
                        msgList.get(i-40).setDetection(String.valueOf(myApplication.listTableData[i]));
                    }
                    break;
                case 3:
                    for (int i = 80; i < 120; i++) {
                        msgList.get(i-80).setId(String.valueOf(i+1));
                        msgList.get(i-80).setDetection(String.valueOf(myApplication.listTableData[i]));
                    }
                    break;
                case 4:
                    for (int i = 120; i < 160; i++) {
                        msgList.get(i-120).setId(String.valueOf(i+1));
                        msgList.get(i-120).setDetection(String.valueOf(myApplication.listTableData[i]));
                    }
                    break;
                case 5:
                    for (int i = 160; i < 200; i++) {
                        msgList.get(i-160).setId(String.valueOf(i+1));
                        msgList.get(i-160).setDetection(String.valueOf(myApplication.listTableData[i]));
                    }
                    break;
            }
        }else {
            switch (pageID) {
                case 1:
                    for (int i = 0; i < 40; i++) {
                        msgList.get(i).setId(String.valueOf(i+1));
                        //msgList.get(i).setDetection(String.valueOf(listTableData[i]));
                    }
                    break;
                case 2:
                    for (int i = 40; i < 80; i++) {
                        msgList.get(i-40).setId(String.valueOf(i+1));
                    }
                    break;
                case 3:
                    for (int i = 80; i < 120; i++) {
                        msgList.get(i-80).setId(String.valueOf(i+1));
                    }
                    break;
                case 4:
                    for (int i = 120; i < 160; i++) {
                        msgList.get(i-120).setId(String.valueOf(i+1));
                    }
                    break;
                case 5:
                    for (int i = 160; i < 200; i++) {
                        msgList.get(i-160).setId(String.valueOf(i+1));
                    }
                    break;
            }
        }
    }

    public void showText(String str) {
        Toast.makeText(MsgTable.this, str, Toast.LENGTH_SHORT).show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.isCheckable())
        {
            item.setCheckable(true);
        }
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(writeThread);
        mHandler.removeCallbacks(judgeState);
    }







    /*private void tmpTest() {
        //做测试
        tmp = new ArrayList<>();
        String[] data = getResources().getString(R.string.msgTest1).split("\\|");
        for (int i = 0; i < 40; i++) {
            HashMap<String, String> msg = new HashMap<>();
            msg.put("num", i+1+"");
            msg.put("unit", data[1]);
            msg.put("standard", data[2]);
            msg.put("measure", data[3]);
            msg.put("remark", data[4]);
            tmp.add(msg);
        }
        tmpAdapter = new SimpleAdapter(this,
                tmp, R.layout.list_item,
                new String[]{"num", "unit", "standard", "measure", "remark"},
                new int[]{R.id.message_id, R.id.message_unit,
                        R.id.message_standard, R.id.message_detection, R.id.message_remark});
        msgView.setAdapter(tmpAdapter);
    }

    public void switchView(View view) {
        String[] data;
        switch (view.getId()) {
            case R.id.page1:
                //做测试
                tmp.clear();
                data = getResources().getString(R.string.msgTest1).split("\\|");
                for (int i = 0; i < 40; i++) {
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("num", i+1+"");
                    msg.put("unit", data[1]);
                    msg.put("standard", data[2]);
                    msg.put("measure", data[3]);
                    msg.put("remark", data[4]);
                    tmp.add(msg);
                }
                tmpAdapter.notifyDataSetChanged();
                break;
            case R.id.page2:
                //做测试
                tmp.clear();
                data = getResources().getString(R.string.msgTest2).split("\\|");
                for (int i = 0; i < 40; i++) {
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("num", i+41+"");
                    msg.put("unit", data[1]);
                    msg.put("standard", data[2]);
                    msg.put("measure", data[3]);
                    msg.put("remark", data[4]);
                    tmp.add(msg);
                }
                tmpAdapter.notifyDataSetChanged();
                break;
            case R.id.page3:
                //做测试
                tmp.clear();
                data = getResources().getString(R.string.msgTest3).split("\\|");
                for (int i = 0; i < 40; i++) {
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("num", i+81+"");
                    msg.put("unit", data[1]);
                    msg.put("standard", data[2]);
                    msg.put("measure", data[3]);
                    msg.put("remark", data[4]);
                    tmp.add(msg);
                }
                tmpAdapter.notifyDataSetChanged();
                break;
            case R.id.page4:
                //做测试
                tmp.clear();
                data = getResources().getString(R.string.msgTest4).split("\\|");
                for (int i = 0; i < 40; i++) {
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("num", i+121+"");
                    msg.put("unit", data[1]);
                    msg.put("standard", data[2]);
                    msg.put("measure", data[3]);
                    msg.put("remark", data[4]);
                    tmp.add(msg);
                }
                tmpAdapter.notifyDataSetChanged();
                break;
            case R.id.page5:
                //做测试
                tmp.clear();
                data = getResources().getString(R.string.msgTest5).split("\\|");
                for (int i = 0; i < 40; i++) {
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("num", i+161+"");
                    msg.put("unit", data[1]);
                    msg.put("standard", data[2]);
                    msg.put("measure", data[3]);
                    msg.put("remark", data[4]);
                    tmp.add(msg);
                }
                tmpAdapter.notifyDataSetChanged();
                break;
        }
    }*/

}

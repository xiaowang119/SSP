package com.example.zhx.ssp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import MyDialChartView.HumidityMeter;
import MyDialChartView.LiquidMeter;
import MyDialChartView.MyGraphicalView;
import MyDialChartView.PressureMeter;
import MyDialChartView.SpeedMeter;
import MyDialChartView.TachoMeter;
import MyDialChartView.TemperatureMeter;
import myUtil.BluetoothService;

import static com.example.zhx.ssp.MainActivity.advance;
import static com.example.zhx.ssp.MainActivity.later;
import static com.example.zhx.ssp.MainActivity.isAbnormal;
import static com.example.zhx.ssp.MainActivity.mBluetoothService;

public class ExaminePage extends Activity {

    private int deviceID = 0;
    private int progress1_max = 100;
    private int progress2_max = 100;
    private int switchNum = 10;
    private int progressNum = 2;
    private String deviceName;
    private List<TextView> progressTextList;
    private boolean avoidRepetition = false;
    private TextView tableText1, tableText2, tableText3, tableUnit1, tableUnit2,
            tableUnit3, stateTip_connected, stateTip_disconnected, stateTip_abnormal;

    private byte[] packet = new byte[12];
    private Handler mHandler = new Handler();
    MyGraphicalView meter1, meter2, meter3;

    protected void onCreate(Bundle saveIntanceState) {
        super.onCreate(saveIntanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_meter);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_custom_title);

        //通过上一个页面获取设备的id
        Intent msg = getIntent();
        if (msg != null) {
            deviceName = msg.getStringExtra("deviceName");
        }

        initView();
        start();
    }

    private void start() {
        setPacket();
        setTableData(new int[3][2]);
        mHandler.post(reFresh);
        //mHandler.postDelayed(writeThread, 100);
        mHandler.postDelayed(judgeState, 1000);
    }

    //界面控件初始化
    private void initView() {
        //SeekBar progress1, progress2;
        //List<ToggleButton> switchList;
        //ToggleButton sw1, sw2, sw3, sw4, sw5, sw6, sw7, sw8, sw9, sw10;

        //按照设定数量，获取进度条（拖条）
        if(progressNum>4) {
            progressNum = 4;
        }
        progressTextList = new ArrayList<>();
        for (int i=1; i<=progressNum; i++) {
            int layoutId = getResId("progressTextLayout"+i, R.id.class);
            findViewById(layoutId).setVisibility(View.VISIBLE);
            int progressTextId = getResId("progress"+i+"_text", R.id.class);
            progressTextList.add((TextView) findViewById(progressTextId));
            int progressId = getResId("progress"+i, R.id.class);
            SeekBar tmp = findViewById(progressId);
            tmp.setVisibility(View.VISIBLE);
            tmp.setOnSeekBarChangeListener(new MyProgressListener());
        }


        /*progress1 = findViewById(R.id.progress1);
        progress2 = findViewById(R.id.progress2);
        progress1.setMax(progress1_max);
        progress1.setMax(progress2_max);
        progress1.setOnSeekBarChangeListener(new MyProgressListener());
        progress2.setOnSeekBarChangeListener(new MyProgressListener());
        //获取拖条的数据显示文本框
        progress1_text = findViewById(R.id.progress1_text);
        progress2_text = findViewById(R.id.progress2_text);*/


        //按照设定数量，获取双态开关
        if(switchNum > 20) {
            //不可超过上限20个
            switchNum = 20;
        }
        //switchList = new ArrayList<>();
        for (int i = 1; i <= switchNum; i++) {
            int id = getResId("switch" + i, R.id.class);
                //int id = R.id.class.getField("switch" + i).getInt(null);
            ToggleButton tmp = findViewById(id);
            tmp.setVisibility(View.VISIBLE);
            tmp.setOnCheckedChangeListener(new MySwitchListener());
            //switchList.add(tmp);
        }

        /*sw1 = findViewById(R.id.switch1);
        sw2 = findViewById(R.id.switch2);
        sw3 = findViewById(R.id.switch3);
        sw4 = findViewById(R.id.switch4);
        sw5 = findViewById(R.id.switch5);
        sw6 = findViewById(R.id.switch6);
        sw7 = findViewById(R.id.switch7);
        sw8 = findViewById(R.id.switch8);
        sw9 = findViewById(R.id.switch9);
        sw10 = findViewById(R.id.switch10);
        sw1.setOnCheckedChangeListener(new MySwitchListener());
        sw2.setOnCheckedChangeListener(new MySwitchListener());
        sw3.setOnCheckedChangeListener(new MySwitchListener());
        sw4.setOnCheckedChangeListener(new MySwitchListener());
        sw5.setOnCheckedChangeListener(new MySwitchListener());
        sw6.setOnCheckedChangeListener(new MySwitchListener());
        sw7.setOnCheckedChangeListener(new MySwitchListener());
        sw8.setOnCheckedChangeListener(new MySwitchListener());
        sw9.setOnCheckedChangeListener(new MySwitchListener());
        sw10.setOnCheckedChangeListener(new MySwitchListener());*/

        //获取虚拟表的参数显示文本框
        tableText1 = findViewById(R.id.table_text1);
        tableText2 = findViewById(R.id.table_text2);
        tableText3 = findViewById(R.id.table_text3);
        tableUnit1 = findViewById(R.id.table_unit1);
        tableUnit2 = findViewById(R.id.table_unit2);
        tableUnit3 = findViewById(R.id.table_unit3);

        //画表
        drawMeter();

        //设备获取状态提示框
        stateTip_abnormal = findViewById(R.id.state_abnormal);
        stateTip_connected = findViewById(R.id.state_connected);
        stateTip_disconnected = findViewById(R.id.state_disconnected);
        if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
            String tip = "已连接到 " + MainActivity.remoteDevice.getName();
            stateTip_connected.setText(tip);
            stateTip_connected.setVisibility(View.VISIBLE);
            stateTip_disconnected.setVisibility(View.GONE);
        }
    }

    //根据前页的选择画出虚拟表盘
    private void drawMeter(){
        LinearLayout meterLayout1, meterLayout2, meterLayout3;

        //获取虚拟表的父布局
        meterLayout1 = findViewById(R.id.meter1_layout);
        meterLayout2 = findViewById(R.id.meter2_layout);
        meterLayout3 = findViewById(R.id.meter3_layout);

        //设置虚拟表在父控件中的布局方式
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 5.5f);

        //按需求选择填充的虚拟表
        /* TemperatureMeter temperatureMeter = new TemperatureMeter(this);
        temperatureMeter.setLayoutParams(tmp);
        meterLayout1.addView(temperatureMeter, 0);*/

        switch (deviceName) {
            case "温度传感器":
                meter1 = new TemperatureMeter(this, 90f);
                meter1.setLayoutParams(layout);
                meterLayout1.addView(meter1, 0);
                meter2 = new TemperatureMeter(this, 180f);
                meter2.setLayoutParams(layout);
                meterLayout2.addView(meter2, 0);
                meter3 = new TemperatureMeter(this);
                meter3.setLayoutParams(layout);
                meterLayout3.addView(meter3, 0);
                tableUnit1.setText("℃");
                tableUnit2.setText("℃");
                tableUnit3.setText("℃");
                break;
            case "湿度传感器":
                meter1 = new HumidityMeter(this, 90f);
                meter1.setLayoutParams(layout);
                meterLayout1.addView(meter1, 0);
                meter2 = new HumidityMeter(this, 180f);
                meter2.setLayoutParams(layout);
                meterLayout2.addView(meter2, 0);
                meter3 = new HumidityMeter(this);
                meter3.setLayoutParams(layout);
                meterLayout3.addView(meter3, 0);
                tableUnit1.setText("%");
                tableUnit2.setText("%");
                tableUnit3.setText("%");
                break;
            case "压力传感器":
                meter1 = new PressureMeter(this, 90f);
                meter1.setLayoutParams(layout);
                meterLayout1.addView(meter1, 0);
                meter2 = new PressureMeter(this, 180f);
                meter2.setLayoutParams(layout);
                meterLayout2.addView(meter2, 0);
                meter3 = new PressureMeter(this);
                meter3.setLayoutParams(layout);
                meterLayout3.addView(meter3, 0);
                tableUnit1.setText("KPa");
                tableUnit2.setText("KPa");
                tableUnit3.setText("KPa");
                break;
            case "速度传感器":
                meter1 = new SpeedMeter(this, 90f);
                meter1.setLayoutParams(layout);
                meterLayout1.addView(meter1, 0);
                meter2 = new SpeedMeter(this, 180f);
                meter2.setLayoutParams(layout);
                meterLayout2.addView(meter2, 0);
                meter3 = new SpeedMeter(this);
                meter3.setLayoutParams(layout);
                meterLayout3.addView(meter3, 0);
                tableUnit1.setText("km/h");
                tableUnit2.setText("km/h");
                tableUnit3.setText("km/h");
                break;
            case "转速传感器":
                meter1 = new TachoMeter(this, 90f);
                meter1.setLayoutParams(layout);
                meterLayout1.addView(meter1, 0);
                meter2 = new TachoMeter(this, 180f);
                meter2.setLayoutParams(layout);
                meterLayout2.addView(meter2, 0);
                meter3 = new TachoMeter(this);
                meter3.setLayoutParams(layout);
                meterLayout3.addView(meter3, 0);
                tableUnit1.setText("r/min");
                tableUnit2.setText("r/min");
                tableUnit3.setText("r/min");
                break;
            case "液位传感器":
                meter1 = new LiquidMeter(this, 90f);
                meter1.setLayoutParams(layout);
                meterLayout1.addView(meter1, 0);
                meter2 = new LiquidMeter(this, 180f);
                meter2.setLayoutParams(layout);
                meterLayout2.addView(meter2, 0);
                meter3 = new LiquidMeter(this);
                meter3.setLayoutParams(layout);
                meterLayout3.addView(meter3, 0);
                tableUnit1.setVisibility(View.GONE);
                tableUnit2.setVisibility(View.GONE);
                tableUnit3.setVisibility(View.GONE);
                break;
        }
    }

    //设置数据包默认的部分内容
    private void setPacket() {
        int head = Integer.parseInt("aa", 16);
        int tail = Integer.parseInt("bb", 16);
        int count = Integer.parseInt("08", 16);
        packet[0] = (byte)head;
        packet[1] = (byte)head;
        packet[2] = (byte)deviceID;
        packet[3] = (byte)count;
        packet[10] = (byte)tail;
        packet[11] = (byte)tail;
    }

    //定时向下位机发送数据包的线程
    private Runnable writeThread = new Runnable() {
        @Override
        public void run() {
            //只有当有设备接入时才会发送数据包
            if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                synchronized (ExaminePage.this) {
                    packet[9] = getCheckSum();
                    mBluetoothService.write(packet);
                }
            }
            mHandler.postDelayed(writeThread, 100);
        }
    };

    //临时定义一个随机决定表盘指针的函数
    private void refreshPointer() {
        Random random = new Random();
        meter1.setCurrentStatus(random.nextFloat());
        meter2.setCurrentStatus(random.nextFloat());
        meter3.setCurrentStatus(random.nextFloat());
        //重构画面
        meter1.invalidate();
        meter2.invalidate();
        meter3.invalidate();
    }

    //定时刷新虚拟表的数据
    private Runnable reFresh = new Runnable() {
        @Override
        public void run() {
            refreshPointer();
            if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                if (!MainActivity.isAbnormal) {
                    setTableData(MainActivity.meterData);
                }else {
                    setTableData(new int[3][2]);
                    stateTip_connected.setVisibility(View.GONE);
                    stateTip_disconnected.setVisibility(View.GONE);
                    stateTip_abnormal.setVisibility(View.VISIBLE);
                }
            }else {
                setTableData(new int[3][2]);
                stateTip_abnormal.setVisibility(View.GONE);
                stateTip_connected.setVisibility(View.GONE);
                stateTip_disconnected.setVisibility(View.VISIBLE);
            }
            mHandler.postDelayed(reFresh, 1000);
        }
    };

    //根据收到数据包的时间间隔对设备的工作状态进行判断
    private Runnable judgeState = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                if (advance == later && !avoidRepetition) {
                    isAbnormal = true;
                    showText("设备状态异常！");
                    avoidRepetition = true;
                    //advance = !later;
                } else if (advance != later){
                    isAbnormal = false;
                    advance = later;
                    avoidRepetition = false;
                }
            }else {
                isAbnormal = false;
            }
            mHandler.postDelayed(judgeState, 1000);
        }
    };

    //设置虚拟表参数
    private void setTableData(int[][] msg) {
        for (int i = 0; i < 3; i++) {
            double data = (double)msg[i][0] + (double)msg[i][1]/1000;
            String tmp = String.valueOf(data);
            switch (i + 1) {
                case 1:
                    tableText1.setText(tmp);
                    break;
                case 2:
                    tableText2.setText(tmp);
                    break;
                case 3:
                    tableText3.setText(tmp);
                    break;
            }
        }
    }

    //得到当前数据包的校验和
    private byte getCheckSum() {
        byte checkSum = 0;
        for (int index = 2; index < 9; index++) {
            checkSum += (byte)(packet[index] & 0x0f);
        }
        return checkSum;
    }

    private class MyProgressListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //String value = progress + "";
            switch (seekBar.getId()) {
                case R.id.progress1 :
                    progressTextList.get(0).setText(String.valueOf(progress));

                    //按照自己的需求设置托条1具体的值
                    packet[7] = (byte)progress;
                    break;
                case R.id.progress2 :
                    progressTextList.get(1).setText(String.valueOf(progress));

                    //按照自己的需求设置托条2具体的值
                    packet[8] = (byte)progress;
                    break;
                case R.id.progress3 :
                    progressTextList.get(2).setText(String.valueOf(progress));

                    //按照自己的需求设置托条3具体的值
                    packet[7] = (byte)progress;
                    break;
                case R.id.progress4 :
                    progressTextList.get(3).setText(String.valueOf(progress));

                    //按照自己的需求设置托条4具体的值
                    packet[8] = (byte)progress;
                    break;
            }
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private class MySwitchListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //通过两态开关来决定上位机的数据包内容
            switch (buttonView.getId()) {
                case R.id.switch1 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x01);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xfe);
                    }
                    break;
                case R.id.switch2 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x02);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xfd);
                    }
                    break;
                case R.id.switch3 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x04);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xfb);
                    }
                    break;
                case R.id.switch4 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x08);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xf7);
                    }
                    break;
                case R.id.switch5 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x10);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xef);
                    }
                    break;
                case R.id.switch6 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x20);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xdf);
                    }
                    break;
                case R.id.switch7 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x40);
                    } else {
                        packet[4] = (byte)(packet[4] & 0xbf);
                    }
                    break;
                case R.id.switch8 :
                    if (isChecked) {
                        packet[4] = (byte)(packet[4] | 0x80);
                    } else {
                        packet[4] = (byte)(packet[4] & 0x7f);
                    }
                    break;
                case R.id.switch9 :
                    if (isChecked) {
                        packet[5] = (byte)(packet[5] | 0x01);
                    } else {
                        packet[5] = (byte)(packet[5] & 0xfe);
                    }
                    break;
                case R.id.switch10 :
                    if (isChecked) {
                        packet[5] = (byte)(packet[5] | 0x02);
                    } else {
                        packet[5] = (byte)(packet[5] & 0xfd);
                    }
                    break;
            }
        }
    }

    public void showText(String str) {
        Toast.makeText(ExaminePage.this, str, Toast.LENGTH_SHORT).show();
    }

    //通过字符串获取资源的id，进而访问资源
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
        mHandler.removeCallbacks(reFresh);
    }
}

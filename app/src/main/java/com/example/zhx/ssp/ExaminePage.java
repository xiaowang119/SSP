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

import MyDialChartView.HumidityMeter;
import MyDialChartView.LiquidMeter;
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

    private long deviceID;
    private boolean avoidRepetition = false;
    private TextView progress1_text, progress2_text,
            tableText1, tableText2, tableText3, tableUnit1, tableUnit2, tableUnit3,
            stateTip_connected, stateTip_disconnected, stateTip_abnormal;

    private byte[] packet = new byte[12];
    private Handler mHandler = new Handler();

    protected void onCreate(Bundle saveIntanceState) {
        super.onCreate(saveIntanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_meter);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_custom_title);

        //通过上一个页面获取设备的id
        Intent msg = getIntent();
        if (msg != null) {
            deviceID = msg.getLongExtra("deviceID", 0L);
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
        SeekBar progress1, progress2;
        ToggleButton sw1, sw2, sw3, sw4, sw5, sw6, sw7, sw8, sw9, sw10;

        //获取进度条（拖条）
        progress1 = (SeekBar)findViewById(R.id.progress1);
        progress2 = (SeekBar)findViewById(R.id.progress2);
        progress1.setOnSeekBarChangeListener(new MyProgressListener());
        progress2.setOnSeekBarChangeListener(new MyProgressListener());

        //获取十个双态开关
        sw1 = (ToggleButton)findViewById(R.id.switch1);
        sw2 = (ToggleButton)findViewById(R.id.switch2);
        sw3 = (ToggleButton)findViewById(R.id.switch3);
        sw4 = (ToggleButton)findViewById(R.id.switch4);
        sw5 = (ToggleButton)findViewById(R.id.switch5);
        sw6 = (ToggleButton)findViewById(R.id.switch6);
        sw7 = (ToggleButton)findViewById(R.id.switch7);
        sw8 = (ToggleButton)findViewById(R.id.switch8);
        sw9 = (ToggleButton)findViewById(R.id.switch9);
        sw10 = (ToggleButton)findViewById(R.id.switch10);
        sw1.setOnCheckedChangeListener(new MySwitchListener());
        sw2.setOnCheckedChangeListener(new MySwitchListener());
        sw3.setOnCheckedChangeListener(new MySwitchListener());
        sw4.setOnCheckedChangeListener(new MySwitchListener());
        sw5.setOnCheckedChangeListener(new MySwitchListener());
        sw6.setOnCheckedChangeListener(new MySwitchListener());
        sw7.setOnCheckedChangeListener(new MySwitchListener());
        sw8.setOnCheckedChangeListener(new MySwitchListener());
        sw9.setOnCheckedChangeListener(new MySwitchListener());
        sw10.setOnCheckedChangeListener(new MySwitchListener());

        //获取虚拟表的参数显示文本框
        tableText1 = (TextView)findViewById(R.id.table_text1);
        tableText2 = (TextView)findViewById(R.id.table_text2);
        tableText3 = (TextView)findViewById(R.id.table_text3);
        tableUnit1 = (TextView)findViewById(R.id.table_unit1);
        tableUnit2 = (TextView)findViewById(R.id.table_unit2);
        tableUnit3 = (TextView)findViewById(R.id.table_unit3);

        //画表
        drawMeter();

        //获取拖条的数据显示文本框
        progress1_text = (TextView)findViewById(R.id.progress1_text);
        progress2_text = (TextView)findViewById(R.id.progress2_text);

        //设备获取状态提示框
        stateTip_abnormal = (TextView)findViewById(R.id.state_abnormal);
        stateTip_connected = (TextView)findViewById(R.id.state_connected);
        stateTip_disconnected = (TextView)findViewById(R.id.state_disconnected);
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
        meterLayout1 = (LinearLayout)findViewById(R.id.meter1_layout);
        meterLayout2 = (LinearLayout)findViewById(R.id.meter2_layout);
        meterLayout3 = (LinearLayout)findViewById(R.id.meter3_layout);

        //设置虚拟表在父控件中的布局方式
        LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 5.5f);

        //按需求选择填充的虚拟表
        /* TemperatureMeter temperatureMeter = new TemperatureMeter(this);
        temperatureMeter.setLayoutParams(tmp);
        meterLayout1.addView(temperatureMeter, 0);*/
        TemperatureMeter temperatureMeter;
        HumidityMeter humidityMeter;
        PressureMeter pressureMeter;
        SpeedMeter speedMeter;
        LiquidMeter liquidMeter;
        TachoMeter tachoMeter;

        switch ((int)deviceID) {
            case 0:
                temperatureMeter = new TemperatureMeter(this);
                temperatureMeter.setLayoutParams(tmp);
                meterLayout1.addView(temperatureMeter, 0);
                temperatureMeter = new TemperatureMeter(this);
                temperatureMeter.setLayoutParams(tmp);
                meterLayout2.addView(temperatureMeter, 0);
                temperatureMeter = new TemperatureMeter(this);
                temperatureMeter.setLayoutParams(tmp);
                meterLayout3.addView(temperatureMeter, 0);
                tableUnit1.setText("℃");
                tableUnit2.setText("℃");
                tableUnit3.setText("℃");
                break;
            case 1:
                humidityMeter = new HumidityMeter(this);
                humidityMeter.setLayoutParams(tmp);
                meterLayout1.addView(humidityMeter, 0);
                humidityMeter = new HumidityMeter(this);
                humidityMeter.setLayoutParams(tmp);
                meterLayout2.addView(humidityMeter, 0);
                humidityMeter = new HumidityMeter(this);
                humidityMeter.setLayoutParams(tmp);
                meterLayout3.addView(humidityMeter, 0);
                tableUnit1.setText("%");
                tableUnit2.setText("%");
                tableUnit3.setText("%");
                break;
            case 2:
                pressureMeter = new PressureMeter(this);
                pressureMeter.setLayoutParams(tmp);
                meterLayout1.addView(pressureMeter, 0);
                pressureMeter = new PressureMeter(this);
                pressureMeter.setLayoutParams(tmp);
                meterLayout2.addView(pressureMeter, 0);
                pressureMeter = new PressureMeter(this);
                pressureMeter.setLayoutParams(tmp);
                meterLayout3.addView(pressureMeter, 0);
                tableUnit1.setText("KPa");
                tableUnit2.setText("KPa");
                tableUnit3.setText("KPa");
                break;
            case 3:
                speedMeter = new SpeedMeter(this);
                speedMeter.setLayoutParams(tmp);
                meterLayout1.addView(speedMeter, 0);
                speedMeter = new SpeedMeter(this);
                speedMeter.setLayoutParams(tmp);
                meterLayout2.addView(speedMeter, 0);
                speedMeter = new SpeedMeter(this);
                speedMeter.setLayoutParams(tmp);
                meterLayout3.addView(speedMeter, 0);
                tableUnit1.setText("km/h");
                tableUnit2.setText("km/h");
                tableUnit3.setText("km/h");
                break;
            case 4:
                tachoMeter = new TachoMeter(this);
                tachoMeter.setLayoutParams(tmp);
                meterLayout1.addView(tachoMeter, 0);
                tachoMeter = new TachoMeter(this);
                tachoMeter.setLayoutParams(tmp);
                meterLayout2.addView(tachoMeter, 0);
                tachoMeter = new TachoMeter(this);
                tachoMeter.setLayoutParams(tmp);
                meterLayout3.addView(tachoMeter, 0);
                tableUnit1.setText("r/min");
                tableUnit2.setText("r/min");
                tableUnit3.setText("r/min");
                break;
            case 5:
                liquidMeter = new LiquidMeter(this);
                liquidMeter.setLayoutParams(tmp);
                meterLayout1.addView(liquidMeter, 0);
                liquidMeter = new LiquidMeter(this);
                liquidMeter.setLayoutParams(tmp);
                meterLayout2.addView(liquidMeter, 0);
                liquidMeter = new LiquidMeter(this);
                liquidMeter.setLayoutParams(tmp);
                meterLayout3.addView(liquidMeter, 0);
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

    //定时刷新虚拟表的数据
    private Runnable reFresh = new Runnable() {
        @Override
        public void run() {
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
            mHandler.postDelayed(reFresh, 100);
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
            String value = progress + "";
            if (seekBar.getId() == R.id.progress1) {
                progress1_text.setText(value);

                //按照自己的需求设置托条1具体的值
                packet[7] = (byte)Integer.parseInt(value, 10);
            }else {
                progress2_text.setText(value);

                //按照自己的需求设置托条2具体的值
                packet[8] = (byte)Integer.parseInt(value, 10);
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

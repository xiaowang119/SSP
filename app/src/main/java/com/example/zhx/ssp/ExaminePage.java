package com.example.zhx.ssp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import MyDialChartView.AmpereMeter;
import MyDialChartView.HumidityMeter;
import MyDialChartView.LiquidMeter;
import MyDialChartView.MyGraphicalView;
import MyDialChartView.PressureMeter;
import MyDialChartView.SpeedMeter;
import MyDialChartView.TachoMeter;
import MyDialChartView.TemperatureMeter;
import MyDialChartView.VoltMeter;
import myUtil.BluetoothService;
import myUtil.Constants;
import myUtil.DataApplication;
import myUtil.MyThread;

public class ExaminePage extends Activity {

    //连接设备的id
    private int deviceID = 0;
    //连接设备的名称
    private String deviceName;
    //四个拖条的最大值
    private static int progress1_max = 100,
            progress2_max = 100, progress3_max = 100, progress4_max = 100;
    //定义开关数量
    private int switchNum = 10;
    //定义拖条数量
    private int progressNum = 2;
    //拖条的文本信息显示框组成的链表
    private List<TextView> progressTextList;
    //flag变量，防止反复的异常提升
    private boolean avoidRepetition = false;
    //虚拟表的文本控件
    private TextView tableText1, tableText2, tableText3, tableUnit1, tableUnit2,
            tableUnit3, stateTip_connected, stateTip_disconnected, stateTip_abnormal;
    //发给下位机的数据包
    private byte[] packet = new byte[12];
    //虚拟表的单位和最大标识范围
    private float unit_Max[][] = new float[3][2];
    private Handler mHandler;
    //虚拟表，按需进行初始化（父类引用指向子类对象）
    //MyGraphicalView meter1, meter2, meter3;
    MyGraphicalView meter[] = new MyGraphicalView[3];
    private DataApplication myApplication;



    protected void onCreate(Bundle saveIntanceState) {
        super.onCreate(saveIntanceState);
        //更改界面title样式
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_meter);
        getWindow().setBackgroundDrawable(null);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_custom_title);

        myApplication = (DataApplication) getApplication();
        //通过上一个页面获取设备的name
        Intent msg = getIntent();
        if (msg != null) {
            deviceName = msg.getStringExtra("deviceName");
        }
        initView();

        setHandler();
        start();
        new MyThread(250, mHandler, Constants.FROM_REFRESH_THREAD).start();

    }

    private void setHandler() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case Constants.FROM_REFRESH_THREAD:
                        reFresh();
                        break;
                    case Constants.FROM_STATUS_THREAD:

                        break;
                    case Constants.FROM_WRITE_THREAD:

                        break;
                }
            }
        };
    }

    private void start() {
        setPacket();
        setTableData(new float[3]);
        //mHandler.post(reFresh);
        //new RefreshUtil(mmHandler).start();
        //mHandler.postDelayed(writeThread, 100);
        //mHandler.postDelayed(judgeState, 1000);
    }

    //界面控件初始化
    private void initView() {
        //初始化虚拟表参数
        unit_Max[0][0] = 0.1f;
        unit_Max[0][1] = 220;
        unit_Max[1][0] = 0.1f;
        unit_Max[1][1] = 2000;
        unit_Max[2][0] = 1;
        unit_Max[2][1] = 2000;

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
            switch (i) {
                case 1: tmp.setMax(progress1_max); break;
                case 2: tmp.setMax(progress2_max); break;
                case 3: tmp.setMax(progress3_max); break;
                case 4: tmp.setMax(progress4_max); break;
            }
            tmp.setOnSeekBarChangeListener(new MyProgressListener());
        }

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
        if (myApplication.mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
            String tip = "已连接到 " + myApplication.remoteDevice.getName();
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

        switch (deviceName) {
            case "温度传感器":
                meter[0] = new TemperatureMeter(this, 90f);
                meter[0].setLayoutParams(layout);
                meterLayout1.addView(meter[0], 0);
                meter[1] = new TemperatureMeter(this, 180f);
                meter[1].setLayoutParams(layout);
                meterLayout2.addView(meter[1], 0);
                meter[2] = new TemperatureMeter(this);
                meter[2].setLayoutParams(layout);
                meterLayout3.addView(meter[2], 0);
                tableUnit1.setText("℃");
                tableUnit2.setText("℃");
                tableUnit3.setText("℃");
                break;
            case "湿度传感器":
                meter[0] = new HumidityMeter(this, 90f);
                meter[0].setLayoutParams(layout);
                meterLayout1.addView(meter[0], 0);
                meter[1] = new HumidityMeter(this, 180f);
                meter[1].setLayoutParams(layout);
                meterLayout2.addView(meter[1], 0);
                meter[2] = new HumidityMeter(this);
                meter[2].setLayoutParams(layout);
                meterLayout3.addView(meter[2], 0);
                tableUnit1.setText("%");
                tableUnit2.setText("%");
                tableUnit3.setText("%");
                break;
            case "压力传感器":
                meter[0] = new PressureMeter(this, 90f);
                meter[0].setLayoutParams(layout);
                meterLayout1.addView(meter[0], 0);
                meter[1] = new PressureMeter(this, 180f);
                meter[1].setLayoutParams(layout);
                meterLayout2.addView(meter[1], 0);
                meter[2] = new PressureMeter(this);
                meter[2].setLayoutParams(layout);
                meterLayout3.addView(meter[2], 0);
                tableUnit1.setText("KPa");
                tableUnit2.setText("KPa");
                tableUnit3.setText("KPa");
                break;
            case "速度传感器":
                meter[0] = new SpeedMeter(this);
                meter[0].setLayoutParams(layout);
                meterLayout1.addView(meter[0], 0);
                meter[1] = new AmpereMeter(this, 180f);
                meter[1].setLayoutParams(layout);
                meterLayout2.addView(meter[1], 0);
                meter[2] = new VoltMeter(this, 180f);
                meter[2].setLayoutParams(layout);
                meterLayout3.addView(meter[2], 0);
                tableUnit1.setText("km/h");
                tableUnit2.setText("mA");
                tableUnit3.setText("mV");
                break;
            case "转速传感器":
                meter[0] = new TachoMeter(this);
                meter[0].setLayoutParams(layout);
                meterLayout1.addView(meter[0], 0);
                meter[1] = new TachoMeter(this);
                meter[1].setLayoutParams(layout);
                meterLayout2.addView(meter[1], 0);
                meter[2] = new TachoMeter(this);
                meter[2].setLayoutParams(layout);
                meterLayout3.addView(meter[2], 0);
                tableUnit1.setText("r/min");
                tableUnit2.setText("r/min");
                tableUnit3.setText("r/min");
                break;
            case "液位传感器":
                meter[0] = new LiquidMeter(this, 90f);
                meter[0].setLayoutParams(layout);
                meterLayout1.addView(meter[0], 0);
                meter[1] = new LiquidMeter(this, 180f);
                meter[1].setLayoutParams(layout);
                meterLayout2.addView(meter[1], 0);
                meter[2] = new LiquidMeter(this);
                meter[2].setLayoutParams(layout);
                meterLayout3.addView(meter[2], 0);
                tableUnit1.setVisibility(View.GONE);
                tableUnit2.setVisibility(View.GONE);
                tableUnit3.setVisibility(View.GONE);
                break;
        }
    }

    //定时向下位机发送数据包的线程
    private Runnable writeThread = new Runnable() {
        @Override
        public void run() {
            //只有当有设备接入时才会发送数据包
            if (myApplication.mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                synchronized (ExaminePage.this) {
                    packet[9] = getCheckSum();
                    myApplication.mBluetoothService.write(packet);
                }
            }
            mHandler.postDelayed(writeThread, 500);
        }
    };



    private void reFresh() {
        //Log.i("CZQ", "刷新一次");
        setTableData(refreshPointer());
        //setTableData(new float[3]);
        /*if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
            if (MainActivity.isAbnormal) {
                stateTip_connected.setVisibility(View.GONE);
                stateTip_disconnected.setVisibility(View.GONE);
                stateTip_abnormal.setVisibility(View.VISIBLE);
            }
        }else {
            stateTip_abnormal.setVisibility(View.GONE);
            stateTip_connected.setVisibility(View.GONE);
            stateTip_disconnected.setVisibility(View.VISIBLE);
        }*/
    }

    //定义一个刷新表盘指针的函数
    private float[] refreshPointer() {
        //根据数据实时跳变
        /*float tmpData[] = new float[3];
        //long time = System.currentTimeMillis();
        for (int i=0; i<3; i++) {
            tmpData[i] = (meterData[i][0]*256 + meterData[i][1]) * unit_Max[i][0];
            float tmp = tmpData[i]/unit_Max[i][1];
            //Log.i("CZQ", ""+tmp);
            if (tmp > 1) {
                meter[i].setCurrentStatus(1);
            } else if (tmp < 0){
                meter[i].setCurrentStatus(0);
            } else {
                meter[i].setCurrentStatus(tmp);
            }
            meter[i].invalidate();
            tmpData[i] = (float)(Math.round(tmpData[i]*100))/100;
        }
       // Log.i("CZQ", Long.toString(System.currentTimeMillis()-time));
        return tmpData;*/



        //测试型随机跳变
        /*float tmp = (float) Math.random();
        meter[0].setCurrentStatus(tmp);
        meter[1].setCurrentStatus(tmp);
        meter[2].setCurrentStatus(tmp);
        meter[0].invalidate();
        meter[1].invalidate();
        meter[2].invalidate();
        return new float[3];*/


        //更具变化方向缓慢进行
        float tmpData[] = new float[3];
        for (int i=0; i<3; i++) {
            tmpData[i] = (myApplication.meterData[i][0]*256
                    + myApplication.meterData[i][1]) * unit_Max[i][0];
            float current = tmpData[i]/unit_Max[i][1];
            float old = meter[i].getmPercentage();
            Log.i("CZQ",Float.toString(current)+"-"+Float.toString(old));
            if(Math.abs(current-old) < 1.0f/12) {
                if (current > 1) {
                    meter[i].setCurrentStatus(1);
                    tmpData[i] = 1;
                } else if(current < 0) {
                    meter[i].setCurrentStatus(0);
                    tmpData[i] = 0;
                } else {
                    meter[i].setCurrentStatus(current);
                    tmpData[i] = current*unit_Max[i][1];
                }
            } else {
                if (current > old) {
                    if (old+(1.0f/12) < 1) {
                        meter[i].setCurrentStatus(old+(1.0f/12));
                        tmpData[i] = (old+(1.0f/12))*unit_Max[i][1];
                    } else {
                        meter[i].setCurrentStatus(1);
                        tmpData[i] = 1;
                    }
                } else {
                    if (old-(1.0f/12) > 0) {
                        meter[i].setCurrentStatus(old-(1.0f/12));
                        tmpData[i] = (old-(1.0f/12))*unit_Max[i][1];
                    } else {
                        meter[i].setCurrentStatus(0);
                        tmpData[i] = 0;
                    }
                }
            }
            meter[i].invalidate();
        }
        return tmpData;
    }



    //根据收到数据包的时间间隔对设备的工作状态进行判断
    /*private Runnable judgeState = new Runnable() {
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
    };*/

    //设置虚拟表参数
    private void setTableData(float[] msg) {
        for (int i = 0; i < 3; i++) {
            String tmp = String.valueOf(msg[i]);
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

    //按返回键时销毁当前activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.isCheckable()) {
            item.setCheckable(true);
        }
        switch (item.getItemId()) {
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
        //mHandler.removeCallbacks(judgeState);
        //mHandler.removeCallbacks(reFresh);
    }
}

package com.example.zhx.ssp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import MyUtil.DeviceListAdapter;
import MyUtil.DeviceListItem;

public class Mode1 extends AppCompatActivity {
    private int name_id[] = {
            R.string.temp_meter,
            R.string.humi_meter,
            R.string.pres_meter,
            R.string.spee_meter,
            R.string.tach_meter,
            R.string.liqu_meter,
    };
    private int drawable_id[] = {
            R.drawable.temperature_meter,
            R.drawable.humidity_meter,
            R.drawable.pressure_meter,
            R.drawable.speed_meter,
            R.drawable.tacho_meter,
            R.drawable.liquid_meter
    };

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode1);

        show();
    }

    private void show() {
        ListView sensorView = (ListView)findViewById(R.id.sensorView1);
        sensorView.setOnItemClickListener(new MyItemClickListener());

        List<DeviceListItem> sensorList = new ArrayList<>();
        for (int i=0; i<name_id.length; i++) {
            sensorList.add(new DeviceListItem(name_id[i], drawable_id[i]));
        }

        //DeviceListAdapter是自己构造的用于显示传感器列表的适配器、里面对itme的样式做了细致的设定
        ArrayAdapter<DeviceListItem> sensorAdapter = new DeviceListAdapter(this, sensorList);
        sensorView.setAdapter(sensorAdapter);
    }

    private class MyItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent meter = new Intent();
            TextView tmpView = view.findViewById(R.id.sensor_list_text);
            String name = tmpView.getText().toString();
            meter.putExtra("deviceName", name);
            meter.setClass(Mode1.this, ExaminePage.class);
            startActivity(meter);
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


   /* public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }*/

    /*public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition+ listView.getChildCount() - 1;
        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }*/

/*
    public void write(byte[] request) {
        if (socket != null) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(request);
                Toast.makeText(Mode1.this,"XDRCFVTGYBHUNJ",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//发送数据
*/

}

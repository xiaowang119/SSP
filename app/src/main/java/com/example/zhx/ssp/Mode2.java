package com.example.zhx.ssp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mode2 extends AppCompatActivity {

    private ListView sensorView;
    private List<String> sensorList;
    private ArrayAdapter<String> sensorAdapter;

    protected void onCreate(Bundle savedInstantceState) {
        super.onCreate(savedInstantceState);
        setContentView(R.layout.activity_mode2);

        sensorView = (ListView)findViewById(R.id.sensorView2);
        sensorView.setOnItemClickListener(new MyItemClickListener());
        show();

    }

    private void show() {
        sensorList = new ArrayList<>();
        String[] senoers = this.getString(R.string.sensor2).split("\\|");
        for (int i=0; i<senoers.length; i++) {
            sensorList.add(senoers[i].trim());
        }
        sensorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, sensorList);
        sensorView.setAdapter(sensorAdapter);
    }

    private class MyItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent table = new Intent();
            String device = (String) parent.getItemAtPosition(position);
            table.putExtra("deviceName", device);
            table.putExtra("deviceID", id);
            table.setClass(Mode2.this, MsgTable.class);
            startActivity(table);
        }
    }

    //在返回的同时销毁当前活动页面
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

}

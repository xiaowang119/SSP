package com.example.zhx.ssp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;

public class CarType extends AppCompatActivity {


    int[] idList = new int[12];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.type_activity);

        init();
    }

    private void init() {
        //String idName[] = new String[12];
        //TextView viewList[] = new TextView[12];
        for (int i=0; i<idList.length; i++) {
            //idName[i] = "R.id.carType"+ (i+1);
            idList[i] = getResId("carType"+ (i+1), R.id.class);
            TextView tmp = findViewById(idList[i]);
            tmp.setOnClickListener(new MyOnClickListener());
        }
    }


    //通过资源id名获取其id号
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            Intent testPage = new Intent();
            testPage.setClass(CarType.this, ExaminePage.class);
            //把用户选定的车型id号传到测试页面
            //testPage.putExtra("carID", id);
            testPage.putExtra("deviceName", "速度传感器");
            startActivity(testPage);
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

}

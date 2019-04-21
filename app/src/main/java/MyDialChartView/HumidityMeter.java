package MyDialChartView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import org.xclcharts.chart.DialChart;
import org.xclcharts.common.MathHelper;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.plot.PlotAttrInfo;

import java.util.ArrayList;
import java.util.List;


public class HumidityMeter extends MyGraphicalView {

    //private String TAG = "DialChart05View";
    private DialChart chart = new MyChart();
    //private float mPercentage = 0.0f;

    public HumidityMeter(Context context) {
        super(context);
        chartRender();
    }

    public HumidityMeter(Context context, float totalAngel) {
        super(context);
        chart = new MyChart(totalAngel);
        chartRender();
    }

    public HumidityMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        chartRender();
    }

    public HumidityMeter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        chartRender();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chart.setChartRange(w ,h );
    }


    public void chartRender() {
        try {
            //设置标题背景
            chart.setApplyBackgroundColor(true);
            chart.setBackgroundColor(Color.WHITE);
            //绘制边框
            chart.showRoundBorder();

            //设置当前百分比
            chart.getPointer().setPercentage(mPercentage);

            //设置指针长度
            chart.getPointer().setLength(0.82f);

            //增加轴
            addAxis();
            /////////////////////////////////////////////////////////////
            addPointer();
            //设置附加信息
            addAttrInfo();
            /////////////////////////////////////////////////////////////

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void addAxis() {
        List<String> rlabels  = new ArrayList<String>();
        for(int i = 0; i <= 100; i++) {
            if(i == 0 || i%10 == 0) {
                rlabels.add(Integer.toString(i));
            }else{
                rlabels.add("");
            }
        }
        chart.addOuterTicksAxis(0.84f, rlabels);

        //环形颜色轴
        List<Float> ringPercentage = new ArrayList<Float>();
        List<Integer> rcolor  = new ArrayList<Integer>();
        float rper = MathHelper.getInstance().div(1, 100);  //将环形带一百等分
        for (int i = 0; i < 100; i++) {
            ringPercentage.add(rper);
            rcolor.add(Color.rgb(147 + i, 233 - i, 0));
        }


        /*List<Float> ringPercentage = new ArrayList<Float>();
        ringPercentage.add( 0.33f);
        ringPercentage.add( 0.33f);
        ringPercentage.add( 1 - 2 * 0.33f);*/


        /*rcolor.add(Color.rgb(133, 206, 130));
        rcolor.add(Color.rgb(252, 210, 9));
        rcolor.add(Color.rgb(229, 63, 56));*/

        chart.addStrokeRingAxis(0.84f,0.6f, ringPercentage, rcolor);

        /*List<String> rlabels2  = new ArrayList<String>();
        for(int i=0;i<=8;i++)
        {
            rlabels2.add(Integer.toString(i)+"MM");
        }
        chart.addInnerTicksAxis(0.6f, rlabels2);*/

        //chart.getPlotAxis().get(1).getFillAxisPaint().setColor(Color.rgb(28, 129, 243) );

        //chart.getPlotAxis().get(0).hideAxisLine();
        //chart.getPlotAxis().get(2).hideAxisLine();
        chart.getPlotAxis().get(0).getTickMarksPaint().setColor(Color.BLACK);
        //chart.getPlotAxis().get(2).getTickMarksPaint().setColor(Color.WHITE);
        //chart.getPlotAxis().get(2).getTickLabelPaint().setColor(Color.WHITE);


    }


    private void addAttrInfo()
    {

        PlotAttrInfo plotAttrInfo = chart.getPlotAttrInfo();
        //设置附加信息
        Paint paintTB = new Paint();
        paintTB.setColor(Color.GRAY);
        paintTB.setTextAlign(Paint.Align.CENTER);
        paintTB.setTextSize(40);
        //plotAttrInfo.addAttributeInfo(XEnum.Location.TOP, "TOP info", 0.5f, paintTB);
        plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM, "%RH", 0.8f, paintTB);

    }

    public void addPointer()
    {
    }

    public void setCurrentStatus(float percentage)
    {
        //清理
        chart.clearAll();

        mPercentage =  percentage;
        //设置当前百分比
        chart.getPointer().setPercentage(mPercentage);
        addAxis();
        addPointer();
        addAttrInfo();
    }

    @Override
    public void render(Canvas canvas) {
        // TODO Auto-generated method stub
        try {
            chart.render(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG, e.toString());
        }
    }
}

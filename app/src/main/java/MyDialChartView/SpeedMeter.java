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

public class SpeedMeter extends MyGraphicalView {

    //private String TAG = "DialChart01View";

    private DialChart chart = new MyChart();
    //private float mPercentage = 0.0f;

    public SpeedMeter(Context context) {
        super(context);
        chartRender();
    }

    public SpeedMeter(Context context, float totalAngle) {
        super(context);
        chart = new MyChart(totalAngle);
        chartRender();
    }

    public SpeedMeter(Context context, AttributeSet attrs){
        super(context, attrs);
        chartRender();
    }

    public SpeedMeter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        chartRender();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chart.setChartRange(w ,h );
    }

    public void chartRender()
    {
        try {

            //设置标题背景
            chart.setApplyBackgroundColor(true);
            chart.setBackgroundColor(Color.WHITE);
            //绘制边框
            chart.showRoundBorder();

            //设置当前百分比
            chart.getPointer().setPercentage(mPercentage);

            //设置指针长度
            chart.getPointer().setLength(0.8f);

            //增加轴
            addAxis();
            /////////////////////////////////////////////////////////////
            //设置附加信息
            addAttrInfo();

            //设置指针
            //addPointer();
            /////////////////////////////////////////////////////////////

            //chart.getPointer().getPointerPaint().setColor(Color.WHITE);
            chart.getPointer().setPointerStyle(XEnum.PointerStyle.TRIANGLE);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //Log.e(TAG, e.toString());
            e.printStackTrace();
        }

    }

    public void addAxis()
    {
        try{
            //开始设置轴
            //轴1 --最外面的弧线轴
            chart.addArcLineAxis(1);

            //轴2 --外围的标签轴
            List<String> tickLabels  = new ArrayList<String>();
            for(int i = 0; i <= 220; i++) {
                if(i == 0 || i%10 == 0) {
                    tickLabels.add(Integer.toString(i));
                }else if (i%5 == 0){
                    tickLabels.add("");
                }
            }
            chart.addOuterTicksAxis(0.75f, tickLabels);

            //轴3 --环形颜色轴
            List<Float> ringPercentage = new ArrayList<Float>();
            List<Integer> rcolor  = new ArrayList<Integer>();
            float rper = MathHelper.getInstance().div(1, 100);
            for (int i = 0; i < 100; i++) {
                ringPercentage.add(rper);
                rcolor.add(Color.rgb(147 + i, 233 - i, 0));
            }
            chart.addStrokeRingAxis(0.6f,0.4f, ringPercentage, rcolor);

            //轴4 -- 环下面的标签轴
            List<String> rlabels  = new ArrayList<String>();
            for(int i = 0; i <= 180; i++) {
                if(i == 0 || i%5 == 0) {
                    rlabels.add(".");
                }
            }
            chart.addOuterTicksAxis(0.4f, rlabels);

            //轴5 --  最里面的灰色底轴
            //chart.addFillAxis(0.5f,Color.rgb(225, 230, 246));
            List<Float> ringPercentages = new ArrayList<Float>();
            List<Integer> rcolors = new ArrayList<Integer>();
            ringPercentages.add(1.0f);
            rcolors.add(Color.rgb(225, 230, 246));
            chart.addStrokeRingAxis(0.4f,0.1f, ringPercentages, rcolors);

            //轴6  -- 最里面的红色百分比例的轴
            /*List<Float> innerPercentage = new ArrayList<Float>();
            innerPercentage.add(mPercentage);
            List<Integer> innerColor  = new ArrayList<Integer>();
            innerColor.add(Color.rgb(227, 64, 167));
            chart.addFillRingAxis(0.5f,innerPercentage, innerColor);*/

            /////////////////////////////////////////////////////////////
            //设置指定轴属性
            chart.getPlotAxis().get(0).getAxisPaint().setColor(Color.BLUE);
            /////////////////////////////////////////////////////////////

        }catch(Exception ex){
            //Log.e(TAG,ex.toString());
            ex.printStackTrace();
        }
    }


    private void addAttrInfo()
    {
        /////////////////////////////////////////////////////////////
        PlotAttrInfo plotAttrInfo = chart.getPlotAttrInfo();

        //设置附加信息
        Paint paintTB = new Paint();
        paintTB.setColor(Color.GRAY);
        paintTB.setTextAlign(Paint.Align.CENTER);
        paintTB.setTextSize(40);
        plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM, "km/h", 0.8f, paintTB);

    }

    public void addPointer()
    {
        /*chart.addPointer();
        chart.addPointer();

        List<Pointer> mp = chart.getPlotPointer();
        mp.get(0).setPercentage( mPercentage * 0.3f );
        mp.get(0).setLength(0.7f);
        mp.get(0).getPointerPaint().setColor(Color.BLUE);

        mp.get(1).setLength(0.5f);
        mp.get(1).setPointerStyle(XEnum.PointerStyle.TRIANGLE);
        mp.get(1).setPercentage( mPercentage * 0.7f );
        mp.get(1).getPointerPaint().setColor(Color.RED);*/
    }

    public void setCurrentStatus(float percentage)
    {
        mPercentage = percentage;
        //清理
        chart.clearAll();

        //设置当前百分比
        chart.getPointer().setPercentage(mPercentage);

        //轴
        addAxis();
        //附加信息
        addAttrInfo();
        //设置指针
        addPointer();
    }


    @Override
    public void render(Canvas canvas) {
        // TODO Auto-generated method stub
        try{
            chart.render(canvas);

        } catch (Exception e){
            //Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }


}
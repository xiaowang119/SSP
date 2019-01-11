package MyDialChartView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import org.xclcharts.chart.DialChart;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.plot.PlotAttrInfo;
import org.xclcharts.renderer.plot.Pointer;
import org.xclcharts.view.GraphicalView;

import java.util.ArrayList;
import java.util.List;

public class PressureMeter extends GraphicalView {

    private String TAG = "DialChart01View";

    private DialChart chart = new DialChart();
    private float mPercentage = 0.0f;

    public PressureMeter(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        chartRender();
    }

    public PressureMeter(Context context, AttributeSet attrs){
        super(context, attrs);
        chartRender();
    }

    public PressureMeter(Context context, AttributeSet attrs, int defStyle) {
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
            chart.getPointer().setLength(0.8f, 0.2f);

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
            chart.getPointer().getPointerPaint().setStrokeWidth(3);
            chart.getPointer().getPointerPaint().setStyle(Paint.Style.FILL);

            chart.getPointer().getPointerPaint().setColor(Color.rgb(242, 110, 131));
            chart.getPointer().getBaseCirclePaint().setColor(Color.rgb(238, 204, 71));
            chart.getPointer().setBaseRadius(10f);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
        }

    }

    public void addAxis()
    {
        try{
            //开始设置轴
            //轴1 --最外面的弧线轴
            chart.addArcLineAxis(0.99f);

            //轴2 --的环形颜色轴
            List<Float> ringPercentages = new ArrayList<Float>();
            List<Integer> rcolors = new ArrayList<Integer>();
            ringPercentages.add(1.0f);
            rcolors.add(Color.rgb(77, 77, 77));
            chart.addStrokeRingAxis(0.98f,0.8f, ringPercentages, rcolors);

            //轴3 --标签轴
            List<String> rlabels  = new ArrayList<String>();
            for(int i = 0; i <= 60; i++) {
                if(i == 0 || i%10 == 0) {
                    rlabels.add(Integer.toString(i));
                }else{
                    rlabels.add("");
                }
            }
            chart.addInnerTicksAxis(0.8f, rlabels);


            //轴3 --环形颜色轴
            /*List<Float> ringPercentage = new ArrayList<Float>();
            float rper = MathHelper.getInstance().div(1, 4); //相当于40%	//270, 4
            ringPercentage.add(rper);
            ringPercentage.add(rper);
            ringPercentage.add(rper);
            ringPercentage.add(rper);

            List<Integer> rcolor  = new ArrayList<Integer>();
            rcolor.add(Color.rgb(242, 110, 131));
            rcolor.add(Color.rgb(238, 204, 71));
            rcolor.add(Color.rgb(42, 231, 250));
            rcolor.add(Color.rgb(140, 196, 27));
            chart.addStrokeRingAxis(0.75f,0.6f, ringPercentage, rcolor);
            */
            /////////////////////////////////////////////////////////////
            //设置指定轴属性
            chart.getPlotAxis().get(0).getAxisPaint().setColor(Color.BLACK);
            /////////////////////////////////////////////////////////////


        }catch(Exception ex){
            Log.e(TAG,ex.toString());
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
        plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM, "KPa", 0.8f, paintTB);
    }

    public void addPointer()
    {
        chart.addPointer();
        chart.addPointer();

        List<Pointer> mp = chart.getPlotPointer();
        mp.get(0).setPercentage( mPercentage * 0.3f );
        mp.get(0).setLength(0.7f);
        mp.get(0).getPointerPaint().setColor(Color.BLUE);

        mp.get(1).setLength(0.5f);
        mp.get(1).setPointerStyle(XEnum.PointerStyle.TRIANGLE);
        mp.get(1).setPercentage( mPercentage * 0.7f );
        mp.get(1).getPointerPaint().setColor(Color.RED);
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
            Log.e(TAG, e.toString());
        }
    }


}
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

import java.util.ArrayList;
import java.util.List;

public class LiquidMeter extends MyGraphicalView {

    private String TAG = "DialChart01View";

    private DialChart chart = new MyChart();
    private float mPercentage = 0.0f;

    public LiquidMeter(Context context) {
        super(context);
        chartRender();
    }

    public LiquidMeter(Context context, float totalAngle) {
            super(context);
            chart = new MyChart(totalAngle);
            chartRender();
        }

    public LiquidMeter(Context context, AttributeSet attrs){
        super(context, attrs);
        chartRender();
    }

    public LiquidMeter(Context context, AttributeSet attrs, int defStyle) {
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
            chart.getPointer().setLength(0.75f);

            //增加轴
            addAxis();
            /////////////////////////////////////////////////////////////
            //设置附加信息
            addAttrInfo();

            //设置指针
            //addPointer();
            /////////////////////////////////////////////////////////////

            //chart.getPointer().getPointerPaint().setColor(Color.WHITE);
            //chart.getPointer().setPointerStyle(XEnum.PointerStyle.TRIANGLE);

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
            chart.addArcLineAxis(0.95f);

            //轴2 --外围的标签轴
            List<String> rlabel  = new ArrayList<String>();
            for(int i = 0; i < 9; i++) {
                rlabel.add("");
            }
            chart.addInnerTicksAxis(0.85f, rlabel);



            List<String> rlabels  = new ArrayList<String>();
            for(int i = 0; i <= 4; i++) {
                if (i == 0 || i == 4) {
                    rlabels.add(Integer.toString(i/4));
                } else {
                    rlabels.add(i + "/4");
                }
            }
            chart.addInnerTicksAxis(0.81f, rlabels);



            //轴3 --环形颜色轴
           /* List<Float> ringPercentage = new ArrayList<Float>();
            float rper = MathHelper.getInstance().div(1, 4); //相当于40%	//270, 4
            ringPercentage.add(rper);
            ringPercentage.add(rper);
            ringPercentage.add(rper);
            ringPercentage.add(rper);

            List<Integer> rcolor  = new ArrayList<Integer>();
            rcolor.add(Color.rgb(90, 175, 247));
            rcolor.add(Color.rgb(100, 222, 142));
            rcolor.add(Color.rgb(245, 185, 126));
            rcolor.add(Color.rgb(251, 91, 33));
            chart.addStrokeRingAxis(0.75f,0.65f, ringPercentage, rcolor);
*/

            /////////////////////////////////////////////////////////////
            //设置指定轴属性
            //chart.getPlotAxis().get(0).getAxisPaint().setColor(Color.BLUE);
            /////////////////////////////////////////////////////////////

            chart.getPlotAxis().get(2).hideAxisLine();
            /*chart.addLineAxis(XEnum.Location.TOP,1.6f);
            chart.addLineAxis(XEnum.Location.BOTTOM,1.6f);
            chart.addLineAxis(XEnum.Location.LEFT,1.6f);
            chart.addLineAxis(XEnum.Location.RIGHT,1.6f);
            if(chart.getPlotAxis().size() >= 6)chart.getPlotAxis().get(6).getAxisPaint().setColor(Color.BLUE);
            if(chart.getPlotAxis().size() >= 7)chart.getPlotAxis().get(7).getAxisPaint().setColor(Color.GREEN);
            if(chart.getPlotAxis().size() >= 8)chart.getPlotAxis().get(8).getAxisPaint().setColor(Color.YELLOW);
            if(chart.getPlotAxis().size() >= 9)chart.getPlotAxis().get(9).getAxisPaint().setColor(Color.RED);*/

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
        plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM, "water", 0.8f, paintTB);
    }

    public void addPointer()
    {
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

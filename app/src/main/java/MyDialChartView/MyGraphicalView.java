package MyDialChartView;

import android.content.Context;
import android.util.AttributeSet;
import org.xclcharts.view.GraphicalView;

public abstract class MyGraphicalView extends GraphicalView {
    /*private DialChart chart = new DialChart();*/
    float mPercentage = 0.0f;

    public MyGraphicalView(Context context) {
        super(context);
        this.initChartView();
    }

    public MyGraphicalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initChartView();
    }

    public MyGraphicalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initChartView();
    }

    public void setCurrentStatus(float percentage) {
        mPercentage = percentage;
        //清理
        /*chart.clearAll();

        //设置当前百分比
        chart.getPointer().setPercentage(mPercentage);

        //轴
        addAxis();
        //附加信息
        addAttrInfo();
        //设置指针
        addPointer();*/
    }

    private void addAxis() {}
    private void addAttrInfo() {}
    private void addPointer() {}

    public float getmPercentage() {
        return mPercentage;
    }
}

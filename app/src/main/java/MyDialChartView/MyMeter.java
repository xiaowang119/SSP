package MyDialChartView;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import myUtil.Constants;

/**
 * 作者：created by 11374 2019-04-26
 * 邮箱：xxxxx.qq.com
 */
public class MyMeter extends View {

    public boolean isAnimFinished = true;
    public int myWidth;

    public MyMeter(Context context) {
        this(context, null);
    }

    public MyMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public int getRealTimeValue() {return 0;}

    public void setRealTimeValue(int velocity) {}
}

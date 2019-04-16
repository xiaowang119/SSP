package MyDialChartView;

import org.xclcharts.chart.DialChart;

public class MyChart extends DialChart {
    public MyChart() {
        super();
    }

    public MyChart(float totalAngle) {
        super();
        this.setStartAngle(270f-totalAngle/2);
        this.setTotalAngle(totalAngle);
    }
}

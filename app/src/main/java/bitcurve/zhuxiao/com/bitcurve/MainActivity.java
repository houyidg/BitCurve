package bitcurve.zhuxiao.com.bitcurve;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LineGraphicView lineGraphicView;
    ArrayList<Double> yList;
    ArrayList<Integer> yRectangleList;
    private int count = 50;

    public void refresh(View v) {
        generateChart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineGraphicView = (LineGraphicView) findViewById(R.id.line_graphic);
        generateChart();
    }

    private String TAG = "TAG";

    private void generateChart() {
        yList = new ArrayList<>();
        //矩形
        yRectangleList = new ArrayList<>();
        ArrayList<String> xRawDatas = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double v = Math.random() * count;
            BigDecimal b1 = new BigDecimal(Double.toString(v));
            yList.add(b1.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            yRectangleList.add((int) (Math.random() * count));
            xRawDatas.add(i + "");
        }
        /**
         *  * @param yRawData
         * @param yRectangleList
         * @param xRawData
         * @param maxYValue
         * @param maxYListValue
         * @param xDistanceValue
         * @param averageYValue
         * @param maxYRectangleValue
         * @param maxYRectangleListValue
         */
        int maxYValue = 150;//px
        int maxYRectangleValue = 100;
        int maxYListValue = count;
        int maxYRectangleListValue = count;
        lineGraphicView.setMarginb(50);
        lineGraphicView.setData(yList, yRectangleList, xRawDatas, maxYValue, maxYListValue, 2, 2, maxYRectangleValue, maxYRectangleListValue);
    }

    boolean isBlackTheme=false;
    public void refreshColor(View v) {
        lineGraphicView.setCurveGradientScaleX(0.2f);
        if(!isBlackTheme){
            lineGraphicView.updateSkin(R.color.black_text,R.color.black_bg,R.color.black_curve_start,R.color.black_curve_end,R.color.black_rect_start,R.color.black_rect_end);
        }else{
            lineGraphicView.updateSkin(R.color.white_text,R.color.white_bg,R.color.white_curve_start,R.color.white_curve_end,R.color.white_rect_start,R.color.white_rect_end);
        }
        isBlackTheme = !isBlackTheme;
    }
}

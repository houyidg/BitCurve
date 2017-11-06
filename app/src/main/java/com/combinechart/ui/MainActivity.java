package com.combinechart.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.combinechart.R;
import com.combinechart.widget.CombineChartView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    CombineChartView lineGraphicView;
    ArrayList<Float> yList;
    ArrayList<Float> yRectangleList;
    private int count = 3;

    public void refresh(View v) {
        generateChart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineGraphicView = (CombineChartView) findViewById(R.id.line_graphic);
        generateChart();
    }

    private String TAG = "TAG";

    private void generateChart() {
        yList = new ArrayList<>();
        //矩形
        yRectangleList = new ArrayList<>();
        ArrayList<String> xRawDatas = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            yList.add((float) (Math.random() * count));

            yRectangleList.add((float) (Math.random() * count));
            xRawDatas.add(i + "");
        }
        /**
         *  * @param yRawData
         * @param yRectangleList
         * @param xRawData
         * @param curveScale
         * @param maxYListValue
         * @param xDistanceValue
         * @param averageYValue
         * @param rectScale
         * @param maxYRectangleListValue
         */
        int xDistance = 2;
        lineGraphicView.setCurveData(xRawDatas, xDistance ,yList);
    }

    boolean isBlackTheme=false;
    public void refreshColor(View v) {
        lineGraphicView.setCurveGradientScaleX(0.2f);
        if(!isBlackTheme){
            lineGraphicView.setChartSkin(getResources().getColor(R.color.black_bg),
                    getResources().getColor(R.color.black_curve_start),
                    getResources().getColor(R.color.black_curve_end),
                    getResources().getColor(R.color.black_rect_start),
                    getResources().getColor(R.color.black_rect_end));
        }else{
            lineGraphicView.setChartSkin(getResources().getColor(R.color.white_bg),
                    getResources().getColor(R.color.white_curve_start),
                    getResources().getColor(R.color.white_curve_end),
                    getResources().getColor(R.color.white_column_start),
                    getResources().getColor(R.color.white_column_end));
        }
        isBlackTheme = !isBlackTheme;
    }
}

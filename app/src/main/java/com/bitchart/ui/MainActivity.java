package com.bitchart.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bitchart.R;
import com.bitchart.widget.CombineChartView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    CombineChartView lineGraphicView;
    ArrayList<Integer> yList;
    ArrayList<Integer> yRectangleList;
    private int count = 50;

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
            yList.add((int) (Math.random() * count));
            yRectangleList.add((int) (Math.random() * count));
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
        int curveScale = 1;//px
        int rectScale = 2;
        int xDistance = 2;
        lineGraphicView.setData(xRawDatas, xDistance ,yList , curveScale);
    }

    boolean isBlackTheme=false;
    public void refreshColor(View v) {
        lineGraphicView.setCurveGradientScaleX(0.2f);
        if(!isBlackTheme){
            lineGraphicView.updateSkin(R.color.black_bg,R.color.black_curve_start,R.color.black_curve_end,R.color.black_rect_start,R.color.black_rect_end);
        }else{
            lineGraphicView.updateSkin(R.color.white_bg,R.color.white_curve_start,R.color.white_curve_end,R.color.white_rect_start,R.color.white_rect_end);
        }
        isBlackTheme = !isBlackTheme;
    }
}

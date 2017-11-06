package com.combinechart.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.combinechart.R;
import com.combinechart.listener.TouchActionListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CombineChartView extends View implements TouchActionListener {
    /**
     * 曲线上总点数
     */
    private Point[] mPoints;
    private Rect[] mRects;

    /**
     * 更新皮肤
     */
    public void setChartSkin(int bgColor, int startCurveColor, int endCurveColor, int startColumnColor, int endColumnColor) {
        this.bgColor = bgColor;

        this.startCurveColor = startCurveColor;
        this.endCurveColor = endCurveColor;

        this.startColumnColor = startColumnColor;
        this.endColumnColor = endColumnColor;
        invalidate();
    }

    /**
     * 设置 渐变效果前者颜色所占比例
     *
     * @param scaleX
     */
    public void setCurveGradientScaleX(float scaleX) {
        curveGradientScaleX = scaleX;
    }

    private static enum Linestyle {
        Line, Curve
    }

    /**
     * 图标类型
     */
    private static enum ChartStyle {
        Curve, Column, CombineDatagram
    }

    private int bgColor = Color.WHITE;
    private int startCurveColor;
    private int startColumnColor;
    private int endCurveColor;
    private int endColumnColor;
    private float curveGradientScaleX = 1f;
    private float columnGradientScaleX = 1f;

    public void setDyCharts(int dyCharts) {
        this.dyCharts = dyCharts;
    }

    /**
     * 两图上下间距
     */
    private int dyCharts = 0;

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    private int marginTop = 0;

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    private int marginBottom = 0;
    private static final int CIRCLE_SIZE = 10;
    private float mPointRadius = CIRCLE_SIZE / 2;

    private Context mContext;
    private Paint mPaint;
    private Resources res;
    private DisplayMetrics dm;
    private Linestyle mLineStyle = Linestyle.Curve;
    private ChartStyle mChartStyle;
    private int canvasHeight;
    private int canvasWidth;

    /**
     * 矩形图的最大y
     */
    private int columnMaxY = 0;
    /**
     * 曲线图的 基准高度
     */
    private int curveMaxY = 0;
    private int columnScaleFactor = 1;
    private int curveScaleFactor = 1;

    private boolean isMeasure = true;
    private float maxYValue;
    /**
     * 矩形所在区域最大值
     */
    private float maxYColumnValue;

    /**
     * x轴的间距
     */
    private int xDistanceValue;


    /**
     * 纵坐标值
     */
    private List<Float> yCurveRawData;
    private List<Float> yColumnRawData;
    /**
     * 横坐标值
     */
    private List<String> xRawDatas;
    private List<Float> xList = new ArrayList<Float>();// 记录每个x的值

    public CombineChartView(Context context) {
        this(context, null);
    }

    public CombineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    @Override
    public int isHit(MotionEvent event) {
        int pointSize = 0;
        int curX = (int) event.getX();
        int curY = (int) event.getY();
        if (mPoints != null) {
            pointSize = mPoints.length;
            if (curveLineRect.contains(curX, curY)) {
                for (int i = 0; i < mPoints.length; i++) {
                    Point point = mPoints[i];
                    float x0 = point.x - (mPointRadius + 10);
                    float x2 = point.x + (mPointRadius + 10);
                    if (curX >= x0 && curX <= x2) {//为了方便点击，把坐标点范围增大了10像素
                        return i;
                    }
                }
            }
        }
        if (mRects != null) {
            Log.e(TAG, "isHit: columnLineRect.contains(curX,curY)" + columnLineRect.contains(curX, curY) + "," + columnLineRect.flattenToString());
            if (columnLineRect.contains(curX, curY)) {
                for (int i = 0; i < mRects.length; i++) {
                    Rect rect = mRects[i];
                    int left = rect.left;
                    int right = rect.right;
                    if (curX >= left && curX <= right) {//为了方便点击，把坐标点范围增大了10像素
                        return i + pointSize;
                    }
                }
            }
        }
        return -1;
    }

    private void initView() {
        this.res = mContext.getResources();
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startCurveColor = res.getColor(R.color.white_curve_start);
        startColumnColor = res.getColor(R.color.white_column_start);
        endCurveColor = res.getColor(R.color.white_curve_end);
        endColumnColor = res.getColor(R.color.white_column_end);
        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isMeasure && mChartStyle != null) {
            this.canvasHeight = getHeight();
            this.canvasWidth = getWidth();

            switch (mChartStyle) {
                case Column: {
                    if (columnMaxY == 0) {
                        int columnMinY = marginTop;
                        columnMaxY = (canvasHeight - marginBottom);
                        maxYColumnValue = (canvasHeight - marginBottom - marginTop);
                        columnLineRect = new Rect(0, columnMinY, canvasWidth, columnMaxY);
                    }
                    break;
                }

                case Curve: {
                    if (curveMaxY == 0) {
                        int curveMinY = marginTop;
                        curveMaxY = (canvasHeight - marginBottom);
                        maxYValue = (canvasHeight - marginBottom - marginTop);
                        curveLineRect = new Rect(0, curveMinY, canvasWidth, curveMaxY);
                    }
                    break;
                }

                case CombineDatagram: {
                    int enableValue = canvasHeight - marginBottom - marginTop - dyCharts;
                    float rectScale = columnScaleFactor * 1.0f / (columnScaleFactor + curveScaleFactor);
                    float curveScale = curveScaleFactor * 1.0f / (columnScaleFactor + curveScaleFactor);
                    maxYColumnValue = rectScale * enableValue;
                    maxYValue = curveScale * enableValue;
                    if (curveMaxY == 0) {
                        int  curveMinY = marginTop;
                        curveMaxY = (int) (curveMinY + maxYValue);
                        curveLineRect = new Rect(0, curveMinY, canvasWidth, curveMaxY);
                    }

                    if (columnMaxY == 0) {
                        int columnMinY = curveMaxY + dyCharts;
                        columnMaxY = (int) (columnMinY + maxYColumnValue);
                        columnLineRect = new Rect(0, columnMinY, canvasWidth, columnMaxY);
                    }
                    break;
                }
            }
            Log.e(TAG, "onSizeChanged: maxYValue" + maxYValue + ",maxYColumnValue:" + maxYColumnValue + ",columnMaxY:" + columnMaxY + ",curveMaxY:" + curveMaxY+",canvasHeight:"+canvasHeight);

            isMeasure = false;
        }
    }

    private Rect curveLineRect;
    private Rect columnLineRect;

    @Override
    protected void onDraw(Canvas canvas) {
        if (mChartStyle == null) {
            return;
        }
        prepareXList();
        if (mChartStyle == ChartStyle.Curve || mChartStyle == ChartStyle.CombineDatagram) {
            mPoints = getPoints();
            mPaint.setStrokeWidth(dip2px(2.5f));
            mPaint.setStyle(Style.STROKE);
            mPaint.setShader(new LinearGradient(0, 0, curveGradientScaleX * canvasWidth, 0, startCurveColor, endCurveColor, Shader.TileMode.MIRROR));
            if (mLineStyle == Linestyle.Curve) {
                drawScrollLine(canvas);
            } else {
                drawLine(canvas);
            }

            mPaint.setStyle(Style.FILL);
            drawCircles(canvas);
        }

        if (mChartStyle == ChartStyle.Column || mChartStyle == ChartStyle.CombineDatagram) {
            mRects = getmColumns();
            mPaint.setShader(new LinearGradient(0, 0, columnGradientScaleX * canvasWidth, 0, startColumnColor, endColumnColor, Shader.TileMode.MIRROR));
            drawColumn(canvas, mRects);
        }

        drawBg(canvas);
    }

    private void drawBg(Canvas canvas) {
        setBackgroundColor(bgColor);
    }

    private void prepareXList() {
        xList.clear();
        float perSize = canvasWidth * 1.0f / (xRawDatas.size() + 1);
        for (int i = 0; i < xRawDatas.size(); i++) {
            xList.add(perSize * (i + 1));
        }
    }

    private Point[] getPoints() {
        int size = yCurveRawData.size();
        Point[] points = new Point[size];
        float maxRawValue = yCurveRawData.get(0);
        for (int i = 1; i < size; i++) {
            float value = yCurveRawData.get(i);
            if (maxRawValue < value) {
                maxRawValue = value;
            }
        }

        for (int i = 0; i < size; i++) {
            float oldValue = yCurveRawData.get(i) * 1.0f;
            int newValue = (int) (maxYValue * (1 - oldValue / maxRawValue) + marginTop );
            Log.e(TAG, "getPoints: newValue"+newValue+",oldValue:"+oldValue+",maxRawValue:"+maxRawValue+",marginTop:"+marginTop+",maxYValue:"+maxYValue);
            points[i] = new Point(xList.get(i).intValue(), newValue);
        }
        return points;
    }

    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            canvas.drawCircle(mPoints[i].x, mPoints[i].y, CIRCLE_SIZE / 2, mPaint);
        }
    }

    private void drawScrollLine(Canvas canvas) {
        Point startp = new Point();
        Point endp = new Point();
        for (int i = 0; i < mPoints.length - 1; i++) {
            startp = mPoints[i];
            endp = mPoints[i + 1];
            int wt = (startp.x + endp.x) / 2;
            Point p3 = new Point();
            Point p4 = new Point();
            p3.y = startp.y;
            p3.x = wt;
            p4.y = endp.y;
            p4.x = wt;

            Path path = new Path();
            path.moveTo(startp.x, startp.y);
            path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
            canvas.drawPath(path, mPaint);
        }
    }

    private void drawLine(Canvas canvas) {
        Point startp = new Point();
        Point endp = new Point();
        for (int i = 0; i < mPoints.length - 1; i++) {
            startp = mPoints[i];
            endp = mPoints[i + 1];
            canvas.drawLine(startp.x, startp.y, endp.x, endp.y, mPaint);
        }
    }

    private void drawColumn(Canvas canvas, Rect[] rects) {
        for (int i = 0; i < rects.length; i++) {
            canvas.drawRect(rects[i], mPaint);
        }
    }

    private Rect[] getmColumns() {
        Rect[] rects = new Rect[yColumnRawData.size()];

        float maxRawValue = yColumnRawData.get(0);
        for (int i = 0; i < yColumnRawData.size(); i++) {
            float value = yColumnRawData.get(i);
            if (maxRawValue < value) {
                maxRawValue = value;
            }
        }
        int dx = (int) (xList.get(1) - xList.get(0) - (xDistanceValue <= 2 ? 2 : xDistanceValue));
        for (int i = 0; i < yColumnRawData.size(); i++) {
            float oldValue = yColumnRawData.get(i) * 1.0f;
            int newValue = (int) (maxYColumnValue * (1 - oldValue / maxRawValue) + marginTop + dyCharts + maxYValue);
            int top = newValue;
            int bottom = columnMaxY;
            float x = xList.get(i);
            int left = (int) (x - dx / 2);
            int right = (int) (x + dx / 2);
            //int left, int top, int right, int bottom
            Log.e(TAG, "getmColumns: left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
            rects[i] = new Rect(left, top, right, bottom);
        }
        return rects;
    }

    public void setData(List<String> xRawData, List<Float> yCurveData, int curveScale, List<Float> yColumnData,
                        int columnScale) {
        setData(xRawData, 2, yCurveData, curveScale, yColumnData, columnScale);
    }

    public void setData(List<String> xRawData, int xDistanceValue, List<Float> yCurveData, int curveScale, List<Float> yColumnData,
                        int columnScale) {
        if (yCurveData == null || yColumnData == null || yCurveData.size() != yColumnData.size()) {
            return;
        }
        this.curveScaleFactor = curveScale;

        //曲线坐标
        setCurveParams(yCurveData);

        this.columnScaleFactor = columnScale;

        //柱形图坐标
        setRectangleParams(yColumnData);

        //公共坐标参数
        setXParams(xRawData, xDistanceValue);

        mChartStyle = ChartStyle.CombineDatagram;

        invalidate();
    }

    public void setColumnData(List<String> xRawData, List<Float> yColumnList) {
        setColumnData(xRawData, 2, yColumnList);
    }

    public void setColumnData(List<String> xRawData, int xDistanceValue, List<Float> yColumnList) {
        //柱形图坐标
        setRectangleParams(yColumnList);

        //公共坐标参数
        setXParams(xRawData, xDistanceValue);

        mChartStyle = ChartStyle.Column;

        invalidate();
    }

    public void setCurveData(List<String> xRawData, List<Float> yCurveList) {
        setCurveData(xRawData, 2, yCurveList);
    }

    public void setCurveData(List<String> xRawData, int xDistanceValue, List<Float> yRawData) {

        //曲线坐标
        setCurveParams(yRawData);

        //公共坐标参数
        setXParams(xRawData, xDistanceValue);

        mChartStyle = ChartStyle.Curve;

        invalidate();
    }

    private void setXParams(List<String> xRawData, int xDistanceValue) {
        this.xDistanceValue = xDistanceValue;
        this.xRawDatas = xRawData;

        Log.e(TAG, "setXParams:xRawData: " + xRawData.size());
    }

    private void setRectangleParams(List<Float> yColumnList) {
        this.yColumnRawData = yColumnList;
    }

    private void setCurveParams(List<Float> yRawData) {
        this.yCurveRawData = yRawData;
    }

    String selectValue = "";

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int hit = isHit(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            Log.e(TAG, "onTouchEvent: hit" + hit);
            if (hit != -1) {
                int pointSize = 0;
                if (mPoints != null) {
                    pointSize = mPoints.length;
                }
                if (hit <= pointSize) {
                    selectValue = " line:(" + xRawDatas.get(hit) + "," + yCurveRawData.get(hit) + ")";
                } else {
                    hit -= pointSize;
                    selectValue = " rectangle:(" + xRawDatas.get(hit) + "," + yColumnRawData.get(hit) + ")";
                }
            }
//            else {
//                selectValue = "";
//            }
            invalidate();
        }
        return true;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        return (int) (dpValue * dm.density + 0.5f);
    }

}
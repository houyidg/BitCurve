package com.bitchart.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
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

import com.bitchart.R;
import com.bitchart.listener.TouchActionListener;

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
    public void updateSkin(int bgColor, int startCurveColor, int endCurveColor, int startRectangleColor, int endRectangleColor) {
        this.bgColor = bgColor;

        this.startCurveColor = startCurveColor;
        this.endCurveColor = endCurveColor;

        this.startRectangleColor = startRectangleColor;
        this.endRectangleColor = endRectangleColor;
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

    private int bgColor = R.color.white_bg;
    private int startCurveColor = R.color.white_curve_start;
    private int startRectangleColor = R.color.white_rect_start;
    private int endCurveColor = R.color.white_curve_end;
    private int endRectangleColor = R.color.white_rect_end;
    private float curveGradientScaleX = 1f;
    private float rectGradientScaleX = 1f;
    /**
     * 两图上下间距
     */
    private int dyEachOther = 50;
    private int marginTop = 10;
    private int marginBottom = 10;
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
    private int rectMaxY = 0;
    private int rectMinY = 0;
    /**
     * 曲线图的 基准高度
     */
    private int curveMaxY = 0;
    private int curveMinY = 0;
    private int rectangleScaleFactor = 1;
    private int curveScaleFactor = 1;

    private boolean isMeasure = true;
    private float maxYValue;
    /**
     * 矩形所在区域最大值
     */
    private float maxYRectangleValue;

    /**
     * x轴的间距
     */
    private int xDistanceValue;


    /**
     * 纵坐标值
     */
    private ArrayList<Integer> yRawData;
    private List<Integer> yRectabgleRawData;
    /**
     * 横坐标值
     */
    private ArrayList<String> xRawDatas;
    private ArrayList<Float> xList = new ArrayList<Float>();// 记录每个x的值

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
            Log.e(TAG, "isHit: rectangleLineRect.contains(curX,curY)" + rectangleLineRect.contains(curX, curY) + "," + rectangleLineRect.flattenToString());
            if (rectangleLineRect.contains(curX, curY)) {
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

        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isMeasure) {
            this.canvasHeight = getHeight();
            this.canvasWidth = getWidth();

            switch (mChartStyle) {
                case Column: {
                    if (rectMaxY == 0) {
                        rectMinY = marginTop;
                        rectMaxY = (canvasHeight - marginBottom);
                        rectangleLineRect = new Rect(0, rectMinY, canvasWidth, rectMaxY);
                    }
                    break;
                }

                case Curve: {
                    if (curveMaxY == 0) {
                        curveMinY = marginTop;
                        curveMaxY = (canvasHeight - marginBottom);
                        curveLineRect = new Rect(0, curveMinY, canvasWidth, curveMaxY);
                    }
                    break;
                }

                case CombineDatagram: {
                    int enableValue = canvasHeight - marginBottom - marginTop - dyEachOther;
                    float rectScale = rectangleScaleFactor * 1.0f / (rectangleScaleFactor + curveScaleFactor);
                    float curveScale = curveScaleFactor * 1.0f / (rectangleScaleFactor + curveScaleFactor);
                    maxYRectangleValue = rectScale * enableValue;
                    maxYValue = curveScale * enableValue;
                    if (curveMaxY == 0) {
                        curveMinY = marginTop;
                        curveMaxY = (int) (curveMinY + maxYValue);
                        curveLineRect = new Rect(0, curveMinY, canvasWidth, curveMaxY);
                    }

                    if (rectMaxY == 0) {
                        rectMinY = curveMaxY + dyEachOther;
                        rectMaxY = (int) (rectMinY + maxYRectangleValue);
                        rectangleLineRect = new Rect(0, rectMinY, canvasWidth, rectMaxY);
                    }

                    Log.e(TAG, "onSizeChanged: maxYValue" + maxYValue + ",maxYRectangleValue:" + maxYRectangleValue + ",rectMaxY:" + rectMaxY + ",rectMinY:" + rectMinY + ",curveMaxY:" + curveMaxY + ",curveMinY:" + curveMinY);

                    break;
                }
            }

            isMeasure = false;
        }
    }

    private Rect curveLineRect;
    private Rect rectangleLineRect;

    @Override
    protected void onDraw(Canvas canvas) {
        prepareXList();

        if (mChartStyle == ChartStyle.Curve || mChartStyle == ChartStyle.CombineDatagram) {
            mPoints = getPoints();
            mPaint.setStrokeWidth(dip2px(2.5f));
            mPaint.setStyle(Style.STROKE);
            mPaint.setShader(new LinearGradient(0, 0, curveGradientScaleX * canvasWidth, 0, res.getColor(startCurveColor), res.getColor(endCurveColor), Shader.TileMode.MIRROR));
            if (mLineStyle == Linestyle.Curve) {
                drawScrollLine(canvas);
            } else {
                drawLine(canvas);
            }

            mPaint.setStyle(Style.FILL);
            drawCircles(canvas);
        }


        if (mChartStyle == ChartStyle.Column || mChartStyle == ChartStyle.CombineDatagram) {
            mRects = getmRects();
            mPaint.setShader(new LinearGradient(0, 0, rectGradientScaleX * canvasWidth, 0, res.getColor(startRectangleColor), res.getColor(endRectangleColor), Shader.TileMode.MIRROR));
            drawRectangle(canvas, mRects);
        }

        drawBg(canvas);
    }

    private void drawBg(Canvas canvas) {
        setBackgroundColor(res.getColor(bgColor));
    }

    private void prepareXList() {
        xList.clear();
        float perSize = canvasWidth * 1.0f / (xRawDatas.size() + 1);
        for (int i = 0; i < xRawDatas.size(); i++) {
            xList.add(perSize * (i + 1));
        }
    }

    private Point[] getPoints() {
        Point[] points = new Point[yRawData.size()];
        int minRawValue = yRawData.get(0), maxRawValue = yRawData.get(0);
        for (int i = 1; i < yRawData.size(); i++) {
            Integer value = yRawData.get(i);
            if (maxRawValue < value) {
                maxRawValue = value;
            }
            if (minRawValue > value) {
                minRawValue = value;
            }
        }

        int newMaxValue = Integer.MIN_VALUE;
        int newMinValue = Integer.MAX_VALUE;
        //转换坐标系后的数组
        Integer[] newData = new Integer[yRawData.size()];
        for (int i = 0; i < yRawData.size(); i++) {
            float oldValue = yRawData.get(i) * 1.0f;
            int newValue = (int) (curveMaxY * (1 - (oldValue / maxRawValue)));
            if (newMaxValue < newValue) {
                newMaxValue = newValue;
            }
            if (newMinValue > newValue) {
                newMinValue = newValue;
            }
            newData[i] = newValue;
        }

      getNewMaxValue(newMaxValue, newMinValue,curveMinY,curveMaxY, newData);


        for (int i = 0; i < newData.length; i++) {
            points[i] = new Point(xList.get(i).intValue(), newData[i]);
        }
        return points;
    }

    private int getNewMaxValue(int newMaxValue, int newMinValue,int standardMinY, int standardMaxY, Integer[] newData) {
        //最低点向下平移
        if (newMinValue < standardMinY) {
            int dy = standardMinY - newMinValue;
            newMaxValue += dy;
            newMinValue += dy;
            for (int i = 0; i < newData.length; i++) {
                newData[i] += dy;
            }

            //再缩放
            for (int i = 0; i < newData.length; i++) {
                float oldValue = newData[i] * 1.0f;
                int newValue = (int) (oldValue * standardMaxY / newMaxValue);
                newData[i] = newValue;
            }


            int newMaxValue2 = (int) (newMaxValue * standardMaxY / newMaxValue*1.0f);
            int newMinValue2 = (int) (newMinValue * standardMaxY / newMaxValue*1.0f);

            if(newMinValue2+2 >= standardMinY && newMaxValue2-2 <= standardMaxY){
                return newMaxValue2;
            }
            //缩放之后再次获取最小值
            Log.e(TAG, "getNewMaxValue: newMinValue:"+newMinValue+",standardMinY:"+standardMinY+",newMaxValue:"+newMaxValue+",standardMaxY:"+standardMaxY+",newMaxValue2:"+newMaxValue2+",newMinValue2:"+newMinValue2);
            return  getNewMaxValue( newMaxValue2,  newMinValue2, standardMinY,  standardMaxY,newData);
        }else{
            return newMaxValue;
        }
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

    private void drawRectangle(Canvas canvas, Rect[] rects) {
        for (int i = 0; i < rects.length; i++) {
            canvas.drawRect(rects[i], mPaint);
        }
    }

    private Rect[] getmRects() {
        Rect[] rects = new Rect[yRectabgleRawData.size()];

        int minRawValue = yRectabgleRawData.get(0), maxRawValue = yRectabgleRawData.get(0);
        for (int i = 0; i < yRectabgleRawData.size(); i++) {
            Integer value = yRectabgleRawData.get(i);
            if (maxRawValue < value) {
                maxRawValue = value;
            }
            if (minRawValue > value) {
                minRawValue = value;
            }
        }

        int newMaxValue = Integer.MIN_VALUE;
        int newMinValue = Integer.MAX_VALUE;
        //转换坐标系后的数组
        Integer[] newData = new Integer[yRectabgleRawData.size()];
        for (int i = 0; i < yRectabgleRawData.size(); i++) {
            float oldValue = yRectabgleRawData.get(i) * 1.0f;
            int newValue = (int) (rectMaxY * (1 - (oldValue / maxRawValue)));
            if (newMaxValue < newValue) {
                newMaxValue = newValue;
            }
            if (newMinValue > newValue) {
                newMinValue = newValue;
            }
            newData[i] = newValue;
        }

        getNewMaxValue(newMaxValue, newMinValue,rectMinY,rectMaxY, newData);
        //再缩放
        int dx = (int) (xList.get(1) - xList.get(0) - (xDistanceValue <= 2 ? 2 : xDistanceValue));
        for (int i = 0; i < newData.length; i++) {
            int top = newData[i];
            int bottom = rectMaxY;
            float x = xList.get(i);
            int left = (int) (x - dx / 2);
            int right = (int) (x + dx / 2);
            //int left, int top, int right, int bottom
            Log.e(TAG, "getmRects: left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
            rects[i] = new Rect(left, top, right, bottom);
        }
        return rects;
    }


    public void setData(ArrayList<String> xRawData, int xDistanceValue, ArrayList<Integer> yRawData, int curveScale, ArrayList<Integer> yRectangleList,
                        int rectangleScale) {
        //曲线坐标
        setCurveParams(yRawData, curveScale);

        //柱形图坐标
        setRectangleParams(yRectangleList, rectangleScale);

        //公共坐标参数
        setXParams(xRawData, xDistanceValue);

        mChartStyle = ChartStyle.CombineDatagram;

        invalidate();
    }

    public void setData(ArrayList<String> xRawData, int xDistanceValue, ArrayList<Integer> yRectangleList,
                        int rectangleScale) {
        //柱形图坐标
        setRectangleParams(yRectangleList, rectangleScale);

        //公共坐标参数
        setXParams(xRawData, xDistanceValue);

        mChartStyle = ChartStyle.Column;

        invalidate();
    }

    public void setCurveData(ArrayList<String> xRawData, int xDistanceValue, ArrayList<Integer> yRawData, int curveScale) {
        //曲线坐标
        setCurveParams(yRawData, curveScale);

        //公共坐标参数
        setXParams(xRawData, xDistanceValue);

        mChartStyle = ChartStyle.Curve;

        invalidate();
    }

    private void setXParams(ArrayList<String> xRawData, int xDistanceValue) {
        this.xDistanceValue = xDistanceValue;
        this.xRawDatas = xRawData;
    }

    private void setRectangleParams(List<Integer> yRectangleList, int rectangleScale) {
        this.yRectabgleRawData = yRectangleList;
        this.rectangleScaleFactor = rectangleScale;
    }

    private void setCurveParams(ArrayList<Integer> yRawData, int curveScale) {
        this.yRawData = yRawData;
        this.curveScaleFactor = curveScale;
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
                    selectValue = " line:(" + xRawDatas.get(hit) + "," + yRawData.get(hit) + ")";
                } else {
                    hit -= pointSize;
                    selectValue = " rectangle:(" + xRawDatas.get(hit) + "," + yRectabgleRawData.get(hit) + ")";
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
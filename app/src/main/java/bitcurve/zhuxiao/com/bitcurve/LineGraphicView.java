package bitcurve.zhuxiao.com.bitcurve;

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

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

class LineGraphicView extends View implements TouchActionListener {
    /**
     * 曲线上总点数
     */
    private Point[] mPoints;
    private Rect[] mRects;

    /**
     * 更新皮肤
     */
    public void updateSkin(int textColor, int bgColor, int startCurveColor, int endCurveColor, int startRectangleColor, int endRectangleColor) {
        this.textColor = textColor;
        this.bgColor = bgColor;

        this.startCurveColor = startCurveColor;
        this.endCurveColor = endCurveColor;

        this.startRectangleColor = startRectangleColor;
        this.endRectangleColor = endRectangleColor;
        invalidate();
    }

    /**
     * 设置 渐变效果前者颜色所占比例
     * @param scaleX
     */
    public void setCurveGradientScaleX(float scaleX) {
        curveGradientScaleX = scaleX;
    }

    private static enum Linestyle {
        Line, Curve
    }

    private int textColor = R.color.white_text;
    private int bgColor = R.color.white_bg;
    private int startCurveColor = R.color.white_curve_start;
    private int startRectangleColor = R.color.white_rect_start;
    private int endCurveColor = R.color.white_curve_end;
    private int endRectangleColor = R.color.white_rect_end;
    private float curveGradientScaleX = 1f;
    private float rectGradientScaleX = 1f;

    private static final int CIRCLE_SIZE = 10;
    private float mPointRadius = CIRCLE_SIZE / 2;
    private Context mContext;
    private Paint mPaint, mTextPaint;
    private Resources res;
    private DisplayMetrics dm;
    private Linestyle mStyle = Linestyle.Curve;
    private int canvasHeight;
    private int canvasWidth;
    /**
     * 曲线图的 基准高度
     */
    private int bheight = 0;
    /**
     * y轴的基准高度
     */
    private int height = 0;
    /**
     * 矩形图的 基准高度
     */
    private int bRectangleHeight = 0;

    /**
     * 两图上下间距
     */
    private int dyEachOther = 50;

    /**
     * y轴宽度所需的空间
     */
    private int blwidh;
    private boolean isMeasure = true;
    /**
     * 曲线Y轴最大值
     */
    private int maxYListValue;

    private int maxYValue;
    /**
     * 矩形所在区域最大值
     */
    private float maxYRectangleValue;
    /**
     * 矩形数据最大值
     */
    private float maxYRectangleListValue;
    /**
     * Y轴间距值
     */
    private int averageYValue;
    /**
     * x轴的间距
     */
    private int xDistanceValue;
    private int marginTop = 20;
    private int marginBottom = 40;
    private String selectValue = "";

    /**
     * 纵坐标值
     */
    private ArrayList<Double> yRawData;
    private ArrayList<Integer> yRectabgleRawData;
    /**
     * 横坐标值
     */
    private ArrayList<String> xRawDatas;
    private ArrayList<Float> xList = new ArrayList<Float>();// 记录每个x的值
    private int spacingHeight;

    public LineGraphicView(Context context) {
        this(context, null);
    }

    public LineGraphicView(Context context, AttributeSet attrs) {
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

        this.mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
    }

    int left;
    int top;
    int right;
    int bottom;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isMeasure) {
            this.canvasHeight = getHeight();
            this.canvasWidth = getWidth();
            left = getLeft();
            top = getTop();
            right = getRight();
            bottom = getBottom();
            Log.e(TAG, "onSizeChanged: " + left + "," + top + "," + right + "," + bottom);
            if (height == 0) {
                height = (canvasHeight - marginBottom);
            }
            if (bheight == 0) {
                bheight = (int) (canvasHeight - marginBottom - maxYRectangleValue - dyEachOther);
                curveLineRect = new Rect(0, (int) (canvasHeight - marginBottom - maxYRectangleValue - dyEachOther - maxYValue), canvasWidth, bheight);
            }

            if (bRectangleHeight == 0) {
                bRectangleHeight = (canvasHeight - marginBottom);
                rectangleLineRect = new Rect(0, (int) (canvasHeight - marginBottom - maxYRectangleValue), canvasWidth, bRectangleHeight);
            }

//            blwidh = dip2px(30);
            isMeasure = false;
        }
    }

    private Rect curveLineRect;
    private Rect rectangleLineRect;

    @Override
    protected void onDraw(Canvas canvas) {
        prepareXList();
//        drawAllXLine(canvas);
//        // 画直线（纵向）
//        drawAllYLine(canvas);
        // 点的操作设置
        mPoints = getPoints();
        mPaint.setStrokeWidth(dip2px(2.5f));
        mPaint.setStyle(Style.STROKE);
        mPaint.setShader(new LinearGradient(0, 0, curveGradientScaleX*canvasWidth, 0, res.getColor(startCurveColor), res.getColor(endCurveColor), Shader.TileMode.MIRROR));
        if (mStyle == Linestyle.Curve) {
            drawScrollLine(canvas);
        } else {
            drawLine(canvas);
        }
        mPaint.setStyle(Style.FILL);
        drawCircles(canvas);

        mRects = getmRects();
        mPaint.setShader(new LinearGradient(0, 0, rectGradientScaleX*canvasWidth, 0, res.getColor(startRectangleColor), res.getColor(endRectangleColor), Shader.TileMode.MIRROR));
        drawRectangle(canvas, mRects);

        drawBtcText(canvas);

        drawBg(canvas);
    }

    private void drawBg(Canvas canvas) {
        setBackgroundColor(res.getColor(bgColor));
    }

    private void prepareXList() {
        xList.clear();
        float perSize = canvasWidth * 1.0f / (yRawData.size() + 1);
        for (int i = 0; i < yRawData.size(); i++) {
            xList.add(perSize * (i + 1));
        }

        Log.e(TAG, "prepareXList: xList" + xList.get(xList.size() - 1) + ",canvasWidthL:" + canvasWidth + ",perSize:" + perSize + ",xList.size():" + xList.size());
    }

    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            canvas.drawCircle(mPoints[i].x, mPoints[i].y, CIRCLE_SIZE / 2, mPaint);
        }
    }

    private void drawBtcText(Canvas canvas) {
        mTextPaint.setColor(res.getColor(textColor));
        mTextPaint.setTextSize(dip2px(60));
        mTextPaint.setStrokeWidth(dip2px(20));
        Rect rect = new Rect();
        mTextPaint.getTextBounds("22450", 0, 5, rect);
        int width = rect.width();
        int height = rect.height();
        int singleWidth = width / 5;

        mTextPaint.setTextSize(dip2px(20));
        mTextPaint.setStrokeWidth(dip2px(10));
        int left = (this.canvasWidth - width) / 2 - singleWidth;
        int top = bheight - height + 5 - maxYValue;
        float ￥Width = mTextPaint.measureText("￥");
        canvas.drawText("￥", left, top + 5, mTextPaint);

        mTextPaint.setTextSize(dip2px(60));
        mTextPaint.setStrokeWidth(dip2px(20));
        left += ￥Width;
        canvas.drawText("22450", left, top, mTextPaint);

        mTextPaint.setTextSize(dip2px(30));
        mTextPaint.setStrokeWidth(dip2px(10));
        left += +width;
        canvas.drawText(".32 ", left, top, mTextPaint);

        top -= height + 50;
        canvas.drawText(selectValue, left - width - ￥Width, top, mTextPaint);
    }

    /**
     * 画所有横向表格，包括X轴
     */
    private void drawAllXLine(Canvas canvas) {
        for (int i = 0; i < spacingHeight + 1; i++) {
            canvas.drawLine(blwidh, height - (height / spacingHeight) * i + marginTop, (canvasWidth - blwidh),
                    height - (height / spacingHeight) * i + marginTop, mPaint);// Y坐标
            drawText(String.valueOf(averageYValue * i) + "," + (height - (height / spacingHeight) * i + marginTop), blwidh / 2, height - (height / spacingHeight) * i + marginTop,
                    canvas);
        }
    }

    /**
     * 画所有纵向表格，包括Y轴
     */
    private void drawAllYLine(Canvas canvas) {
        for (int i = 0; i < yRawData.size(); i++) {
            canvas.drawLine(blwidh + (canvasWidth - blwidh) / yRawData.size() * i, marginTop, blwidh
                    + (canvasWidth - blwidh) / yRawData.size() * i, height + marginTop, mPaint);
            drawText(xRawDatas.get(i), blwidh + (canvasWidth - blwidh) / yRawData.size() * i, height + dip2px(26),
                    canvas);// X坐标

            Log.e(TAG, "drawAllYLine: x:" + (blwidh + (canvasWidth - blwidh) / yRawData.size() * i) + ",y:" + (height + dip2px(26)));
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

    private void drawRectangle(Canvas canvas, Rect[] rects) {
        for (int i = 0; i < rects.length; i++) {
            canvas.drawRect(rects[i], mPaint);
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

    private void drawText(String text, int x, int y, Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(dip2px(12));
        p.setColor(res.getColor(R.color.color_999999));
        p.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(text, x, y, p);
    }

    private Point[] getPoints() {
        Point[] points = new Point[yRawData.size()];

        //maxYValue
        float tmpValue = (maxYValue) * maxYListValue / bheight;

        float factor = tmpValue / maxYListValue;

        for (int i = 0; i < yRawData.size(); i++) {
            int ph = (int) (bheight * (1 - (yRawData.get(i) * factor / maxYListValue)));
            Float x = xList.get(i);
            points[i] = new Point(x.intValue(), ph + marginTop);
        }
        return points;
    }

    private Rect[] getmRects() {
        Rect[] rects = new Rect[yRectabgleRawData.size()];

        //计算临界 y轴值
        float tmpValue = (maxYRectangleValue) * maxYRectangleListValue / bRectangleHeight;

        float factor = tmpValue / maxYRectangleListValue;

        int dx = (int) (xList.get(1) - xList.get(0) - (xDistanceValue <= 2 ? 2 : xDistanceValue));
        for (int i = 0; i < yRectabgleRawData.size(); i++) {
            int top = (int) (bRectangleHeight * (1 - (yRectabgleRawData.get(i) * factor / maxYRectangleListValue)) + marginTop);
            int bottom = bRectangleHeight + marginTop;
            float x = xList.get(i);
            int left = (int) (x - dx / 2);
            int right = (int) (x + dx / 2);
            //int left, int top, int right, int bottom
            Log.e(TAG, "getmRects: left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
            rects[i] = new Rect(left, top, right, bottom);
        }
        return rects;
    }

    /**
     * @param yRawData
     * @param yRectangleList
     * @param xRawData
     * @param maxYValue
     * @param maxYListValue
     * @param xDistanceValue
     * @param averageYValue
     * @param maxYRectangleValue
     * @param maxYRectangleListValue
     */
    public void setData(ArrayList<Double> yRawData, ArrayList<Integer> yRectangleList, ArrayList<String> xRawData, int maxYValue, int maxYListValue, int xDistanceValue, int averageYValue,
                        int maxYRectangleValue, int maxYRectangleListValue) {
        this.maxYValue = maxYValue;
        this.maxYListValue = maxYListValue;
        this.maxYRectangleListValue = maxYRectangleListValue;
        this.xDistanceValue = xDistanceValue;
        this.maxYRectangleValue = maxYRectangleValue;
        this.averageYValue = averageYValue;
        this.mPoints = new Point[yRawData.size()];
        this.xRawDatas = xRawData;
        this.yRectabgleRawData = yRectangleList;
        this.yRawData = yRawData;
        this.spacingHeight = maxYListValue / averageYValue;
        invalidate();
    }

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

    public void setTotalvalue(int maxValue) {
        this.maxYListValue = maxValue;
    }

    public void setPjvalue(int averageValue) {
        this.averageYValue = averageValue;
    }

    public void setMargint(int marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginb(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setMstyle(Linestyle mStyle) {
        this.mStyle = mStyle;
    }

    public void setBheight(int bheight) {
        this.bheight = bheight;
    }
}
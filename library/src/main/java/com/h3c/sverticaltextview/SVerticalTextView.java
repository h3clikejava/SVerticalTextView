package com.h3c.sverticaltextview;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by H3c on 12/16/15.
 */
public class SVerticalTextView extends View {
    private final static String TAG = "H3c";
    private final static boolean IS_DEBUG = true;

    private static float DENSITY;// 屏幕密度
    private static int ROW_HEIGHT;// 行高
    private static int COLUMN_WIDTH;// 列宽

    private boolean isVertical = true;// 是否垂直显示文本
    private String mText;// 文本
    private int mTextColor = Color.BLACK;// 文本颜色
    private float mMaxTextSize;// 最大文字大小[目前没有用到]
    private TextPaint mTextPaint;

    public SVerticalTextView(Context context) {
        super(context);

        init();
    }

    public SVerticalTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        int[] set = {
                android.R.attr.background, // idx 0
                android.R.attr.text        // idx 1
        };
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        Drawable d = a.getDrawable(0);
        CharSequence t = a.getText(1);
        mText = t.toString();
        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.SVerticalTextView);
        mMaxTextSize = a.getDimension(R.styleable.SVerticalTextView_textSize, 0);
        mTextColor = a.getColor(R.styleable.SVerticalTextView_textColor, Color.BLACK);
        a.recycle();

        init();
    }

    /**
     * 初始化参数
     */
    private void init() {
        DENSITY = getResources().getDisplayMetrics().density;

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = DENSITY;
        mTextPaint.setTextSize(((mMaxTextSize > 0) ? mMaxTextSize : 16 * DENSITY));
        mTextPaint.setColor(mTextColor);

        ROW_HEIGHT = (int)(0 * DENSITY);
        COLUMN_WIDTH = (int)(0 * DENSITY);

        // 加载第三方字体
        AssetManager mgr = getContext().getAssets();//得到AssetManager
        Typeface tf = Typeface.createFromAsset(mgr, "jkt.TTF");
        mTextPaint.setTypeface(tf);
    }

    /**
     * 设置文本
     * @param text
     */
    private boolean reMeasureTextSize = true;// 重算文字大小
    public void setText(String text) {
        this.mText = text;
        this.reMeasureTextSize = true;
        invalidate();
    }

    /**
     * 动态改变方向
     * @param orientation
     */
    public void setOrientation(int orientation) {
        if(LinearLayout.VERTICAL == orientation) {
            isVertical = true;
        } else {
            isVertical = false;
        }
        invalidate();
    }

    public int getOrientation() {
        if(isVertical) {
            return LinearLayout.VERTICAL;
        }

        return LinearLayout.HORIZONTAL;
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        if(TextUtils.isEmpty(mText)) return;

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        // 获得真实可绘制区域大小
        int width = viewWidth - paddingLeft - paddingRight;
        int height = viewHeight - paddingTop - paddingBottom;

        // 根据View大小测量文字大小
        float FONT_SIZE = resetTextSize(width, height, mText, mTextPaint);

        // 文字基线［目前没有用到］
//        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
//        float baseLineHeight = fontMetrics.descent;

        // 一行显示字数
        int COUNT_IN_ROW = (int)(width / FONT_SIZE + COLUMN_WIDTH);// 一行显示的字数
        // 一列显示字数
        int COUNT_IN_COLUMN = (int) (height / (FONT_SIZE + ROW_HEIGHT));// 一列显示的字数

        if(IS_DEBUG) {
            // 绘制文字大小
            Paint testPaint = new Paint();
            testPaint.setColor(Color.RED);
            canvas.drawRect(0, 0, COUNT_IN_ROW, COUNT_IN_COLUMN, testPaint);
        }

        // 分段绘制文字
        char[] chars = mText.toCharArray();
        int index = 0;// 当前索引
        int INIT_Y = (int)(mTextPaint.getTextSize() + paddingTop);// 第一个文字的y值[文字底部的值]
        int viewEnd = isVertical ? (width + paddingLeft) : paddingLeft;

        for (char c: chars) {
            Point point = null;
            if(isVertical) {
                point = measureCharVerticalPosition(index++, viewEnd, COUNT_IN_COLUMN, INIT_Y, FONT_SIZE);
            } else {
                point = measureCharHorizontalPosition(index++, viewEnd, COUNT_IN_ROW, INIT_Y, FONT_SIZE);
            }

            if(point != null) {
                // 绘制文字
                canvas.drawText(String.valueOf(c), point.x, point.y, mTextPaint);
            } else {
                Log("ERROR:" + c);
            }
        }

        Log("w:" + width + " h:" + height + " pl:" + paddingLeft + "  t:" + mText + "===" + mMaxTextSize+"===" +mTextPaint.getTextSize()+ "===");
    }

    /**
     * 重算文字大小
     * @param vWidth
     * @param vHeight
     * @param text
     * @param paint
     */
    private float resetTextSize(int vWidth, int vHeight, String text, TextPaint paint) {
        if(paint == null || vWidth < 1 || vHeight < 1 || TextUtils.isEmpty(text)) return 0;

        if(reMeasureTextSize) {
            int textsize = measureTextSize(vWidth, vHeight, text);
            paint.setTextSize(textsize);
            return textsize;
        }

        return paint.getTextSize();
    }

    /**
     * 测量文字的大小，用区域面积除以总字数等于单字面积，估算出文字大小
     *
     * ps: 貌似这样算出来的文字比预想的要小一点，但是目前凑合可以用，先这么招吧。
     * @param width
     * @param height
     * @param text
     */
    private int measureTextSize(int width, int height, String text) {
        double area = width * height;
        int count = text.length();
        float fontArea = (float)((area - ((COLUMN_WIDTH + ROW_HEIGHT) * count)) / count);
        float averageSideWidth = (float) Math.sqrt(fontArea);// 平均文字大小

        boolean flag = true;
        while (flag) {
            int tmpFontCount = ((int)(height / (averageSideWidth + ROW_HEIGHT)) * (int)(width / (averageSideWidth + COLUMN_WIDTH)));
            if(tmpFontCount >= count) {
                flag = false;
            } else {
                averageSideWidth -= getResources().getDisplayMetrics().density;
            }
        }

        return (int) averageSideWidth;
    }

    /**
     * 测量文字绘制的位置
     */
    private Point measureCharVerticalPosition(int index, int fontStartXInView, int countInColumn, int initY, float fontSize) {
        Point result = new Point();
        result.x = (int)(fontStartXInView - (fontSize + COLUMN_WIDTH) * (index / countInColumn + 1));
        result.y = (initY + ROW_HEIGHT) * (index % countInColumn) + initY;
        return result;
    }
    private Point measureCharHorizontalPosition(int index, int fontStartXInView, int countInRow, int initY, float fontSize) {
        //        Rect textBounds = new Rect();
        //        paint.getTextBounds(String.valueOf(c), 0, 1, textBounds);

        Point result = new Point();
        // 计算出当前文字区域
        result.x = (int)((fontSize + COLUMN_WIDTH) * (index % countInRow) + fontStartXInView);
        result.y = (initY + ROW_HEIGHT) * (index / countInRow) + initY;

        return result;
    }

    public void Log(String content) {
        if(IS_DEBUG) {
            Log.e(TAG, content);
        }
    }
}
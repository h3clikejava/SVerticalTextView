package com.h3c.sverticaltextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;

/**
 * Created by H3c on 12/16/15.
 */
public class SVerticalTextView extends View {
    private final static String TAG = "SVerticalTextView";
    private final static boolean IS_DEBUG = true;

    private static float DENSITY;// 屏幕密度
    private static int ROW_HEIGHT;// 行高 ，现在程序里设定为字号的0.5倍
    private static int COLUMN_WIDTH;// 列宽
    private static char NEW_LINE_CHAR = '\n';

    private boolean isVertical = false;// 是否垂直显示文本
    private boolean isShadow = false;// 文字阴影
    private String mText;// 文本
    private int mTextColor = Color.BLACK;// 文本颜色
    private int shadowColor = Color.WHITE;// 阴影颜色
    private float mMaxTextSize;// 最大文字大小
    private int mMixTextSize = 10;// 最小文字大小
    private TextPaint mTextPaint;

    public SVerticalTextView(Context context) {
        super(context);

        init();
    }

    public SVerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SVerticalTextView);
        setMaxTextSize(a.getDimension(R.styleable.SVerticalTextView_SVerticalTextView_maxTextSize, 0));
        setTextColor(a.getColor(R.styleable.SVerticalTextView_SVerticalTextView_textColor, Color.BLACK));
        a.recycle();

        init();
    }

    /**
     * 初始化参数
     */
    private void init() {
        DENSITY = getResources().getDisplayMetrics().density;

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setDither(true);
        mTextPaint.density = DENSITY;
        mTextPaint.setTextSize(((mMaxTextSize > 0) ? mMaxTextSize : 16 * DENSITY));
        setTextColor(mTextColor);

        COLUMN_WIDTH = (int)(0 * DENSITY);
    }

    public void setFontType(String filePath) {
        if(!TextUtils.isEmpty(filePath)) {
            Typeface tf = null;
            try {
                tf = Typeface.createFromFile(filePath);
            } catch (Exception e) {
                (new File(filePath)).delete();
            }
            if(tf != null) {
                mTextPaint.setTypeface(tf);
                return;
            }
        }
        mTextPaint.setTypeface(null);
    }

    public void setMaxTextSize(float size) {
        mMaxTextSize = size;
    }

    /**
     * 设置文本
     * @param text
     */
    private boolean reMeasureTextSize = true;// 重算文字大小
    public void setText(String text) {
        setText(text, false);
    }

    public void setText(String text, boolean isVertical) {
        setText(text, isVertical, false);
    }

    public void setText(String text, boolean isVertical, boolean isShadow) {
        this.mText = text;
        this.reMeasureTextSize = true;
        this.isVertical = isVertical;
        this.isShadow = isShadow;
        invalidate();
    }

    public void setTextColor(int color) {
        mTextColor = color;

        if(Color.WHITE == color) {
            shadowColor = Color.BLACK;
        } else {
            shadowColor = Color.WHITE;
        }

        if(mTextPaint != null) {
            mTextPaint.setColor(color);
        }
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
        Integer[] rowInfo = new Integer[2];// ［列数，行数］
        float FONT_SIZE = resetTextSize(width, height, mText, mTextPaint, rowInfo);
        if(width == 0 || height == 0 || FONT_SIZE == 0) return;

        // 文字基线［目前没有用到］
//        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
//        float baseLineHeight = fontMetrics.descent;

        if(IS_DEBUG) {
//            // 绘制文字大小
            Paint testPaint = new Paint();
            testPaint.setColor(Color.RED);
//            canvas.drawRect(0, 0, COUNT_IN_ROW, COUNT_IN_COLUMN, testPaint);
            canvas.drawRect(0, 0, viewWidth, viewHeight, testPaint);
            testPaint.setColor(Color.GREEN);
            canvas.drawRect(paddingLeft, paddingTop, viewWidth - paddingRight, viewHeight - paddingBottom, testPaint);
        }

        // 分段绘制文字
        char[] chars = mText.toCharArray();

        // 绘制阴影
        if(isShadow) {
            mTextPaint.setShadowLayer(15, 0, 0, shadowColor);
        }

        Point lastCharPoint = new Point(
                isVertical ? (int)(viewWidth - FONT_SIZE) : 0, // 上一个文字的X
                0// 上一个文字的Y[文字底部的值]
        );
        int XOffset;
        if(isVertical) {
            XOffset = - (int)(paddingRight + (width - rowInfo[0] * FONT_SIZE - (rowInfo[0]- 1) * ROW_HEIGHT) / 2);
        } else {
            XOffset = (int)(paddingLeft + (width - rowInfo[0] * FONT_SIZE - (rowInfo[0]- 1) * COLUMN_WIDTH) / 2);
        }
        int YOffset = (int)(mTextPaint.getTextSize() - 4 * DENSITY) + paddingTop
                + (int)(((height - rowInfo[1] * FONT_SIZE - (rowInfo[1] - 1) * (isVertical ? COLUMN_WIDTH : ROW_HEIGHT)) / 2));
        for (char c: chars) {
            // 先画文字
            if(NEW_LINE_CHAR == c) {// 换行刷新位子，不画文字
            } else {
                // 画文字
                canvas.drawText(String.valueOf(c), lastCharPoint.x + XOffset, lastCharPoint.y + YOffset, mTextPaint);
            }
            // 刷新文字位子
            lastCharPoint = measureCharHorizontalPosition(c,
                    lastCharPoint.x, lastCharPoint.y,
                    0, ((int)(0.5 * FONT_SIZE)),
                    isVertical, (int)FONT_SIZE,
                    (isVertical ? viewWidth : viewWidth - paddingRight), viewHeight - paddingBottom);

            // 越界或者画完了
            if(lastCharPoint == null) break;
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
    private float resetTextSize(int vWidth, int vHeight, String text, TextPaint paint, Integer[] rowInfo) {
        if(paint == null || vWidth < 1 || vHeight < 1 || TextUtils.isEmpty(text)) return 0;

        if(reMeasureTextSize) {
            int textsize = measureTextSize(vWidth, vHeight, text, rowInfo);
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
    private int measureTextSize(int width, int height, String text, Integer[] rowInfo) {
        double area = width * height;
        int count;// 区域内总文字数
        int minInRow = -1;// 行最小字数
        int minInColumn = -1;// 列最小字数

        String[] tmpNewLineTexts = text.split("\\n");
        int maxTextCountInColumn = tmpNewLineTexts.length;// 最大行数
        int maxTextCountInARow = 0;// 最大列数
        for (String rowStrInText : tmpNewLineTexts) {
            if(maxTextCountInARow < rowStrInText.length()) {
                maxTextCountInARow = rowStrInText.length();
            }
        }

        count = maxTextCountInColumn * maxTextCountInARow;
        if(isVertical) {
            minInColumn = maxTextCountInARow;
            rowInfo[1] = maxTextCountInARow;
            rowInfo[0] = maxTextCountInColumn;
        } else {
            minInRow = maxTextCountInARow;
            rowInfo[0] = maxTextCountInARow;
            rowInfo[1] = maxTextCountInColumn;
        }

        float fontArea = (float)(area / count);// 文字区域大小
        float averageSideWidth = (float) Math.sqrt(fontArea);// 平均文字大小 [这个文字大小包括了字间距]
        // 行号设定为字间距的0.5
        ROW_HEIGHT = (int)(averageSideWidth / 3);
        float realFontWidth = averageSideWidth - ROW_HEIGHT;

        // 文字大小不允许超过设定的最大文字大小
        if(mMaxTextSize > 0 && realFontWidth > mMaxTextSize) {
            realFontWidth = mMaxTextSize;
        }

        boolean flag = true;
        while (flag) {
            int countInRow = (int)(width / (realFontWidth + (isVertical ? ROW_HEIGHT : COLUMN_WIDTH)));
            int countInColumn = (int)(height / (realFontWidth + (isVertical ? COLUMN_WIDTH : ROW_HEIGHT)));
            int tmpFontCount = (countInRow * countInColumn);
            if(tmpFontCount >= count && countInRow > minInRow && countInColumn > minInColumn) {
                flag = false;
            } else {
                realFontWidth -= DENSITY;
            }
        }

        return (int) (realFontWidth > mMixTextSize ? realFontWidth : mMixTextSize);
    }

    /**
     * 获得文字偏移，主要用于文字居中
     * @return
     */
    private int getTextOffset(int viewWidth, float fontSize, int countInRow) {
        int spaceWidth = (int)(viewWidth - fontSize * countInRow);// 空余位置的宽度
        return spaceWidth >> 1;
    }

    /**
     * 测量文字绘制的位置
     */
    private Point measureCharHorizontalPosition(char c,
                                                int lastX, int lastY,
                                                int widthPadding, int heightPadding,
                                                boolean isVertical, int fontSize,
                                                int viewWidth, int viewHeight) {
        int addToOrientation;// 加载文字方向 0右，1下，2左

        if(NEW_LINE_CHAR == c) {
            addToOrientation = isVertical ? 2 : 1;
            if(isVertical) {
                lastY = 0;
            } else {
                lastX = 0;
            }
        } else {
            addToOrientation = isVertical ? 1 : 0;
        }

        int newX = lastX;
        int newY = lastY;
        switch (addToOrientation) {
            case 0:
                newX = lastX + fontSize + widthPadding;
                break;
            case 1:
                newY = lastY + fontSize + (isVertical ? widthPadding : heightPadding);
                break;
            case 2:
                newX = lastX - fontSize - heightPadding;
                break;
        }

        // 越界换行
        if(newX > viewWidth || newX < 0 || newY > viewHeight) {
            if(isVertical) {
                newX = lastX - fontSize - heightPadding;
                newY = 0;
            } else {
                newX = 0;
                newY = lastX - fontSize - heightPadding;
            }
        }

        // 换行后仍旧越界
        if(newX > viewWidth || newX < 0 || newY > viewHeight) {
            return null;
        }
        return new Point(newX, newY);
    }

    public void Log(String content) {
        if(IS_DEBUG) {
            Log.e(TAG, content);
        }
    }
}
package com.h3c.sverticaltextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
    private boolean isFitMode = true;// 充满模式［会自动撑满整个View，否则会按照文字默认不换行计算字号］
    private String mText;// 文本
    private int mTextColor = Color.BLACK;// 文本颜色
    private int shadowColor = Color.WHITE;// 阴影颜色
    private float mMaxTextSize;// 最大文字大小
    private float mMinTextSize = -1;// 最小文字大小
    private int LETTER_PADDING = 2;// 字母间距
    private int SPACE_PADDING;// 空格间距
    private TextPaint mTextPaint;

    public SVerticalTextView(Context context) {
        super(context);

        init();
    }

    public SVerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SVerticalTextView);
        setMinTextSize(a.getDimension(R.styleable.SVerticalTextView_SVerticalTextView_minTextSize, 0));
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
        SPACE_PADDING = (int) (DENSITY * 4);

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

    public void setMinTextSize(float size) {
        mMinTextSize = size;
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
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLineHeight = fontMetrics.descent;

        if(IS_DEBUG) {
//            // 绘制文字大小
            Paint testPaint = new Paint();
            testPaint.setColor(Color.RED);
//            canvas.drawRect(0, 0, COUNT_IN_ROW, COUNT_IN_COLUMN, testPaint);
            canvas.drawRect(0, 0, viewWidth, viewHeight, testPaint);
            testPaint.setColor(Color.GREEN);
            canvas.drawRect(paddingLeft, paddingTop, viewWidth - paddingRight, viewHeight - paddingBottom, testPaint);
        }

        // 提前计算出各个文字绘制的位子
        char[] chars = mText.toCharArray();
        int charsLength = chars.length;
        Point[] charPoints = new Point[charsLength];
        charPoints[0] = new Point(
                isVertical ? (int)(width - FONT_SIZE) : 0, // 上一个文字的X
                0);// 上一行文字的Y[文字底部的值]
        int maxWidth = charPoints[0].x;
        int maxHeight = charPoints[0].y;
        for (int n = 1; n < charsLength; n++) {
            charPoints[n] = measureCharHorizontalPosition(
                    chars[n - 1], chars[n],
                    charPoints[n - 1].x, charPoints[n - 1].y,
                    0, ((int)(0.5 * FONT_SIZE)),
                    isVertical, (int)FONT_SIZE,
                    (int)(width - FONT_SIZE), (int)(height - FONT_SIZE));

            if(charPoints[n] == null) break;// 越界不画
            if(NEW_LINE_CHAR == chars[n]) continue;// 换行不算距离
            if(charPoints[n].y > maxHeight) {
                maxHeight = charPoints[n].y;
            }

            if(isVertical && charPoints[n].x < maxWidth) {
                maxWidth = charPoints[n].x;
            } else if(!isVertical && charPoints[n].x > maxWidth) {
                maxWidth = charPoints[n].x;
            }
//            canvas.drawLine(0, charPoints[n].y, viewWidth, charPoints[n].y, mTextPaint);
        }

        // 计算居中偏移
        int XOffset;
        if(isVertical) {
            maxWidth = maxWidth + paddingLeft;
            XOffset = (int)(paddingLeft - ((maxWidth + (0.5 * ROW_HEIGHT)
                    - (paddingLeft + width / 2)))
                    - ((viewWidth - maxWidth - paddingRight - (0.5 * ROW_HEIGHT)) / 2));
        } else {
            XOffset = (int)(paddingLeft + (width - maxWidth - FONT_SIZE) / 2);
        }
        int YOffset = (int)(FONT_SIZE - baseLineHeight + DENSITY) + paddingTop
                + ((int)(height - maxHeight - FONT_SIZE) / 2);

        // 绘制阴影
        if(isShadow) {
            mTextPaint.setShadowLayer(15, 0, 0, shadowColor);
        } else {
            mTextPaint.setShadowLayer(0, 0, 0, shadowColor);
        }

//        canvas.drawLine(0, maxHeight, viewWidth, maxHeight, mTextPaint);
//        canvas.drawLine(maxWidth, 0, maxWidth, viewHeight, mTextPaint);
//        canvas.drawLine((int)(maxWidth + (0.5 * ROW_HEIGHT)), 0, (int)(maxWidth + (0.5 * ROW_HEIGHT)), viewHeight, mTextPaint);
//        canvas.drawLine((paddingLeft + width / 2), 0, (paddingLeft + width / 2), viewHeight, mTextPaint);

        // 画文字
        for (int n = 0; n < charsLength; n++) {
            char c = chars[n];
            if(NEW_LINE_CHAR == c) {
            } else {
                Point charPoint = charPoints[n];
                if(charPoint == null) break;// 文字越界不绘制

                int charDrawX = charPoint.x + XOffset;
                int charDrawY = charPoint.y + YOffset;
                // 字母竖排修正X位置
                if(isVertical) {
                    if(c >= 0x21 && c <= 0x7E) {
                        switch (c) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                charDrawX += FONT_SIZE * 0.2;
                                break;
                            case 'C':
                            case 'H':
                            case 'K':
                            case 'G':
                            case 'N':
                            case 'O':
                            case 'Q':
                                charDrawX += FONT_SIZE * 0.1;
                                break;
                            case 'A':
                            case 'B':
                            case 'D':
                            case 'E':
                            case 'F':
                            case 'J':
                            case 'P':
                            case 'L':
                            case 'R':
                            case 'S':
                            case 'T':
                            case 'U':
                            case 'V':
                            case 'X':
                            case 'Y':
                            case 'Z':
                                charDrawX += FONT_SIZE * 0.15;
                                break;
                            case 'I':
                                charDrawX += FONT_SIZE * 0.3;
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'g':
                            case 'h':
                            case 'k':
                            case 'n':
                            case 'o':
                            case 'p':
                            case 'q':
                            case 's':
                            case 'u':
                            case 'z':
                                charDrawX += FONT_SIZE * 0.2;
                                break;
                            case 'f':
                            case 'v':
                            case 'x':
                            case 'y':
                                charDrawX += FONT_SIZE * 0.25;
                                break;
                            case 'i':
                            case 'j':
                            case 'l':
                                charDrawX += FONT_SIZE * 0.35;
                                break;
                            case 'r':
                            case 't':
                                charDrawX += FONT_SIZE * 0.3;
                                break;
                            case 'w':
                                charDrawX += FONT_SIZE * 0.1;
                                break;
                            case '?':
                            case '.':
                            case ',':
                            case '/':
                            case '$':
                            case '&':
                            case '(':
                            case '=':
                            case '+':
                            case '_':
                            case '<':
                            case '>':
                                charDrawX += FONT_SIZE * 0.2;
                                break;
                            case '~':
                            case '#':
                            case '%':
                                charDrawX += FONT_SIZE * 0.1;
                                break;
                            case '-':
                            case '*':
                            case '^':
                            case '!':
                            case ')':
                            case '|':
                            case ':':
                            case ';':
                            case '`':
                                charDrawX += FONT_SIZE * 0.3;
                                break;
                        }
                    }
                }
                canvas.drawText(String.valueOf(c), charDrawX, charDrawY, mTextPaint);
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

        // 算出默认最大行数和最大列数
        String[] tmpNewLineTexts = text.split("\\n");
        int maxTextCountInColumn = tmpNewLineTexts.length;// 最大行数
        int maxTextCountInARow = 0;// 最大列数
        for (String rowStrInText : tmpNewLineTexts) {
            if(maxTextCountInARow < rowStrInText.length()) {
                maxTextCountInARow = rowStrInText.length();
            }
        }

        // 得到总格子数
        count = maxTextCountInColumn * maxTextCountInARow;
        if(isVertical) {
            minInColumn = maxTextCountInARow;
            rowInfo[0] = maxTextCountInColumn;
            rowInfo[1] = maxTextCountInARow;
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


            if((tmpFontCount >= count) && (isFitMode || (!isFitMode && countInRow > minInRow && countInColumn > minInColumn))) {
                flag = false;
            } else {
                realFontWidth -= DENSITY;
            }
        }

        // 文字小于最小字号，需要自动换行
        if(realFontWidth < mMinTextSize) {
            int maxCount;

            if(isVertical) {
                maxCount = (int)(height / mMinTextSize + COLUMN_WIDTH);
            } else {
                maxCount = (int)(width / mMinTextSize + COLUMN_WIDTH);
            }

            int moreCount = 0;// 多出来的列数
            for (String rowStrInText : tmpNewLineTexts) {
                if(maxCount < rowStrInText.length()) {
                    moreCount += rowStrInText.length() / maxCount;
                }
            }

            if(isVertical) {
                rowInfo[0] += moreCount;
                rowInfo[1] = maxCount;
            } else {
                rowInfo[0] = maxCount;
                rowInfo[1] += moreCount;
            }
            realFontWidth = mMinTextSize;
        }

        return (int) realFontWidth;
    }

    /**
     * 测量文字绘制的位置
     */
    private Point measureCharHorizontalPosition(char c, char nextC,
                                                int lastX, int lastY,
                                                int widthPadding, int heightPadding,
                                                boolean isVertical, int fontSize,
                                                int viewWidth, int viewHeight) {
        int originalFontSize = fontSize;
        boolean isLetter = (c >= 0x20 && c <= 0x7F);// 是否为字母
        if(isLetter && !isVertical) {
            if(c == 0x20) {// 空格宽度
                fontSize = SPACE_PADDING;
            } else {
                Rect textBounds = new Rect();
                mTextPaint.getTextBounds(String.valueOf(c), 0, 1, textBounds);
                fontSize = textBounds.width() + LETTER_PADDING;

                switch (c) {
                    case '1':// 1.
                        fontSize += 2 * DENSITY;
                        break;
                    case '3':// 3.
                        fontSize += DENSITY;
                        break;
                    case '5':// 5.
                        fontSize += DENSITY;
                        break;
                    case '8':// 8.
                        fontSize += DENSITY;
                        break;
                    case '9':// 9.
                        fontSize += DENSITY;
                        break;
                    case 'A':// A.
                        fontSize -= 3 * DENSITY;
                    case 'I':// I.
                        fontSize += 2 * DENSITY;
                        break;
                    case 'M':// M.
                        fontSize += DENSITY;
                        break;
                    case 'N':// N.
                        fontSize += DENSITY;
                        break;
                    case 'U':// U.
                        fontSize += DENSITY;
                        break;
                    case 'b':// b.
                        fontSize += DENSITY;
                        break;
                    case 'd':// d.
                        fontSize += DENSITY;
                        break;
                    case 'f':// f.
                        fontSize -= DENSITY;
                        break;
                    case 'g':// g.
                        fontSize += DENSITY;
                        break;
                    case 'h':// h.
                    case 'i':// i.
                        fontSize += DENSITY;
                        break;
                    case 'l':// l.
                        fontSize += DENSITY;
                        break;
                    case 'm':// m.
                        fontSize += DENSITY;
                        break;
                    case 'n':// n.
                        fontSize += DENSITY;
                        break;
                    case 'p':// p.
                        fontSize += DENSITY;
                        break;
                    case 'q':// q.
                        fontSize += DENSITY;
                        break;
                    case 'r':// r.
                        fontSize += DENSITY;
                        break;
                    case 's':// s.
                        fontSize += DENSITY;
                        break;
                    case 'u':// u.
                        fontSize += DENSITY;
                        break;
                    case 'y':// y.
                        fontSize -= DENSITY;
                        break;
                }
            }
        }

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
            // 如果自动换行和手动换行冲突，就不用自动换行了
            if(NEW_LINE_CHAR == nextC) return new Point(newX, newY);
            if(isVertical) {
                newX = lastX - fontSize - heightPadding;
                newY = 0;
            } else {
                newX = 0;
                newY = lastY + originalFontSize + heightPadding;
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
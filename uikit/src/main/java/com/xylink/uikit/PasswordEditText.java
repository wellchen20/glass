package com.xylink.uikit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.lang.reflect.Field;

public class PasswordEditText extends AppCompatEditText {
    private Context mContext;
    private int maxLength;
    private int textColor;
    private Paint mPaintText;
    private Paint mPaintLine;
    private int backgroundColor;
    private int borderColor;
    private int borderSelectedColor;
    private float borderRadius;
    private float borderWidth;
    private int coverCirclrColor;
    private float coverCirclrRadius;
    private GradientDrawable gradientDrawable = new GradientDrawable();

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttrs(attrs, defStyleAttr);
        init();
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.PasswordEditText, defStyleAttr, 0);
        backgroundColor = array.getColor(R.styleable.PasswordEditText_psw_background_color, Color.parseColor("#FFFFFF"));
        borderColor = array.getColor(R.styleable.PasswordEditText_psw_border_color, Color.parseColor("#FF0000"));
        borderSelectedColor = array.getColor(R.styleable.PasswordEditText_psw_border_selected_color, 0);
        textColor = array.getColor(R.styleable.PasswordEditText_psw_text_color, Color.parseColor("#FF0000"));
        borderRadius = array.getDimension(R.styleable.PasswordEditText_psw_border_radius, dip2px(6));
        borderWidth = array.getDimension(R.styleable.PasswordEditText_psw_border_width, dip2px(1));
        coverCirclrColor = array.getColor(R.styleable.PasswordEditText_psw_cover_circle_color, Color.parseColor("#FF0000"));
        coverCirclrRadius = array.getDimension(R.styleable.PasswordEditText_psw_cover_circle_radius, 0);
        array.recycle();
    }

    private void init() {
        maxLength = getMaxLength();
        setCursorVisible(false);
        setTextColor(Color.TRANSPARENT);
        //触摸获取焦点
        setFocusableInTouchMode(true);
        setOnLongClickListener(view -> true);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setColor(textColor);
        mPaintText.setTextSize(getTextSize());

        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setColor(borderColor);
        mPaintLine.setStrokeWidth(borderWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        float itemH = getMeasuredHeight();
        float itemW;
        gradientDrawable.setStroke((int) borderWidth, borderColor);

        gradientDrawable.setCornerRadius(borderRadius);
        gradientDrawable.setColor(backgroundColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Android系统大于等于API16，使用setBackground
            setBackground(gradientDrawable);
        } else {
            //Android系统小于API16，使用setBackgroundDrawable
            setBackgroundDrawable(gradientDrawable);
        }
        itemW = measuredWidth / maxLength;
        for (int i = 1; i < maxLength; i++) {
            float startX = itemW * i;
            float startY = 0;
            float stopX = startX;
            float stopY = itemH;
            canvas.drawLine(startX, startY, stopX, stopY, mPaintLine);
        }

        String currentText = getText().toString();
        for (int i = 0; i < maxLength; i++) {
            if (!TextUtils.isEmpty(currentText) && i < currentText.length()) {
                float circleRadius = itemW * 0.5f * 0.5f;
                if (circleRadius > itemH / 2f) {
                    circleRadius = itemH * 0.5f * 0.5f;
                }
                if (coverCirclrRadius > 0) {
                    circleRadius = coverCirclrRadius;
                }
                float startX = (itemW / 2f) + itemW * i;
                float startY = (itemH) / 2.0f;
                mPaintText.setColor(coverCirclrColor);
                canvas.drawCircle(startX, startY, circleRadius, mPaintText);
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        invalidate();
        if (onTextChangeListener != null) {
            if (getText().toString().length() == getMaxLength()) {
                onTextChangeListener.onTextChange(getText().toString(), true);
            } else {
                onTextChangeListener.onTextChange(getText().toString(), false);
            }
        }
    }

    public int getMaxLength() {
        int length = 0;
        try {
            InputFilter[] inputFilters = getFilters();
            for (InputFilter filter : inputFilters) {
                Class<?> c = filter.getClass();
                if (c.getName().equals("android.text.InputFilter$LengthFilter")) {
                    Field[] f = c.getDeclaredFields();
                    for (Field field : f) {
                        if (field.getName().equals("mMax")) {
                            field.setAccessible(true);
                            length = (Integer) field.get(filter);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private OnTextChangeListener onTextChangeListener;

    public void setOnTextChangeListener(OnTextChangeListener onTextChangeListener) {
        this.onTextChangeListener = onTextChangeListener;
    }

    public interface OnTextChangeListener {
        /**
         * 监听输入变化
         *
         * @param text       当前的文案
         * @param isComplete 是不是完成输入
         */
        void onTextChange(String text, boolean isComplete);
    }
}

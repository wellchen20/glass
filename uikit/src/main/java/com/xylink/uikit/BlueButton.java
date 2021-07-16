package com.xylink.uikit;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * 蓝色按钮, 已经设置默认点击效果, 背景颜色, 文字大小颜色. 宽度和高度需自行设置
 *
 * @author zhangyazhou
 * @date 2018/12/25
 */
public class BlueButton extends AppCompatButton {

    public BlueButton(Context context) {
        super(context);
        initDefaultParams();
    }

    public BlueButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefaultParams();
    }

    public BlueButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaultParams();
    }

    private void initDefaultParams() {
        setBackgroundResource(R.drawable.bg_new_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextColor(getResources().getColorStateList(R.color.color_blue_button_text, getContext().getTheme()));
        } else {
            setTextColor(getResources().getColorStateList(R.color.color_blue_button_text));
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.dimen_button_text));
    }
}

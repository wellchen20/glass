package com.xylink.uikit.dialog;

import android.view.View;

/**
 * 单次点击监听器
 * @author zhangyazhou
 */
public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long TIME_DIVIDER_IN_MS = 300;
    private long lastClickTime;

    @Override
    public void onClick(View v) {
        long currentClickTime = System.currentTimeMillis();
        if (currentClickTime - lastClickTime >= TIME_DIVIDER_IN_MS) {
            lastClickTime = currentClickTime;
            onSingleClick(v);
        }
    }

    /**
     * 单次点击回调
     * @param view 被点击的View
     */
    public abstract void onSingleClick(View view);
}

package com.xylink.sdk.sample;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerNoSlide extends ViewPager {

    private boolean isCanScroll = true;

    public ViewPagerNoSlide(Context context) {
        super(context);
    }

    public ViewPagerNoSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 是否可以滑动
     *
     * @param isCanScroll
     */
    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        //android.util.Log.d("debug", "scrollTo, x : " + x + ", y : " + y + ", width : " + getWidth());
        if (x <= getWidth() * (getAdapter().getCount() - 1)) {
            super.scrollTo(x, y);
        }
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        //android.util.Log.d("debug", "onPageScrolled, position : " + position + ", offset : " + offset + ", offsetPixels : " + offsetPixels);
        super.onPageScrolled(position, offset, offsetPixels);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //android.util.Log.d("debug", "onTouchEvent, isCanScroll : " + isCanScroll);
        if (isCanScroll) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isCanScroll) {
            try {
                super.onTouchEvent(ev);
                return super.onInterceptTouchEvent(ev);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }

    }
}

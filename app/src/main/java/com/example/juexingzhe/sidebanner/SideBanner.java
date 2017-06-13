package com.example.juexingzhe.sidebanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * Created by juexingzhe on 2017/6/13.
 */

public class SideBanner extends RelativeLayout {

    private static final int SCREEN_PAGE_LIMIT = 3;
    private static final int SCROLLER_DURATION_TIME = 600;

    private boolean mSideMode;
    private boolean mLoop;
    private boolean mIndicator;

    private ViewPager mViewPager;
    private LinearLayout mIndicatorContainer;


    public SideBanner(Context context) {
        super(context);
        init();
    }

    public SideBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttrs(context, attrs);
        init();
    }

    public SideBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(context, attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SideBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        readAttrs(context, attrs);
        init();
    }

    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SideBanner);
        mSideMode = typedArray.getBoolean(R.styleable.SideBanner_side_mode, true);
        mLoop = typedArray.getBoolean(R.styleable.SideBanner_loop, false);
        mIndicator = typedArray.getBoolean(R.styleable.SideBanner_indicator, false);

    }

    private void init() {

        View view = null;

        if (mSideMode) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.side_banner, this, true);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.normal_banner, this, true);
        }

        mViewPager = (ViewPager) view.findViewById(R.id.side_banner_viewpager);
        mViewPager.setOffscreenPageLimit(SCREEN_PAGE_LIMIT);

        mIndicatorContainer = (LinearLayout) view.findViewById(R.id.side_banner_indicator);

        setViewPagerScroll();

    }


    public void setSideMode(boolean mSideMode) {
        this.mSideMode = mSideMode;
    }

    public void setLoop(boolean mLoop) {
        this.mLoop = mLoop;
    }

    public void setIndicator(boolean mIndicator) {
        this.mIndicator = mIndicator;
    }

    private void setViewPagerScroll() {

        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            MyScroller myScroller = new MyScroller(mViewPager.getContext());
            mScroller.set(mViewPager, myScroller);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }


    private static class MyScroller extends Scroller {

        private int mDuration = SCROLLER_DURATION_TIME;

        public MyScroller(Context context) {
            super(context);
        }

        public MyScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public MyScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public int getScrollDuration() {
            return mDuration;
        }

        public void setScrollDuration(int mDuration) {
            this.mDuration = mDuration;
        }
    }


}

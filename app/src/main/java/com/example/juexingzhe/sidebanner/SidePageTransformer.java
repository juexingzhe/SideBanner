package com.example.juexingzhe.sidebanner;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by juexingzhe on 2017/6/14.
 */

public class SidePageTransformer implements ViewPager.PageTransformer{

    private static final int OUT_LEFT_SCREEN = -1;
    private static final int OUT_RIGHT_SCREEN = 1;

    private static final float SCALEY = 0.8f;

    @Override
    public void transformPage(View page, float position) {
        if (position < OUT_LEFT_SCREEN || position > OUT_RIGHT_SCREEN){
            page.setScaleY(SCALEY);
        }else {
            page.setScaleY(Math.max(SCALEY, (1 - Math.abs(position))));
        }
    }
}

package com.example.juexingzhe.sidebanner;

import android.content.Context;
import android.view.View;

/**
 * Created by juexingzhe on 2017/6/14.
 */

public interface SideViewHolder<T> {

    View getView(Context context);

    void onBind(Context context, int position, T data);

}

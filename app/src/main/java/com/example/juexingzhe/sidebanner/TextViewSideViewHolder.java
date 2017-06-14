package com.example.juexingzhe.sidebanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by juexingzhe on 2017/6/14.
 */

public class TextViewSideViewHolder implements SideViewHolder<String> {
    private TextView textView;

    @Override
    public View getView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_item_textview, null);
        textView = (TextView) view.findViewById(R.id.banner_tv);
        return textView;
    }

    @Override
    public void onBind(Context context, int position, String data) {
        textView.setText(data);
    }
}

package com.example.juexingzhe.sidebanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by juexingzhe on 2017/6/14.
 */

public class ImageSideViewHolder implements SideViewHolder<Integer>{
    private ImageView imageView;

    @Override
    public View getView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_item, null);
        imageView = (ImageView) view.findViewById(R.id.banner_image);
        return imageView;
    }

    @Override
    public void onBind(Context context, int position, Integer data) {
        imageView.setImageResource(data);
    }
}

package com.example.juexingzhe.sidebanner;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int[] RES = new int[]{R.mipmap.image1,R.mipmap.image2,R.mipmap.image3};

    private SideBanner mSideBanner;

    private ArrayList<Integer> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSideBanner = (SideBanner) findViewById(R.id.side_banner);
        mDatas = new ArrayList();
        for (int i = 0; i < RES.length; i++){
            mDatas.add(RES[i]);
        }

        mSideBanner.setPages(mDatas, new SideViewHolderCreator() {
            @Override
            public ImageSideViewHolder createSideViewHolder() {
                return new ImageSideViewHolder();
            }
        });
        mSideBanner.setBannerClickListener(new SideBanner.BannerClickListener() {
            @Override
            public void onPageClick(View view, int position) {
                Toast.makeText(MainActivity.this, "点击了" + String.valueOf(position), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onResume() {
        if (mSideBanner != null){
            mSideBanner.startPlay();
        }
        super.onResume();
    }

    private class ImageSideViewHolder implements SideViewHolder<Integer>{
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

    @Override
    protected void onDestroy() {
        if (mSideBanner != null){
            mSideBanner.stopPlay();
        }
        super.onDestroy();
    }
}

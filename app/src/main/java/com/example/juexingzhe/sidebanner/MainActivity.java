package com.example.juexingzhe.sidebanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int[] RES = new int[]{R.mipmap.image1,R.mipmap.image2,R.mipmap.image3};
    public static final String[] TEXT_RES = new String[]{"Hello", "LLN"};

    private SideBanner mSideBanner;

    private ArrayList<Integer> mDatas;
    private ArrayList<String> mStringDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSideBanner = (SideBanner) findViewById(R.id.side_banner);
        mDatas = new ArrayList();
        mStringDatas = new ArrayList();
        for (int i = 0; i < RES.length; i++){
            mDatas.add(RES[i]);
        }

        for (int i = 0; i < TEXT_RES.length; i++){
            mStringDatas.add(TEXT_RES[i]);
        }

//        mSideBanner.setPages(mDatas, new SideViewHolderCreator() {
//            @Override
//            public SideViewHolder createSideViewHolder() {
//                return new ImageSideViewHolder();
//            }
//        });
        mSideBanner.setPages(mStringDatas, new SideViewHolderCreator() {
            @Override
            public SideViewHolder createSideViewHolder() {
                return new TextViewSideViewHolder();
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
    protected void onPause() {
        if (mSideBanner!=null){
            mSideBanner.stopPlay();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mSideBanner != null){
            mSideBanner.startPlay();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mSideBanner != null){
            mSideBanner.stopPlay();
        }
        super.onDestroy();
    }
}

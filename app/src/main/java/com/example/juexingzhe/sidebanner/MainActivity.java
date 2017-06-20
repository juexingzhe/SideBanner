package com.example.juexingzhe.sidebanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int[] RES = new int[]{R.mipmap.image1};
    public static final String[] TEXT_RES = new String[]{"Hello", "LLN"};

    private SideBanner mSideBannerImage;
    private SideBanner mSideBannerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSideBannerImage = (SideBanner) findViewById(R.id.side_banner_image);
        mSideBannerText = (SideBanner)findViewById(R.id.side_banner_text);

        final ArrayList<Integer> datas = new ArrayList();
        final ArrayList<String> stringDatas = new ArrayList();
        for (int i = 0; i < RES.length; i++){
            datas.add(RES[i]);
        }

        for (int i = 0; i < TEXT_RES.length; i++){
            stringDatas.add(TEXT_RES[i]);
        }

        mSideBannerImage.setSideBannerAdapter(new SideBanner.SideBannerAdapter() {
            @Override
            public int getCount() {
                return datas.size();
            }

            @Override
            public void onPageClick(View view, int position) {
                Toast.makeText(MainActivity.this, "点击了" + String.valueOf(position), Toast.LENGTH_LONG).show();
            }

            @Override
            public SideViewHolderCreator getSideViewHolderCreator() {
                return new SideViewHolderCreator(){

                    @Override
                    public SideViewHolder createSideViewHolder() {
                        return new ImageSideViewHolder();
                    }
                };
            }

            @Override
            public Object getItemData(int position) {
                return datas.get(position);
            }
        });

//        mSideBannerImage.setPages(datas, new SideViewHolderCreator() {
//            @Override
//            public SideViewHolder createSideViewHolder() {
//                return new ImageSideViewHolder();
//            }
//        }).setBannerClickListener(new SideBanner.BannerClickListener() {
//            @Override
//            public void onPageClick(View view, int position) {
//                Toast.makeText(MainActivity.this, "点击了" + String.valueOf(position), Toast.LENGTH_LONG).show();
//            }
//        });

        mSideBannerText.setSideBannerAdapter(new SideBanner.SideBannerAdapter() {
            @Override
            public int getCount() {
                return stringDatas.size();
            }

            @Override
            public void onPageClick(View view, int position) {
                Toast.makeText(MainActivity.this, "点击了" + String.valueOf(position), Toast.LENGTH_LONG).show();
            }

            @Override
            public SideViewHolderCreator getSideViewHolderCreator() {
                return new SideViewHolderCreator() {
                    @Override
                    public SideViewHolder createSideViewHolder() {
                        return new TextViewSideViewHolder();
                    }
                };
            }

            @Override
            public Object getItemData(int position) {
                return stringDatas.get(position);
            }
        });

//        mSideBannerText.setPages(stringDatas, new SideViewHolderCreator() {
//            @Override
//            public SideViewHolder createSideViewHolder() {
//                return new TextViewSideViewHolder();
//            }
//        }).setBannerClickListener(new SideBanner.BannerClickListener() {
//            @Override
//            public void onPageClick(View view, int position) {
//                Toast.makeText(MainActivity.this, "点击了" + String.valueOf(position), Toast.LENGTH_LONG).show();
//            }
//        });

    }

    @Override
    protected void onPause() {
        if (mSideBannerImage !=null){
            mSideBannerImage.stopPlay();
        }
        if (mSideBannerText != null){
            mSideBannerText.stopPlay();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mSideBannerImage != null){
            mSideBannerImage.startPlay();
        }
        if (mSideBannerText != null){
            mSideBannerText.startPlay();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mSideBannerImage != null){
            mSideBannerImage.stopPlay();
        }
        if (mSideBannerText != null){
            mSideBannerText.stopPlay();
        }
        super.onDestroy();
    }
}

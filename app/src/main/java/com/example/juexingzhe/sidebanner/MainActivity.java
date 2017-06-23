package com.example.juexingzhe.sidebanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int[] RES = new int[]{R.mipmap.image1, R.mipmap.image2, R.mipmap.image3};

    private SideBanner mSideBannerImage;
    private SideBanner mSideBannerBuild;
    private LinearLayout mBannerContainer;

    private SideBanner.SideBannerAdapter sideBannerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSideBannerImage = (SideBanner) findViewById(R.id.side_banner_image);
        mBannerContainer = (LinearLayout) findViewById(R.id.banner_container);

        final ArrayList<Integer> datas = new ArrayList();
        for (int i = 0; i < RES.length; i++){
            datas.add(RES[i]);
        }

        initData(datas);

        mSideBannerBuild = new SideBanner.Builder()
                .setSideMode(true)
                .setIndicator(false)
                .setAutoPlay(true)
                .setLoop(true)
                .setDutationTime(3000)
                .setSideBannerAdapter(sideBannerAdapter).build(MainActivity.this);
        mBannerContainer.addView(mSideBannerBuild);

        mSideBannerImage.setSideBannerAdapter(sideBannerAdapter);

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

    private void initData(final List<Integer> datas){
         sideBannerAdapter = new SideBanner.SideBannerAdapter() {
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
        };
    }

    @Override
    protected void onPause() {
        if (mSideBannerImage !=null){
            mSideBannerImage.stopPlay();
        }
        if (mSideBannerBuild != null){
            mSideBannerBuild.stopPlay();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mSideBannerImage != null){
            mSideBannerImage.startPlay();
        }
        if (mSideBannerBuild != null){
            mSideBannerBuild.startPlay();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mSideBannerImage != null){
            mSideBannerImage.stopPlay();
        }
        if (mSideBannerBuild != null){
            mSideBannerBuild.stopPlay();
        }
        super.onDestroy();
    }
}

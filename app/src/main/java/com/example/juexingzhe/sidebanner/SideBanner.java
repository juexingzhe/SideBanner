package com.example.juexingzhe.sidebanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by juexingzhe on 2017/6/13.
 * Done:
 * 1.可设置平常Banner、叠影Banner、平常ViewPager和轮播ViewPager
 * （loop：设置是否可以循环，autoPlay：设置是否自动播放；自动播放需要开启loop）
 * 2.可配置指示点，包括有无、间距等
 * 3.可配置切换速度
 * 4.数据量1个的情况下默认配置非Banner，根据数据动态改变布局，最后显示只类似ImageView
 * 5.可改变ViewPager页片的显示布局，通过SideViewHolder配置，默认提供ImageSideViewHolder
 * TODO:
 * 1.Builder
 * 2.可配置不同布局和数据
 */

public class SideBanner<T> extends RelativeLayout {

    private static final int SCREEN_PAGE_LIMIT = 2;
    private static final int SCROLLER_DURATION_TIME = 800;     //设置切换速度
    private static final int PAGE_MARGIN = 20;
    private static final int DOT_MARGIN = 8;
    private static final int BANNER_DELAY_TIME = 3000;
    private static final int BANNER_MOVE_NEXT_ITEM = 0x102;
    private static final int BANNER_MARGIN = 5;

    //attr
    private boolean mSideMode;
    private boolean mLoop;
    private boolean mAutoPlay;
    private boolean mIndicator;
    private int mPageMargin;
    private int mDotMargin;
    private int mIndicatorBackNormal;
    private int mIndicatorBackSelected;

    private ViewPager mViewPager;
    private LinearLayout mIndicatorContainer;

    private LinkedList<ImageView> mIndicatorList;
    private Handler mHandler;

    private BannerAdapter mBannerAdapter;
    private List<T> mInfos;

    //status
    private int mCurrentPos;
    private boolean isInAutoPaly;

    //Scroller
    private SideBannerScroller mSideBannerScroller;

    //ClickListener
    private BannerClickListener mBannerClickListener;


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
        mAutoPlay = typedArray.getBoolean(R.styleable.SideBanner_autoPlay, true);
        mIndicator = typedArray.getBoolean(R.styleable.SideBanner_indicator, false);
        mPageMargin = typedArray.getDimensionPixelSize(R.styleable.SideBanner_page_margin, PAGE_MARGIN);

        if (mIndicator) {
            mDotMargin = typedArray.getDimensionPixelSize(R.styleable.SideBanner_dot_margin, DOT_MARGIN);
            mIndicatorBackNormal = typedArray.getResourceId(R.styleable.SideBanner_indicator_back_normal, R.drawable.indicator_normal);
            mIndicatorBackSelected = typedArray.getResourceId(R.styleable.SideBanner_indicator_back_selected, R.drawable.indicator_selected);
        }

        if (!mLoop) {//如果不能循环则停止自动banner效果
            mAutoPlay = false;
        }
    }

    private void init() {
        View view;
        mInfos = new ArrayList<>();
        mIndicatorList = new LinkedList<>();

        if (mSideMode) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.side_banner, this, true);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.normal_banner, this, true);
        }

        mViewPager = (ViewPager) view.findViewById(R.id.side_banner_viewpager);
        mViewPager.setOffscreenPageLimit(SCREEN_PAGE_LIMIT);

        if (mIndicator) {
            mIndicatorContainer = (LinearLayout) view.findViewById(R.id.side_banner_indicator);
        }

        setBannerScroll();
        setPageTransformer();
        setPlayHandler();

    }

    public void setSideMode(boolean mSideMode) {
        this.mSideMode = mSideMode;
        if (!mSideMode) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
            layoutParams.leftMargin = BANNER_MARGIN;
            layoutParams.rightMargin = BANNER_MARGIN;
            mViewPager.setLayoutParams(layoutParams);
        }
    }

    public void setLoop(boolean mLoop) {
        this.mLoop = mLoop;
        if (!mLoop) {
            setAutoPlay(false);
        }
    }

    public void setIndicator(boolean mIndicator) {
        this.mIndicator = mIndicator;
    }

    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
    }

    public void setPageMargin(int pageMargin) {
        mPageMargin = pageMargin;
    }

    public void setDotMargin(int dotMargin) {
        mDotMargin = dotMargin;
    }

    public void setPages(List<T> datas, SideViewHolderCreator sideViewHolderCreator) {
        mInfos = datas;

        mBannerAdapter = new BannerAdapter(datas, sideViewHolderCreator);

        if (datas.size() == 1) {
            processDatasSizeLess();
        }

        //ViewPager
        mViewPager.setAdapter(mBannerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setPageMargin(mPageMargin);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPos = position;
                setIndicatorStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Indicator
        initIndicators();
    }

    public void startPlay() {
        if (!mAutoPlay || isInAutoPaly) return;
        if (mHandler == null) {
            setPlayHandler();
        }
        isInAutoPaly = true;
        mHandler.sendEmptyMessageDelayed(BANNER_MOVE_NEXT_ITEM, BANNER_DELAY_TIME);
    }

    public void stopPlay() {
        if (mHandler == null) return;
        isInAutoPaly = false;

        removeAllMessages();
    }

    public void setBannerScrollerSpeed(int time) {
        mSideBannerScroller.setScrollDuration(time);
    }


    public void setBannerClickListener(BannerClickListener bannerClickListener) {
        this.mBannerClickListener = bannerClickListener;
    }


    /**
     * 数据只有1个情况
     */
    private void processDatasSizeLess() {
        setSideMode(false);
        setLoop(false);
        setIndicator(false);
        mBannerAdapter.notifyDataSetChanged();
    }

    private void setIndicatorStatus() {
        if (!mIndicator) return;
        int realPosition = mCurrentPos % mIndicatorContainer.getChildCount();

        for (int i = 0; i < mIndicatorContainer.getChildCount(); i++) {
            mIndicatorList.get(i).setImageResource(mIndicatorBackNormal);
            if (i == realPosition) {
                mIndicatorList.get(i).setImageResource(mIndicatorBackSelected);
            }
        }
    }

    private void initIndicators() {
        if (!mIndicator) return;

        mIndicatorContainer.removeAllViews();
        mIndicatorList.clear();

        for (int i = 0; i < mInfos.size(); i++) {
            ImageView imageView = new ImageView(getContext());

            if (i == mCurrentPos % mInfos.size()) {
                imageView.setImageResource(mIndicatorBackSelected);
            } else {
                imageView.setImageResource(mIndicatorBackNormal);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = mDotMargin;
            imageView.setLayoutParams(layoutParams);

            mIndicatorList.add(imageView);
            mIndicatorContainer.addView(imageView);
        }

    }

    private void setBannerScroll() {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mSideBannerScroller = new SideBannerScroller(mViewPager.getContext());
            mScroller.set(mViewPager, mSideBannerScroller);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void setPageTransformer() {
        if (mSideMode) {
            mViewPager.setPageTransformer(false, new SidePageTransformer());
        }
    }

    private void setPlayHandler() {
        if (mAutoPlay) {
            mHandler = new BannerHandler(mViewPager);
        }
    }

    private void removeAllMessages() {
        if (mHandler == null) return;
        mHandler.removeMessages(BANNER_MOVE_NEXT_ITEM);
    }

    class BannerHandler extends Handler {
        private WeakReference<ViewPager> mWeakReference;

        public BannerHandler(ViewPager viewPager) {
            mWeakReference = new WeakReference(viewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BANNER_MOVE_NEXT_ITEM:
                    isInAutoPaly = true;
                    mCurrentPos++;

                    if (mCurrentPos == mBannerAdapter.getCount() - 1) {
                        mCurrentPos = 0;
                        mWeakReference.get().setCurrentItem(mCurrentPos, false);
                        mHandler.sendEmptyMessageDelayed(BANNER_MOVE_NEXT_ITEM, BANNER_DELAY_TIME);
                    } else {
                        mWeakReference.get().setCurrentItem(mCurrentPos, false);
                        mHandler.sendEmptyMessageDelayed(BANNER_MOVE_NEXT_ITEM, BANNER_DELAY_TIME);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static class SideBannerScroller extends Scroller {

        private int mDuration = SCROLLER_DURATION_TIME;

        public SideBannerScroller(Context context, int duration) {
            this(context);
            mDuration = duration;
        }

        public SideBannerScroller(Context context) {
            super(context);
        }

        public SideBannerScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public SideBannerScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public void setScrollDuration(int mDuration) {
            this.mDuration = mDuration;
        }
    }

    private class BannerAdapter extends PagerAdapter {

        private List<T> mDatas;

        private SideViewHolder mSideViewHolder;

        public BannerAdapter(List<T> datas, SideViewHolderCreator sideViewHolderCreator) {

            mDatas = new ArrayList<>();

            mDatas.addAll(datas);

            if (sideViewHolderCreator == null) {
                mSideViewHolder = new ImageSideViewHolder();
            }
            mSideViewHolder = sideViewHolderCreator.createSideViewHolder();
        }

        @Override
        public int getCount() {
            //return mDatas.size() + (mLoop ? FOR_LOOP : 0);
            return mLoop ? Integer.MAX_VALUE : getRealCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * 获取图片的真实位置
         */
//        private int getRealCount(int position) {
//            if (position == 0) {
//                return mDatas.size() - 1;
//            } else if (position == mDatas.size() + FOR_LOOP - 1) {
//                return 0;
//            }
//            return position - 1;
//        }
        private int getRealCount() {
            return mDatas == null ? 0 : mDatas.size();
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            ImageView imageView = mImageViews.get(position);
//            if (getRealCount(position) > -1 && getRealCount(position) < mDatas.size()) {
//                imageView.setImageResource(mDatas.get(getRealCount(position)));
//            }
//            container.addView(imageView);
//            return imageView;

            View view = getView(container, position);
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
            bindView(container, position);
            container.addView(view);

            return view;
        }

        private View getView(ViewGroup container, final int position) {
            View view = mSideViewHolder.getView(container.getContext());
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBannerClickListener != null) {
                        mBannerClickListener.onPageClick(v, position);
                    }
                }
            });
            return view;
        }

        private void bindView(ViewGroup container, int position) {
            int realPosition = position % getRealCount();
            mSideViewHolder.onBind(container.getContext(), realPosition, mDatas.get(realPosition));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mBannerAdapter == null) return super.dispatchTouchEvent(ev);

        if (!mLoop) return super.dispatchTouchEvent(ev);

//        if (mCurrentPos == 0) {
//            mViewPager.setCurrentItem(mInfos.size(), false);
//        } else if (mCurrentPos == mInfos.size() + 1) {
//            mViewPager.setCurrentItem(1, false);
//        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                removeAllMessages();
                break;
            case MotionEvent.ACTION_UP:
                if (mAutoPlay) {
                    mHandler.sendEmptyMessageDelayed(BANNER_MOVE_NEXT_ITEM, BANNER_DELAY_TIME);
                }
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    public interface BannerClickListener {
        void onPageClick(View view, int position);
    }

    public interface SideBannerAdapter {
        /**
         * 获取数据个数
         *
         * @return
         */
        int getCount();




    }

}

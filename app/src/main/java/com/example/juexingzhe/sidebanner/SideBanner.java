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
import java.util.LinkedList;

/**
 * Created by juexingzhe on 2017/6/13.
 * Done:
 * 1.可设置平常Banner、叠影Banner、平常ViewPager和轮播ViewPager
 * （loop：设置是否可以循环，autoPlay：设置是否自动播放；自动播放需要开启loop）
 * 2.可配置指示点，包括有无、间距等
 * 3.可配置切换速度
 * 4.数据量1个的情况下默认配置非Banner，根据数据动态改变布局，最后显示只类似ImageView
 * 5.可改变ViewPager页片的显示布局，通过SideViewHolder配置，默认提供ImageSideViewHolder
 * 6.起始位置不能设置0，容易滑到头导致没有轮播的效果(要同时更新mCurrentPos，否则直接setCurrentItem会ANR)
 * TODO:
 */

public class SideBanner<T> extends RelativeLayout {

    private static final int SCREEN_PAGE_LIMIT = 2;
    private static final int SCROLLER_DURATION_TIME = 800;     //设置切换速度
    private static final int PAGE_MARGIN = 20;
    private static final int DOT_MARGIN = 8;
    private static final int BANNER_DELAY_TIME = 3000;        //设置轮播速度
    private static final int BANNER_MOVE_NEXT_ITEM = 0x102;
    private static final int NOT_BANNER_MARGIN = 5;

    //attr
    private boolean mSideMode = true;
    private boolean mLoop;
    private boolean mAutoPlay;
    private boolean mIndicator;
    private int mPageMargin;
    private int mDotMargin;
    private int mIndicatorBackNormal;
    private int mIndicatorBackSelected;

    private ViewPager mViewPager;
    private View view;
    private LinearLayout mIndicatorContainer;

    private LinkedList<ImageView> mIndicatorList;
    private Handler mHandler;

    private BannerAdapter mBannerAdapter;

    //status
    private int mCurrentPos;
    private boolean isInAutoPaly;

    //Scroller
    private SideBannerScroller mSideBannerScroller;

    private SideBannerAdapter mSideBannerAdapter;


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
        typedArray.recycle();
    }

    private void init() {
        if (mSideMode) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.side_banner, this, true);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.normal_banner, this, true);
        }

        mViewPager = (ViewPager) view.findViewById(R.id.side_banner_viewpager);
        mViewPager.setOffscreenPageLimit(SCREEN_PAGE_LIMIT);

        if (mIndicator) {
            mIndicatorList = new LinkedList<>();
            mIndicatorContainer = (LinearLayout) view.findViewById(R.id.side_banner_indicator);
        }

        setBannerScroll(SCROLLER_DURATION_TIME);
        setPageTransformer();
        setPlayHandler();
    }

    public void setSideMode(boolean mSideMode) {
        this.mSideMode = mSideMode;
        if (!mSideMode) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
            layoutParams.leftMargin = NOT_BANNER_MARGIN;
            layoutParams.rightMargin = NOT_BANNER_MARGIN;
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
        if (mIndicator && mIndicatorContainer == null) {
            mIndicatorContainer = (LinearLayout) view.findViewById(R.id.side_banner_indicator);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.mAutoPlay = autoPlay;
    }

    public void setPageMargin(int pageMargin) {
        this.mPageMargin = pageMargin;
    }

    public void setDotMargin(int dotMargin) {
        this.mDotMargin = dotMargin;
    }

    public void setIndicatorBackNormal(int indicatorBackNormal){
        this.mIndicatorBackNormal = indicatorBackNormal;
    }

    public void setmIndicatorBackSelected(int indicatorBackSelected){
        this.mIndicatorBackSelected = indicatorBackSelected;
    }

    public void setSideBannerAdapter(SideBannerAdapter sideBannerAdapter) {
        this.mSideBannerAdapter = sideBannerAdapter;
        setPages();
    }

    /**
     * 设置数据、布局与绑定数据
     */
    private void setPages() {
        mBannerAdapter = new BannerAdapter(mSideBannerAdapter.getSideViewHolderCreator());

        if (mSideBannerAdapter.getCount() == 1) {
            processDatasSizeLess();
        }

        initViewPager();

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

    /**
     * 设置手动滑动切换速度
     *
     * @param time
     */
    public void setBannerScrollerSpeed(int time) {
        setBannerScroll(time);
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

    /**
     * 更新指示点
     */
    private void updateIndicatorStatus() {
        if (!mIndicator) return;
        int realPosition = mCurrentPos % mIndicatorContainer.getChildCount();

        for (int i = 0; i < mIndicatorContainer.getChildCount(); i++) {
            mIndicatorList.get(i).setImageResource(mIndicatorBackNormal);
            if (i == realPosition) {
                mIndicatorList.get(i).setImageResource(mIndicatorBackSelected);
            }
        }
    }

    private void initViewPager() {
        //ViewPager
        mViewPager.setAdapter(mBannerAdapter);
        mCurrentPos = mBannerAdapter.getStartItem();
        mViewPager.setCurrentItem(mCurrentPos);

        mViewPager.setPageMargin(mPageMargin);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPos = position;
                updateIndicatorStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initIndicators() {
        if (!mIndicator) return;

        mIndicatorContainer.removeAllViews();
        mIndicatorList.clear();

        for (int i = 0; i < mSideBannerAdapter.getCount(); i++) {
            ImageView imageView = new ImageView(getContext());

            if (i == mCurrentPos % mSideBannerAdapter.getCount()) {
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

    /**
     * 设置Banner轮播速度
     */
    private void setBannerScroll(int time) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mSideBannerScroller = new SideBannerScroller(mViewPager.getContext(), time);
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

    private class BannerHandler extends Handler {
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
        private SideViewHolder mSideViewHolder;

        public BannerAdapter(SideViewHolderCreator sideViewHolderCreator) {
            if (sideViewHolderCreator == null) {
                mSideViewHolder = new ImageSideViewHolder();
                return;
            }
            mSideViewHolder = sideViewHolderCreator.createSideViewHolder();
        }

        @Override
        public int getCount() {
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
            return mSideBannerAdapter.getCount();
        }

        /**
         * 设置起始位置
         */
        private int getStartItem() {
            if (!mLoop) {
                return 0;
            }

            int pos = Integer.MAX_VALUE / 2;
            int remainder = pos % getRealCount();

            return pos - remainder;
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
                    if (mSideBannerAdapter != null) {
                        mSideBannerAdapter.onPageClick(v, position % getRealCount());
                    }
                }
            });
            return view;
        }

        private void bindView(ViewGroup container, int position) {
            int realPosition = position % getRealCount();
            //mSideViewHolder.onBind(container.getContext(), realPosition, mDatas.get(realPosition));
            mSideViewHolder.onBind(container.getContext(), realPosition, mSideBannerAdapter.getItemData(realPosition));
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
                    if (mHandler == null){
                        setPlayHandler();
                    }
                    mHandler.sendEmptyMessageDelayed(BANNER_MOVE_NEXT_ITEM, BANNER_DELAY_TIME);
                }
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    public interface SideBannerAdapter {
        /**
         * 获取数据个数
         *
         * @return
         */
        int getCount();

        /**
         * ClickListener
         *
         * @param view
         * @param position
         */
        void onPageClick(View view, int position);

        /**
         * @return Holder Creator
         */
        SideViewHolderCreator getSideViewHolderCreator();

        /**
         * 单个数据
         *
         * @param position
         * @return
         */
        Object getItemData(int position);

    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{

        private boolean sideMode;
        private boolean loop;
        private boolean autoPlay;
        private boolean indicator;
        private int pageMargin;
        private int dotMargin;
        private int indicatorBackNormal;
        private int indicatorBackSelected;
        private int durationtime;
        private SideBannerAdapter sideBannerAdapter;

        private SideBanner sideBanner;


        public Builder(){
            sideMode = true;
            loop = true;
            autoPlay = true;
            indicator = true;
            pageMargin = PAGE_MARGIN;
            dotMargin = DOT_MARGIN;
            indicatorBackNormal = R.drawable.indicator_normal;
            indicatorBackSelected = R.drawable.indicator_selected;
            durationtime = SCROLLER_DURATION_TIME;
            sideBannerAdapter = null;
        }

        public Builder setSideMode(boolean sideMode) {
            this.sideMode = sideMode;
            return this;
        }

        public Builder setLoop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public Builder setAutoPlay(boolean autoPlay) {
            this.autoPlay = autoPlay;
            return this;
        }

        public Builder setIndicator(boolean indicator) {
            this.indicator = indicator;
            return this;
        }

        public Builder setPageMargin(int pageMargin) {
            this.pageMargin = pageMargin;
            return this;
        }

        public Builder setDotMargin(int dotMargin) {
            this.dotMargin = dotMargin;
            return this;
        }

        public Builder setIndicatorBackNormal(int indicatorBackNormal) {
            this.indicatorBackNormal = indicatorBackNormal;
            return this;
        }

        public Builder setIndicatorBackSelected(int indicatorBackSelected) {
            this.indicatorBackSelected = indicatorBackSelected;
            return this;
        }

        public Builder setDutationTime(int durationtime) {
            this.durationtime = durationtime;
            return this;
        }

        public Builder setSideBannerAdapter(SideBannerAdapter sideBannerAdapter) {
            this.sideBannerAdapter = sideBannerAdapter;
            return this;
        }

        public SideBanner build(Context context){
            sideBanner = new SideBanner(context);
            sideBanner.setSideMode(sideMode);
            sideBanner.setLoop(loop);
            sideBanner.setAutoPlay(autoPlay);
            sideBanner.setIndicator(indicator);
            sideBanner.setPageMargin(pageMargin);
            sideBanner.setDotMargin(dotMargin);
            sideBanner.setIndicatorBackNormal(indicatorBackNormal);
            sideBanner.setmIndicatorBackSelected(indicatorBackSelected);
            sideBanner.setBannerScrollerSpeed(durationtime);
            sideBanner.setSideBannerAdapter(sideBannerAdapter);
            return sideBanner;
        }
    }



}

package com.wei.android.lib.oneactivity.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理滑动返回手势
 */

public class SwipeBackFrameLayout extends FrameLayout {

    private static final int FAST_SWIPE_TIME = 150;         // 认为是快速滑动的时间判断
    private static final int FAST_SWIPE_DISTANCE = 10;      // 认为是快速滑动的距离判断

    private SwipeBackListener mSwipeBackListener;           // 滑动回调

    private int mTouchSlop;                                 // 最小移动距离标准
    private float mActionDownRawX;                          // 手指按下时的记录
    private float mActionDownRawY;                          // 手指按下时的记录
    private boolean mIsBadSwipe = false;                    // 一开始就向左滑动的话，本次滑动作废
    private boolean mIsSwipeAvailable = false;              // 当前滑动符合滑动返回的要求
    private boolean mIsSwiping = false;                     // 当前正在滑动中
    private boolean mIsHandleSwiping = true;                // 是否处理本次滑动
    private long mTempSwipeBeginTime;                       // 标记开始滑动的时间
    private int mFastSwipeDistanceInDp;                     // 认为是快速滑动的距离判断运算值
    private List<View> mHorizonScrollViewList;              // 触摸位置下可以向右滑动的 View 的列表

    public SwipeBackFrameLayout(@NonNull Context context) {
        super(context);
    }

    public SwipeBackFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeBackFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeBackFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSwipeBackListener(SwipeBackListener swipeBackListener) {
        mSwipeBackListener = swipeBackListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        // DOWN 是触发的基础锚点，重置一切
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsBadSwipe = false;
            mIsSwipeAvailable = false;
            mIsSwiping = false;
            mIsHandleSwiping = true;
            mActionDownRawX = event.getRawX();
            mActionDownRawY = event.getRawY();
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            mFastSwipeDistanceInDp = dp2px(FAST_SWIPE_DISTANCE);
            mHorizonScrollViewList = findHorizonScrollViewList(event);
        }

        // 不能在子 View 处理之前就拦截
        super.dispatchTouchEvent(event);

        // MOVE 用来判断是否要符合拦截标准，如果符合标准并成功接连触发 onTouchEvent 则为正确的滑动返回手势
        if (mIsHandleSwiping && !mIsBadSwipe && !mIsSwipeAvailable && !mIsSwiping && event.getAction() == MotionEvent.ACTION_MOVE) {
            float diffX = event.getRawX() - mActionDownRawX;
            if (Math.abs(diffX) >= mTouchSlop) {
                if (diffX > 0) {

                    // 子不用我再用
                    boolean isChildScroll = false;
                    if (mHorizonScrollViewList != null) {
                        for (int i = 0; i < mHorizonScrollViewList.size(); i++) {
                            if (mHorizonScrollViewList.get(i).canScrollHorizontally(-1)) {
                                isChildScroll = true;
                                break;
                            }
                        }
                    }
                    if (!isChildScroll) {
                        float diffY = Math.abs(event.getRawY() - mActionDownRawY);
                        if (diffX > diffY) {
                            mIsSwipeAvailable = true;
                        } else {
                            mIsBadSwipe = true;
                        }
                    }
                } else {
                    mIsBadSwipe = true;
                }
            }
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mIsSwipeAvailable || super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 如果在 dispatchTouchEvent 时已经确认滑动符合要求，就开始分析滑动
        if (mIsSwipeAvailable) {

            // 如果处理，才需要判断
            if (mIsHandleSwiping) {

                // 滑动事件：开始滑动 + 滑动过程变化
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!mIsSwiping) {
                        mIsSwiping = true;
                        mActionDownRawX = event.getRawX();
                        mActionDownRawY = event.getRawY();
                        mTempSwipeBeginTime = System.currentTimeMillis();
                        mIsHandleSwiping = mSwipeBackListener != null && mSwipeBackListener.canSwipeBack();
                        if (mIsHandleSwiping) {
                            requestDisallowInterceptTouchEvent(true);
                        }
                    }

                    if (mIsHandleSwiping) {
                        mSwipeBackListener.onSwipeBackChange((int) (event.getRawX() - mActionDownRawX));
                    }
                }

                // 抬手事件：滑动流程结束
                if (mIsHandleSwiping && event.getAction() == MotionEvent.ACTION_UP && mIsSwiping) {
                    if (mSwipeBackListener != null) {
                        boolean isFastSwipeTime = System.currentTimeMillis() - mTempSwipeBeginTime < FAST_SWIPE_TIME;
                        boolean isFastSwipeDistance = (event.getRawX() - mActionDownRawX) > mFastSwipeDistanceInDp;
                        boolean isHorizontal = (event.getRawX() - mActionDownRawX) > Math.abs(event.getRawY() - mActionDownRawY);
                        mSwipeBackListener.onSwipeBackTouchUp(isFastSwipeTime && isFastSwipeDistance && isHorizontal);
                    }
                }
            }

            // 针对滑动符合要求的情况下的滑动，事件都是需要消费的
            return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 回调告知向右滑了多少位置 px
     */
    public interface SwipeBackListener {

        boolean canSwipeBack();

        void onSwipeBackChange(int moveDistance);

        void onSwipeBackTouchUp(boolean isFastSwipe);
    }

    /**
     * 根据触摸位置，按层级查找接触到的，可以向右滚动的视图列表
     */
    private List<View> findHorizonScrollViewList(MotionEvent motionEvent) {
        Rect rect = new Rect();
        List<View> viewList = new ArrayList<>();
        View topView = getChildAt(getChildCount() - 1);
        findHorizonScrollViewList(topView, viewList, rect, (int) motionEvent.getRawX(), (int) motionEvent.getRawY());
        return viewList;
    }

    /**
     * 根据触摸位置，按层级查找接触到的，可以向右滚动的视图列表，递归查找
     */
    private void findHorizonScrollViewList(View view, List<View> viewList, Rect rect, int x, int y) {
        if (view.getVisibility() == VISIBLE) {
            view.getGlobalVisibleRect(rect);
            if (rect.contains(x, y)) {
                if (view.canScrollHorizontally(-1)) {
                    viewList.add(view);
                }
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = ((ViewGroup) view);
                    int childCount = viewGroup.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        findHorizonScrollViewList(viewGroup.getChildAt(i), viewList, rect, x, y);
                    }
                }
            }
        }
    }

    /**
     * dp 转 px
     */
    public int dp2px(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}

package com.wei.android.lib.oneactivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 解决内嵌滚动事件的冲突，以内层进行最优先的抢占，不需要的话再逐层分发继续抢占，层次越深，优先级越高，越容易抢到
 */

class V_NestedScrollableHost extends FrameLayout {

    private float mRawXOnActionDown;                    // 手指触摸时的落地点位置
    private boolean mIgnoreThisMove = false;            // 当前事件不符合要求
    private boolean mHadCatchTouchEvent = false;        // 触摸事件被我拦截下来啦
    private boolean mCanHandleTouchEvent = true;        // 没被请求拦截的话我可以拦截事件

    public V_NestedScrollableHost(@NonNull Context context) {
        super(context);
    }

    public V_NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public V_NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public V_NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        mCanHandleTouchEvent = !disallowIntercept;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        // 开始触摸时全体重置
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mRawXOnActionDown = event.getRawX();
            mIgnoreThisMove = false;
            mHadCatchTouchEvent = false;
            mCanHandleTouchEvent = true;
        }

        // 需要先让常规流程进行传递传播分发
        boolean result = super.dispatchTouchEvent(event);

        // 分发结束后，最内层这里先执行，不管3721先抢下山头
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mCanHandleTouchEvent) {
                getParent().requestDisallowInterceptTouchEvent(true);
                mHadCatchTouchEvent = true;
                mIgnoreThisMove = false;
            }
            return result;
        }

        // 如果没有被请求不允许拦截，没有自我放弃的话，我只能执行两种操作：发现不需要就放弃，或者别人放弃后自己抢回来
        if (mCanHandleTouchEvent && !mIgnoreThisMove && event.getAction() == MotionEvent.ACTION_MOVE) {
            int diffX = (int) (mRawXOnActionDown - event.getRawX());
            if (diffX != 0) {
                View child = getChildAt(0);
                if (child != null) {
                    boolean isChildCanScroll = child.canScrollHorizontally((int) (mRawXOnActionDown - event.getRawX()));
                    if (isChildCanScroll) {
                        if (!mHadCatchTouchEvent) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mHadCatchTouchEvent = true;
                            mIgnoreThisMove = false;
                        }
                    } else {
                        if (mHadCatchTouchEvent) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                            mHadCatchTouchEvent = false;
                            mIgnoreThisMove = true;
                        }
                    }
                }
            }
        }

        return result;
    }
}

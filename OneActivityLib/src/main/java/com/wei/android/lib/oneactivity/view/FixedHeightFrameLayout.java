package com.wei.android.lib.oneactivity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 固定高度，并感知输入法弹起的高度变化
 */

public class FixedHeightFrameLayout extends FrameLayout {

    private int mLastMeasureHeight;                                 // 最后一次测量的预期高度
    private int mLayoutInitHeight = 0;                              // 应该固化的高度

    private int mLastChangeHeight;                                  // 上次变化的高度差值
    private OnKeyboardChangeListener mOnKeyboardChangeListener;     // 变化的回调监听

    private int mLastParentHeight;                                  // 父层的高度，用来监听是否整体发生变化了
    private OnLayoutChangeListener mOnLayoutChangeListener;         // 父层变化监听

    public FixedHeightFrameLayout(@NonNull Context context) {
        super(context);
    }

    public FixedHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bindParentLayoutChangeListener();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 获取最后的测量高度
        mLastMeasureHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 如果需要固定高度就改变原有参数值
        if (mLayoutInitHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mLayoutInitHeight, MeasureSpec.getMode(heightMeasureSpec));
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        bindParentLayoutChangeListener();

        // 已经开始捕捉了才有效
        if (mLayoutInitHeight > 0) {

            // 如果父布局有变动导致自己和父布局高度不一致，就重置一次
            if (mLastParentHeight != mLayoutInitHeight || getHeight() != ((View) getParent()).getHeight()) {
                mLastParentHeight = mLayoutInitHeight;
                mLastMeasureHeight = 0;
                mLastChangeHeight = 0;
                return;
            }

            // 检测高度变化
            int changeHeight = mLayoutInitHeight - mLastMeasureHeight;
            if (changeHeight != mLastChangeHeight) {
                mLastChangeHeight = changeHeight;
                if (mOnKeyboardChangeListener != null) {
                    mOnKeyboardChangeListener.onKeyboardChange(changeHeight);
                }
            }
        }
    }

    /**
     * 绑定父层高度变化监听，从设计上，就是依赖于父层不会因为键盘弹起而改变
     */
    private void bindParentLayoutChangeListener() {
        if (mOnLayoutChangeListener != null) {
            return;
        }

        View parent = (View) getParent();
        if (parent != null) {
            mOnLayoutChangeListener = new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mLayoutInitHeight = view.getHeight();
                    if (getHeight() != mLayoutInitHeight) {
                        requestLayout();
                    }
                }
            };
            parent.addOnLayoutChangeListener(mOnLayoutChangeListener);
        }
    }

    /**
     * 监听高度变化回调
     */
    public void setOnKeyboardChangeListener(OnKeyboardChangeListener onKeyboardChangeListener) {
        mOnKeyboardChangeListener = onKeyboardChangeListener;
    }

    public interface OnKeyboardChangeListener {
        void onKeyboardChange(int keyboardHeight);
    }
}

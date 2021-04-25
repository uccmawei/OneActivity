package com.wei.android.lib.oneactivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * 对外暴露的底层，已经做好交互封装，底部滑上来的对话框
 */

public abstract class LibBottomDialog extends Page {

    private static final int PAGE_FADE_TIME = 420;      // 渐变动画时长

    private View mViewBackground;                       // 灰色背景
    protected FrameLayout mLayoutContainer;             // 内容归属位置

    private boolean mIsAnimating;                       // 动画执行过程中不允许返回

    public LibBottomDialog(PageActivity pageActivity) {
        super(pageActivity);
        setTranslucentMode(true);
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();

        mPageView.setOnClickListener(this);
    }

    @Override
    protected void onDoCreateView(OnFinishListener listener) {
        RelativeLayout relativeLayout = new RelativeLayout(mPageActivity);
        mViewBackground = new View(mPageActivity);
        mViewBackground.setBackgroundColor(Color.parseColor(PageManager.GRAY_MASK_COLOR_30));
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mViewBackground.setLayoutParams(layoutParams1);
        mLayoutContainer = new FrameLayout(mPageActivity);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mLayoutContainer.setLayoutParams(layoutParams2);
        relativeLayout.addView(mViewBackground);
        relativeLayout.addView(mLayoutContainer);
        Utils.blockAllEvents(mLayoutContainer);
        setPageView(relativeLayout);
        blockTouch(true);
        super.onDoCreateView(new OnFinishListener() {
            @Override
            public void onFinished() {
                if (listener != null) {
                    listener.onFinished();
                }
            }
        });
    }

    @Override
    protected final void onDoShowAnimation(List<Page> pageList, OnFinishListener listener, boolean isNoAnimationMode) {

        // 无动画模式
        if (isNoAnimationMode) {
            return;
        }

        // 动画是手动触发
        if (listener != null) {
            listener.onFinished();
        }
    }

    @Override
    protected final void onDoCancelAnimation(List<Page> pageList, OnFinishListener listener, boolean isNoAnimationMode) {

        // 无动画模式
        if (isNoAnimationMode) {
            return;
        }

        // 开始动画
        mRootView.post(new Runnable() {
            @Override
            public void run() {

                // 背景淡出
                int fadeTime_Background = (int) (PAGE_FADE_TIME * 0.8f);
                Utils.makeDecelerateAnimation(100, 0, fadeTime_Background, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        mViewBackground.setAlpha(animValue / 100.0f);
                    }

                    @Override
                    public void onAnimationEnd() {
                        if (listener != null) {
                            listener.onFinished();
                        }
                    }
                });

                // 内容下滑离开
                int height = mLayoutContainer.getHeight();
                int fadeTime_Content = (int) (PAGE_FADE_TIME * 0.6f);
                Utils.makeDecelerateAnimation(0, height, fadeTime_Content, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        mLayoutContainer.setTranslationY(animValue);
                    }

                    @Override
                    public void onAnimationEnd() {

                    }
                });
            }
        });
    }

    @Override
    protected boolean onBackPressed() {
        return mIsAnimating || super.onBackPressed();
    }

    @Override
    protected void onViewClick(View view) {
        super.onViewClick(view);

        if (view == mPageView) {
            cancel();
        }
    }

    // 显示页面内容
    protected void doShowAnimation() {
        mIsAnimating = true;
        mRootView.post(new Runnable() {
            @Override
            public void run() {

                // 显示，背景淡入
                mRootView.setVisibility(View.VISIBLE);
                Utils.makeDecelerateAnimation(0, 100, PAGE_FADE_TIME, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        mViewBackground.setAlpha(animValue / 100.0f);
                    }

                    @Override
                    public void onAnimationEnd() {
                        blockTouch(false);
                        mIsAnimating = false;
                    }
                });

                // 内容上滑进入
                int fadeTime = (int) (PAGE_FADE_TIME * 0.9f);
                Utils.makeDecelerateAnimation(mLayoutContainer.getHeight(), 0, fadeTime, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        mLayoutContainer.setTranslationY(animValue);
                    }

                    @Override
                    public void onAnimationEnd() {

                    }
                });
            }
        });
    }
}

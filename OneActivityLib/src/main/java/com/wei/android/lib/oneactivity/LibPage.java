package com.wei.android.lib.oneactivity;

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 对外暴露的底层，已经做好交互封装
 */

public abstract class LibPage extends Page {

    private static final int PAGE_SWIPE_TIME = 400;         // Page 页面切换动画时长
    private static final float PAGE_MOVE_RATE = 0.25f;      // 上层 Page 页面切换时底下 Page 的移动偏移率

    protected View mViewBackground;                         // 切换时灰色背景
    protected FrameLayout mLayoutContainer;                 // 再次封装的业务容器

    public LibPage(PageActivity pageActivity) {
        super(pageActivity);
    }

    @Override
    protected void onDoCreateView(OnFinishListener listener) {
        FrameLayout frameLayout = new FrameLayout(mPageActivity);
        mViewBackground = new View(mPageActivity);
        mViewBackground.setBackgroundColor(Color.parseColor(PageManager.GRAY_MASK_COLOR_20));
        mLayoutContainer = new FrameLayout(mPageActivity);
        frameLayout.addView(mViewBackground);
        frameLayout.addView(mLayoutContainer);
        setPageView(frameLayout);
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
            mRootView.setVisibility(View.VISIBLE);
            return;
        }

        // 单个页面不走动画
        if (pageList.size() <= 1) {
            mRootView.setVisibility(View.VISIBLE);
            if (listener != null) {
                listener.onFinished();
            }
            return;
        }

        // 正常左右切换动画
        final List<Page> tempPausePageList = new ArrayList<>();
        for (int i = pageList.size() - 2; i >= 0; i--) {
            tempPausePageList.add(pageList.get(i));
            if (!pageList.get(i).isTranslucentMode()) {
                break;
            }
        }

        // 开始动画
        mRootView.post(new Runnable() {
            @Override
            public void run() {

                // 恢复可见
                mRootView.setVisibility(View.VISIBLE);

                // 开始动画
                final int width = getRootPageContainer().getWidth();
                Utils.makeDecelerateAnimation(width, 0, PAGE_SWIPE_TIME, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        for (int i = 0; i < tempPausePageList.size(); i++) {
                            tempPausePageList.get(i).mRootView.setTranslationX(-PAGE_MOVE_RATE * (width - animValue));
                        }
                        mLayoutContainer.setTranslationX(animValue);
                        mViewBackground.setAlpha((1.0f * (width - animValue)) / width);
                    }

                    @Override
                    public void onAnimationEnd() {
                        if (listener != null) {
                            listener.onFinished();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected final void onDoCancelAnimation(List<Page> pageList, OnFinishListener listener, boolean isNoAnimationMode) {

        // 无动画模式
        if (isNoAnimationMode) {
            return;
        }

        // 单个页面不走动画
        if (pageList.size() <= 1) {
            if (listener != null) {
                listener.onFinished();
            }
            return;
        }

        // 正常左右切换动画
        final List<Page> tempResumePageList = new ArrayList<>();
        for (int i = pageList.size() - 2; i >= 0; i--) {
            tempResumePageList.add(pageList.get(i));
            if (!pageList.get(i).isTranslucentMode()) {
                break;
            }
        }

        // 开始动画
        mRootView.post(new Runnable() {
            @Override
            public void run() {

                // 开始动画
                final int width = getRootPageContainer().getWidth();
                Utils.makeDecelerateAnimation(0, width, PAGE_SWIPE_TIME, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        for (int i = 0; i < tempResumePageList.size(); i++) {
                            tempResumePageList.get(i).mRootView.setTranslationX(-PAGE_MOVE_RATE * (width - animValue));
                        }
                        mLayoutContainer.setTranslationX(animValue);
                        mViewBackground.setAlpha((1.0f * (width - animValue)) / width);
                    }

                    @Override
                    public void onAnimationEnd() {
                        if (listener != null) {
                            listener.onFinished();
                        }
                    }
                });
            }
        });
    }
}

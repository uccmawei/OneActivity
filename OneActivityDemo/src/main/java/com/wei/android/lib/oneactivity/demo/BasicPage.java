package com.wei.android.lib.oneactivity.demo;

import android.view.View;
import android.view.ViewGroup;

import com.wei.android.lib.oneactivity.listener.OnFinishListener;
import com.wei.android.lib.oneactivity.page.Page;
import com.wei.android.lib.oneactivity.page.PageActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicPage extends Page {

    private static final int PAGE_SWIPE_TIME = 600;         // Page 页面切换动画时长
    private static final float PAGE_MOVE_RATE = 0.25f;      // 上层 Page 页面切换时底下 Page 的移动偏移率

    private View mViewBackground;                           // 切换时灰色背景
    private ViewGroup mLayoutContainer;                     // 再次封装的业务容器

    public BasicPage(PageActivity pageActivity) {
        super(pageActivity);
    }

    protected abstract int getLayoutRes();

    @Override
    protected final void onDoCreateView(OnFinishListener listener) {
        Utils.inflate(mPageActivity, R.layout.basic_page, new Utils.OnInflateListener() {
            @Override
            public void onInflateFinished(View view) {
                mPageView = view;
                mViewBackground = view.findViewById(R.id.mViewBackground);
                mLayoutContainer = view.findViewById(R.id.mLayoutContainer);
                Utils.inflate(mPageActivity, getLayoutRes(), new Utils.OnInflateListener() {
                    @Override
                    public void onInflateFinished(View view) {
                        mLayoutContainer.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                        if (listener != null) {
                            listener.onFinished();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected final void onDoShowAnimation(List<Page> pageList, OnFinishListener listener, boolean isNoAnimationMode) {
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

        // 先不显示，等会儿走动画
        mPageView.setVisibility(View.GONE);

        // 正常左右切换动画
        final List<Page> tempPausePageList = new ArrayList<>();
        for (int i = pageList.size() - 2; i >= 0; i--) {
            tempPausePageList.add(pageList.get(i));
            if (!pageList.get(i).isTranslucentMode()) {
                break;
            }
        }

        // 开始左右切换的动画
        mPageView.post(new Runnable() {
            @Override
            public void run() {
                mPageView.setVisibility(View.VISIBLE);

                final int width = getFloatContainer().getWidth();
                Utils.makeAnimation(width, 0, PAGE_SWIPE_TIME, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        for (int i = 0; i < tempPausePageList.size(); i++) {
                            tempPausePageList.get(i).mPageView.setTranslationX(-PAGE_MOVE_RATE * (width - animValue));
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

        mPageView.post(new Runnable() {
            @Override
            public void run() {
                final int width = getFloatContainer().getWidth();
                Utils.makeAnimation(0, width, PAGE_SWIPE_TIME, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        for (int i = 0; i < tempResumePageList.size(); i++) {
                            tempResumePageList.get(i).mPageView.setTranslationX(-PAGE_MOVE_RATE * (width - animValue));
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

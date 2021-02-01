package com.wei.android.lib.oneactivity.demo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import com.wei.android.lib.oneactivity.listener.OnFinishListener;
import com.wei.android.lib.oneactivity.page.Page;
import com.wei.android.lib.oneactivity.page.PageActivity;

import java.util.List;

public abstract class BasicDialog extends Page {

    private static final int PAGE_FADE_TIME = 400;      // 渐变动画时长

    private View mViewBackground;                       // 灰色背景
    private ViewGroup mLayoutContainer;                 // 内容归属位置
    private Space mSpace;                               // 键盘弹起占位

    public BasicDialog(PageActivity pageActivity) {
        super(pageActivity);
        setTranslucentMode(true);
    }

    protected abstract int getLayoutRes();

    @Override
    protected void onPageInit() {
        super.onPageInit();

        mPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    @Override
    protected void onPagePause() {
        super.onPagePause();
        handleKeyboardHeightChange(0);
    }

    @Override
    protected final void onDoCreateView(OnFinishListener listener) {
        Utils.inflate(mPageActivity, R.layout.basic_dialog, new Utils.OnInflateListener() {
            @Override
            public void onInflateFinished(View view) {
                mPageView = view;
                mViewBackground = view.findViewById(R.id.mViewBackground);
                mLayoutContainer = view.findViewById(R.id.mLayoutContainer);
                mSpace = view.findViewById(R.id.mSpace);
                Utils.inflate(mPageActivity, getLayoutRes(), new Utils.OnInflateListener() {
                    @Override
                    public void onInflateFinished(View view) {
                        mLayoutContainer.addView(view);
                        view.setClickable(true);
                        view.setFocusable(true);
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

        mPageView.setVisibility(View.GONE);
        mPageView.post(new Runnable() {
            @Override
            public void run() {

                // 淡入缩放动画，底下渐变
                mPageView.setVisibility(View.VISIBLE);
                Utils.makeAnimation(0, 100, PAGE_FADE_TIME, new Utils.PageAnimationListener() {
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

                // 淡入缩放动画，内容渐变和缩放
                int fadeTime = (int) (PAGE_FADE_TIME * 0.9f);
                Utils.makeAnimation(0, 100, fadeTime, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        float alphaValue = animValue / 100.0f;
                        mLayoutContainer.setAlpha(alphaValue);

                        float scaleValue = 0.93f + 0.07f * alphaValue;
                        mLayoutContainer.setScaleX(scaleValue);
                        mLayoutContainer.setScaleY(scaleValue);
                    }

                    @Override
                    public void onAnimationEnd() {

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

        mPageView.post(new Runnable() {
            @Override
            public void run() {

                // 淡出缩放动画，底下渐变
                int fadeTime_Background = (int) (PAGE_FADE_TIME * 0.7f);
                Utils.makeAnimation(100, 0, fadeTime_Background, new Utils.PageAnimationListener() {
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

                // 淡入缩放动画，内容渐变和缩放
                int fadeTime_Content = (int) (PAGE_FADE_TIME * 0.3f);
                Utils.makeAnimation(100, 0, fadeTime_Content, new Utils.PageAnimationListener() {
                    @Override
                    public void onAnimationUpdate(int from, int to, int animValue) {
                        float alphaValue = animValue / 100.0f;
                        mLayoutContainer.setAlpha(alphaValue);

                        float scaleValue = 0.95f + 0.05f * alphaValue;
                        mLayoutContainer.setScaleX(scaleValue);
                        mLayoutContainer.setScaleY(scaleValue);
                    }

                    @Override
                    public void onAnimationEnd() {

                    }
                });
            }
        });
    }

    @Override
    protected final void onKeyboardChange(int keyboardHeight) {
        super.onKeyboardChange(keyboardHeight);

        if (isPageOnActive()) {
            handleKeyboardHeightChange(keyboardHeight);
        }
    }

    // 处理键盘高度变化
    private void handleKeyboardHeightChange(int keyboardHeight) {
        Utils.makeAnimation(mSpace.getHeight(), keyboardHeight, 300, new Utils.PageAnimationListener() {
            @Override
            public void onAnimationUpdate(int from, int to, int animValue) {
                Utils.setViewHeight(mSpace, animValue);
            }

            @Override
            public void onAnimationEnd() {

            }
        });
    }
}

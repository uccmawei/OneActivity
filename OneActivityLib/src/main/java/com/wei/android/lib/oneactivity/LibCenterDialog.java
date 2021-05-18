package com.wei.android.lib.oneactivity;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.List;

/**
 * 对外暴露的底层，已经做好交互封装，中间弹出的对话框
 */

public abstract class LibCenterDialog extends Page {

    private boolean mCancelableByClickOutside = true;      // 点击空白处可以关闭
    private boolean mCancelableByOnBackPressed = true;     // 返回按钮可以关闭

    private static final int PAGE_FADE_TIME = 300;          // 渐变动画时长

    private View mViewBackground;                           // 灰色背景
    protected LinearLayout mLayoutContainer;                // 内容归属位置
    private Space mSpace;                                   // 键盘弹起占位

    private boolean mIsAnimating;                           // 动画执行过程中不允许返回

    public LibCenterDialog(PageActivity pageActivity) {
        super(pageActivity);
        setTranslucentMode(true);
    }

    public LibCenterDialog setCancelableByClickOutside(boolean cancelableByClickOutside) {
        mCancelableByClickOutside = cancelableByClickOutside;
        return this;
    }

    public LibCenterDialog setCancelableByOnBackPressed(boolean cancelableByOnBackPressed) {
        mCancelableByOnBackPressed = cancelableByOnBackPressed;
        return this;
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();

        mPageView.setOnClickListener(this);
    }

    @Override
    protected void onPagePause() {
        super.onPagePause();
        Utils.handleKeyboardChange(mSpace, 0);
    }

    @Override
    protected void onDoCreateView(OnFinishListener listener) {
        FrameLayout frameLayout = new FrameLayout(mPageActivity);
        mViewBackground = new View(mPageActivity);
        mViewBackground.setBackgroundColor(Color.parseColor(PageManager.GRAY_MASK_COLOR_30));
        LinearLayout linearLayout = new LinearLayout(mPageActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        Space space1 = new Space(mPageActivity);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams1.weight = 1;
        space1.setLayoutParams(layoutParams1);
        mLayoutContainer = new LinearLayout(mPageActivity);
        mLayoutContainer.setOrientation(LinearLayout.VERTICAL);
        mLayoutContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutContainer.setLayoutParams(layoutParams2);
        Space space3 = new Space(mPageActivity);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams3.weight = 1;
        space3.setLayoutParams(layoutParams3);
        mSpace = new Space(mPageActivity);
        mSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        linearLayout.addView(space1);
        linearLayout.addView(mLayoutContainer);
        linearLayout.addView(space3);
        linearLayout.addView(mSpace);
        frameLayout.addView(mViewBackground);
        frameLayout.addView(linearLayout);
        setPageView(frameLayout);
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
            mRootView.setVisibility(View.VISIBLE);
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

                // 内容淡出
                int fadeTime_Content = (int) (PAGE_FADE_TIME * 0.3f);
                Utils.makeDecelerateAnimation(100, 0, fadeTime_Content, new Utils.PageAnimationListener() {
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
    protected boolean onBackPressed() {
        return mIsAnimating || !mCancelableByOnBackPressed || super.onBackPressed();
    }

    @Override
    protected final void onKeyboardChange(int keyboardHeight) {
        super.onKeyboardChange(keyboardHeight);

        if (isPageOnActive()) {
            Utils.handleKeyboardChange(mSpace, keyboardHeight);
        }
    }

    @Override
    protected void onViewClick(View view) {
        super.onViewClick(view);

        if (view == mPageView && mCancelableByClickOutside) {
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

                // 内容淡入
                int fadeTime = (int) (PAGE_FADE_TIME * 0.9f);
                Utils.makeDecelerateAnimation(0, 100, fadeTime, new Utils.PageAnimationListener() {
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
}


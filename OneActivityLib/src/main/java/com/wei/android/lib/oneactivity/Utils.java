package com.wei.android.lib.oneactivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 * 工具类
 */

class Utils {

    /**
     * 自动绑定视图
     */
    public static void bindView(Object object, View view) {
        try {
            Field[] fieldArray = object.getClass().getDeclaredFields();
            for (Field field : fieldArray) {
                BindView bindView = field.getAnnotation(BindView.class);
                if (bindView != null) {
                    field.setAccessible(true);
                    field.set(object, view.findViewById(bindView.value()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 状态栏沉浸式
     */
    public static void setImmersiveMode(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 关闭键盘
     */
    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 拦截所有事件
     */
    public static void blockAllEvents(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    /**
     * 处理键盘弹起
     */
    public static void handleKeyboardChange(View mSpace, int keyboardHeight) {
        makeDecelerateAnimation(mSpace.getHeight(), keyboardHeight, 300, new PageAnimationListener() {
            @Override
            public void onAnimationUpdate(int from, int to, int animValue) {
                setViewHeight(mSpace, animValue);
            }

            @Override
            public void onAnimationEnd() {

            }
        });
    }

    /**
     * 设置高度
     */
    public static void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    /**
     * 简化动画
     */
    public static void makeDecelerateAnimation(final int from, final int to, long duration, final PageAnimationListener pageAnimationListener) {
        doAnimation(from, to, duration, new DecelerateInterpolator(2.0f), pageAnimationListener);
    }

    /**
     * 简化动画
     */
    private static void doAnimation(final int from, final int to, long duration, TimeInterpolator interpolator, final PageAnimationListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (listener != null) {
                    listener.onAnimationUpdate(from, to, (Integer) animation.getAnimatedValue());
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
        });
        animator.start();
    }

    /**
     * 简化动画回调
     */
    public interface PageAnimationListener {

        void onAnimationUpdate(int from, int to, int animValue);

        void onAnimationEnd();
    }
}

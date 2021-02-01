package com.wei.android.lib.oneactivity.page;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.wei.android.lib.oneactivity.annotation.BindView;

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
     * 简化动画
     */
    public static void makeAnimation(final int from, final int to, long duration, final Utils.PageAnimationListener pageAnimationListener) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator(2.0f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (pageAnimationListener != null) {
                    pageAnimationListener.onAnimationUpdate(from, to, (Integer) animation.getAnimatedValue());
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (pageAnimationListener != null) {
                    pageAnimationListener.onAnimationEnd();
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

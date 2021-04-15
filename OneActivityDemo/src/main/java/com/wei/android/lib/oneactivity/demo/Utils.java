package com.wei.android.lib.oneactivity.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;

public class Utils {

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取导航栏高度
     */
    public static int getNavigationBarHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return ((realSize.y - size.y) > (height - 10)) ? height : 0;
    }

    /**
     * 唤起键盘
     */
    public static void showKeyboard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            editText.requestFocus();
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 关闭键盘
     */
    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getWindow().getCurrentFocus();
            if (inputMethodManager != null && view != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
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

    /**
     * 简化异步实例化
     */
    public static void inflate(Context context, int layoutRes, OnInflateListener listener) {
        new AsyncLayoutInflater(context).inflate(layoutRes, null, new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resId, @Nullable ViewGroup parent) {
                if (listener != null) {
                    listener.onInflateFinished(view);
                }
            }
        });
    }

    /**
     * 简化异步实例化回调
     */
    public interface OnInflateListener {
        void onInflateFinished(View view);
    }
}

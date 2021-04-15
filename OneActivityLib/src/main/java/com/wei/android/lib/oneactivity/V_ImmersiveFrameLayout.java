package com.wei.android.lib.oneactivity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 默认开启 setFitsSystemWindows 会导致有额外的 padding，所以这里需要强制都改成 0 即可实现整个屏幕都是 APP 内容
 */

class V_ImmersiveFrameLayout extends FrameLayout {

    public V_ImmersiveFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public V_ImmersiveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public V_ImmersiveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public V_ImmersiveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setFitsSystemWindows(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setPadding(0, 0, 0, getPaddingBottom());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

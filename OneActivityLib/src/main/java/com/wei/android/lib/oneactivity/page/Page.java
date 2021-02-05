package com.wei.android.lib.oneactivity.page;

import android.widget.FrameLayout;

import com.wei.android.lib.oneactivity.listener.OnFinishListener;

import java.util.List;

/**
 * 单页面型
 */

public abstract class Page extends BasicPage {

    protected Page mThis;                           // 小丑竟然是我自己

    private boolean mIsPageShowed;                  // 防止多次调用
    private boolean mIsPageCanceled;                // 防止多次调用

    private Object mResultData;                     // 传递数据
    private boolean mIsTranslucentMode = false;     // 透明模式

    public Page(PageActivity pageActivity) {
        super(pageActivity);
        mThis = this;
    }

    /**
     * 把当前 Page 显示到前台
     */
    public void show() {
        mPageActivity.getPageManager().show(this);
    }

    /**
     * 把当前 Page 显示到前台，并关闭之前的全部
     */
    public void showAndClearAll() {
        mPageActivity.getPageManager().showAndClearAll(this);
    }

    /**
     * 把当前 Page 关闭
     */
    public void cancel() {
        mPageActivity.getPageManager().cancel(this);
    }

    /**
     * 获取存放悬浮窗页面的空间
     */
    protected FrameLayout getFloatContainer() {
        return mPageActivity.getPageManager().getFloatContainer();
    }

    /**
     * 获取当前页面存储的需要返回到上层页面的数据
     */
    protected Object getResultData() {
        return mResultData;
    }

    /**
     * 存储当前需要返回给上层页面的数据
     */
    protected void setResultData(Object resultData) {
        mResultData = resultData;
    }

    /**
     * 透明模式不走动画
     */
    public boolean isTranslucentMode() {
        return mIsTranslucentMode;
    }

    /**
     * 透明模式不走动画
     */
    protected void setTranslucentMode(boolean translucentMode) {
        if (isPageInit()) {
            throw new IllegalStateException(getPageLogName() + " can't setTranslucentMode after page init");
        }

        mIsTranslucentMode = translucentMode;
    }

    /**
     * 设定 Tab 模式
     */
    protected void createTabHelper(int innerPageContainerId) {
        mTabHelper = new TabHelper(this, innerPageContainerId);
    }

    /**
     * 触发滑动返回的确认操作
     * 返回 false 代表不可以滑动返回，返回 true 代表可以滑动返回
     */
    protected boolean canSwipeBack() {
        return true;
    }

    /**
     * 键盘弹起收下时的高度变化监听
     */
    protected void onKeyboardChange(int keyboardHeight) {

    }

    /**
     * Page 需要自己实现视图的动画
     */
    protected void onDoShowAnimation(List<Page> pageList, OnFinishListener listener, boolean isNoAnimationMode) {
        if (listener != null) {
            listener.onFinished();
        }
    }

    /**
     * Page 需要自己实现视图的动画
     */
    protected void onDoCancelAnimation(List<Page> pageList, OnFinishListener listener, boolean isNoAnimationMode) {
        if (listener != null) {
            listener.onFinished();
        }
    }

    // --------------------------  标记位  --------------------------

    public boolean isPageShowed() {
        return mIsPageShowed;
    }

    void setPageShowed() {
        mIsPageShowed = true;
    }

    public boolean isPageCanceled() {
        return mIsPageCanceled;
    }

    void setPageCanceled() {
        mIsPageCanceled = true;
    }
}

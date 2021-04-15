package com.wei.android.lib.oneactivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

/**
 * 底层
 */

abstract class PageWrapper implements View.OnClickListener {

    public final PageActivity mPageActivity;        // 底层 Activity
    public final FrameLayout mRootView;             // 页面的底层容器

    protected View mPageView;                       // 业务 View
    protected TabHelper mTabHelper;                 // 适配 Tab 模式
    private View mTouchInterceptor;                 // 触摸拦截

    private boolean mIsPageInit;                    // 防止多次调用
    private boolean mIsPageStart;                   // 防止多次调用
    private boolean mIsPageOnActive;                // 当前是活跃状态
    private boolean mIsPagePause;                   // 防止多次调用
    private boolean mIsPageStop;                    // 防止多次调用
    private boolean mIsPageDestroy;                 // 防止多次调用

    /**
     * 默认构造方法
     */
    protected PageWrapper(PageActivity pageActivity) {
        mPageActivity = pageActivity;
        mRootView = new FrameLayout(mPageActivity);
        mRootView.setBackgroundColor(Color.TRANSPARENT);
        mRootView.setVisibility(View.INVISIBLE);
        Utils.blockAllEvents(mRootView);
    }

    /**
     * 为了避免在 new 对象的时候就初始化，所以不建议把逻辑放在构造方法中
     * <p>
     * 建议把初始化代码放在 onPageInit 方法中，由 PageManager 统一调用触发
     * <p>
     * Page 在压入栈之前，执行动画之前回调
     */
    protected void onPageInit() {
        System.out.println("PAGE_LIFE_LOG onPageInit " + getPageLogName());
    }

    /**
     * 在 pageManager 把 page 压入栈，页面切换动画执行完成后触发
     */
    protected void onPageStart() {
        if (mTabHelper != null) {
            mTabHelper.onPageStart();
        }

        System.out.println("PAGE_LIFE_LOG onPageStart " + getPageLogName());
    }

    /**
     * 在 page 页面恢复到可见时回调，跟 Activity 不同的是，在 onPageStart 后不会调用此方法
     */
    protected void onPageResume(Object resultData) {
        if (mTabHelper != null) {
            mTabHelper.onPageResume(resultData);
        }

        System.out.println("PAGE_LIFE_LOG onPageResume " + getPageLogName());
    }

    /**
     * 在 page 页面被其他 Page 覆盖时回调
     */
    protected void onPagePause() {
        if (mTabHelper != null) {
            mTabHelper.onPagePause();
        }

        System.out.println("PAGE_LIFE_LOG onPagePause " + getPageLogName());
    }

    /**
     * 在 page 页面被其他 Page 完全覆盖时回调
     */
    protected void onPageStop() {
        if (mTabHelper != null) {
            mTabHelper.onPageStop();
        }

        System.out.println("PAGE_LIFE_LOG onPageStop " + getPageLogName());
    }

    /**
     * 在 page 页面完全销毁时触发
     */
    protected void onPageDestroy() {
        if (mTabHelper != null) {
            mTabHelper.onPageDestroy();
        }

        System.out.println("PAGE_LIFE_LOG onPageDestroy " + getPageLogName());
    }

    /**
     * 按返回键的时候触发
     */
    protected boolean onBackPressed() {
        return mTabHelper != null && mTabHelper.onBackPressed();
    }

    /**
     * 触摸拦截
     */
    protected void blockTouch(boolean visible) {
        if (visible) {
            if (mTouchInterceptor == null) {
                mTouchInterceptor = new View(mPageActivity);
                Utils.blockAllEvents(mTouchInterceptor);
                mRootView.addView(mTouchInterceptor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            mTouchInterceptor.bringToFront();
            mTouchInterceptor.setVisibility(View.VISIBLE);
        } else {
            if (mTouchInterceptor != null) {
                mTouchInterceptor.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 拦截 onPageInit 的多次响应，返回 true 代表成功挂入该状态
     */
    boolean callOnPageInit() {
        if (!mIsPageInit && !mIsPageStart && !mIsPageOnActive && !mIsPagePause && !mIsPageStop && !mIsPageDestroy) {
            mIsPageInit = true;
            onPageInit();
            return true;
        }

        return false;
    }

    /**
     * 拦截 onPageStart 的多次响应，返回 true 代表成功挂入该状态
     */
    boolean callOnPageStart() {
        if (mIsPageInit && !mIsPageStart && !mIsPageOnActive && !mIsPagePause && !mIsPageStop && !mIsPageDestroy) {
            mIsPageStart = true;
            mIsPageOnActive = true;
            onPageStart();
            return true;
        }

        return false;
    }

    /**
     * 拦截 onPageResume 的多次响应，返回 true 代表成功挂入该状态
     */
    boolean callOnPageResume(Object resultData) {
        if (mIsPageInit && mIsPageStart && !mIsPageOnActive && (mIsPagePause || mIsPageStop) && !mIsPageDestroy) {
            mIsPageOnActive = true;
            mIsPagePause = false;
            mIsPageStop = false;
            onPageResume(resultData);
            return true;
        }

        return false;
    }

    /**
     * 拦截 onPagePause 的多次响应，返回 true 代表成功挂入该状态
     */
    boolean callOnPagePause() {
        if (mIsPageInit && mIsPageStart && mIsPageOnActive && !mIsPagePause && !mIsPageStop && !mIsPageDestroy) {
            mIsPageOnActive = false;
            mIsPagePause = true;
            onPagePause();
            return true;
        }

        return false;
    }

    /**
     * 拦截 onPageStop 的多次响应，返回 true 代表成功挂入该状态
     */
    boolean callOnPageStop() {
        if (mIsPageInit && mIsPageStart && !mIsPageOnActive && mIsPagePause && !mIsPageStop && !mIsPageDestroy) {
            mIsPageStop = true;
            onPageStop();
            return true;
        }

        return false;
    }

    /**
     * 拦截 onPageDestroy 的多次响应，返回 true 代表成功挂入该状态
     */
    boolean callOnPageDestroy() {
        if (!mIsPageDestroy) {
            mIsPageDestroy = true;
            onPageDestroy();
            return true;
        }

        return false;
    }

    protected void setPageView(View pageView) {
        mPageView = pageView;
        mRootView.addView(pageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * Page 需要自己实现视图的创建
     */
    protected void onDoCreateView(OnFinishListener listener) {
        if (listener != null) {
            listener.onFinished();
        }
    }

    /**
     * Page 需要自己实现视图查找
     */
    protected <T extends View> T findViewById(int id) {
        return mPageView.findViewById(id);
    }

    /**
     * 获取字符串
     */
    protected String getString(int resId) {
        return mPageActivity.getString(resId);
    }

    /**
     * 获取字符串
     */
    protected String getString(int resId, Object... formatArgs) {
        return mPageActivity.getString(resId, formatArgs);
    }

    /**
     * 获取 Drawable
     */
    protected Drawable getDrawable(int resId) {
        return ContextCompat.getDrawable(mPageActivity, resId);
    }

    /**
     * 设定 Tab 模式
     */
    protected void createTabHelper(int innerPageContainerId) {
        mTabHelper = new TabHelper(this, innerPageContainerId);
    }

    @Override
    public final void onClick(View view) {
        if (!FastClickUtils.isFastClick()) {
            onViewClick(view);
        }
    }

    /**
     * 自带点击效果
     */
    protected void onViewClick(View view) {

    }

    // -------------------- 状态获取判断 --------------------

    public boolean isPageInit() {
        return mIsPageInit;
    }

    public boolean isPageStart() {
        return mIsPageStart;
    }

    public boolean isPageOnActive() {
        return mIsPageOnActive;
    }

    public boolean isPagePause() {
        return mIsPagePause;
    }

    public boolean isPageStop() {
        return mIsPageStop;
    }

    // -------------------- 额外底层方便 --------------------

    public String getPageLogName() {
        return getClass().getName();
    }
}

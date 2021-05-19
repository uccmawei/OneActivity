package com.wei.android.lib.oneactivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理 Page 栈
 */

class PageManager {

    public static final String GRAY_MASK_COLOR_20 = "#4C000000";    // 滑动返回页面遮罩层 20% 透明度黑色
    public static final String GRAY_MASK_COLOR_30 = "#66000000";    // 对话框背景

    private static final int SWIPE_ANIMATION_DURATION = 300;        // Page 页面切换动画时长
    private static final float SWIPE_MOVE_RATE = 0.25f;             // 上层 Page 页面切换时底下 Page 的移动偏移率

    private final PageActivity mPageActivity;                       // 根 Activity
    private final List<Page> mPageList;                             // Page 栈

    private V_SwipeBackFrameLayout mPageListContainer;              // 堆放 Page 页面的容器，并实现了滑动返回反馈机制
    private View mPageTouchInterceptor;                             // 拦截 Page 切换中的时候的触摸事件
    private View mGrayMaskView;                                     // 灰色遮罩，滑动返回用

    private boolean mIsFirstTimeOnResume = true;                    // 我们忽略 Activity 的第一次启动的 onResume 回调

    private boolean mIsSwiping;                                     // 正在滑
    private Page mTempSwipingPage;                                  // 正在滑动返回的 Page
    private List<Page> mTempSwipeBackToResumePageList;              // 滑动返回时，可以看得到的后面的 Page 列表
    private int mTempMoveDistance;                                  // 最后一次滑动的移动距离

    private boolean mIsInBusy;                                      // 页面栈正忙
    private List<PageTask> mPageTaskList;                           // 暂存的 Page 对象列表
    private List<PageTask> mPageTaskListDoing;                      // 暂存的 Page 对象列表，正在处理中的

    protected PageManager(PageActivity pageActivity) {
        mPageActivity = pageActivity;
        mPageList = new ArrayList<>();

        initLifecycle();
        initActivityTheme();
        initBasicViewLayoutSystem();
    }

    /**
     * 从 Activity 回调
     */
    private void onActivityLifeResume() {
        Page topPage = getTopPage();
        if (topPage != null) {
            topPage.callOnPageResume(null);
        }
    }

    /**
     * 从 Activity 回调
     */
    private void onActivityLifePause() {
        Page topPage = getTopPage();
        if (topPage != null) {
            topPage.callOnPagePause();
        }
    }

    /**
     * 从 Activity 回调
     */
    private void onActivityLifeStop() {
        Page topPage = getTopPage();
        if (topPage != null) {
            topPage.callOnPagePause();
            topPage.callOnPageStop();
        }
    }

    /**
     * 从 Activity 回调
     */
    private void onActivityLifeDestroy() {
        if (mPageList != null) {
            for (int i = 0; i < mPageList.size(); i++) {
                mPageList.get(i).callOnPagePause();
                mPageList.get(i).callOnPageStop();
                mPageList.get(i).callOnPageDestroy();
            }
        }
    }

    /**
     * 从 Activity 回调，返回 true 表示消费拦截，false 的话就让 Activity 处理
     */
    protected void onBackPressed() {
        handleOnBackPressed();
    }

    /**
     * 监听 Activity 的生命周期
     */
    private void initLifecycle() {
        mPageActivity.getLifecycle().addObserver(new ActivityLifeObserver());
    }

    /**
     * 监听 Activity 的生命周期
     */
    private class ActivityLifeObserver implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        private void onActivityResume() {
            if (mIsFirstTimeOnResume) {
                mIsFirstTimeOnResume = false;
                return;
            }
            onActivityLifeResume();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        private void onActivityPause() {
            onActivityLifePause();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        private void onActivityStop() {
            onActivityLifeStop();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        private void onActivityDestroy() {
            onActivityLifeDestroy();
        }
    }

    /**
     * 修改 Activity 样式
     */
    private void initActivityTheme() {
        Utils.setImmersiveMode(mPageActivity.getWindow());
    }

    /**
     * 初始化底层自带布局体系
     * <p>
     * 代码创建视图，确保代码流程是同步，并且代码比 XML 读取快得多，可维护性嘛，只能牺牲一些改动最小的部分了
     */
    private void initBasicViewLayoutSystem() {
        V_ImmersiveFrameLayout immersiveFrameLayout = new V_ImmersiveFrameLayout(mPageActivity);        // 沉浸式
        V_FixedHeightFrameLayout fixedHeightFrameLayout = new V_FixedHeightFrameLayout(mPageActivity);  // 高度锁定
        mPageTouchInterceptor = new View(mPageActivity);                                                // 触摸交互拦截层
        mGrayMaskView = new View(mPageActivity);                                                        // 灰色遮罩
        mPageListContainer = new V_SwipeBackFrameLayout(mPageActivity);                                 // 滑动返回

        // 默认效果
        mPageTouchInterceptor.setAlpha(0.0f);
        Utils.blockAllEvents(mPageTouchInterceptor);
        mPageTouchInterceptor.setVisibility(View.GONE);
        mGrayMaskView.setBackgroundColor(Color.parseColor(GRAY_MASK_COLOR_20));
        mGrayMaskView.setVisibility(View.GONE);

        // 层次绑定
        fixedHeightFrameLayout.addView(mPageListContainer);
        fixedHeightFrameLayout.addView(mGrayMaskView);
        fixedHeightFrameLayout.addView(mPageTouchInterceptor);

        // 塞入 Activity 中
        immersiveFrameLayout.addView(fixedHeightFrameLayout);
        mPageActivity.setContentView(immersiveFrameLayout);
        mPageActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 绑定键盘弹起的处理逻辑
        fixedHeightFrameLayout.setOnKeyboardChangeListener(new V_FixedHeightFrameLayout.OnKeyboardChangeListener() {
            @Override
            public void onKeyboardChange(int keyboardHeight) {
                handleKeyboardChange(keyboardHeight);
            }
        });

        // 绑定滑动返回交互逻辑
        mPageListContainer.setSwipeBackListener(new V_SwipeBackFrameLayout.SwipeBackListener() {
            @Override
            public boolean canSwipeBack() {
                return handleSwipeBackBegin();
            }

            @Override
            public void onSwipeBackChange(int moveDistance) {
                handleSwipeBackChange(moveDistance);
            }

            @Override
            public void onSwipeBackTouchUp(boolean isFastSwipe) {
                handleSwipeBackTouchUp(isFastSwipe);
            }
        });
    }

    /**
     * 获取尺寸
     */
    protected int getPageWindowWidth() {
        return mPageListContainer.getWidth();
    }

    /**
     * 获取尺寸
     */
    protected int getPageWindowHeight() {
        return mPageListContainer.getHeight();
    }

    /**
     * 打开一个 Page
     */
    protected void show(Page page) {
        doPageShow(page, false);
    }

    /**
     * 关闭一个 Page
     */
    protected void cancel(Page page) {
        doPageCancel(page, false, false);
    }

    /**
     * 关闭所有并打开新的
     */
    protected void showAndClearAll(Page page) {
        doPageShow(page, false);
        for (int i = mPageList.size() - 1; i >= 0; i--) {
            if (mPageList.get(i) != page) {
                doPageCancel(mPageList.get(i), false, false);
            }
        }
    }

    /**
     * 展示新的 Page
     */
    private void doPageShow(Page pageToShow, boolean isBusyTask) {
        if (pageToShow == null) {
            throw new IllegalStateException("doPageShow() - NullPointException");
        }

        if (pageToShow.isPageShowed()) {
            throw new IllegalStateException("doPageShow() - Don't show again");
        }

        // 忙碌拦截
        if (!isBusyTask) {
            if (busyCheck(pageToShow, false)) {
                return;
            }
            mIsInBusy = true;
        }

        // 取消焦点和键盘
        clearFocusAndHideKeyboard();

        // 打断手势返回
        interruptSwipeBackIfNeeded();

        // 标记位
        pageToShow.setPageShowed();

        // 开启动画拦截，防止动画期间交互重叠触发
        mPageTouchInterceptor.setVisibility(View.VISIBLE);

        // 获取当前顶部 Page ，它将要被覆盖
        Page pageToPause = getTopPage();
        if (pageToPause != null) {
            pageToPause.callOnPagePause();
        }

        // 创建视图
        pageToShow.onDoCreateView(new OnFinishListener() {
            @Override
            public void onFinished() {

                // 绑定
                Utils.bindView(pageToShow, pageToShow.mRootView);
                pageToShow.callOnPageInit();

                // 入队
                mPageList.add(pageToShow);
                mPageListContainer.addView(pageToShow.mRootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                // 无动画模式
                if (isBusyTask) {
                    pageToShow.onDoShowAnimation(mPageList, null, true);
                    doPageShowDone(pageToShow);
                    return;
                }

                // 进场动画
                pageToShow.onDoShowAnimation(mPageList, new OnFinishListener() {
                    @Override
                    public void onFinished() {
                        doPageShowDone(pageToShow);
                    }
                }, false);
            }
        });
    }

    /**
     * 展示新的 Page
     */
    private void doPageShowDone(Page pageToShow) {

        // 启动
        pageToShow.callOnPageStart();

        // 新的 Page 不是对话框并且不是透明背景的话才能回调底层的 Page 的 onPageStop
        if (!pageToShow.isTranslucentMode()) {
            for (int i = mPageList.size() - 1; i >= 0; i--) {
                if (mPageList.get(i) != pageToShow) {
                    mPageList.get(i).mRootView.setVisibility(View.GONE);
                    mPageList.get(i).callOnPageStop();
                    if (!mPageList.get(i).isTranslucentMode()) {
                        break;
                    }
                }
            }
        }

        // 处理挂起任务
        handleBusyTask();
    }

    /**
     * 关闭当前的 Page
     */
    private void doPageCancel(Page pageToCancel, boolean isBusyTask, boolean isSwipeToCancel) {
        if (pageToCancel == null) {
            throw new IllegalStateException("doPageCancel() - NullPointException");
        }

        if (pageToCancel.isPageCanceled()) {
            throw new IllegalStateException("doPageCancel() - Don't cancel again");
        }

        // 忙碌拦截
        if (!isBusyTask) {
            if (busyCheck(pageToCancel, true)) {
                return;
            }
            mIsInBusy = true;
        }

        // 最后一个 Page 与 Activity 共存亡
        if (mPageList.size() <= 1) {
            mPageActivity.finish();
            return;
        }

        // 是否为最顶层，最顶层才取消焦点，和打断手势返回
        boolean isTopPage = getTopPage() == pageToCancel;
        if (isTopPage) {
            clearFocusAndHideKeyboard();
            interruptSwipeBackIfNeeded();
        }

        // 标记位
        pageToCancel.setPageCanceled();

        // 获取 Page 的视图
        Page pageToResume = mPageList.get(mPageList.size() - 2);

        // 开启动画拦截
        mPageTouchInterceptor.setVisibility(View.VISIBLE);

        // 将被关闭的 Page 回调一下
        pageToCancel.callOnPagePause();

        // 恢复可见视图
        for (int i = mPageList.size() - 2; i >= 0; i--) {
            mPageList.get(i).mRootView.setVisibility(View.VISIBLE);
            if (!mPageList.get(i).isTranslucentMode()) {
                break;
            }
        }

        // 无动画模式
        if (!isTopPage || isBusyTask || isSwipeToCancel) {
            pageToCancel.onDoCancelAnimation(mPageList, null, true);
            doPageCancelDone(pageToCancel, pageToResume, isTopPage);
            return;
        }

        // 关闭页面动画
        pageToCancel.onDoCancelAnimation(mPageList, new OnFinishListener() {
            @Override
            public void onFinished() {
                doPageCancelDone(pageToCancel, pageToResume, true);
            }
        }, false);
    }

    /**
     * 页面关闭完成后回调
     */
    private void doPageCancelDone(Page pageToCancel, Page pageToResume, boolean isTopPage) {

        // 回调生命周期
        pageToCancel.callOnPageStop();

        // 移除视图和列表
        mPageListContainer.removeView(pageToCancel.mRootView);
        mPageList.remove(pageToCancel);

        // 将要被恢复的 Page 恢复啦
        if (isTopPage) {
            if (pageToResume != null) {
                pageToResume.callOnPageResume(pageToCancel.getResultData());
            }
        }

        // 将被关闭的 Page 回调一下
        pageToCancel.callOnPageDestroy();

        // 处理挂起任务
        handleBusyTask();
    }

    /**
     * 页面正忙判断拦截
     */
    private boolean busyCheck(Page page, boolean isToCancel) {
        if (mIsInBusy) {
            if (mPageTaskList == null) {
                mPageTaskList = new ArrayList<>();
            }
            mPageTaskList.add(new PageTask(page, isToCancel));
            return true;
        }

        return false;
    }

    /**
     * 处理忙碌期间的挂起任务
     */
    private void handleBusyTask() {

        // 无挂起任务，取消触摸拦截，恢复标记位
        if (mPageTaskListDoing == null || mPageTaskListDoing.isEmpty()) {
            if (mPageTaskList == null || mPageTaskList.isEmpty()) {
                mPageTouchInterceptor.setVisibility(View.GONE);
                mIsInBusy = false;
                return;
            }

            // 取出挂起任务
            mPageTaskListDoing = mPageTaskList;
            mPageTaskList = new ArrayList<>();
        }

        // 按顺序处理
        PageTask pageTask = mPageTaskListDoing.remove(0);
        if (pageTask.isToCancel) {
            doPageCancel(pageTask.mPage, true, false);
        } else {
            doPageShow(pageTask.mPage, true);
        }
    }

    /**
     * 处理返回事件
     */
    private void handleOnBackPressed() {
        if (mPageTouchInterceptor.getVisibility() == View.VISIBLE) {
            return;
        }

        if (mPageList == null || mPageList.isEmpty()) {
            return;
        }

        // 优先让顶层的 Page 处理返回事件
        int pageSize = mPageList.size();
        Page topPage = mPageList.get(pageSize - 1);
        if (topPage.onBackPressed()) {
            return;
        }

        // 不是最后一个 Page 的话就关闭 Page
        if (pageSize > 1) {
            cancel(topPage);
            return;
        }

        // 否则就不处理，让 Activity 直接一起关闭
        mPageActivity.finish();
    }

    /**
     * 处理页面高度发生变化时的回调
     */
    private void handleKeyboardChange(int keyboardHeight) {
        for (int i = 0; i < mPageList.size(); i++) {
            mPageList.get(i).onKeyboardChange(keyboardHeight);
        }
    }

    /**
     * 滑动返回开始，捕捉相关的页面视图
     */
    private boolean handleSwipeBackBegin() {

        // 顶层 Page
        Page topPage = getTopPage();
        if (topPage == null) {
            return false;
        }

        // 透明模式 Page 不能滑动返回
        if (topPage.isTranslucentMode()) {
            return false;
        }

        // 最后一个 Page 不能滑动返回
        if (mPageList.size() <= 1) {
            mTempSwipingPage = null;
            mTempSwipeBackToResumePageList = null;
            return false;
        }

        // 获取顶层要滑动返回的 Page
        if (!topPage.canSwipeBack()) {
            return false;
        }

        // 剔除焦点收起键盘
        clearFocusAndHideKeyboard();

        // 滑动开始后就只能滑动啦，不能再点击啦
        mPageTouchInterceptor.setVisibility(View.VISIBLE);

        // 获取滑动的 Page 和相关视图
        mTempSwipingPage = topPage;
        mIsSwiping = true;

        // 滑动返回的话，底下的可见 Page 需要显示出来可见
        mTempSwipeBackToResumePageList = new ArrayList<>();
        for (int i = mPageList.size() - 2; i >= 0; i--) {
            mPageList.get(i).mRootView.setVisibility(View.VISIBLE);
            mTempSwipeBackToResumePageList.add(mPageList.get(i));
            if (!mPageList.get(i).isTranslucentMode()) {
                break;
            }
        }

        return true;
    }

    /**
     * 滑动返回中
     */
    private void handleSwipeBackChange(int moveDistance) {
        if (mTempSwipingPage == null || mTempSwipeBackToResumePageList == null) {
            return;
        }

        if (!mIsSwiping) {
            return;
        }

        if (moveDistance < 0) {
            moveDistance = 0;
        }

        mTempMoveDistance = moveDistance;
        updatePageViewBySwipeMove(moveDistance);
    }

    /**
     * 滑动返回手势抬起，要么恢复，要么触发关闭页面
     */
    private void handleSwipeBackTouchUp(boolean isFastSwipe) {
        if (mTempSwipingPage == null || mTempSwipeBackToResumePageList == null) {
            return;
        }

        if (!mIsSwiping) {
            return;
        }

        // 判断是否需要滑动返回确认，获取页面宽度
        int width = mTempSwipingPage.getPageWindowWidth();
        boolean doSwipeToCancelPage = isFastSwipe || mTempMoveDistance > (width / 3.0f);

        // 恢复界面，看下是否需要触发确认回调
        if (!doSwipeToCancelPage) {
            Utils.makeDecelerateAnimation(mTempMoveDistance, 0, SWIPE_ANIMATION_DURATION, new Utils.PageAnimationListener() {
                @Override
                public void onAnimationUpdate(int from, int to, int animValue) {
                    updatePageViewBySwipeMove(animValue);
                }

                @Override
                public void onAnimationEnd() {
                    for (int i = 0; i < mTempSwipeBackToResumePageList.size(); i++) {
                        mTempSwipeBackToResumePageList.get(i).mRootView.setVisibility(View.GONE);
                    }

                    mIsSwiping = false;
                    mGrayMaskView.setVisibility(View.GONE);
                    mPageTouchInterceptor.setVisibility(View.GONE);
                }
            });
            return;
        }

        // 关闭当前页面，将被关闭的 Page 回调一下
        Utils.makeDecelerateAnimation(mTempMoveDistance, width, SWIPE_ANIMATION_DURATION, new Utils.PageAnimationListener() {
            @Override
            public void onAnimationUpdate(int from, int to, int animValue) {
                updatePageViewBySwipeMove(animValue);
            }

            @Override
            public void onAnimationEnd() {
                mIsSwiping = false;
                mGrayMaskView.setVisibility(View.GONE);
                doPageCancel(mTempSwipingPage, false, true);
            }
        });
    }

    /**
     * 根据手势滑动距离刷新页面内容移动
     */
    private void updatePageViewBySwipeMove(int moveDistance) {
        if (mTempSwipingPage == null) {
            return;
        }

        mGrayMaskView.setVisibility(View.VISIBLE);
        int width = mTempSwipingPage.getPageWindowWidth();
        mTempSwipingPage.mRootView.setTranslationX(moveDistance);
        mGrayMaskView.setTranslationX(moveDistance - width);
        mGrayMaskView.setAlpha((1.0f * (width - moveDistance)) / width);
        for (int i = 0; i < mTempSwipeBackToResumePageList.size(); i++) {
            mTempSwipeBackToResumePageList.get(i).mRootView.setTranslationX(SWIPE_MOVE_RATE * (moveDistance - width));
        }
    }

    /**
     * 打断滑动返回操作，如果正在滑动的话
     */
    private void interruptSwipeBackIfNeeded() {
        if (!mIsSwiping) {
            return;
        }

        mIsSwiping = false;
        updatePageViewBySwipeMove(0);

        if (mTempSwipeBackToResumePageList != null) {
            for (int i = 0; i < mTempSwipeBackToResumePageList.size(); i++) {
                mTempSwipeBackToResumePageList.get(i).mRootView.setVisibility(View.GONE);
            }
        }

        mGrayMaskView.setVisibility(View.GONE);
    }

    /**
     * 清除当前焦点，隐藏键盘
     */
    private void clearFocusAndHideKeyboard() {
        View currentFocusView = mPageActivity.getWindow().getCurrentFocus();
        if (currentFocusView != null) {
            Utils.hideKeyboard(currentFocusView);
            currentFocusView.clearFocus();
        }
    }

    /**
     * 获取栈顶的 Page
     */
    private Page getTopPage() {
        if (mPageList == null || mPageList.isEmpty()) {
            return null;
        }

        return mPageList.get(mPageList.size() - 1);
    }

    /**
     * 事件分发
     */
    protected void sendEvent(Object eventObject) {
        if (mPageList != null) {
            for (int i = 0; i < mPageList.size(); i++) {
                mPageList.get(i).onReceivedEvent(eventObject);
            }
        }
    }

    /**
     * 暂存需要操作的 Page 以及他们的目标方法
     */
    private static class PageTask {

        Page mPage;
        boolean isToCancel;

        public PageTask(Page page, boolean isToCancel) {
            mPage = page;
            this.isToCancel = isToCancel;
        }
    }
}

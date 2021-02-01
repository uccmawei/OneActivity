package com.wei.android.lib.oneactivity.page;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.wei.android.lib.oneactivity.listener.OnFinishListener;
import com.wei.android.lib.oneactivity.view.NestedScrollableHost;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一实现 Tab 模式
 */

public class TabHelper {

    private final BasicPage mPage;
    private final ViewPager2 mViewPager2;
    private final List<InnerPage> mInnerPageList;
    private final InnerPageAdapter mInnerPageAdapter;

    private InnerPage mCurrentInnerPage;
    private OnInnerPageChangeListener mOnInnerPageChangeListener;

    public TabHelper(BasicPage page, int innerPageContainerId) {
        mPage = page;
        mViewPager2 = new ViewPager2(mPage.mPageActivity);
        mInnerPageList = new ArrayList<>();
        mInnerPageAdapter = new InnerPageAdapter();
        mViewPager2.setOffscreenPageLimit(5);
        mViewPager2.setAdapter(mInnerPageAdapter);
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                callInnerPageOnPageStartOrResume(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        NestedScrollableHost nestedScrollableHost = new NestedScrollableHost(mPage.mPageActivity);
        nestedScrollableHost.addView(mViewPager2);

        ((ViewGroup) mPage.mPageView.findViewById(innerPageContainerId)).addView(nestedScrollableHost);
    }

    /**
     * 从 Page 回调
     */
    protected void onPageStart() {
        if (!mInnerPageList.isEmpty()) {
            callInnerPageOnPageStartOrResume(mViewPager2.getCurrentItem());
        }
    }

    /**
     * 从 Page 回调
     */
    protected void onPageResume(Object resultData) {
        if (mCurrentInnerPage != null) {
            mCurrentInnerPage.callOnPageResume(resultData);
        }
    }

    /**
     * 从 Page 回调
     */
    protected void onPagePause() {
        if (mCurrentInnerPage != null) {
            mCurrentInnerPage.callOnPagePause();
        }
    }

    /**
     * 从 Page 回调
     */
    protected void onPageStop() {
        if (mCurrentInnerPage != null) {
            mCurrentInnerPage.callOnPageStop();
        }
    }

    /**
     * 从 Page 回调
     */
    protected void onPageDestroy() {
        if (!mInnerPageList.isEmpty()) {
            for (int i = 0; i < mInnerPageList.size(); i++) {
                mInnerPageList.get(i).callOnPageDestroy();
            }
        }
    }

    /**
     * 从 Page 回调
     */
    protected boolean onBackPressed() {
        if (mCurrentInnerPage != null) {
            return mCurrentInnerPage.onBackPressed();
        }
        return false;
    }

    /**
     * 设置一个 InnerPage
     */
    public void setInnerPage(InnerPage innerPage) {
        killAllPage();
        mInnerPageList.clear();
        mInnerPageList.add(innerPage);
        mInnerPageAdapter.notifyDataSetChanged();
    }

    /**
     * 设置一个 InnerPage 列表
     */
    public void setInnerPageList(List<? extends InnerPage> innerPageList) {
        killAllPage();
        mInnerPageList.clear();
        mInnerPageList.addAll(innerPageList);
        mInnerPageAdapter.notifyDataSetChanged();
    }

    /**
     * 设置当前显示哪个 InnerPage
     */
    public void setCurrentPage(int position, boolean smoothScroll) {
        if (position >= 0 && position < mInnerPageList.size()) {
            mViewPager2.setCurrentItem(position, smoothScroll);
        }
    }

    /**
     * 监听 InnerPage 变化
     */
    public void setOnInnerPageChangeListener(OnInnerPageChangeListener onInnerPageChangeListener) {
        mOnInnerPageChangeListener = onInnerPageChangeListener;
    }

    /**
     * 设置是否可以滑动切换
     */
    public void setScrollable(boolean scrollable) {
        mViewPager2.setUserInputEnabled(scrollable);
    }

    /**
     * 滑动切换或加载完成，调用，启动，暂停，恢复
     */
    private void callInnerPageOnPageStartOrResume(int targetPosition) {

        // 口号要对应上
        int index = mViewPager2.getCurrentItem();
        if (index != targetPosition || index < 0 || index >= mInnerPageList.size()) {
            return;
        }

        // 载体都还没启动你急啥
        if (!mPage.isPageStart()) {
            return;
        }

        // 先暂停运行中的
        if (mCurrentInnerPage != null) {
            mCurrentInnerPage.callOnPagePause();
            mCurrentInnerPage.callOnPageStop();
        }

        // 抓到哪吒了
        InnerPage innerPage = mInnerPageList.get(index);

        // 未初始化，不能启动
        if (!innerPage.isPageInit()) {
            return;
        }

        // 未启动，就先启动
        if (!innerPage.isPageStart()) {
            if (innerPage.callOnPageStart()) {
                mCurrentInnerPage = innerPage;
                if (mOnInnerPageChangeListener != null) {
                    mOnInnerPageChangeListener.onInnerPageChange(targetPosition);
                }
            }
            return;
        }

        // 恢复已经暂停的
        if (innerPage.isPagePause() || innerPage.isPageStop()) {
            if (innerPage.callOnPageResume(null)) {
                mCurrentInnerPage = innerPage;
                if (mOnInnerPageChangeListener != null) {
                    mOnInnerPageChangeListener.onInnerPageChange(targetPosition);
                }
            }
        }
    }

    /**
     * InnerPage 适配器
     */
    private class InnerPageAdapter extends RecyclerView.Adapter<InnerPageViewHolder> {

        @NonNull
        @Override
        public InnerPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new InnerPageViewHolder(frameLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull InnerPageViewHolder holder, int position) {
            ViewGroup viewGroup = (ViewGroup) holder.itemView;
            viewGroup.removeAllViews();
            viewGroup.setTag(position);
            initInnerPageView(viewGroup, position);
        }

        // 实例化 InnerPage
        private void initInnerPageView(final ViewGroup viewGroup, final int position) {

            // 已经实例化的直接加进去
            if (mInnerPageList.get(position).isPageInit()) {
                if ((int) viewGroup.getTag() == position) {
                    viewGroup.addView(mInnerPageList.get(position).mPageView);
                    callInnerPageOnPageStartOrResume(position);
                }
                return;
            }

            // 实例化视图，绑定，塞入，生命周期
            mInnerPageList.get(position).onDoCreateView(new OnFinishListener() {
                @Override
                public void onFinished() {

                    // 拦截点击穿透
                    mInnerPageList.get(position).mPageView.setClickable(true);
                    mInnerPageList.get(position).mPageView.setFocusable(true);

                    // 绑定
                    Utils.bindView(mInnerPageList.get(position), mInnerPageList.get(position).mPageView);
                    mInnerPageList.get(position).callOnPageInit();
                    if ((int) viewGroup.getTag() == position) {
                        viewGroup.addView(mInnerPageList.get(position).mPageView);
                    }

                    callInnerPageOnPageStartOrResume((int) viewGroup.getTag());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mInnerPageList.size();
        }
    }

    /**
     * InnerPage 容器
     */
    private static class InnerPageViewHolder extends RecyclerView.ViewHolder {

        public InnerPageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /**
     * 清空当前页面
     */
    private void killAllPage() {
        for (int i = 0; i < mInnerPageList.size(); i++) {
            mInnerPageList.get(i).callOnPagePause();
            mInnerPageList.get(i).callOnPageStop();
            mInnerPageList.get(i).callOnPageDestroy();
        }
    }

    /**
     * 回调
     */
    public interface OnInnerPageChangeListener {

        void onInnerPageChange(int currentIndex);
    }
}

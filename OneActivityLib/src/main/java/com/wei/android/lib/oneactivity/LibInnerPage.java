package com.wei.android.lib.oneactivity;

import android.widget.FrameLayout;

public abstract class LibInnerPage extends InnerPage {

    protected FrameLayout mLayoutContainer;             // 页面视图容器

    protected LibInnerPage(PageActivity pageActivity) {
        super(pageActivity);
    }

    @Override
    protected void onDoCreateView(OnFinishListener listener) {
        mLayoutContainer = new FrameLayout(mPageActivity);
        setPageView(mLayoutContainer);
        super.onDoCreateView(new OnFinishListener() {
            @Override
            public void onFinished() {
                if (listener != null) {
                    listener.onFinished();
                }
            }
        });
    }
}

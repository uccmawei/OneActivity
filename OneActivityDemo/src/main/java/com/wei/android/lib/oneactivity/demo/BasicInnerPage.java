package com.wei.android.lib.oneactivity.demo;

import android.view.View;
import android.view.ViewGroup;

import com.wei.android.lib.oneactivity.listener.OnFinishListener;
import com.wei.android.lib.oneactivity.page.InnerPage;
import com.wei.android.lib.oneactivity.page.Page;

public abstract class BasicInnerPage extends InnerPage {

    public BasicInnerPage(Page page) {
        super(page);
    }

    protected abstract int getLayoutRes();

    @Override
    protected final void onDoCreateView(OnFinishListener listener) {
        Utils.inflate(mPageActivity, getLayoutRes(), new Utils.OnInflateListener() {
            @Override
            public void onInflateFinished(View view) {
                mPageView = view;
                mRootView.addView(mPageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (listener != null) {
                    listener.onFinished();
                }
            }
        });
    }
}

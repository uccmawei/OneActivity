package com.wei.android.lib.oneactivity.demo;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.wei.android.lib.oneactivity.LibPage;
import com.wei.android.lib.oneactivity.OnFinishListener;
import com.wei.android.lib.oneactivity.PageActivity;

public abstract class BasicPage extends LibPage {

    public BasicPage(PageActivity pageActivity) {
        super(pageActivity);
    }

    protected abstract int getLayoutRes();

    @Override
    protected final void onDoCreateView(OnFinishListener listener) {
        super.onDoCreateView(new OnFinishListener() {
            @Override
            public void onFinished() {
                Utils.inflate(mPageActivity, getLayoutRes(), new Utils.OnInflateListener() {
                    @Override
                    public void onInflateFinished(View view) {
                        mLayoutContainer.setBackgroundColor(Color.WHITE);
                        mLayoutContainer.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                        if (listener != null) {
                            listener.onFinished();
                        }
                    }
                });
            }
        });
    }
}

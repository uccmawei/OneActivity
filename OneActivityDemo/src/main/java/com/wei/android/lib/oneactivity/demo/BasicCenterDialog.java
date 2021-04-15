package com.wei.android.lib.oneactivity.demo;

import android.view.View;
import android.view.ViewGroup;

import com.wei.android.lib.oneactivity.LibCenterDialog;
import com.wei.android.lib.oneactivity.OnFinishListener;
import com.wei.android.lib.oneactivity.PageActivity;

public abstract class BasicCenterDialog extends LibCenterDialog {

    public BasicCenterDialog(PageActivity pageActivity) {
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
                        mLayoutContainer.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        Utils.blockAllEvents(view);
                        if (listener != null) {
                            listener.onFinished();
                        }
                    }
                });
            }
        });
    }
}

package com.wei.android.lib.oneactivity.demo;

import android.view.View;

import com.wei.android.lib.oneactivity.PageActivity;

public class TestDialog extends BasicCenterDialog {

    public TestDialog(PageActivity pageActivity) {
        super(pageActivity);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.test_dialog;
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();
        doShowAnimation();

        Utils.showKeyboard(findViewById(R.id.mEditText));
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();

        findViewById(R.id.mLayoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }
}

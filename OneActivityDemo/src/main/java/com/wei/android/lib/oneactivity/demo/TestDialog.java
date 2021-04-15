package com.wei.android.lib.oneactivity.demo;

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
}

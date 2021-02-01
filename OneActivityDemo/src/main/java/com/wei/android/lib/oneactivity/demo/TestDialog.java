package com.wei.android.lib.oneactivity.demo;

import com.wei.android.lib.oneactivity.page.PageActivity;

public class TestDialog extends BasicDialog {

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

        Utils.showKeyboard(findViewById(R.id.mEditText));
    }
}

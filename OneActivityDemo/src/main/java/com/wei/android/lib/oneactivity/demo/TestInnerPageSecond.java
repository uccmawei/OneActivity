package com.wei.android.lib.oneactivity.demo;

import android.widget.TextView;

import com.wei.android.lib.oneactivity.annotation.BindView;
import com.wei.android.lib.oneactivity.page.Page;

public class TestInnerPageSecond extends BasicInnerPage {

    @BindView(R.id.mTvText)
    private TextView mTvText;

    private final String mText;

    public TestInnerPageSecond(Page page, String text) {
        super(page);
        mText = text;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.test_inner_page_second;
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();
        mTvText.setText(mText);
    }

    @Override
    public String getPageLogName() {
        return super.getPageLogName() + " " + mText;
    }
}

package com.wei.android.lib.oneactivity.demo;

import android.widget.TextView;

import com.wei.android.lib.oneactivity.BindView;

public class TestInnerPageSecond extends BasicInnerPage {

    @BindView(R.id.mTvText)
    private TextView mTvText;

    private BasicPage mBasicPage;
    private final String mText;

    public TestInnerPageSecond(BasicPage basicPage, String text) {
        super(basicPage.mPageActivity);
        mBasicPage = basicPage;
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

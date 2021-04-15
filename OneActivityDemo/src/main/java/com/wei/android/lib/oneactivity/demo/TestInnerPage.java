package com.wei.android.lib.oneactivity.demo;

import android.widget.TextView;

import com.wei.android.lib.oneactivity.BindView;

import java.util.ArrayList;
import java.util.List;

public class TestInnerPage extends BasicInnerPage {

    @BindView(R.id.mTvText)
    private TextView mTvText;

    private BasicPage mBasicPage;
    private final String mText;

    protected TestInnerPage(BasicPage basicPage, String text) {
        super(basicPage.mPageActivity);
        mBasicPage = basicPage;
        mText = text;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.test_inner_page;
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();
        mTvText.setText(mText);

        createTabHelper(R.id.mLayoutInnerPageContainer);
        List<BasicInnerPage> innerPageList = new ArrayList<>();
        innerPageList.add(new TestInnerPageSecond(mBasicPage, "World_001"));
        innerPageList.add(new TestInnerPageSecond(mBasicPage, "World_002"));
        mTabHelper.setInnerPageList(innerPageList);
    }

    @Override
    public String getPageLogName() {
        return super.getPageLogName() + " " + mText;
    }
}

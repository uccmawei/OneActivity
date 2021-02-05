package com.wei.android.lib.oneactivity.demo;

import android.widget.TextView;

import com.wei.android.lib.oneactivity.annotation.BindView;
import com.wei.android.lib.oneactivity.page.InnerPage;
import com.wei.android.lib.oneactivity.page.Page;

import java.util.ArrayList;
import java.util.List;

public class TestInnerPage extends BasicInnerPage {

    @BindView(R.id.mTvText)
    private TextView mTvText;

    private final String mText;

    public TestInnerPage(Page page, String text) {
        super(page);
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
        List<InnerPage> innerPageList = new ArrayList<>();
        innerPageList.add(new TestInnerPageSecond(mRootPage, "World_001"));
        innerPageList.add(new TestInnerPageSecond(mRootPage, "World_002"));
        mTabHelper.setInnerPageList(innerPageList);
    }

    @Override
    public String getPageLogName() {
        return super.getPageLogName() + " " + mText;
    }
}

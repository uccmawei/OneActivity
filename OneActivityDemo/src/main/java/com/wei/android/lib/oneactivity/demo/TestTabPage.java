package com.wei.android.lib.oneactivity.demo;

import com.wei.android.lib.oneactivity.page.InnerPage;
import com.wei.android.lib.oneactivity.page.PageActivity;

import java.util.ArrayList;
import java.util.List;

public class TestTabPage extends BasicPage {

    public TestTabPage(PageActivity pageActivity) {
        super(pageActivity);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.test_tab_page;
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();

        createTabHelper(R.id.mLayoutInnerPageContainer);
        List<InnerPage> innerPageList = new ArrayList<>();
        innerPageList.add(new TestInnerPage(mSelf, "Hello_001"));
        innerPageList.add(new TestInnerPage(mSelf, "Hello_002"));
        mTabHelper.setInnerPageList(innerPageList);
    }
}

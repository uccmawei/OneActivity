package com.wei.android.lib.oneactivity.page;

/**
 * 可以嵌入型
 */

public abstract class InnerPage extends BasicPage {

    protected Page mRootPage;

    public InnerPage(Page page) {
        super(page.mPageActivity);
        mRootPage = page;
    }
}

package com.wei.android.lib.oneactivity.page;

/**
 * 可以嵌入型
 */

public abstract class InnerPage extends BasicPage {

    protected Page mPage;
    protected InnerPage mSelf;

    public InnerPage(Page page) {
        super(page.mPageActivity);
        mPage = page;
        mSelf = this;
    }
}


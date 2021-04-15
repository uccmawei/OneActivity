package com.wei.android.lib.oneactivity.demo;

import android.view.View;

import com.wei.android.lib.oneactivity.BindView;
import com.wei.android.lib.oneactivity.PageActivity;

public class TestPage extends BasicPage implements View.OnClickListener {

    @BindView(R.id.mTvTestPage)
    private View mTvTestPage;
    @BindView(R.id.mTvTestDialog)
    private View mTvTestDialog;
    @BindView(R.id.mTvTestTabPage)
    private View mTvTestTabPage;

    public TestPage(PageActivity pageActivity) {
        super(pageActivity);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.test_page;
    }

    @Override
    protected void onPageInit() {
        super.onPageInit();

        mTvTestPage.setOnClickListener(this);
        mTvTestDialog.setOnClickListener(this);
        mTvTestTabPage.setOnClickListener(this);
    }

    @Override
    protected void onViewClick(View view) {
        super.onViewClick(view);

        if (view == mTvTestPage) {
            new TestPage(mPageActivity).show();
            return;
        }
        if (view == mTvTestDialog) {
            new TestDialog(mPageActivity).show();
            return;
        }
        if (view == mTvTestTabPage) {
            new TestTabPage(mPageActivity).show();
        }
    }
}

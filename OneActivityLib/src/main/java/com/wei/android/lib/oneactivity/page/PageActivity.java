package com.wei.android.lib.oneactivity.page;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 基底
 */

public abstract class PageActivity extends AppCompatActivity {

    private PageManager mPageManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPageManager = new PageManager(this);
    }

    @Override
    public void onBackPressed() {
        mPageManager.onBackPressed();
    }

    /**
     * 获取底层 PageManager
     */
    protected PageManager getPageManager() {
        return mPageManager;
    }
}

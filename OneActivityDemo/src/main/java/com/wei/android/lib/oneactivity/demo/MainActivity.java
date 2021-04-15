package com.wei.android.lib.oneactivity.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.wei.android.lib.oneactivity.PageActivity;

public class MainActivity extends PageActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new TestPage(this).show();
    }
}

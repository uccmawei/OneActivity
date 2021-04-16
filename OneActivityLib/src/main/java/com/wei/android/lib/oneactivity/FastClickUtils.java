package com.wei.android.lib.oneactivity;

import android.os.SystemClock;

/**
 * 用于拦截同时多处点击引起的并发
 */

public class FastClickUtils {

    private static long sLastClickTime;

    public static boolean isFastClick() {
        if (SystemClock.elapsedRealtime() - sLastClickTime > 300) {
            sLastClickTime = SystemClock.elapsedRealtime();
            return false;
        }
        return true;
    }
}

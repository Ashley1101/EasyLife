package com.deepspring.tide.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deepspring.tide.lib.PreferencesHelper;

/**
 * Created by jiahongfei on 2017/9/27.
 */

public class TodayStepShutdownReceiver extends BroadcastReceiver {

    private static final String TAG = "TodayStepShutdownReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            PreferencesHelper.setShutdown(context,true);
        }
    }

}

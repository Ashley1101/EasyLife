package com.deepspring.lib.receiver;

import android.content.Context;
import android.content.Intent;

import com.deepspring.tide.lib.BaseClickBroadcast;

public class MyReceiver extends BaseClickBroadcast {

    private static final String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        com.deepspring.lib.application.TSApplication tsApplication = (com.deepspring.lib.application.TSApplication) context.getApplicationContext();
        if (!tsApplication.isForeground()) {
            Intent mainIntent = new Intent(context, com.deepspring.lib.ui.activity.SportActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        } else {

        }
    }
}

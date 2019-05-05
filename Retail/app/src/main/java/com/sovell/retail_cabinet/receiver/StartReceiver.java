package com.sovell.retail_cabinet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.sovell.retail_cabinet.ui.SplashActivity;

public class StartReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), ACTION)) {

            Intent splashIntent = new Intent(context, SplashActivity.class);

            splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(splashIntent);
        }
    }
}

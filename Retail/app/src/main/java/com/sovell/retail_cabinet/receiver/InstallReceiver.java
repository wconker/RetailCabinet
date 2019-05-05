package com.sovell.retail_cabinet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sovell.retail_cabinet.ui.SplashActivity;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            Intent splashIntent = new Intent(context, SplashActivity.class);
            splashIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            splashIntent.setAction(Intent.ACTION_MAIN);
            splashIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(splashIntent);
        }
    }
}

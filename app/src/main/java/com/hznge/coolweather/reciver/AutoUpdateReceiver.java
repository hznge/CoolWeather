package com.hznge.coolweather.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hznge.coolweather.service.AutoUpdateService;

/**
 * Created by hznge on 16-12-22.
 */

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startActivity(i);
    }
}

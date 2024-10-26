package com.example.seguimientodecalorias;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MotivationNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity mainActivity = new MainActivity();
        mainActivity.lanzarNotificacion2();
    }
}


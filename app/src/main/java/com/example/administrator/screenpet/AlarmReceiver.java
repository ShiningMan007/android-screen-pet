package com.example.administrator.screenpet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Alarm Receiver", "Yep!");

        boolean button_click = intent.getExtras().getBoolean("extra");
        long selected_id = intent.getExtras().getLong("music_id");


        Log.e("Alarm Receiver", "key is "+String.valueOf(button_click));
        Log.e("Alarm Receiver", "music id is: "+ String.valueOf(selected_id));

        // Create and intent to ringtone service
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        service_intent.putExtra("extra", button_click);
        service_intent.putExtra("selected_id", selected_id);

        context.startService(service_intent);
    }
}

package com.example.administrator.screenpet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class RingtonePlayingService extends Service {

    MediaPlayer mediaPlayer;
    int start_id;
    boolean music_is_playing = false;
    long music_selected_id;
    String content;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MusicService", "Yep2");

        //fetch the extra boolean
        boolean click_button = intent.getExtras().getBoolean("extra");
        music_selected_id = intent.getExtras().getLong("selected_id");
        content = intent.getExtras().getString("content");

        if(click_button){
            start_id = 1;
        }
        else if(!click_button) {
            start_id = 0;
        }
        else{
            start_id =0;
        }

        Log.e("Music Service", "start id is "+start_id);



        if(! music_is_playing && start_id == 1){
            Log.e("Music Service", "There is no music, and you want to start.");
            switch ((int)music_selected_id){
                case 0:
                    mediaPlayer = MediaPlayer.create(this, R.raw.ringtone1);
                    break;
                case 1:
                    mediaPlayer = MediaPlayer.create(this, R.raw.ringtone2);
                    break;
                default:
                    mediaPlayer = MediaPlayer.create(this, R.raw.ringtone2);
                    break;
            }
            mediaPlayer.start();
            music_is_playing = true;

            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            Intent intent_to_clock = new Intent(this.getApplicationContext(), ClockActivity.class);

            PendingIntent pendingIntent_to_clock = PendingIntent.getActivity(this, 0,
                    intent_to_clock, 0);

            Notification notification_popup = new Notification.Builder(this)
                    .setContentTitle(content)
                    .setContentText("Click me!").setContentIntent(pendingIntent_to_clock)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true).build();


            notificationManager.notify(0, notification_popup);

        }
        else if(music_is_playing && start_id == 0){
            Log.e("Music Service", "There is music, and you want to end");
            mediaPlayer.stop();
            mediaPlayer.reset();
            music_is_playing = false;

        }else if(!music_is_playing && start_id == 0){
            Log.e("Music Service", "There is no music, and you want to end.");
            this.music_is_playing = false;

        }else if(music_is_playing && start_id == 1){
            Log.e("Music Service", "There is music, and you want to start.");
            this.music_is_playing = true;
        }else{
            Log.e("Music Service", "Something goes here.");
            this.music_is_playing = false;
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        Log.e("Music Service", "On Destroy.");
        super.onDestroy();
        this.music_is_playing = false;
    }
}

package com.example.mp3clone;

import static com.example.mp3clone.MyApplication.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;

public class MyService extends Service {
    private SongPlayer songPlayer = new SongPlayer();
    public static final int ACTION_PAUSE = 0;
    public static final int ACTION_NEXT = 1;
    public static final int ACTION_PREVIOUS = 2;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification(songPlayer.getSong());
        int action = intent.getIntExtra("action",3 );
        handleAction(action);
        sendActionToActivity(action);
        return START_NOT_STICKY;
    }

    private void handleAction(int action) {
        switch (action){
            case ACTION_PAUSE:{
                if(songPlayer.isPlaying()){
                    songPlayer.pauseSong();
                }
                else{
                    songPlayer.resumeSong();
                }
                break;
            }
            case ACTION_NEXT:{
                Log.e("TAG", "handleAction: "+ACTION_NEXT );
                songPlayer.nextSong();
                break;
            }
            case ACTION_PREVIOUS:{
                Log.e("TAG", "handleAction: "+ACTION_PREVIOUS );
                songPlayer.previousSong();
                break;
            }
            default:{
                Log.e("TAG", "handleAction: "+ACTION_PAUSE );
                songPlayer.resumeSong();
                break;
            }
        }
        sendNotification(songPlayer.getSong());
    }

    private void sendNotification(Song song){

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT );

        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        Bitmap bitmap = null;
        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    Bitmap bitmap = Glide
                            .with(MyService.this)
                            .asBitmap()
                            .load(song.getThumbnail())
                            .submit()
                            .get();
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID)
                            .setContentTitle(song.getTitle())
                            .setContentText(song.getSinger())
                            .setSmallIcon(R.drawable.ic_music_note)
                            .setLargeIcon(bitmap)
                            .setContentIntent(pendingIntent)
                            // Apply the media style template
                            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0,1,2)
                                    .setMediaSession(mediaSessionCompat.getSessionToken()));
                    if(songPlayer.isPlaying()){
                        notificationBuilder.addAction(R.drawable.ic_previous, "Previous", getPendingIntent(MyService.this,ACTION_PREVIOUS))
                                .addAction(R.drawable.ic_pause, "Pause", getPendingIntent(MyService.this,ACTION_PAUSE))
                                .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent(MyService.this,ACTION_NEXT));
                    }
                    else{
                        notificationBuilder.addAction(R.drawable.ic_previous, "Previous", getPendingIntent(MyService.this,ACTION_PREVIOUS))
                                .addAction(R.drawable.ic_play, "Pause", getPendingIntent(MyService.this,ACTION_PAUSE))
                                .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent(MyService.this,ACTION_NEXT));
                    }
                    Notification notification = notificationBuilder.build();
                    startForeground(1, notification);
                }
                catch (Exception exception){
                    Log.e("TAG", "run: "+exception );
                }
            }
        };
        thread.start();

    }
    private PendingIntent getPendingIntent(Context context, int action){
        Intent intent = new Intent(this,MyReceiver.class);
        intent.putExtra("action",action);
        return PendingIntent.getBroadcast(context.getApplicationContext(),action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private void sendActionToActivity(int action){
        Intent intent = new Intent("send_action_to_activity");
        intent.putExtra("action", action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

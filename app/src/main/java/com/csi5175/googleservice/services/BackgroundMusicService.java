/*
 * Yan Zhang
 * 300052103
 * This service is for playing music in background
 * */
package com.csi5175.googleservice.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.csi5175.googleservice.R;

public class BackgroundMusicService extends Service {

    //DEBUG only
    private static final String TAG = BackgroundMusicService.class.getSimpleName();
    //private field
    MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    /*
     * create a new music player holder for predefined music
     * */
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.eott);
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return Service.START_STICKY;
    }

    /*
     * stop music service and reinitialize the music player
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }

}

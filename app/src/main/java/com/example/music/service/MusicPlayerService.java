package com.example.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;

public class MusicPlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MusicPlayerBinder();
    private OnPreparedListener onPreparedListener;
    private OnCompletionListener onCompletionListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String songUrl = intent.getStringExtra("songUrl");
        try {
            // Cài đặt MediaPlayer từ URL bài hát
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                if (onPreparedListener != null) {
                    onPreparedListener.onPrepared();
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.onPreparedListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.onCompletionListener = listener;
    }

    public void play() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public interface OnPreparedListener {
        void onPrepared();
    }

    public interface OnCompletionListener {
        void onCompletion();
    }
}
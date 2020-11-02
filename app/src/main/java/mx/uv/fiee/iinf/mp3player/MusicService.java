package mx.uv.fiee.iinf.mp3player;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

public class MusicService extends Service {
    private MediaPlayer player = new MediaPlayer();
    private final IBinder binder = new MusicBinder();
    private static final String CHANNEL_ID = "NOTIFICATION";
    private static final int NOTIFICATION_ID = 0;
    private int mCurrentPos = 0;
    private boolean mPause = false;
    private String mCurrentSong;
    private NotificationCompat.Builder builder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isPlaying()) {
            player.stop();
            player.release();
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_queue_music_white_24dp)
                .setContentTitle("MP3-Player")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setAutoCancel(false);
        player.setOnPreparedListener(MediaPlayer::start);
        player.setOnCompletionListener(MediaPlayer::reset);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }
    void pauseSong(){
        if(isPlaying()){
            player.pause();
            mCurrentPos = player.getCurrentPosition();
            mPause = true;
        }
    }
    void playSong(String uri, String title){
        mCurrentSong = uri;
        mPause = false;
        builder.setContentText(title);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
        reset();
        try {
            assert player != null;
            player.setDataSource(getBaseContext(), Uri.parse(uri));
            player.prepare();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
    void resumeSong(){
        player.seekTo(mCurrentPos);
        player.start();
        mPause = false;
    }
    void reset(){
        if (isPlaying()) {
            player.stop();
            player.reset();
        }
    }
    String getCurrentSong(){
        return mCurrentSong;
    }
    boolean isPlaying(){
        return player != null && player.isPlaying();
    }
    int getCurrentPos(){
        return player.getCurrentPosition();
    }
    void seekTo(int mCurrentPos){
        player.seekTo(mCurrentPos);
    }
    int getDuration(){
        return player.getDuration();
    }
    boolean isPaused(){
        return mPause;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}

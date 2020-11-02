package mx.uv.fiee.iinf.mp3player;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DetailsActivity extends Activity{

    private SeekBar sbProgress;
    private MusicService musicService;
    private String mCurrentSong;
    private static final String CHANNEL_ID = "NOTIFICATION";
    private boolean mBound = false;
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Event", "onPause");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Event", "onResume");
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.d("Event", "onStop");
        unbindService(serviceConnection);
        mBound = false;
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d("Event", "onStart");
        Intent intent = new Intent(getBaseContext (), MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Event", "onCreate");
        Bundle song = getIntent().getExtras();
        mCurrentSong = song.getString("song");
        String mTitle = song.getString("title");
        setContentView(R.layout.activity_details);
        sbProgress = findViewById(R.id.sbProgress);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_queue_music_white_24dp)
                .setContentTitle("MP3-Player")
                .setContentText(mTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(0, builder.build());
        Handler updateHandler = new Handler();
        DetailsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if(mBound){
                    if(musicService.isPlaying()) {
                        sbProgress.setMax(musicService.getDuration() / 1000);
                        sbProgress.setProgress(musicService.getCurrentPos() / 1000);
                    }
                }
                updateHandler.postDelayed(this, 1000);
            }
        });
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mBound && musicService.isPlaying() && fromUser){
                    musicService.seekTo(progress * 1000);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        ImageButton play = findViewById(R.id.play);
        play.setOnClickListener(v -> {
            startService(new Intent(this, MusicService.class));
            if(musicService.isPaused()){
                musicService.resumeSong();
            }else{
                musicService.playSong(mCurrentSong);
            }
        });
        ImageButton pause = findViewById(R.id.pause);
        pause.setOnClickListener(v -> {
            musicService.pauseSong();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Event", "onDestroy");
    }
    ServiceConnection serviceConnection = new ServiceConnection () {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            if(!mCurrentSong.equals(musicService.getCurrentSong())){
                musicService.reset();
            }
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
}
package mx.uv.fiee.iinf.mp3player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends Activity {
    static MediaPlayer player = null;
    static HashMap <Integer, String> songs;
    int mCurrentSong = -1;
    static boolean fromResume = false;
    SeekBar sbProgress;
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Event", "onPause");
        if(player != null && player.isPlaying())
        {
            player.pause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Event", "onResume");
        @SuppressLint("CommitPrefEdits") SharedPreferences preferences = getSharedPreferences("preferences", 0);
        int song = preferences.getInt("currentSong", -1);
        int pos = preferences.getInt("position", -1);
        if(song != -1 && pos != -1 && mCurrentSong != -1)
        {
            player.seekTo(pos);
            player.start();
        }
        fromResume = true;
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.d("Event", "onStop");
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
        if(player != null)
        {
            editor.putInt("position", player.getCurrentPosition());
            editor.putInt("currentSong", mCurrentSong);
            editor.apply();
        }
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d("Event", "onStart");
        @SuppressLint("CommitPrefEdits") SharedPreferences preferences = getSharedPreferences("preferences", 0);
        int song = preferences.getInt("currentSong", -1);
        int pos = preferences.getInt("position", -1);
        if(song != -1 && pos != -1 && !fromResume)
        {
            resumeSong(song, pos);
        }
    }
    void resumeSong(int whatSong, int pos)
    {
        mCurrentSong = whatSong;
        Uri mediaUri = Uri.parse("android.resource://" + getBaseContext ().getPackageName () + "/" + whatSong);
        try {
            player.setDataSource(getBaseContext(), mediaUri);
            player.prepare();
            player.seekTo(pos);
        } catch (IOException ex) { ex.printStackTrace(); }
        sbProgress.setProgress(pos / 1000);
    }
    void playSong(int whatSong){
        if (player != null && player.isPlaying()) {
            player.stop();
            sbProgress.setProgress(0);
            player.reset();
        }
        Uri mediaUri = Uri.parse("android.resource://" + getBaseContext ().getPackageName () + "/" + whatSong);
        try {
            player.setDataSource(getBaseContext(), mediaUri);
            player.prepare();
            Toast.makeText(getApplicationContext(), "Now playing: " + songs.get(whatSong), Toast.LENGTH_LONG).show();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Event", "onCreate");
        setContentView (R.layout.activity_main);
        sbProgress = findViewById(R.id.sbProgress);
        Handler updateHandler = new Handler();
        player = new MediaPlayer();
        songs = new HashMap<Integer, String>();
        songs.put(R.raw.mr_blue_sky, "Mr. Blue Sky");
        songs.put(R.raw.lake_shore_drive, "Lake Shoe Drive");
        songs.put(R.raw.fox_on_the_run, "Fox On The Run");
        MainActivity.this.runOnUiThread( new Runnable() {
            public void run() {
                if(player != null){
                    if(player.isPlaying()) {
                        sbProgress.setProgress(player.getCurrentPosition() / 1000);
                    }
                }
                updateHandler.postDelayed(this, 1000);
            }
        });
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(player != null && fromUser)
                {
                    player.seekTo(progress * 1000);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        player.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.reset();
            mCurrentSong = -1;
        });
        player.setOnPreparedListener(mediaPlayer -> {
            sbProgress.setMax(mediaPlayer.getDuration() / 1000);
            mediaPlayer.start();
        });
        Button btnAudio1 = findViewById(R.id.btnAudio1);
        btnAudio1.setOnClickListener (v -> {
            mCurrentSong = R.raw.mr_blue_sky;
            playSong(mCurrentSong);
        });
        Button btnAudio2 = findViewById (R.id.btnAudio2);
        btnAudio2.setOnClickListener (v -> {
            mCurrentSong = R.raw.lake_shore_drive;
            playSong(mCurrentSong);
        });
        Button btnAudio3 = findViewById (R.id.btnAudio3);
        btnAudio3.setOnClickListener (v -> {
            mCurrentSong = R.raw.fox_on_the_run;
            playSong(mCurrentSong);
        });
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        if(player != null && player.isPlaying()){
            Log.d("INFO", "onSave");
            outState.putInt("position", player.getCurrentPosition());
            outState.putBoolean("isPlaying", true);
            outState.putInt("currentSong", mCurrentSong);
            player.pause();
        }
        else outState.putBoolean("isPlaying", false);
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstance){
        super.onRestoreInstanceState(savedInstance);
        if(player != null && savedInstance.getBoolean("isPlaying")){
            Log.d("INFO", "onRestore");
            int pos = savedInstance.getInt("position");
            int song = savedInstance.getInt("currentSong");
            resumeSong(song, pos);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Event", "onDestroy");
        // cleanup
        super.onStop();
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }
        player = null;
        fromResume = false;
    }
}

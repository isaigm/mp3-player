package mx.uv.fiee.iinf.mp3player;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;

public class DetailsActivity extends Activity{

    static MediaPlayer player = null;
    String mCurrentSong = "";
    SeekBar sbProgress;
    int mLastPos = 0;
    boolean fromPause = false;
    static boolean fromSave = false;
    int duration = 0;
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
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d("Event", "onStart");
    }
    void resumeSong(int pos)
    {
        try {
            player.setDataSource(getBaseContext(), Uri.parse(mCurrentSong));
            player.prepare();
            player.seekTo(pos);
        } catch (IOException ex) { ex.printStackTrace(); }
        sbProgress.setProgress(pos / 1000);
    }
    void playSong(){
        if (player != null && player.isPlaying()) {
            player.stop();
            sbProgress.setProgress(0);
            player.reset();
        }
        try {
            assert player != null;
            player.setDataSource(getBaseContext(),  Uri.parse(mCurrentSong));
            player.prepare();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Event", "onCreate");
        Bundle song = getIntent().getExtras();
        if(song != null){
            mCurrentSong = song.getString("song");
        }
        setContentView(R.layout.activity_details);
        sbProgress = findViewById(R.id.sbProgress);
        Handler updateHandler = new Handler();
        player = new MediaPlayer();
        DetailsActivity.this.runOnUiThread( new Runnable() {
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
                    if(player.isPlaying() || fromPause){
                        player.seekTo(progress * 1000);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        player.setOnCompletionListener(MediaPlayer::reset);
        player.setOnPreparedListener(mediaPlayer -> {
            sbProgress.setMax(mediaPlayer.getDuration() / 1000);
            mediaPlayer.start();
        });
        ImageButton play = findViewById(R.id.play);
        play.setOnClickListener(v -> {
            if(!player.isPlaying()){
              if(fromPause){
                  if(fromSave){
                      resumeSong(mLastPos);
                      fromSave = false;
                  }else player.start();
                  fromPause = false;
              }else {
                  playSong();
              }
            }
        });
        ImageButton pause = findViewById(R.id.pause);
        pause.setOnClickListener(v -> {
            if(player.isPlaying()){
                mLastPos = player.getCurrentPosition();
                duration = player.getDuration();
                player.pause();
                fromPause = true;
            }
        });
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        Log.d("Event", "onSaveInstanceState");
        if(fromPause){
            outState.putBoolean("fromPause", true);
            outState.putInt("position", mLastPos);
            outState.putInt("duration", duration);
        }
        if(player != null && player.isPlaying()){
            Log.d("INFO", "onSave");
            outState.putInt("position", player.getCurrentPosition());
            outState.putBoolean("isPlaying", true);
            outState.putString("currentSong", mCurrentSong);
        }
        else outState.putBoolean("isPlaying", false);
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstance){
        super.onRestoreInstanceState(savedInstance);
        Log.d("Event", "onRestoreInstanceState");
        int pos = savedInstance.getInt("position");
        if(savedInstance.getBoolean("fromPause"))
        {
            mLastPos = pos;
            fromPause = true;
            fromSave = true;
            int duration = savedInstance.getInt("duration");
            sbProgress.setMax(duration / 1000);
            sbProgress.setProgress(pos / 1000);
        }
        if(player != null && savedInstance.getBoolean("isPlaying")){
            Log.d("INFO", "onRestore");
            mCurrentSong = savedInstance.getString("currentSong");
            resumeSong(pos);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Event", "onDestroy");
        super.onStop();
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }
        fromSave = false;
        player = null;
    }
}
package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.VideoView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.justaway.util.MessageUtil;

public class VideoActivity extends FragmentActivity {

    @Bind(R.id.player)
    VideoView player;

    @Bind(R.id.guruguru)
    ProgressBar guruguru;


    @OnClick(R.id.cover)
    void close() {
        finish();
    }

    public VideoActivity() {
    }

    boolean musicWasPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        if (args == null) {
            MessageUtil.showToast("Missing Bundle in Intent");
            finish();
            return;
        }

        String videoUrl = args.getString("videoUrl");

        if (videoUrl == null) {
            MessageUtil.showToast("Missing videoUrl in Bundle");
            finish();
            return;
        }

        musicWasPlaying = ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isMusicActive();

        guruguru.setVisibility(View.VISIBLE);
        player.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                finish();
                return false;
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                guruguru.setVisibility(View.GONE);
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                player.seekTo(0);
                player.start();
            }
        });
        player.setVideoURI(Uri.parse(videoUrl));
        player.start();
    }

    @Override
    protected void onDestroy() {
        if (musicWasPlaying) {
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            sendBroadcast(i);
        }
        super.onDestroy();
    }
}

package info.justaway;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.justaway.util.MessageUtil;

public class VideoActivity extends FragmentActivity implements MediaPlayer.OnPreparedListener {

    @Bind(R.id.player)
    VideoView player;

    @Bind(R.id.guruguru)
    ProgressBar guruguru;


    @OnClick(R.id.cover)
    void close() {
        finish();
    }

    private MediaPlayer mMediaPlayer = null;

    public VideoActivity() {
    }

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

        guruguru.setVisibility(View.VISIBLE);
        final MediaController mediaController = new MediaController(this);
        mediaController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        player.setMediaController(mediaController);
        player.setOnPreparedListener(this);
        player.setVideoURI(Uri.parse(videoUrl));
        player.start();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        guruguru.setVisibility(View.GONE);
        mp.setLooping(true);
        mMediaPlayer = mp;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

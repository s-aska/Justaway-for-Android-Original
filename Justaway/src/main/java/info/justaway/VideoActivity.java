package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.justaway.model.TwitterManager;
import info.justaway.util.MessageUtil;
import info.justaway.util.StatusUtil;
import twitter4j.Status;

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

        final String statusUrl = args.getString("statusUrl");
        if (statusUrl != null && !statusUrl.isEmpty()) {
            Pattern pattern = Pattern.compile("https?://twitter\\.com/\\w+/status/(\\d+)/video/(\\d+)/?.*");
            Matcher matcher = pattern.matcher(statusUrl);
            if (matcher.find()) {
                final Long statusId = Long.valueOf(matcher.group(1));
                new AsyncTask<Void, Void, Status>() {
                    @Override
                    protected twitter4j.Status doInBackground(Void... params) {
                        try {
                            return TwitterManager.getTwitter().showStatus(statusId);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(twitter4j.Status status) {
                        if (status != null) {
                            final String videoUrl = StatusUtil.getVideoUrl(status);
                            if (videoUrl != null && !videoUrl.isEmpty()) {
                                setVideoURI(videoUrl);
                            }
                        }
                    }
                }.execute();
                return;
            }
        }
        final String videoUrl = args.getString("videoUrl");

        if (videoUrl == null) {
            MessageUtil.showToast("Missing videoUrl in Bundle");
            finish();
            return;
        }

        setVideoURI(videoUrl);
    }

    private void setVideoURI(final String videoUrl) {
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
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

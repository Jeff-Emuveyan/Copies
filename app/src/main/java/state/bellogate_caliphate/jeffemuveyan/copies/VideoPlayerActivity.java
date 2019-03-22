   package state.bellogate_caliphate.jeffemuveyan.copies;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

   public class VideoPlayerActivity extends AppCompatActivity {

    private AdView mAdView;
    public static SimpleExoPlayerView videoView;
    SimpleExoPlayer exoPlayer;
    public static CircularProgressBar progressBar;
    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = (SimpleExoPlayerView)findViewById(R.id.videoView);
        progressBar = (CircularProgressBar)findViewById(R.id.progressBar);
        handler = new Handler();


        //custom ActionBar
        //NOTE: Here we set the background of our actionBar to white. This will work but it will also cover the menuItem icon whose
        //color is also white. To solve this, we changed our menuItem color to black. Go to styles.xml to see how we did it.
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));

        //load our ad
        mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //get the video link
        Intent i = getIntent();
        String videoLink = i.getStringExtra("videoLink");


        try {

            //We use exoplayer so that our videos load faster.
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

            Uri uri = Uri.parse(videoLink);

            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);

            videoView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);

            exoPlayer.addListener(new ExoPlayer.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    if(playbackState == ExoPlayer.STATE_BUFFERING){
                        progressBar.setVisibility(View.VISIBLE);
                    }else{
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    if(playbackState == ExoPlayer.STATE_ENDED){

                        exoPlayer.seekTo(0); //if the video has finished playing, restart it.
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    Toast.makeText(VideoPlayerActivity.this,"Network problem...",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPositionDiscontinuity() {
                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                }
            });

            exoPlayer.setPlayWhenReady(true);

        }catch (Exception e){
            Toast.makeText(VideoPlayerActivity.this, "Error"+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

       @Override
       public void onBackPressed() {
           super.onBackPressed();
           if(exoPlayer != null) {
               exoPlayer.stop();
               exoPlayer = null;
           }
       }


       @Override
       protected void onDestroy() {
           super.onDestroy();

           if(exoPlayer != null) {
               exoPlayer.stop();
               exoPlayer = null;
           }
       }
   }

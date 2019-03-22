package Fragments_Secondary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;


public class SelectStartingPointFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int CAMERA_RQ = 6969;

    TextView songNameTextView, durationTextView;
    SeekBar seekBar;
    ImageButton changeSongButton;
    Button makeVideoButton;

    MediaPlayer mediaPlayer;

    String songName;
    String songPath;
    int pointInSec;
    long duration;
    final static int SELECT_AUDIO = 2;
    int FILE_REQUEST_CODE = 33;

    private AdView mAdView;

    Context c;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select_starting_point, container, false);
        songNameTextView = (TextView)view.findViewById(R.id.textViewSongName);
        durationTextView= (TextView)view.findViewById(R.id.textView2);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        changeSongButton = (ImageButton) view.findViewById(R.id.imageButtonChange);
        makeVideoButton = (Button) view.findViewById(R.id.makeVideoButton);

        c = getActivity();

        //load our ad
        mAdView = (AdView)view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Just do this because the user may choose to press back button
        MainActivity.navigation.setVisibility(View.VISIBLE);


        //When the fragment starts,we collect the song name and path from previous fragment;
        Bundle bundle = getArguments();
        songName = bundle.getString("songName");
        songPath = bundle.getString("songPath");

        songNameTextView.setText(songName);

        durationTextView.setText("Your song starts at: 00:00");


        //we play the song
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


        //Now let us get the durationOfFullSong of the song
        duration = mediaPlayer.getDuration(); //this returns durationOfFullSong of song in milliseconds

        //now that we have the durationOfFullSong, we:
        seekBar.setMax(Integer.parseInt(String.valueOf(duration)));



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            long newPoint;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {

                //where progress value range from 0 -> the seekBars Max value
                newPoint = Long.parseLong(String.valueOf(progressValue));

                //update the durationOfFullSong textView as the user moves
                durationTextView.setText("Your song starts at: " + new SimpleDateFormat("mm:ss").format(new Date(newPoint)));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                //if the user is starts moving the seekBar, pause the song. (NOTE: DONT STOP THE SONG, PAUSE IT CUZ OF ERROR)
                mediaPlayer.pause();

            }


            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //once the user stops moving the seekBar we play the song from the current position

                pointInSec = Integer.parseInt(String.valueOf(newPoint));//just convert the newPoint (long) to an integer

                mediaPlayer.seekTo(pointInSec);

                mediaPlayer.start();



            }
        });


        changeSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                pickAudio();
            }
        });


        makeVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //launch camera to make video

                //but first check if phone has camera
                if(phoneSupportsCamera() ) {

                    // pause the song
                    mediaPlayer.pause();

                    //Let us send the songPath, songName and position to start from to the next fragment
                    Bundle bundle = new Bundle();
                    bundle.putString("songPath", songPath);
                    //and
                    bundle.putInt("pointInSec", pointInSec);

                    CameraFragment cameraFragment = new CameraFragment();
                    cameraFragment.setArguments(bundle);

                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, cameraFragment);
                    ft.addToBackStack("SelectStartingPoint");
                    ft.commit();

                    vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                    MainActivity.viewPagerIsVisible = false;

                }else{
                    Toast.makeText(getActivity(), "Device has no camera support", Toast.LENGTH_LONG).show();
                }


            }
        });


        return view;
    }


    void pickAudio(){

        //we are using a library to pick audio because the default way dosent give use the correct file path for files in sdcard.
        //We have tried all we could do.
        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                .setCheckPermission(true)
                .setShowAudios(true)
                .setShowFiles(false)
                .setShowImages(false)
                .setShowVideos(false)
                .setMaxSelection(1)
                .setSingleClickSelection(true)
                .build()
        );


        startActivityForResult(intent, FILE_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch(requestCode) {

            case 33:

                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                MediaFile file = files.get(0);//get the music file

                songPath = file.getPath();

                //We have selected a song now we want to update the Fragment
                //First pause the current playing song
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }

                //reset the seekBar
                seekBar.setProgress(0);

                songNameTextView.setText(file.getName());

                //we play the song
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(songPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Now let us get the durationOfFullSong of the song
                duration = mediaPlayer.getDuration(); //this returns durationOfFullSong of song in milliseconds

                //now that we have the durationOfFullSong, we:
                seekBar.setMax(Integer.parseInt(String.valueOf(duration)));

        }


    }




    boolean phoneSupportsCamera(){

        PackageManager pm = c.getPackageManager();
        if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;

        }else{
            //If it dosen't support
            return false;
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        mediaPlayer.pause();

    }

    @Override
    public void onPause() {
        super.onPause();

        mediaPlayer.pause();

    }
}

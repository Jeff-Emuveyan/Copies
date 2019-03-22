package Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.VolumeShaper;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iceteck.silicompressorr.Util;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import Fragments_Secondary.SelectStartingPointFragment;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;


public class SelectTrackFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button pickAudioButton;
    final static int SELECT_AUDIO = 2;
    String selectedPath;


    private AdView mAdView;
    AlertDialog.Builder alert;

    int FILE_REQUEST_CODE = 33;


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
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_select_track, container, false);

        pickAudioButton = (Button)view.findViewById(R.id.pickAudioButton);

        alert = new AlertDialog.Builder(getActivity());

        //load our ad
        mAdView = (AdView)view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        pickAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pickAudio();
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

        try {

            switch (requestCode) {

                case 33:
                    ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                    MediaFile file = files.get(0);//get the music file

                    selectedPath = file.getPath();

                    //We have selected a song now we want to start the SelectStartingPointFragment Fragment

                    //First we send the song path to it:
                    Bundle bundle = new Bundle();
                    bundle.putString("songPath", selectedPath);
                    //We also send the song name
                    bundle.putString("songName", file.getName());

                    SelectStartingPointFragment selectStartingPointFragment = new SelectStartingPointFragment();
                    selectStartingPointFragment.setArguments(bundle);

                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, selectStartingPointFragment);
                    ft.addToBackStack("SelectTrackFragment");
                    ft.commit();

                    //as usual we gotta do this:
                    vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                    MainActivity.viewPagerIsVisible = false;

                    break;

            }

            //NOTE: ALL THESES EXCEPTIONS ARE CAUSED BY THE LIBRARY WE ARE USING TO SELECT AUDIO. OUR OWN CODE IF 100% EXCELLENT
        }catch(IndexOutOfBoundsException e){// this exception occurs when the user pressing 'Done' when no song was selected

            alert.setTitle("Oops! Try again");
            alert.setMessage("You did not choose any song");
            alert.show();

        }catch(NullPointerException e){// this exception occurs when the user tries the go back from the audio picker
            //don't do anything
        }

}


    /*public String getPath(Uri uri) {

        try{  //In case the user chooses an invalid video file

            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = getActivity().getContentResolver().query(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            cursor.close();


            return path;

        }catch(Exception e){

            return "Invalid audio";

        }
    }*/




    /*public String getPath(Uri uri) {

        try{  //In case the user chooses an invalid video file

            String[] proj = { MediaStore.Audio.Media.DATA };
            Cursor cursor = getActivity().managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);

        }catch(Exception e){

            return "cannot load songs from there, Try another folder.";
            //return "Invalid audio";

        }
    }*/


}

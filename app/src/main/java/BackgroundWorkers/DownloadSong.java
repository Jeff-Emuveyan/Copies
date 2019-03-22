package BackgroundWorkers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import Fragments_Secondary.CameraFragment;
import Fragments_Secondary.DownloadSongFragment;

import static Fragments_Secondary.DownloadSongFragment.cancelButton;
import static Fragments_Secondary.DownloadSongFragment.getButton;
import static Fragments_Secondary.DownloadSongFragment.handler;
import static Fragments_Secondary.DownloadSongFragment.loadingStatusTextView;
import static Fragments_Secondary.DownloadSongFragment.progressBar;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;

/**
 * Created by JEFF EMUVEYAN on 4/9/2018.
 */

public class DownloadSong extends AsyncTask<Response, String, String> {

    static Context c;
    Response response;
    String originalVideoPostId;

    byte[] data = new byte[8192];
    float total = 0;
    int readByte = 0;

    String responseMessage = "ass hole";


    public DownloadSong(Context c, String originalVideoPostId) {
        this.c = c;
        this.originalVideoPostId = originalVideoPostId;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }



    @Override
    protected String doInBackground(Response... responses) {

        response = responses[0];


        try{
            final float fileSize = response.body().contentLength();

            BufferedInputStream inputStream = new BufferedInputStream(response.body().byteStream());
            OutputStream outputStream = new FileOutputStream(getStoragePath());

            //first, we want to do this on the UI
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setIndeterminate(false);
                }
            });


            while((readByte = inputStream.read(data)) != -1){
                total = total + readByte;
                outputStream.write(data,0,readByte);

                //we can call a progressBar's 'setProgress' view from any background thread
                progressBar.setProgress((int) ((total/fileSize)*100));

                //But for all other views, we have to use a handler when we want to call the view from any background thread. So:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadingStatusTextView.setText("Fetching..."+String.valueOf((int) ((total/fileSize)*100))+ "%");
                    }
                });

            }

            outputStream.flush();
            outputStream.close();
            response.body().close();

            //now tell the onPostExecute to start the CameraFragment
            responseMessage = "success";


        }catch (final Exception e){
            responseMessage = "failed";
        }


        return responseMessage;
    }


    @Override
    protected void onPostExecute(String responseMessage) {

        if(responseMessage.equals("failed")){

            /*Failure at this AsyncTask can happen because of two reasons:
            * 1) the user presses the 'cancel' button. ---If the user presses cancel before onResponse() is called (this happens when the app is still trying to
            * establish a connection, no data has been read at all. Our AsyncTask class will not be called, hence the error management will simply be done in the onFailure().
            *
            * 2) the user presses the 'cancel button' after onResponse is called (ie..after some data has already been read.) Our AsyncTask is now involved and will crash the app
            * because you have suddenly stopped the loop. It will display a strange exception message. So we simply display a kind message to the user telling him that
            * the download has stopped. Then we secretly delete the unfinished file from the Copies folde.
            *
            * ALSO: this same procedure is what will happen if there is a genuine network error that stops our download half way.
            * */


            Toast.makeText(c, "Downloading stopped", Toast.LENGTH_SHORT).show();

            //delete the song from the folder
            deleteFile();

            DownloadSongFragment.isDownloading = false;

            //show the getButton
            getButton.setVisibility(View.VISIBLE);
            //hide these ones:
            loadingStatusTextView.setVisibility(View.GONE);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }


        else if(responseMessage.equals("success")){

            DownloadSongFragment.isDownloading = false;

            //start the camera fragment
            Bundle bundle = new Bundle();
            bundle.putString("songPath", getStoragePath());
            bundle.putInt("pointInSec", 0);
            bundle.putString("originalVideoPostId", originalVideoPostId);


            CameraFragment cameraFragment = new CameraFragment();
            cameraFragment.setArguments(bundle);


            AppCompatActivity appCompatActivity = (AppCompatActivity)c;
            FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, cameraFragment);
            ft.addToBackStack("DownloadSongFragment");
            ft.commit();

            vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
            MainActivity.viewPagerIsVisible = false;

        }


    }




    private static String getStoragePath(){

        //Random random = new Random();
        //String randomNumber = String.valueOf(random.nextInt(3000));

        if( phoneHasSDcard()) {
            return Environment.getExternalStorageDirectory() + "/Copies/downloadedSong.mp3";
        }else{
            return c.getFilesDir()+ "/Copies/downloadedSong.mp3";
        }

    }




    public static void deleteFile() {

        File audioFile;

        if (phoneHasSDcard()) {
            audioFile = new File(getStoragePath());

            if (audioFile.exists()) {
                audioFile.delete();
            }

        } else {
            audioFile = new File(getStoragePath());

            if (audioFile.exists()) {
                audioFile.delete();
            }

        }
    }



    private static boolean phoneHasSDcard(){

        boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        boolean isThisAnSDSupportedDevice = Environment.isExternalStorageRemovable();

        if (isThisAnSDSupportedDevice && isSDPresent) {//if the phone can support sdcard and the sdcard is present:
            return true;
        } else {
            return false;
        }

    }


}

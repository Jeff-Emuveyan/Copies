package BackgroundWorkers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import Fragments_Secondary.CameraFragment;

/**
 * Created by JEFF EMUVEYAN on 1/23/2018.
 */

public class BackgroundWorker extends AsyncTask<String, Void, String> {


    String songPath;
    String path;
    Context c;
    int pointInSec;

    public BackgroundWorker(Context c, String songPath, int pointInSec) {
        // TODO Auto-generated constructor stub

        this.songPath = songPath;
        this.c = c;
        this.pointInSec = pointInSec;

    }


    @Override
    protected String doInBackground(String... arg0) {
        // TODO Auto-generated method stub

            //copy the song
            File oldSongFile = new File(songPath);
            File newSongFile;

            if (phoneHasSDcard()) {
                newSongFile = new File(Environment.getExternalStorageDirectory() + "/Copies", "copiedAudioFile.mp3");
                path = Environment.getExternalStorageDirectory() + "/Copies/copiedAudioFile.mp3";
            } else {
                newSongFile = new File(c.getFilesDir() + "/Copies", "copiedAudioFile.mp3");
                path = c.getFilesDir() + "/Copies/copiedAudioFile.mp3";
            }

            try {
                FileInputStream fileInputStream = new FileInputStream(oldSongFile);
                FileOutputStream fileOutputStream = new FileOutputStream(newSongFile);

                byte[] b = new byte[1024];
                int noOfBytesRead;

                while ((noOfBytesRead = fileInputStream.read(b)) > 0) {
                    fileOutputStream.write(b, 0, noOfBytesRead);
                }

                fileInputStream.close();
                fileOutputStream.close();

            } catch (FileNotFoundException e) {

                //Toast.makeText(c, "A problem occured...", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(c, "A problem occured...", Toast.LENGTH_LONG).show();

            }

            //We are done copying the song

        return "ASS HOLE"; // hahahahahaha...lmao
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub

            //now we can trim the song because the dupicate file has been deleted
            CameraFragment.trimSong(c,path);



    }

    @Override
    protected void onProgressUpdate(Void... values) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(values);
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


    private void deleteOldOutputfiles(){

                File audioFile, videoFile;

                if(phoneHasSDcard()){
                    audioFile = new File(Environment.getExternalStorageDirectory()+"/Copies/copiesTrimmedAudio.mp3");
                    videoFile = new File(Environment.getExternalStorageDirectory()+"/Copies/finalVideo.mp4");

                    if(audioFile.exists()) {
                        audioFile.delete();
                    }
                    if(videoFile.exists()) {
                        videoFile.delete();
                    }
                }else{
                    audioFile = new File(c.getFilesDir()+"/Copies/copiesTrimmedAudio.mp3");
                    videoFile = new File(c.getFilesDir()+"/Copies/finalVideo.mp4");

                    if(audioFile.exists()) {
                        audioFile.delete();
                    }
                    if(videoFile.exists()) {
                        videoFile.delete();
                    }
                }


    }

}

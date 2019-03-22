package BackgroundWorkers;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URISyntaxException;

import Classes.User;
import Classes.VideoPost;
import Fragments.TimeLineFragment;
import Fragments_Secondary.CameraFragment;


/**
 * Created by JEFF EMUVEYAN on 7/30/2018.
 */

public class CompressVideo extends AsyncTask<String, Integer, String> {

    private Context c;
    private String pathToRawVideoFile, pathToAudioFileForMerging;
    private User user;
    public static String pathToCompressedVideo;
    public static boolean proceedToUploadVideo;

    public static boolean isCompressingAVideo = false;


    private PostVideo postVideo;
    private static VideoPost videoPost;

    private String pathToCopiesFolder; //this is the path to the folder where silicompressor library
    // should save the video after it has compressed it.

    Handler handler;


    public CompressVideo(Context c, String pathToRawVideoFile, String pathToAudioFileForMerging, User user) {
        this.c = c;
        this.pathToRawVideoFile = pathToRawVideoFile;
        this.pathToAudioFileForMerging = pathToAudioFileForMerging;
        this.user = user;

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        isCompressingAVideo = false;

        pathToCompressedVideo = null;
        proceedToUploadVideo = false;// CompressVideo class is not allowed to upload a video unless CameraFragment tells it to.

        if(phoneHasSDcard()){
            pathToCopiesFolder = Environment.getExternalStorageDirectory()+"/Copies/";

        }else{
            pathToCopiesFolder = c.getFilesDir()+"/Copies";
        }

        handler = new Handler();
    }



    @Override
    protected String doInBackground(String... s) {

        isCompressingAVideo = true;

        return compressVideo(pathToRawVideoFile, pathToCopiesFolder);
    }



    @Override
    protected void onPostExecute(String compressVideoStatus) {

        if(compressVideoStatus.equals("DONE") && pathToCompressedVideo.equals("FAILED") == false){
            //If we got a 'DONE' compressing. It means all went well. So:

            //Now we need to know if we should upload the video from here or not so:
            if(proceedToUploadVideo){

                postVideo = new PostVideo(c, CompressVideo.pathToCompressedVideo, pathToAudioFileForMerging, videoPost, user);
                postVideo.execute();

            }else  {
                //just do nothing. Because if at this stage 'proceedToUploadVideo' is false then CameraFragment would be the one to upload the video.
            }

        }else{
            Toast.makeText(c,"Failed", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    protected void onProgressUpdate(Integer... progress) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(progress);
        isCompressingAVideo = true;
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();

    }



    String compressVideo(String pathToRawVideoFile, String pathToCopiesFolder){

        //NOTE: SiliCompressor will compress a given video then give it a new name and then save this video inside a folder path you provide.
        // This means that you can tell where the new video will be saved (Copies folder) but the name of the new video will be given by
        // siliCompressor itself not you. However, it is pretty easy sha to guess the names siliCompressor gives its videos.
        // 'pathToCopiesFolder' is simply the path of the folder we keep our compressed video ie the Copies folder
        // 'pathToFinalVideo' is the full path to the compressed video. It would look something like this: .../Copies/VIDEO_20180802_231614.mp4

        try {
            pathToCompressedVideo = SiliCompressor.with(c).compressVideo(pathToRawVideoFile, pathToCopiesFolder);//This will give us the path to the compressed video.

            if(pathToCompressedVideo != null){//this means we have compressed the video

                isCompressingAVideo = false;

                //now that we have compressed the video, lets merge the video with the audio now so that it is done in the background thread:
                //BUT NOTE that FFMPEG can only run one task at a time so what if CameraFragment is
                // still trying to trim the audio for us or is still merging audio to the raw uncompressed video.
                //we have to wait till it is done doing all those jobs. So:


               //if(CameraFragment.ffmpeg.isFFmpegCommandRunning() == false){ //if CameraFragment has finished using FFmpeg
                   //we go ahead to merge the audio and the compressed video:
                   CameraFragment.mergeAudioAndVideo(c, pathToAudioFileForMerging, pathToCompressedVideo, user);
                   //NOTE: Merging audio to video in ffmpeg is very fast. Its trimming audio that takes time.

              /* }else{
                   //You don't need to do anything here because When CameraFragment finishes doing its task,
                   // it'll merge this compressed video with the audio.
               }*/

                return "DONE";
            }else{
                return "FAILED";
            }

        } catch (URISyntaxException e) {
            return "FAILED";
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


    public static void  setVideoPost(VideoPost vP) {
        videoPost = vP;
    }

}

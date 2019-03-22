package BackgroundWorkers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import Classes.User;
import Classes.VideoPost;
import Fragments.TimeLineFragment;

/**
 * Created by JEFF EMUVEYAN on 1/28/2018.
 */

public class PostVideo extends AsyncTask<Void, Integer, String> {

    VideoPost videoPost;
    String pathToAudioFileForMerging;
    String pathToFinalVideo;
    File videoFile, audioFile;

    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;
    FileInputStream fileInputStream;

    int size;
    int dummy;
    int percentage;

    String videoLocationInServer;
    int serverResponseCode;

    String UPLOAD_URL;


    User user;

    Context c;
    Handler handler;

    public PostVideo(Context c, String pathToFinalVideo, String pathToTrimmedAudio, VideoPost videoPost, User user){

        this.c = c;
        this.videoPost = videoPost;
        this.user = user;
        this.pathToFinalVideo = pathToFinalVideo;
        this.pathToAudioFileForMerging = pathToTrimmedAudio;


    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //Let us get the video so that we can get its name and use that to check if the video exist in the server
        videoFile = new File(pathToFinalVideo);
        audioFile = new File(pathToAudioFileForMerging);
        //videoLocationInServer = "C:/xampp/htdocs/Copies/videos/"+videoFile.getName();

        videoLocationInServer = "/storage/ssd5/348/1574348/public_html/Copies/videos/"+videoFile.getName();//NOTE when running your app on a live server and
        //you want to find your 'videoLocationInServer', you must know this:
        /*Two files in a particular location may appear to have the same file path in the server but they will NOT!
         This is why in our server, folder 'uploads' and folder 'Copies' are in thesame place o but server internally assigns different location to them.
         So if you put an image in folder 'uploads', the image's location path will be /storage/h6/348/1574348/public_html/uploads.
         Now since 'Copies' folder is in the same place as 'uploads', you will expect the path of an image stored in it for example to be:
         /storage/h6/348/1574348/public_html/Copies. This looks possible but it will fail because the server assigns a different path to these folders internally.
         Therefore to find the path of the folder 'Copies', you must do the following:
         Create a dummy php script in the location you want to know the path. ie inside Copies folder.
         write this one line of code in the script 'echo dirname(__FILE__);', this will give you the correct file path to that location.
         We used the script in our server 'location_giver' to get the correct location of any file in Copies folder. This gives us:
         '/storage/ssd5/348/1574348/public_html/Copies/'. As you can see, the paths are not thesame.
         */


        //reveal the uploading cardiview in the timeLine Activity.
        TimeLineFragment.cardViewUpload.setVisibility(View.VISIBLE);

        TimeLineFragment.textViewUploadStatus.setText("Status:"); //this message means we have started uploading texta nd audio.


        //Now, how do we know if this video is an original or a copy, we do this:
        if(videoPost.getOriginalVideoPostId() == null){//this is an original video. So we send data to this link:

            //UPLOAD_URL = "http://192.168.43.123/Copies/upload.php";
            UPLOAD_URL = "https://androidtestsite.000webhostapp.com/Copies/upload.php";
        }else{
            //UPLOAD_URL = "http://192.168.43.123/Copies/upload_copy.php";
            UPLOAD_URL = "https://androidtestsite.000webhostapp.com/Copies/upload_copy.php";
        }

        handler = new Handler();

    }


    @Override
    protected String doInBackground(Void... voids) {

        TimeLineFragment.isUploadingAVideo = true;

        handler.post(new Runnable() {
            @Override
            public void run() {

                TimeLineFragment.progressBarUpload.setIndeterminate(true);//when it is uploading basic data like title, videoLink and even the audio file
                //the progressBar will be indeterminate. But when we are uploading a video, we change the progressBar to determinate.
            }
        });



        try{


            if (!videoFile.isFile()) {
                Log.e("Huzza", "Source File Does not exist");
                return null;
            }


            URL url = new URL(UPLOAD_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            //THE FOLLOWING LINE IS VERY IMPORTANT SO THAT WE CAN UPLOAD LARGE FILES WITHOUT GET OUT OF MEMORY EXCEPTION
            conn.setChunkedStreamingMode(1024);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("myFile", pathToFinalVideo);
            conn.setRequestProperty("myAudioFile", pathToAudioFileForMerging);
            dos = new DataOutputStream(conn.getOutputStream());

            //Now we want to send the video details(video pic-a 64 image, title, period..etc)  and the video file itself.

            //send the title
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getTitle());
            dos.writeBytes(lineEnd);

            //send the period
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"period\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getPeriod());
            dos.writeBytes(lineEnd);

            //so first send the video pic(image64)
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"image64\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getImageLink());  //we are sending a base64 String for now sha, we will convert it over there...
            dos.writeBytes(lineEnd);

            //send the videoLink
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"videoLink\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getVideoLink());
            dos.writeBytes(lineEnd);

            //send the audioLink
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"audioLink\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getAudioLink());
            dos.writeBytes(lineEnd);

            //send the number of copies
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"numberOfCopies\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getNumberOfCopies());
            dos.writeBytes(lineEnd);

            //send the number of likes
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"numberOfLikes\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getNumberOfLikes());
            dos.writeBytes(lineEnd);

            //send the user's id
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"userId\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(user.getUserId());
            dos.writeBytes(lineEnd);

            //send the originalVideoPostId. But we must not send a null value or else the video won't upload. So:
            if(videoPost.getOriginalVideoPostId() == null) {//if this ia an original video.
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"originalVideoPostId\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("none");
                dos.writeBytes(lineEnd);
            }else{// this video is a copy:

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"originalVideoPostId\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(videoPost.getOriginalVideoPostId());
                dos.writeBytes(lineEnd);
            }


            //send the videoType
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"videoType\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoPost.getVideoType());
            dos.writeBytes(lineEnd);




            //send the videoLocationInServer
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"videoLocationInServer\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(videoLocationInServer); //the video location in server
            dos.writeBytes(lineEnd);





            //Now we can simply stop here but we still have to send the audio and video file
            //so we send tha audio first:

            //NOTE: we only send tha audio if the video is an original. Copy videos do not go with thier audio files. So:

            if(videoPost.getOriginalVideoPostId() == null) {

                fileInputStream = new FileInputStream(audioFile);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"myAudioFile\";filename=\"" + pathToAudioFileForMerging + "\"" + lineEnd);
                dos.writeBytes(lineEnd);


                bytesAvailable = fileInputStream.available();
                Log.i("Huzza", "Initial .available : " + bytesAvailable);

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                size = bytesRead;

                //progressBar.setMax(size);


                while (bytesRead > 0) {

                    if(isCancelled()){//isCancelled() is inbuilt. its a good practise to check if the user has cancelled the asynkTask.
                        break;
                    }

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    //Now send data to the progressBar
                    //publishProgress(size - bytesRead);

                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            }
            //******DONE...YOU HAVE JUST SENT AN AUDIO!!********



            //Now send the video file
            fileInputStream = new FileInputStream(videoFile);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + pathToFinalVideo + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            Log.i("Huzza", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            size = bytesRead;


            handler.post(new Runnable() {
                @Override
                public void run() {
                    //set the max progress size of the progress bar
                    TimeLineFragment.progressBarUpload.setIndeterminate(false);
                    TimeLineFragment.progressBarUpload.setMax(size);
                }
            });



            while (bytesRead > 0) {

                if(isCancelled()){//isCancelled() is inbuilt. its a good practise to check if the user has cancelled the asynkTask.
                    break;
                }

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //Now send data to the progressBar
                publishProgress(size - bytesRead);

                TimeLineFragment.progressBarUpload.setProgress(size - bytesRead);//we can call a progressBar's 'setProgress' view from any background thread

                //calculate percentage (algorithm)
                dummy = size - bytesRead;

                percentage = (dummy*100)/size;

                //But for all other views, we have to use a handler when we want to call the view from any background thread. So:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //display %
                        TimeLineFragment.textViewUploadStatus.setText(percentage + "%");
                    }
                });



            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            //******DONE...YOU HAVE JUST SENT A VIDEO!!********


            //Now we continue:

            serverResponseCode = conn.getResponseCode();

            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (final MalformedURLException e) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    TimeLineFragment.isUploadingAVideo = false;
                    TimeLineFragment.cardViewUpload.setVisibility(View.GONE);
                    Toast.makeText(c, "uploading stopped", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        } catch (final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    TimeLineFragment.isUploadingAVideo = false;
                    TimeLineFragment.cardViewUpload.setVisibility(View.GONE);
                    Toast.makeText(c, "uploading stopped", Toast.LENGTH_LONG).show();
                }
            });
                e.printStackTrace();
        }


        if (serverResponseCode == 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            } catch (final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TimeLineFragment.isUploadingAVideo = false;
                        TimeLineFragment.cardViewUpload.setVisibility(View.GONE);
                        Toast.makeText(c, "uploading stopped", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return sb.toString();         //return success message from the server
        }else {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    TimeLineFragment.isUploadingAVideo = false;
                    TimeLineFragment.cardViewUpload.setVisibility(View.GONE);
                    Toast.makeText(c, "uploading stopped", Toast.LENGTH_LONG).show();
                }
            });
            return "Could not upload, Try again";
        }

    }


    @Override
    protected void onProgressUpdate(Integer... progress) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(progress);


        TimeLineFragment.progressBarUpload.setProgress(progress[0]);

        //calculate percentage (algorithm)
        dummy = progress[0];

        percentage = (dummy*100)/size;

        //display %
        TimeLineFragment.textViewUploadStatus.setText(percentage + "%");

    }



    @Override
    protected void onPostExecute(String message) {

        TimeLineFragment.isUploadingAVideo = false;

        TimeLineFragment.cardViewUpload.setVisibility(View.GONE);

        Toast.makeText(c, message, Toast.LENGTH_LONG).show();

        if(videoPost.getOriginalVideoPostId() == null && message.equals("Successfully uploaded!!")){//if this is an original video and if the upload is successful, we reload the timeLine so that the user can see his new upload:

            TimeLineFragment.loadData(0, "refresh");
        }
    }




    @Override
    protected void onCancelled() {
        super.onCancelled();
        //this method runs on the UI thread
        TimeLineFragment.isUploadingAVideo = false;

        TimeLineFragment.cardViewUpload.setVisibility(View.GONE);

        Toast.makeText(c, "upload cancelled", Toast.LENGTH_SHORT).show();
    }


}


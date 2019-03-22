package Fragments_Secondary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import BackgroundWorkers.CompressVideo;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.kosalgeek.android.photoutil.ImageBase64;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import BackgroundWorkers.BackgroundWorker;
import BackgroundWorkers.PostVideo;
import Classes.User;
import Classes.VideoPost;
import at.markushi.ui.CircleButton;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


import static android.content.ContentValues.TAG;
import static android.hardware.Camera.CameraInfo;
import static android.hardware.Camera.open;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.navigation;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static Button postButton;
    public static Button cancelButton;
    CircleButton startButton;
    TextView recordMsg, textViewCountDown, textViewCountDownProgress;
    SeekBar seekBar;
    static TextView timerTextView;
    public static CircularProgressBar progressBar;
    static VideoView videoView;
    static EmojiconEditText editTextVideoTitle;
    static ImageView emojiButton;
    Camera camera;
    SurfaceHolder holder;
    MediaRecorder recorder;
    static String pathToRawVideoFile;
    ImageView imageViewRotateCamera, imageViewFlash;
    boolean frontCameraIsOpen = false;
    boolean isFlashOn = false;
    boolean phoneIsRecording = false;
    boolean isCameraToCountDown = false;
    int secondsToCountDown = 0; //default

    Camera.Parameters p;
    MediaPlayer mediaPlayer;

    View view;
    static String songPath;
    String originalVideoPostId;
    static int pointInSec;
    static long durationOfFullSong;
    static String pathToAudioFileForMerging;
    static String pathToFinalUncompressedVideo;
    public static String pathToFinalCompressedVideo;

    private static  ProgressDialog progressDialog;
    public static FFmpeg ffmpeg;

    static Context c;
    static Random random;
    public static String randomNumber;
    static int kilobytesOfTrimmedSong;

    String theDate, image64;
    EmojIconActions emojIcon;

    CountDownTimer countDownTimer;

    static double inputNumber = 0;

    static int progressBarMaximum;

    static User user;

    public static CompressVideo compressVideo;
    public static PostVideo postVideo;


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
        view = inflater.inflate(R.layout.fragment_camera, container, false);

        startButton = (CircleButton)view.findViewById(R.id.start);
        postButton = (Button)view.findViewById(R.id.post);
        cancelButton = (Button)view.findViewById(R.id.cancel);
        videoView = (VideoView)view.findViewById(R.id.videoView);
        recordMsg = (TextView)view.findViewById(R.id.place_holder_textView);
        timerTextView = (TextView)view.findViewById(R.id.timerTextView);
        textViewCountDown = (TextView)view.findViewById(R.id.textViewCountDown);
        textViewCountDownProgress = (TextView)view.findViewById(R.id.textViewCountDownProgress);
        seekBar = (SeekBar)view.findViewById(R.id.seekBar);
        imageViewRotateCamera = (ImageView)view.findViewById(R.id.imageView_rotate);
        imageViewFlash = (ImageView)view.findViewById(R.id.imageView_flash);
        progressBar = ( CircularProgressBar) view.findViewById(R.id.progressBar);
        editTextVideoTitle = (EmojiconEditText) view.findViewById(R.id.emoji_edit_text);
        emojiButton = (ImageView) view.findViewById(R.id.emoji_button);

        emojIcon = new EmojIconActions(getActivity(), view, emojiButton, editTextVideoTitle);
        emojIcon.ShowEmojicon();

        ffmpeg = FFmpeg.getInstance(getActivity());
        c = getActivity();

        user = User.findById(User.class, (long) 1);

        random = new Random();
        randomNumber = String.valueOf(random.nextInt(1000000));

        pathToAudioFileForMerging = null;

        progressDialog = new ProgressDialog(c);
        progressDialog.setTitle(null);

        //Hide the bottom menu
        navigation.setVisibility(View.INVISIBLE);
        //and the other buttons
        postButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        editTextVideoTitle.setVisibility(View.GONE);
        emojiButton.setVisibility(View.GONE);
        timerTextView.setVisibility(View.INVISIBLE);
        textViewCountDownProgress.setVisibility(View.INVISIBLE);

        loadFFMpegBinary();

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E-yyyy-MM-dd");
        theDate = String.valueOf(ft.format(date));


        //When the fragment starts,we collect the song name, path from previous fragment and position to start playing the song from
        Bundle bundle = getArguments();
        songPath = bundle.getString("songPath");
        pointInSec = bundle.getInt("pointInSec");
        originalVideoPostId = bundle.getString("originalVideoPostId");


        //Now we need to check the number of cameras on the device,so:
        if(!(Camera.getNumberOfCameras() > 1)){
            //if the device does not have 2 cameras, there is no need to rotate so:
            imageViewRotateCamera.setVisibility(View.GONE);
        }

        //we check if the device has a flash
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            //no flash on the device, so:
            imageViewFlash.setVisibility(View.GONE);
        }


        getTrimmingPercentage(songPath);
        progressBar.setMaximum((float)progressBarMaximum);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(phoneIsRecording) {//if phone is recording we cancel the record...
                    //NOTE: We dont use the method stopRecording because this method will stop the record then automatically display a preview
                    //we dont want to preview, we want the entire record to stop. So we use:

                    //stop the song
                    mediaPlayer.pause();

                    //then close the fragment
                    //but first: some part of the video has already been saved so we quickly delete it:
                    File rawVideo;
                    if(phoneHasSDcard()){
                        rawVideo = new File(pathToRawVideoFile); //we have already modified 'pathToRawVideoFile' to work wether the user has memory card or not;

                        if(rawVideo.exists()) {
                            rawVideo.delete();
                        }
                    }

                    //and also the countDownTimer wont stop. I guess it runs on a background thread by default so we gotta do this:
                    countDownTimer.cancel();

                    //now you can close the fragment
                    getActivity().onBackPressed();

                    //There is no need to now say  phoneIsRecording = false; because the fragment is being closed entirely.

                }else{// we are free to start the recording:

                    if(isCameraToCountDown){//first we check if the user wants a count down before starting the record:
                        countDown(secondsToCountDown);//After counting down, the record will start automatically.
                    }else{
                        //we proceed to start recording:
                        //first hide these:
                        textViewCountDown.setVisibility(View.INVISIBLE);
                        seekBar.setVisibility(View.INVISIBLE);

                        //now record:
                        initRecorder();
                        beginRecording();
                        phoneIsRecording = true;
                        //now we change the logo on the camera button
                        startButton.setImageResource(R.drawable.ic_cancel_black_24dp);
                    }
                }

            }
        });


        imageViewRotateCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(frontCameraIsOpen == false){
                    rotateCamera(1);
                    frontCameraIsOpen = true;
                }
                else{
                    rotateCamera(0);
                    frontCameraIsOpen = false;
                }

            }
        });

        imageViewFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFlash();
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRecording();
            }
        });


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editTextVideoTitle.getText().toString().equals("")){

                    Toast.makeText(getActivity(), "write a title...", Toast.LENGTH_SHORT).show();

                }else {
                    image64 = ImageBase64.encode(retrievThumbnailFromVideo(pathToFinalUncompressedVideo));

                    //Build a baby VideoPost object using this constructor. (NOTE the filed "isLiked" is always null)
                    VideoPost videoPost;

                    if (originalVideoPostId == null) {//if this an original video
                        videoPost = new VideoPost(
                                encodedString(editTextVideoTitle.getText().toString()),//we have to encode the string because it may contain Emoji
                                theDate,
                                image64,//Let us send the image64 for now, when we get to our PHP, we will use it to get a proper link.
                                "http://androidtestsite.000webhostapp.com/Copies/videos/finalVideo_" + randomNumber + user.getUserName().replace(" ", "_") + ".mp4",
                                "http://androidtestsite.000webhostapp.com/Copies/audios/copiesTrimmedAudio_" + randomNumber + user.getUserName().replace(" ", "_") + ".mp3",
                                "0",
                                "0",
                                user.getUserId(),
                                originalVideoPostId,//a null value.
                                "original"
                        );
                    }else{

                        videoPost = new VideoPost(
                                encodedString(editTextVideoTitle.getText().toString()),//we have to encode the string because it may contain Emoji
                                theDate,
                                image64,//Let us send the image64 for now, when we get to our PHP, we will use it to get a proper link.
                                "http://androidtestsite.000webhostapp.com/Copies/videos/finalVideo_" + randomNumber + user.getUserName().replace(" ", "_") + ".mp4",
                                "http://androidtestsite.000webhostapp.com/Copies/audios/copiesTrimmedAudio_" + randomNumber + user.getUserName().replace(" ", "_") + ".mp3",
                                "0",
                                "0",
                                user.getUserId(),
                                originalVideoPostId,//it will contain a value this time around.
                                "copy"
                        );

                    }

                    //At this stage, we are free to upload the video as long as CompressVideo has finished compressing. So:
                    if(CompressVideo.isCompressingAVideo == false){// if we are done compressing the video, we upload:

                        postVideo = new PostVideo(c, CompressVideo.pathToCompressedVideo, pathToAudioFileForMerging, videoPost, user);
                        postVideo.execute();

                    }else{//if the video is still compressing, we tell the CompressVideo class to upload the video when it is finished compressing.
                        CompressVideo.proceedToUploadVideo = true;

                        //initialize its videoPost
                        CompressVideo.setVideoPost(videoPost);
                    }

                    //Start the TimeLine Fragment
                    //Now since we are in camera fragment, we need to pop the back stack twice to remove
                    //The camera fragment and SelectStartingPoint fragment to give us SelectTrack fragment

                    android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.popBackStack();
                    fm.popBackStack();

                    //Now we need to move the viewPager from SelectTrack fragment to timeline fragment
                    //but first we:
                    MainActivity.showViewPager();
                    //Now we move to the timeline fragment by saying:
                    vpPager.setCurrentItem(0);

                    //do this since we are going there
                    MainActivity.navigation.setSelectedItemId(R.id.navigation_time_line);

                    //do this too
                    //show the bottom menu
                    navigation.setVisibility(View.VISIBLE);
                }

            }
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                //where progress value range from 0 -> 10 (the seekBars Maximum value is 10)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //if the user is starts moving the seekBar. (Note: the value of the seekBars progress is from 0 - 10)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //once the user stops moving the seekBar. (Note: the value of the seekBars progress is from 0 - 10)
                if(seekBar.getProgress() == 0){
                    textViewCountDown.setText("Timer: None");
                    secondsToCountDown = seekBar.getProgress();// this sets secondsToCountDown to default of 0
                    isCameraToCountDown = false;
                }else if(seekBar.getProgress() > 0){
                    textViewCountDown.setText("Timer: "+seekBar.getProgress()+" sec");

                    //this also means the user wants a count down timer. So lets get the seconds the user wants the count down
                    // timer to give him:
                    secondsToCountDown = seekBar.getProgress();
                    isCameraToCountDown = true;
                }
            }
        });


        return view;
    }


    private void countDown(int seconds){// secondsToCountDown

        textViewCountDownProgress.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);// Hide this button

        //hide these:
        imageViewRotateCamera.setVisibility(View.INVISIBLE);
        imageViewFlash.setVisibility(View.INVISIBLE);
        textViewCountDown.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);

        //convert seconds to milliseconds
        int milliseconds = seconds * 1000;
        countDownTimer = new CountDownTimer(milliseconds+1000, 1000) {//15 seconds total time, 1 second interval
            //NOTE: we added 1000 milliseconds ie 1 second to accommodate lag or delay
            @Override
            public void onTick(long l) {
                textViewCountDownProgress.setText(String.valueOf(l/1000));// We divide because 'l' is in milliseconds.
            }

            @Override
            public void onFinish() {
                //Do what happens when the timer ends:
                //Start recording
                initRecorder();
                beginRecording();
                phoneIsRecording = true;
                startButton.setVisibility(View.VISIBLE);// Hide this button
                //now we change the logo on the camera button
                startButton.setImageResource(R.drawable.ic_cancel_black_24dp);
                textViewCountDownProgress.setVisibility(View.INVISIBLE);

                //reset these
                secondsToCountDown = 0;
                isCameraToCountDown = false;

            }
        };

        countDownTimer.start();
    }


    private void stopPlayBack() {

        videoView.stopPlayback();
    }

    private static void playRecording(String videoPath) {

        MediaController mc = new MediaController(c);
        videoView.setMediaController(mc);
        videoView.setVideoPath(videoPath);
        videoView.start();
        //stopButton.setEnabled(true);

    }

    private void stopRecording() {

        //first we checkif the flash is on so that we can off it:
        if(isFlashOn){

            //Off the flash
            p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);

            isFlashOn = false;

        }

        if(recorder != null){

            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);

            try{
                recorder.stop();

            }catch(IllegalStateException e){

            }
            releaseRecorder();
            recordMsg.setText("");
            timerTextView.setText("");
            releaseCamera();
            startButton.setEnabled(false);
            //stopButton.setEnabled(false);
            //playButton.setEnabled(true);
        }
    }

    private void cancelRecording(){

        //here we simply terminate the Fragment
        //but first let us check if the finalVideo and trimmedAudio are already saved in the phone so that we can delete them
        deleteFiles();

        //we also stop video compression if it is going on:
        if(CompressVideo.isCompressingAVideo){//if we are still compressing a video:
            CameraFragment.compressVideo.cancel(true);// this will stop compression.

            //let's also delete the compressed video file if any part of it has already been saved:
            File file = new File(pathToFinalCompressedVideo);

            if(file.exists()){
                file.delete();
            }
        }

        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }

        //now you can terminate
        getActivity().onBackPressed();
    }

    private void releaseCamera() {

        if(camera != null){

            try{
                camera.reconnect();
            }catch(IOException e){

            }
            camera.release();
            camera = null;

        }
    }

    private void releaseRecorder() {

        if(recorder != null){

            recorder.release();
            recorder = null;
        }
    }

    private void beginRecording() {

        //Hide some buttons
        imageViewRotateCamera.setVisibility(View.INVISIBLE);
        imageViewFlash.setVisibility(View.INVISIBLE);
        timerTextView.setVisibility(View.VISIBLE);

        //Get the timer ready
        countDownTimer = new CountDownTimer(16000, 1000) {//15 seconds total time, 1 second interval
            //NOTE we allow the timer to have a 1 second lead time ie 16 seconds
            @Override
            public void onTick(long l) {
                timerTextView.setText(""+l/1000);
            }

            @Override
            public void onFinish() {
                //Do what happens when the timer ends
            }
        };

        recorder.setOnInfoListener(this);
        recorder.setOnErrorListener(this);
        recorder.start();
        playSong(pointInSec);
        countDownTimer.start();
        recordMsg.setText("Recording...");
        //lest just put this here
        inputNumber = 0;



    }

    private void initRecorder() {

        if(recorder != null) return;

        //Now we want to specify where we want our files to be saved to
        if( phoneHasSDcard()) {
            pathToRawVideoFile = Environment.getExternalStorageDirectory() + "/Copies/rawVideo.mp4";
        }else{
            pathToRawVideoFile = getActivity().getFilesDir()+ "/Copies/rawVideo.mp4";
        }


        File outFile = new File(pathToRawVideoFile);

        if(outFile.exists())
            outFile.delete();

        try{

            camera.stopPreview();
            camera.unlock();
            recorder = new MediaRecorder();

            //When the camera is facing front, you need to change the orientation or else it wont record straight video
            //90 is for back camera, 270 for front facing camera. so:

            if(frontCameraIsOpen){

                recorder.setOrientationHint(270);
            }else{
                recorder.setOrientationHint(90);
            }

            recorder.setCamera(camera);

            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoSize(640,480);
            recorder.setVideoFrameRate(15);
            recorder.setVideoEncodingBitRate(3000000);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setMaxDuration(15000); // limit to fifteen seconds
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setOutputFile(pathToRawVideoFile);
            recorder.prepare();

        }catch(Exception e){
            e.printStackTrace();
        }


    }


    private boolean initCamera() {


        try{
            camera = open();
            camera.setDisplayOrientation(90);
            camera.lock();
            holder = videoView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        }catch(RuntimeException e){

            return false;
        }

        return true;
    }


    private void rotateCamera(int cameraID) {

        //cameraID should be 1 to open front facing camera. 0 for back facing camera.

        releaseCamera();
        releaseRecorder();

        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraID, info);

        try{
            camera = Camera.open(cameraID);
            camera.getParameters().setRotation(getCorrectCameraOrientation(getActivity(), info  , camera));
            camera.setDisplayOrientation(90);
            //camera.setDisplayOrientation(getCorrectCameraOrientation(this, info  , camera));
            Camera.Parameters camParams = camera.getParameters();
            camera.lock();


            //setDisplayOrientation(this, 1, camera);

            holder = videoView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        }catch(Exception e){
            e.printStackTrace();
        }

        if(camera != null){

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{

            Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
        }


    }


    private int getCorrectCameraOrientation(Activity activity, CameraInfo info, Camera camera){

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degress = 0;

        switch(rotation){

            case Surface.ROTATION_0:
                degress = 0;
                break;

            case Surface.ROTATION_90:
                degress = 90;
                break;

            case Surface.ROTATION_180:
                degress = 180;
                break;

            case Surface.ROTATION_270:
                degress = 270;
                break;
        }

        int result;

        if(info.facing == CameraInfo.CAMERA_FACING_FRONT){

            result = (info.orientation + degress)% 360;
            result = (360 - result)% 360;
        }else{
            result = (info.orientation - degress + 360)% 360;
        }

        return result;

    }

    private void  onFlash(){

        //In Android, the flash that will on depends on the camera that is working.
        //so, if your back facing camera is open, when you on the flash, the back flash will on.
        //if your front facing camera is open, when you on the flash, the front flash will on by itself
        //it is automatic, you dont control it.

        p = camera.getParameters();

        if(isFlashOn == false){

            //if ths flash is off, on it.

            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();

            isFlashOn = true;

        }else{
            //Off the flash
            p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);

            isFlashOn = false;

        }

    }

    private void playSong(int pointInSec){

        //we play the song
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(pointInSec);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
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


    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }


    private static void execFFmpegBinary(final Context c, final String[] command, final String operation) {

        try {

            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    // addTextViewToLayout("FAILED with output : "+s);
                    Toast.makeText(c, "Failed...", Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(c)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Error")
                            .setMessage("Failed "+s)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //getActivity().finish();
                                }
                            })
                            .create()
                            .show();

                }

                @Override
                public void onSuccess(String s) {

                    if(operation.equals("trim")) {
                        //If this was a trim operation, once trimming is successful:

                        // (NOTE this small part here only concerns us if we used a file that originally had space.)
                        //its time to delete the copiedAudioFile.mp3 so that only the trimmed one remains. We just do this fo fun sha
                        //because Android will overwrite this video anytime we make a new one.
                        File copiedAudioFile;

                        if(phoneHasSDcard()){
                            copiedAudioFile = new File(Environment.getExternalStorageDirectory()+"/Copies", "copiedAudioFile.mp3");
                            if(copiedAudioFile.exists()) {
                                copiedAudioFile.delete();
                            }
                        }else{
                            copiedAudioFile = new File(c.getFilesDir()+"/Copies", "copiedAudioFile.mp3");
                            if(copiedAudioFile.exists()) {
                                copiedAudioFile.delete();
                            }
                        }

                        //Toast.makeText(c, "Done trimming", Toast.LENGTH_LONG).show();
                        //now we merge the audio and video
                        mergeAudioAndVideo(pathToAudioFileForMerging, pathToRawVideoFile);

                    }
                    if(operation.equals("merge")){
                        //If this was a merge operation, once the video and audio have been merged we:

                        //delete the rawVideoFile (ie the video file that has not been merged with an audio)
                        //But we need to make sure that CompressVideo has finished compressing this raw video so:

                        if(!CompressVideo.isCompressingAVideo) {//if isCompressingAVideo is false
                            // it means we are done compressing the raw video we can go ahead and delete the raw video:
                            File copiedVideoFile;

                            if (phoneHasSDcard()) {
                                copiedVideoFile = new File(Environment.getExternalStorageDirectory() + "/Copies", "rawVideo.mp4");
                                copiedVideoFile.delete();
                            } else {
                                copiedVideoFile = new File(c.getFilesDir() + "/Copies", "rawVideo.mp4");
                                copiedVideoFile.delete();
                            }

                            //and later merge the compressed video with the audio:
                            mergeAudioAndVideo(c, pathToAudioFileForMerging, CompressVideo.pathToCompressedVideo, user);
                        }else{
                            //if CompressVideo is still compressing:
                            //You don't need to do anything here because When CompressVideo finishes doing its task,
                            // it'll delete the raw video
                            //and later merge the compressed video with the audio.
                        }


                        //finally:
                        postButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        editTextVideoTitle.setVisibility(View.VISIBLE);
                        emojiButton.setVisibility(View.VISIBLE);

                        playRecording(pathToFinalUncompressedVideo);//This method plays the playback

                    }
                }

                @Override
                public void onProgress(String s) {

                    if(operation.equals("trim")) {

                        //progressDialog.setMessage(s);
                        progressBar.setIndeterminate(false);
                        progressBar.setVisibility(View.VISIBLE);

                        //The progress message we receiev comes in different formats. We only know two types of progress message we can ge so
                        //we have two ways to display our progress bar depending on the type of message we get:

                        if (s.substring(0, 4).trim().equals("size")) {//if the first text in the message is "size"
                            //a clever way to extract the kilobytes
                            String kiloBytesCompleted = s.replace("size=", "").replace(" ", "").replace("kB", "    ").substring(0, 4);

                            try {
                                //sometimes, strange text still attach to the kilobyte, i donno why. this is my work around:
                                Integer.parseInt(kiloBytesCompleted.trim());//if we get only integers then we can process this integers:

                                int percentage = (Integer.parseInt(kiloBytesCompleted.trim()) * 100) / kilobytesOfTrimmedSong;
                                //progressDialog.setMessage("Processing trim\n" +percentage+"%");
                                if(percentage > 100){
                                    percentage = 100;//fix a sneaky fucking bug
                                }
                                timerTextView.setText(percentage + "%");
                                progressBar.setProgress((float) Integer.parseInt(kiloBytesCompleted.trim()));

                            } catch (NumberFormatException e) {
                                //do nothing
                            } catch (NullPointerException e) {
                                //do nothing
                            }
                        }
                        if(s.substring(0,5).trim().equals("frame")) {//if the first text in the message is "frame"

                            try {

                                progressBar.setMaximum(15);//the maximum seconds ffmpeg will reach.
                                int time = Integer.parseInt(s.replace(":", "").substring(50, 53)); //weed out all the unwanted progress message
                                //until we are left with just the time.

                                timerTextView.setText(String.valueOf(getTrimmingPercentage(time)) + "%");

                                progressBar.setProgress((float) time);

                            } catch (NumberFormatException e) {
                                //do nothing
                            } catch (NullPointerException e) {
                                //do nothing
                            } catch (Exception e) {
                                //do nothing
                            }
                        }

                    }

                }

                @Override
                public void onStart() {

                    //progressDialog.setMessage("Processing...");
                    //progressDialog.show();
                    timerTextView.setVisibility(View.VISIBLE);
                    timerTextView.setText("please wait...");
                    progressBar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg "+command);
                    progressDialog.dismiss();
                    timerTextView.setVisibility(View.INVISIBLE);
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            Toast.makeText(c, "Error", Toast.LENGTH_LONG).show();
        }
    }



    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("FFmpeg is not supported on your device")
                .setMessage("FFmpeg is not supported on your device")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .create()
                .show();

    }



    private static void getTrimmingPercentage(String fullSongPath){
        //What this method does is this: as ffmpeg is trimming the song, this method will tell you how many percentage it'll take for the process to
        //complete.
        //the things we need here are: the path to the original song (the untrimed song)
        //and the kilobytes ffmpeg is processing.

        //first get the durationOfFullSong of the full song (in milliseconds)
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fullSongPath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now let us get the durationOfFullSong of the song
        durationOfFullSong = mediaPlayer.getDuration(); //this returns durationOfFullSong of song in milliseconds

        //now get the size of the full song
        File fullSong = new File(fullSongPath);

        double bytesOfFullSong = fullSong.length();//this returns the file size of the song in bytes.
        double kilobytesOfFullSong = (bytesOfFullSong/1024);//in kilobytes.


        //So what we want to do now is predict how many kilobytes 15 seconds from a full song will be.
        //So mathematically, if durationOfFullSong = kilobytesOfFullSong
        //then,                    15000mn          = x
        //where x is the kilobyes of 15 seconds trim form the full song

        kilobytesOfTrimmedSong = (15000 * (int)kilobytesOfFullSong) / (int)durationOfFullSong;

        progressBarMaximum = (int)kilobytesOfTrimmedSong;//just do this here for the progressBar

        //yea, now that we know how many kilobytes our trimmed song should be, we can compare this to the kilobytes the ffmpeg progesss
        //is giving us to know how many percent has been trimmed.

        //So mathematically, if kilobytesOfTrimSong = 100%
        //                      kiloBytesCompleted  = x%

        //we won't implement this formular here because we can't be creating a MediaPlayer everytime ffmpeg trims a kilobyte.
        // It'll hang the app. Go to your ffmpeg progress method and see how we implemented the formula there..

        //this is how the formula looks when implemented:
        //int percentage = (Integer.parseInt(kiloBytesCompleted.trim()) * 100) / kilobytesOfTrimmedSong;
    }


    private static int  getTrimmingPercentage(int inputSeconds){

        //inputSeconds is the number of seconds FFMPEG has spent trimming your song.
        int totalSeconds = 15; // this is the number of total seconds FFMPEG is supposed to spend totally.

        //so if totalSeconds = 100%
        //inputSeconds = x?

        int percentage; //let that x = percentage. So:

        percentage = (inputSeconds * 100)/ totalSeconds;

        return percentage;
    }


    public static void trimSong(Context c, String songPath){

       /* NOTE: FFMPEG won't work if the output filename it wants to produce already exists.
       So if FFMPEG is to produce an output file name called output.mp4, better make sure that a file does not already have that name in the directory.
       this is why we attach a random number to every output file name so that all our output files will be different.

       But this dosen't concern Android, Android automatically replace old files with new ones.
        This is why we can make several video files having same name (rawVideo.mp4) on top one another.
        However, once you are using FFMPEG, you must delete your old file first before you make a new one OR you change their filenames

       */

        String commandString;

        //First, we check to see if the song name has stupid space
        //We do this because ffmpeg cant trim audio files that have space in thier names
        if(songPath.contains(" ")){
            //we cannot use this file unless we rename it somehow. Here is our trick to trim this type of file:
            copyAudioAndTrim(songPath);

        }else { //if the song name is ok (it has no space)

            double startTimeInSec = (pointInSec / 1000); // convert ms to seconds

            if(phoneHasSDcard()) {
                commandString = "-ss " + startTimeInSec + " -i " + songPath + " -to 15 " + Environment.getExternalStorageDirectory()+"/Copies/copiesTrimmedAudio_"+randomNumber+user.getUserName().replace(" ", "_")+".mp3";//yes you need to check if the user name has space too
                pathToAudioFileForMerging = Environment.getExternalStorageDirectory()+"/Copies/copiesTrimmedAudio_"+randomNumber+user.getUserName().replace(" ", "_")+".mp3";
            }else{
                commandString = "-ss " + startTimeInSec + " -i " + songPath + " -to 15 " + c.getFilesDir()+"/Copies/copiesTrimmedAudio_"+randomNumber+user.getUserName().replace(" ", "_")+".mp3";
                pathToAudioFileForMerging = c.getFilesDir()+"/Copies/copiesTrimmedAudio_"+randomNumber+user.getUserName().replace(" ", "_")+".mp3";
            }

            String[] command = commandString.split(" ");


            if (command.length != 0) {
                execFFmpegBinary(c, command, "trim");

            } else {
                Toast.makeText(c, "You cannot execute empty command", Toast.LENGTH_LONG).show();
            }

        }//END IF

    }


    private static void  mergeAudioAndVideo(String audioPath, String videoPath){
        //This method merges audio to an uncompressed video for preview

        /* NOTE: FFMPEG won't work if the output filename it wants to produce already exists.
       So if FFMPEG is to produce an output file name called output.mp4, better make sure that a file does not already have that name in the directory.
       this is why we attach a random number to every output file name so that all our output files will be different.

       But this dosen't concern Android, Android automatically replace old files with new ones.
        This is why we can make several video files having same name (rawVideo.mp4) on top one another.
        However, once you are using FFMPEG, you must delete your old file first before you make a new one OR you change their filenames

       */

        String commandString;

        //PLEASE SEE HOW WE ADDED TWO RANDOM NUMBERS TO THE NAMES OF OUR OUTPUT VIDEO SO THAT IT WON'T CLASH WITH THE NAME OF THE MERGED COMPRESSED VIDEO
        //WE WILL MAKE SOON. ELSE, FFMPEG WILL GIVE US PROBLEMS
        if(phoneHasSDcard()) {
            commandString = "-i " + audioPath + " -i " + videoPath + " -acodec copy -vcodec copy "+ Environment.getExternalStorageDirectory()+"/Copies/finalVideo_"+randomNumber+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
            pathToFinalUncompressedVideo = Environment.getExternalStorageDirectory()+"/Copies/finalVideo_"+randomNumber+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
        }else{
            commandString = "-i " + audioPath + " -i " + videoPath + " -acodec copy -vcodec copy " + c.getFilesDir()+"/Copies/finalVideo_"+randomNumber+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
            pathToFinalUncompressedVideo = c.getFilesDir()+"/Copies/finalVideo_"+randomNumber+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
        }

        final String[] command = commandString.split(" ");

        if (command.length != 0) {

            execFFmpegBinary(c, command, "merge");

        } else {
            Toast.makeText(c, "You cannot execute empty command", Toast.LENGTH_LONG).show();
        }
    }


    private static void copyAudioAndTrim(String songPath){
       /* As the name implies, we do three things:
        1) Copy the song(so that we can have a new dummy song with an acceptable name,
        2) Trim the necessary part of this new dummy song,
        3) then delete the dummy song leaving the trimmed part for us to use as we like.

        Note: really we dont need to delete our garbage files because anytime the user makes a new video
        they will be replaced (But if it concerns FFmpeg you will have to delete o). They don't get
        piled up. Android automatically replaces old files with new ones. We are
        just deleting for the fun of it and to save user memory space.
       */

        //Now since there is no way for us to be alerted when the song has been successfully copied, we use
        //we need to use an Asynck task class so that its onPostExecute method can tell us when a task has benn completed

        BackgroundWorker backgroundWorker = new BackgroundWorker(c, songPath, pointInSec);
        backgroundWorker.execute();

    }


    public Bitmap retrievThumbnailFromVideo(String video_Path){

        Bitmap bitmap;

        bitmap = ThumbnailUtils.createVideoThumbnail(video_Path, Thumbnails.MINI_KIND);

        return bitmap;
    }


    private void deleteFiles(){

        File audioFile, videoFile;

        if(phoneHasSDcard()){
            audioFile = new File(pathToAudioFileForMerging);
            videoFile = new File(pathToFinalUncompressedVideo);

            if(audioFile.exists()) {
                audioFile.delete();
            }
            if(videoFile.exists()) {
                videoFile.delete();
            }
        }else{
            audioFile = new File(pathToAudioFileForMerging);
            videoFile = new File(pathToFinalUncompressedVideo);

            if(audioFile.exists()) {
                audioFile.delete();
            }
            if(videoFile.exists()) {
                videoFile.delete();
            }
        }

    }

    public static String encodedString(String text) {
        String encodedUrl =null;
        try {
            encodedUrl = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return encodedUrl;
        }
        return encodedUrl;
    }



    public static void  mergeAudioAndVideo(final Context c, String audioPath, String videoPath, User user){

        //Random random = new Random();
        //String randomNumber = String.valueOf(random.nextInt(1000000));

        /* NOTE: FFMPEG won't work if the output filename it wants to produce already exists.
       So if FFMPEG is to produce an output file name called output.mp4, better make sure that a file does not already have that name in the directory.
       this is why we attach a random number to every output file name so that all our output files will be different.

       But this dosen't concern Android, Android automatically replace old files with new ones.
        This is why we can make several video files having same name (rawVideo.mp4) on top one another.
        However, once you are using FFMPEG, you must delete your old file first before you make a new one OR you change their filenames

       */
        String commandString;

        if(phoneHasSDcard()) {
            commandString = "-i " + audioPath + " -i " + videoPath + " -acodec copy -vcodec copy "+ Environment.getExternalStorageDirectory()+"/Copies/finalVideo_"+ randomNumber+user.getUserName().replace(" ", "_")+".mp4";
            pathToFinalCompressedVideo = Environment.getExternalStorageDirectory()+"/Copies/finalVideo_"+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
        }else{
            commandString = "-i " + audioPath + " -i " + videoPath + " -acodec copy -vcodec copy " + c.getFilesDir()+"/Copies/finalVideo_"+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
            pathToFinalCompressedVideo = c.getFilesDir()+"/Copies/finalVideo_"+randomNumber+user.getUserName().replace(" ", "_")+".mp4";
        }

        final String[] command = commandString.split(" ");

        if (command.length != 0) {

            execFFmpegBinary(c, command);

        } else {
            Toast.makeText(c, "You cannot execute empty command", Toast.LENGTH_LONG).show();
        }

    }


    private static void execFFmpegBinary(final Context c, final String[] command) {

        try {

            File trimmedAudioFile = new File(pathToAudioFileForMerging);

            if(!ffmpeg.isFFmpegCommandRunning() && trimmedAudioFile.exists()) {
                //if ffmpeg is no longer in use and we have a trimmed audioFile:

                ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                    @Override
                    public void onFailure(String s) {
                        new AlertDialog.Builder(c)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Error here")
                                .setMessage("Failed " + s)
                                .setCancelable(true)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //getActivity().finish();
                                    }
                                })
                                .create()
                                .show();

                        CompressVideo.pathToCompressedVideo = "FAILED";//tell CompressVideo that things have failed.
                    }

                    @Override
                    public void onSuccess(String s) {

                        CompressVideo.pathToCompressedVideo = pathToFinalCompressedVideo; //send them the path to the final compressed video
                        //that we will later use for upload.

                        //Now since CompressVideo was the one to trigger the action to merge the video and audio,
                        //it means that when it is done merging audio and video, it will be the one to upload the video to the database. So:
                        CompressVideo.proceedToUploadVideo = true;
                    }

                    @Override
                    public void onProgress(String s) {
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFinish() {
                    }
                });

            }else{
                //You don't need to do anything here because When CameraFragment finishes doing its task,
                // it'll merge this compressed video with the audio.
            }

        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            Toast.makeText(c, "Jeff ooooo"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        startButton.setEnabled(true);
        //stopButton.setEnabled(false);
        //playButton.setEnabled(false);
        // stopPlayButton.setEnabled(false);

        if(!initCamera()){
            getActivity().finish();
        }

    }
    @Override
    public void onPause() {
        super.onPause();

        //if the app is paused, we off the flash
        //first we checkif the flash is on so that we can off it:
        if(isFlashOn){

            //Off the flash
            p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);

            isFlashOn = false;

        }

        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
        releaseCamera();
        releaseRecorder();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }

        //reset these
        secondsToCountDown = 0;
        isCameraToCountDown = false;

        if(countDownTimer != null){
            countDownTimer.cancel();
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch(IOException e){

            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        //This is the part where we handle auto focus
        if(camera != null) {
            Camera.Parameters camParams = camera.getParameters();
            camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            camera.setParameters(camParams);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {

        if(i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
            //This is the method that is called after the video recording is over (ie..if the 15 seconds durationOfFullSong is over)

            //stop the song
            mediaPlayer.pause();

            //remove these buttons to create space
            imageViewRotateCamera.setVisibility(View.GONE);
            imageViewFlash.setVisibility(View.GONE);

            timerTextView.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.INVISIBLE);

            stopRecording();//the name is actually deceiving sha, what this method does is to display the playback after recording has ended

            /*
          Now that the video has stopped, we want to do these two things simultaneously:
           FIRST -- AKA TASK 1) quickly trim the audio (This will take some seconds), then use ffmpeg to combine the trimmed audio with the video, then we use this new video to replace
           the old one (ie rawVideo) in copies folder. This new video is the one that will be showed to the user during preview.

           SECOND -- AKA TASK 2) While we are doing all the above, we need to solve one small problem. You see, siliCompressor compresses videos well 100%.
           But problem we have is that if you use ffmpeg to merge audio to a video and then you later compress that video, siliCompressor wont attach
           the audio to the video. You'l have your compressesd video, yes, but no audio. Mute. So siliCompressor is not good on video that have been edited
           with ffmpeg.
           Now this means we can't compress the video we used for user preview because we have already used ffmpeg to attach an audio to it.
           So to get a compressed video, we need to get our raw video again, then compress it, then attach an audio to it. Then send this video to the database.
            All this will be smartly done in a seperate thread so that it doesnt have to wait for the process that prepares video for preview to finish (TASK 1).
           */

            //so now the video is done recording, we start executing our Tasks.
            trimSong(c,songPath);//TASK 1

            compressVideo = new CompressVideo(c, pathToRawVideoFile, pathToAudioFileForMerging, user);
            compressVideo.execute();// TASK 2

        }

    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
    }




}

package Fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import id.zelory.compressor.Compressor;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import BackgroundWorkers.ConvertImageToBaseString;
import Classes.User;
import Fragments_Secondary.EditDashBoardFragment;
import Fragments_Secondary.ViewPostsFragment;
import de.hdodenhof.circleimageview.CircleImageView;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static Fragments.DashBoardFragment.c;
import static Fragments.DashBoardFragment.progressBar;
import static Fragments.DashBoardFragment.reloadButton;
import static android.app.Activity.RESULT_OK;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.navigation;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;


public class DashBoardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    static TextView textViewUserName, textViewGender, textViewNoOfCopies, viewPostTextView;


    //static final String URL_ADDRESS = "http://192.168.43.123/Copies/changeProfilePicture.php";
    //final String URL_ADDRESS_TWO = "http://192.168.43.123/Copies/getNumberOfCopies.php";
    static final String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/changeProfilePicture.php";
    final String URL_ADDRESS_TWO = "https://androidtestsite.000webhostapp.com/Copies/getNumberOfCopies.php";

    FloatingActionButton floatingActionButton;
    FloatingTextButton editButton;
    static CircleImageView circleImageView;
    ImageView enlargedImage;
    public static ProgressBar progressBar;
    public static FloatingActionButton reloadButton;

    View v;
    static Bitmap bitmap;
    Uri uri;
    public static Boolean stillTryingToLoadPicture;

    static User user;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 22131;

    static Intent intentData;

    static Context c;

    static int rltCode;
    private static String pathToProfilePic;

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
        v = inflater.inflate(R.layout.fragment_dash_board, container, false);
        circleImageView = (CircleImageView)v.findViewById(R.id.imageViewProfilePicture);
        floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fab);
        textViewUserName = (TextView)v.findViewById(R.id.user_name);
        textViewGender = (TextView)v.findViewById(R.id.user_gender);
        textViewNoOfCopies = (TextView)v.findViewById(R.id.total_number_of_copies);
        viewPostTextView = (TextView)v.findViewById(R.id.viewPostTextView);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        reloadButton = (FloatingActionButton) v.findViewById(R.id.reload_dashboard_pic);
        editButton = (FloatingTextButton)v.findViewById(R.id.editButton);


        galleryPhoto = new GalleryPhoto(getActivity().getApplicationContext());

        navigation.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        reloadButton.setVisibility(View.INVISIBLE);

        c = getActivity();

        //because the fragment starts by trying to download the user profile picture, we set:
        stillTryingToLoadPicture = true;

        //Just do these
        MainActivity.hideLogOutMenu = false;
        //now refresh the optionsMenu
        getActivity().invalidateOptionsMenu();

        //Just do this
        MainActivity.navigation.setVisibility(View.VISIBLE);


        //Ok when this Fragment starts, lets get our user data from the base and display them:
        user = User.findById(User.class, (long) 1);

        textViewUserName.setText("Name: " + user.getUserName());
        textViewGender.setText("Gender: " + user.getUserGender());
        textViewNoOfCopies.setText("You have been copied " + user.getTotalNumberOfCopies().trim() + " times");

        //Now to set the profile picture:
        //We simply use picasso to load the image from our MySQL base:.
        //PicassoClient.downloadImage(getActivity(), user.getProfilePictureLink(), circleImageView);
        PicassoClient.downloadImage(getActivity(), user.getProfilePictureLink(), circleImageView);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //the user should only be able to change his profile picture to a new one
                // when the Fragment is done trying to load the current profile picture from the MySQL base.
                if (stillTryingToLoadPicture) {
                    Toast.makeText(getActivity(), "...still loading profile picture.", Toast.LENGTH_SHORT).show();
                } else {
                    //Now you can change the image.
                    //if we where in an Activity, we would do it like this
                    //Crop.pickImage(MainActivity.this);
                    //Since we are in a fragement, we do this:
                    Crop.pickImage(c, DashBoardFragment.this);

                }

            }
        });


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //start the editDashBoard fragment

                Fragment editDashBoardFragment = new EditDashBoardFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, editDashBoardFragment);
                ft.addToBackStack("DashBoardFragment");
                ft.commit();

                //as usual we gotta do this:
                vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                MainActivity.viewPagerIsVisible = false;
            }
        });


        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //enlarge the image
                LayoutInflater layout = LayoutInflater.from(getActivity());

                View v = layout.inflate(R.layout.enlarge_circle_image, null);

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setView(v);

                bitmap = ((BitmapDrawable) circleImageView.getDrawable()).getBitmap();

                enlargedImage = (ImageView) v.findViewById(R.id.imageView);
                enlargedImage.setImageBitmap(bitmap);

                //create an alert
                AlertDialog a = alert.create();
                a.show();


            }
        });




        viewPostTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int action = motionEvent.getAction();

                if(action== MotionEvent.ACTION_DOWN){
                    viewPostTextView.setBackgroundColor(Color.parseColor("#e9e9e9"));

                    return true;

                }if(action==MotionEvent.ACTION_UP){
                    viewPostTextView.setBackgroundColor(Color.parseColor("#ffffff"));

                    //start the ViewPostFragment
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", user.getUserId());
                    bundle.putString("userName", user.getUserName());
                    bundle.putString("totalNumberOfCopies", user.getTotalNumberOfCopies());
                    bundle.putString("profilePictureImageLink", user.getProfilePictureLink());//this means that this video is not a copy. it is original.
                    bundle.putString("fragmentName","DashBoardFragment" );

                    ViewPostsFragment viewPostsFragment = new ViewPostsFragment();
                    viewPostsFragment.setArguments(bundle);

                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, viewPostsFragment);
                    ft.addToBackStack("FindPreview");
                    ft.commit();

                    vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                    MainActivity.viewPagerIsVisible = false;

                    return true;
                }


                return false;
            }
        });




        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //load the profile picture again
                reloadButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                circleImageView.setImageBitmap(null);
                //We simply use picasso to load the image from our MySQL base:
                PicassoClient.downloadImage(getActivity(), user.getProfilePictureLink(), circleImageView);

                //do this too
                stillTryingToLoadPicture = true;

            }
        });




        return v;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{//In case the user chooses an invalid image file

            if(requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {///This is the code that executes after the user chooses a picture.

                intentData = data;
                uri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                showViewForCropProcess(uri);//we take the picture to a place the user can crop it

            }else if(requestCode == Crop.REQUEST_CROP){//The is the code that executes after the user chooses the area to crop and then press OK.


                //Ok now we have just gotten the new bitmap uri. But before we display it, lets make a network request to change the profile picture in the server first.
                progressBar.setVisibility(View.VISIBLE);
                reloadButton.setVisibility(View.INVISIBLE);
                stillTryingToLoadPicture = true;
                Toast.makeText(getActivity(), "Updating profile picture...", Toast.LENGTH_LONG).show();

                //Now at this point we need to send the cropped and compressed image (as a Base64) to the server. but before we do that, we need to do two things:
                //1. Compress the cropped image
                //2. Get the Base64 string from the compressed image.
                //3. Send this Base64 string to the server.

                //To compress an image is easy and we can do it fast but to convert to Base64 we need to do it in the background:
                 //new ConvertImageToBaseString(c, "DashBoardFragment").execute(getCompressedBitmap());
                //because of the fact that latest phones have a problem, we have to do this like so:
                //we are not using any compressed bitmap
                 new ConvertImageToBaseString(c, "DashBoardFragment").execute(getCompressedBitmap());
                //circleImageView.setImageBitmap(bitmap);
                //circleImageView.setImageURI(Crop.getOutput(data));
                //circleImageView.setImageBitmap(getCompressedBitmap(data));  perfect


            }
        }catch (Exception e){
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error, something went wrong... "+e.getMessage(), Toast.LENGTH_LONG).show();

            //Just in case it goes the fuck wrong:
            Picasso.with(c).load(uri)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(circleImageView);
        }
    }


    private void showViewForCropProcess(Uri imageUri){

        if( phoneHasSDcard()) {
            //This is where we want to save the cropped image
            pathToProfilePic = Environment.getExternalStorageDirectory() + "/Copies/profile_pic.png";
        }else{
            //or save here.
            pathToProfilePic = getActivity().getFilesDir()+ "/Copies/profile_pic.png";
        }


        File outFile = new File(pathToProfilePic);

        Uri destination = Uri.fromFile(outFile);

        //Now we are to do this:
        //Crop.of(imageUri, destination).asSquare().start(getActivity());
        //But since we are in a fragment, we do this:
        Crop.of(imageUri, destination).asSquare().start(c, DashBoardFragment.this);//this displays the picture in a place you can crop it.
    }




    public static void sendNewImageToServer(final String image64String) {

        //We make a network request
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);
                        stillTryingToLoadPicture = false;

                        if(!response.equals("Failed")) {

                            //Now save this new picture link in our sqlite
                            user.setProfilePictureLink(response);

                            //now save
                            user.save();

                            //finally, change the imageView to the new image the user selected
                            //lets us find the cropped and compressed image
                            if( phoneHasSDcard()) {
                                pathToProfilePic = Environment.getExternalStorageDirectory() + "/Copies/profile_pic.png";
                            }else{
                                pathToProfilePic = c.getFilesDir()+ "/Copies/profile_pic.png";
                            }

                            File file = new File(pathToProfilePic);

                            //This method below works well but it has problems cuz picasso somehow still casches the old image.
                            /*Uri destination = Uri.fromFile(file);

                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(), destination);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            circleImageView.setImageBitmap(bitmap);
                            */

                            //we use this:
                            circleImageView.setImageBitmap(null);

                            Picasso.with(c).load(file)
                                    .fit()//These two methods, fit and centerCrop, enable us compress the image from the sever
                                    .centerCrop()
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .placeholder(R.drawable.ic_person_black).into(circleImageView);





                        }else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(c, response, Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.INVISIBLE);
                        stillTryingToLoadPicture = false;
                        Toast.makeText(c, "Network failure", Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                //Here we send the user name and the image64 to the base

                //Now make the network request
                Map<String, String> params = new HashMap<String, String>();


                params.put("userName", user.getUserName());
                //we send the base64 string for now, we will get the link in the server
                //But wait... the image selected image may have a very large size. Its good we send a compressed version

                //params.put("image64", ImageBase64.encode(getCompressedBitmap(intentData)));
                params.put("image64", image64String);


                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(stringRequest);

    }



    private void getNumberOfCopies() {


        //We make a network request
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS_TWO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        if(!response.equals("Failed")) {

                            //we save the latest number of copies to our local database
                            user.setTotalNumberOfCopies(response);
                            user.save();

                            //and display it
                            String numberOfCopies = user.getTotalNumberOfCopies().trim();

                            if(numberOfCopies.equals("0")){
                                textViewNoOfCopies.setText("You have not been copied by anyone");
                            }else if(numberOfCopies.equals("1")){
                                textViewNoOfCopies.setText("You have been copied once");
                            }else{
                                textViewNoOfCopies.setText("You have been copied a total of " + numberOfCopies + " times");
                            }


                        }else{
                            //do nothing
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //do nothing...
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                //Here we send the user id

                //Now make the network request
                Map<String, String> params = new HashMap<String, String>();

                params.put("userId", user.getUserId());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {//Jeff do this to prevent unwanted timeouts.
            @Override
            public int getCurrentTimeout() {
                return 60000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 60000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);

    }


    public String getImagePath(Uri uri) {

        Cursor cursor = null;


        try{
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = c.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);

        }catch(Exception e){

            return "Invalid";

        }finally {

            if(cursor != null){
                cursor.close();
            }
        }


    }


    private void changeImageViewPicture(){

        String imageFilePath = getImagePath(uri);

        //Now we make a file to know if the user has not deleted the picture
        File file = new File(imageFilePath);

        if(file.exists()){//The image has not been moved or deleted

            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            //circleImageView.setImageBitmap(bitmap);
            Uri uri = Uri.fromFile(file);
            Picasso.with(getActivity()).load(uri).into(circleImageView);

        }else{
            Toast.makeText(getActivity(),"Your profile picture could not be found on your phone", Toast.LENGTH_LONG).show();
        }


    }


    public static void refreshDashBoard(){
        //in the is method we simply refresh the user name, gender and number of copies

        User user = User.findById(User.class, (long) 1);

        textViewUserName.setText("Name: " + user.getUserName());
        textViewGender.setText("Gender: " + user.getUserGender());
        textViewNoOfCopies.setText("You have been copied " + user.getTotalNumberOfCopies().trim() + " times");
    }


    public Bitmap getCompressedBitmap(){

        Bitmap bit;

        //We want to get a compressed bitmap of the cropped image
        //So we find where we kept the cropped picture
        if( phoneHasSDcard()) {
            pathToProfilePic = Environment.getExternalStorageDirectory() + "/Copies/profile_pic.png";
        }else{
            pathToProfilePic = getActivity().getFilesDir()+ "/Copies/profile_pic.png";
        }

        File outFile = new File(pathToProfilePic);

        //compress the file and overrite it on this same location
        try {
            //bit = SiliCompressor.with(c).getCompressBitmap(pathToProfilePic);
            bit = new Compressor(c).compressToBitmap(outFile);
            return bit;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

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


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        //anytime the fragment is visible, we secretly fetch the latest number of copies the user has from our online database. We do this secretly.

        if (isVisibleToUser){
            //Toast.makeText(getActivity(),"fragment is visible", Toast.LENGTH_SHORT).show();
            getNumberOfCopies();
        }

    }



}



class PicassoClient {


    public static void downloadImage(final Context c, String imageUrl, ImageView img) {

        progressBar.setVisibility(View.VISIBLE);
        DashBoardFragment.stillTryingToLoadPicture = true;


        if (imageUrl.length() > 0 && imageUrl != null) {
            Picasso.with(c).load(imageUrl)
                    .fit()//These two methods, fit and centerCrop, enable us compress the image from the sever
                    .centerCrop()
                    .placeholder(R.mipmap.profile_picture_placeholder)
                    /*.error(R.mipmap.profile_picture_placeholder) I just want you to see this, Just in case but we dont want to change the pic to any other if there is error*/
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    /*Now, Picasso caches images by default ie. it saves them temporarily
                    in memory as long as the app is running. You will have to restart the app for Picasso to delete the image from memory.
                    If you like refresh the fragment, picasso will still hold on to its image.
                    This is good news for this fragment but it creates a problem for us when the user chooses to change his profile image.
                    See what happens-
                    Initially, Picasso loads the profile picture and caches it when the fragment starts.
                    If you leave the fragment to another fragment, Picasso will automatically load the saved image and show you.
                    No need for network. Very Lovely for us.
                    However When you change your profile image, the new image is loaded from your phone memory this time and not picasso.
                    If you leave the fragment and return, Picasso* in your onCreate method will trigger and load its cached image instead of
                    the new one you made. Hence you will have to be restarting the app for it to load the new one from the base
                    '.memoryPolicy(MemoryPolicy.NO_CACHE) and  .networkPolicy(NetworkPolicy.NO_CACHE)' removes the cached behaviour of picasso.
                    so now each time our fragment starts, we load the image from the base.

                    We will later use saved and restore instance state to save our image.
                    */
                    .into(img, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.INVISIBLE);
                            DashBoardFragment.stillTryingToLoadPicture = false;
                        }

                        @Override
                        public void onError() {
                            progressBar.setVisibility(View.INVISIBLE);
                            reloadButton.setVisibility(View.VISIBLE);
                            DashBoardFragment.stillTryingToLoadPicture = false;
                            Toast.makeText(c,"Your profile picture could not be loaded", Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {
            Picasso.with(c).load(R.mipmap.profile_picture_placeholder).into(img);
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(c,"Your profile picture could not be loaded", Toast.LENGTH_SHORT).show();
            DashBoardFragment.stillTryingToLoadPicture = false;
        }
    }

}



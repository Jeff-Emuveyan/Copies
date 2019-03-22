package Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.soundcloud.android.crop.Crop;
import id.zelory.compressor.Compressor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import BackgroundWorkers.ConvertImageToBaseString;
import Classes.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.adapterViewPager;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.fragments;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.navigation;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;


public class SignUpFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FloatingActionButton floatingActionButton;
    CircleImageView circleImageView;
    static Button signUpButton;
    static EditText editTextUserName;
    static EditText editTextPassword;
    EditText editTextConfirmPassword;
    static FormEditText editTextEmail;
    static RadioGroup radioGroup;
    static TextView textViewLogin;
    private static ProgressBar progressBar;
    static AlertDialog.Builder alert;

    //final static String URL_ADDRESS = "http://192.168.43.123/Copies/signUp.php";
    final static String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/signUp.php";

    static String theDate;
    String image64;
    static Boolean userSelectedProfilePicture;
    Bitmap bitmap;

    static View v;
    Uri uri;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 22131;

    private static String pathToProfilePic;

    static User user;

    static String id;
    static String profilePictureLink;
    static Intent intentData;

    static Context c;

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

        v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fab);
        circleImageView = (CircleImageView)v.findViewById(R.id.imageViewProfilePicture);
        signUpButton = (Button)v.findViewById(R.id.signUp);
        editTextUserName = (EditText)v.findViewById(R.id.editTextUserName);
        editTextPassword = (EditText)v.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = (EditText)v.findViewById(R.id.editTextConfirmPassword);
        editTextEmail = (FormEditText)v.findViewById(R.id.editTextEmail);
        textViewLogin = (TextView) v.findViewById(R.id.textViewLogin);
        radioGroup = (RadioGroup) v.findViewById(R.id.radioGroup);
        progressBar = (ProgressBar)v.findViewById(R.id.progressBar8);
        alert = new AlertDialog.Builder(getActivity());

        //getActivity().setTitle("SignUp");

        c = getActivity();

        progressBar.setVisibility(View.GONE);

        galleryPhoto = new GalleryPhoto(getActivity().getApplicationContext());

        userSelectedProfilePicture = false;

        //Our Date
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E-yyyy-MM-dd");
        theDate = String.valueOf(ft.format(date));


        //Hide the bottom menu
        navigation.setVisibility(View.INVISIBLE);


        //Also hide the OptionsMenuItem
        MainActivity.hideLogOutMenu = true;
        //now refresh the optionsMenu
        getActivity().invalidateOptionsMenu();

        //Just do this
        MainActivity.navigation.setVisibility(View.INVISIBLE);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(MainActivity.isSigningUpOrLogginIn == false) {// this means the app isn't doing any background operation like trying to loggin a user simultaneously
                    if (isAnyfiledEmpty()) {
                        Toast.makeText(getActivity(), "Missing fields...", Toast.LENGTH_LONG).show();

                    } else {

                        if (doPasswordsMatch()) {


                            if (editTextEmail.testValidity()) {//using a library to test if the Email is valid.

                                //Ok now before we sign up, we need to first convert our profile picture to a base64 string:
                                if (userSelectedProfilePicture) {

                                    new ConvertImageToBaseString(c, "SignUpFragment").execute
                                            (getCompressedBitmap());

                                } else {
                                    Toast.makeText(c, "Select a profile picture",
                                            Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(c, "Invalid email address", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getActivity(), "Your passwords do not match ",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }else{
                    Toast.makeText(getActivity(), "This operation cannot be performed now.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });




        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Now you can change the image.
                //if we where in an Activity, we would do it like this
                //Crop.pickImage(MainActivity.this);
                //Since we are in a fragement, we do this:
                Crop.pickImage(c, SignUpFragment.this);
            }
        });



        textViewLogin.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                int action = arg1.getAction();

                if(action== MotionEvent.ACTION_DOWN){
                    textViewLogin.setBackgroundColor(Color.parseColor("#e9e9e9"));

                    return true;

                }if(action==MotionEvent.ACTION_UP){
                    textViewLogin.setBackgroundColor(Color.parseColor("#ffffff"));
                    //when the user presses and removes his finger, we:
                    //start the Login Fragment
                    vpPager.setCurrentItem(1);


                    return true;
                }
                return false;
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

            }else if(requestCode == Crop.REQUEST_CROP){//The is the code that executes after the user chooses the area to crop and then press "DONE" button.


                //Now at this point we need to send the cropped and compressed image (as a Base64) to the server. but before we do that, we need to do two things:
                //1. Compress the cropped image
                //2. Get the Base64 string from the compressed image.
                //3. Send this Base64 string to the ser

                //To compress an image is easy and we can do it fast. So:
                circleImageView.setImageBitmap(getCompressedBitmap());

                userSelectedProfilePicture = true;
                //that's all

            }
        }catch (Exception e){
            e.printStackTrace();
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
        Crop.of(imageUri, destination).asSquare().start(c, SignUpFragment.this);//this displays the picture in a place you can crop it.
    }


    private boolean doPasswordsMatch(){

        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        if(password.equals(confirmPassword)){

            return true;
        }

        else
            return false;
    }


    private boolean isAnyfiledEmpty(){

        String userName = editTextUserName.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        String userEmail = editTextEmail.getText().toString();

        if(userName.equals("") || password.equals("") || confirmPassword.equals("") ||
                userEmail.equals("")){
            return true;
        }else{
            return false;
        }


    }



    public static void signUpUser(final String image64){

        final ProgressDialog progressDialog = new ProgressDialog(c);
        progressDialog.setMessage("Signing up...");

        progressDialog.show();

        signUpButton.setEnabled(false);
        textViewLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);


        MainActivity.isSigningUpOrLogginIn = true;


        //we now use volley to make a network request to send user details
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();

                        signUpButton.setEnabled(true);
                        textViewLogin.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        MainActivity.isSigningUpOrLogginIn = false;


                        if(response.equals("User already exists")){

                            Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                            alert.setTitle("Choose a different name");
                            alert.setMessage(response);
                            alert.show();

                        }else if(response.equals("This email is already being used by someone else")){

                            Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                            alert.setTitle("Choose a different email");
                            alert.setMessage(response);
                            alert.show();

                        } else if (response.equals("Failed")) {

                            Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                            alert.setTitle("Oops!");
                            alert.setMessage(response);
                            alert.show();

                        } else {

                            //This means all went well.
                            //we will get the user full details. So we simply take the one we want ( ie id and profilePictureLink):

                            try {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo = null;

                                for (int i = 0; i < ja.length(); i++) {

                                    jo = ja.getJSONObject(i);

                                    id = jo.getString("id");
                                    profilePictureLink = jo.getString("profilePictureLink");

                                }

                            } catch (JSONException e) {
                                Toast.makeText(c, "Something went wrong...try again",
                                        Toast.LENGTH_LONG).show();

                            }catch (Exception e) {
                                Toast.makeText(c, "Something went wrong...try again",
                                        Toast.LENGTH_LONG).show();
                            }


                            //Save the user data to the phone sqlite date base.
                            //But first we need the profile pic link in the MySQL.
                            user.setProfilePictureLink(profilePictureLink);

                            //Before we save this user, lets check if a user already exit in the phone
                            //This situation happens if a user has used the app and then logged out. His data will still be on the phone so we
                            //need to overwrite it:
                            User oldUser = User.findById(User.class, (long) 1);
                            if (oldUser == null) {
                                //This means no one has used the app on this phone before.
                                //now just save
                                user.setUserId(String.valueOf(id));
                                user.save();
                            } else {

                                //Overwrite the oldUser
                                oldUser.setUserId(String.valueOf(id));
                                oldUser.setUserName(user.getUserName());
                                oldUser.setUserPassword((user.getUserPassword()));
                                oldUser.setUserEmail(user.getUserEmail());
                                oldUser.setUserGender(user.getUserGender());
                                oldUser.setSignUpPeriod(user.getSignUpPeriod());
                                oldUser.setProfilePictureLink(user.getProfilePictureLink());
                                oldUser.setTotalNumberOfCopies(user.getTotalNumberOfCopies());
                                oldUser.setActivated(user.getActivated());
                                oldUser.setLoggedIn(user.getLoggedIn());

                                //now save
                                oldUser.save();
                            }

                            //Just a simply check to see if things went well
                            User user = User.findById(User.class, (long) 1);
                            if (user != null) {

                                //Now Start the TimeLine Fragment
                                Toast.makeText(c, "SignUp Successful!", Toast.LENGTH_SHORT).show();

                                //First remove the signUpFragment and loginFragment
                                fragments.remove(0);
                                //after removing the first fragment, one fragment remains on the list so we remove it again as so:
                                fragments.remove(0);

                                adapterViewPager.notifyDataSetChanged();

                                //add the new Fragments we need
                                Fragment timeLineFragemnt = new TimeLineFragment();
                                Fragment selectTrackFragment = new SelectTrackFragment();
                                Fragment findFragment = new FindFragment();
                                Fragment dashBoardFragment = new DashBoardFragment();

                                fragments = new ArrayList<>();

                                fragments.add(timeLineFragemnt);
                                fragments.add(selectTrackFragment);
                                fragments.add(findFragment);
                                fragments.add(dashBoardFragment);

                                adapterViewPager.notifyDataSetChanged();

                                vpPager.setAdapter(adapterViewPager);

                                vpPager.setCurrentItem(0);//timeLineFragemnt is now at this position

                                //do this since we are going there
                                MainActivity.navigation.setSelectedItemId(R.id.navigation_time_line);


                            } else {
                                Toast.makeText(c, "Something went wrong...try again",
                                        Toast.LENGTH_LONG).show();

                                alert.setTitle("Oops!");
                                alert.setMessage("Something went wrong...try again");
                                alert.show();
                            }


                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressDialog.dismiss();
                        MainActivity.isSigningUpOrLogginIn = false;


                        Toast.makeText(c, "Unable to connect", Toast.LENGTH_LONG).show();
                        signUpButton.setEnabled(true);
                        textViewLogin.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                //Build a baby user
                user = new User(
                        editTextUserName.getText().toString().trim(),
                        editTextPassword.getText().toString().trim(),
                        editTextEmail.getText().toString().trim(),
                        getUserGender(),
                        theDate,
                        image64, // we send the image64, later we will set the proper link
                        "0",
                        true,
                        true);

                Map<String, String> params = new HashMap<String, String>();

                params.put("userName", user.getUserName());
                params.put("userPassword", user.getUserPassword());
                params.put("userEmail", user.getUserEmail());
                params.put("userGender", user.getUserGender());
                params.put("signUpPeriod", user.getSignUpPeriod());
                params.put("image64", user.getProfilePictureLink());  //we send the base64 string for now we will get the link in the server
                params.put("totalNumberOfCopies", user.getTotalNumberOfCopies());
                params.put("isActivated", user.getActivated().toString()); //convert the boolean to String
                params.put("isLoggedIn", user.getLoggedIn().toString());

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


        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(stringRequest);




    }


    private static String getUserGender(){

        int selectedID = radioGroup.getCheckedRadioButtonId();

        RadioButton radioButton = (RadioButton)v.findViewById(selectedID);

        if(radioButton.getText().toString().equals("Male")){
            return "Male";

        }else if(radioButton.getText().toString().equals("Female")){
            return "Female";

        }

        return null;

    }


    public String getPath(Uri uri) {

        Cursor cursor = null;


        try{
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);

        }catch(Exception e){

            return "Invalid image";

        }finally {

            if(cursor != null){
                cursor.close();
            }
        }


    }



    public Bitmap getCompressedBitmap(){

        //We want to get a compressed bitmap of the cropped image
        //So we find where we kept the cropped picture
        //compress the file and overwrite it on this same location
        Bitmap bit;

        //We want to get a compressed bitmap of the cropped image
        //So we find where we kept the cropped picture
        if( phoneHasSDcard()) {
            pathToProfilePic = Environment.getExternalStorageDirectory() + "/Copies/profile_pic.png";
        }else{
            pathToProfilePic = getActivity().getFilesDir()+ "/Copies/profile_pic.png";
        }

        //compress the file and overwrite it on this same location
        try {
            //bit = SiliCompressor.with(c).getCompressBitmap(pathToProfilePic);
            bit = new Compressor(c).compressToBitmap(new File(pathToProfilePic));
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



}

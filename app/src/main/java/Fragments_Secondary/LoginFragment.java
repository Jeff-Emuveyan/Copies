package Fragments_Secondary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import Fragments.SelectTrackFragment;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Classes.User;
import Fragments.DashBoardFragment;
import Fragments.FindFragment;
import Fragments.TimeLineFragment;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.adapterViewPager;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.fragments;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;


public class LoginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    EditText editTextUserName, editTextUserPassword;
    FloatingTextButton loginButton;
    ProgressBar progressBar;
    //final String URL_ADDRESS = "http://192.168.43.123/Copies/logIn_logOut.php";
    final String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/logIn_logOut.php";

    AlertDialog.Builder alert;
    User user;

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
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        editTextUserName = (EditText)v.findViewById(R.id.editTextUserName);
        editTextUserPassword = (EditText)v.findViewById(R.id.editTextUserPassword);
        loginButton = (FloatingTextButton)v.findViewById(R.id.loginButton);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        alert = new AlertDialog.Builder(getActivity());

        progressBar.setVisibility(View.INVISIBLE);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(MainActivity.isSigningUpOrLogginIn == false) {// this means the app isn't doing any background operation like trying to loggin a user simultaneously

                    loginUser();

                }else{
                    Toast.makeText(getActivity(), "This operation cannot be performed now.", Toast.LENGTH_LONG).show();
                }

            }
        });

        return v;
    }


    private void loginUser() {
        //No matter what the condition is, the first step of login is always to send the user name and password to the base

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
        MainActivity.isSigningUpOrLogginIn = true;


        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);
                        loginButton.setVisibility(View.VISIBLE);
                        MainActivity.isSigningUpOrLogginIn = false;

                        if (response.equals("Login Failed")) {

                            progressBar.setVisibility(View.INVISIBLE);
                            loginButton.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();

                            alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Oops!");
                            alert.setMessage(response);
                            alert.show();

                        }else{//we receive a json response so:
                            progressBar.setVisibility(View.INVISIBLE);
                            loginButton.setVisibility(View.VISIBLE);


                            try {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo = null;

                                for(int i= 0; i<ja.length();i++){

                                    jo = ja.getJSONObject(i);

                                    String userId = String.valueOf(jo.getInt("id"));
                                    String userName = jo.getString("userName");
                                    String userPassword = jo.getString("userPassword");
                                    String userEmail = jo.getString("userEmail");
                                    String userGender = jo.getString("userGender");
                                    String signUpPeriod = jo.getString("signUpPeriod");
                                    String profilePictureLink = jo.getString("profilePictureLink");
                                    String totalNumberOfCopies = jo.getString("totalNumberOfCopies");
                                    Boolean isActivated = Boolean.parseBoolean(jo.getString("isActivated"));
                                    Boolean isLoggedIn = Boolean.parseBoolean(jo.getString("isLoggedIn"));

                                    user = new User(userId, userName, userPassword, userEmail, userGender, signUpPeriod, profilePictureLink, totalNumberOfCopies, isActivated, isLoggedIn  );


                                }//END LOOP

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            //Before we save this user, lets check if a user already exit in the phone
                            //This situation happens if a user has used the app and then logged out. His data will still be on the phone so we
                            //need to overwrite it:
                            User oldUser = User.findById(User.class, (long) 1);
                            if(oldUser == null){
                                //This means no one has used the app on this phone before.
                                //now just save
                                user.save();
                            }else{

                                //Overwrite the oldUser
                                oldUser.setUserId(user.getUserId());
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
                            if(user != null) {

                                //Now Start the TimeLine Fragment
                                Toast.makeText(getActivity(), "Login Successful!", Toast.LENGTH_SHORT).show();

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


                            }else{
                                Toast.makeText(getActivity(), "Something went wrong...try again", Toast.LENGTH_SHORT).show();

                                alert.setTitle("Oops!");
                                alert.setMessage("Something went wrong...try again");
                                alert.show();
                            }
                           // Toast.makeText(getActivity(), "Successful!", Toast.LENGTH_LONG).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.INVISIBLE);
                        loginButton.setVisibility(View.VISIBLE);
                        MainActivity.isSigningUpOrLogginIn = false;

                        alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Oops!");
                        alert.setMessage("Something went wrong! Try again..");
                        alert.show();
                        Toast.makeText(getActivity(), "Failed...", Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                    //Now make the network request
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("userName", editTextUserName.getText().toString().trim());
                    params.put("userPassword", editTextUserPassword.getText().toString().trim());

                    return params;

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }





}

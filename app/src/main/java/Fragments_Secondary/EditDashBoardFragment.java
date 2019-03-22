package Fragments_Secondary;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

import java.util.HashMap;
import java.util.Map;

import Classes.User;
import Fragments.DashBoardFragment;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;


public class EditDashBoardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    EditText editTextUserName,editTextUserEmail, editTextCurrentPassword, editTextNewPassword, editTextConfirmPassword;
    CheckBox checkBox;
    RadioGroup radioGroup;
    RadioButton radioButtonMale, radioButtonFemale;
    View view;
    FloatingTextButton saveButton;
    AlertDialog.Builder alert;

    ProgressBar progressBar;

    Boolean userWantsToChangePassword;

    //final String URL_ADDRESS = "http://192.168.43.123/Copies/editUserDetails.php";
    final String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/editUserDetails.php";

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
        view = inflater.inflate(R.layout.fragment_edit_dashboard, container, false);

        editTextUserName = (EditText)view.findViewById(R.id.editText3);
        editTextUserEmail= (EditText)view.findViewById(R.id.editTextEmail);
        editTextCurrentPassword = (EditText)view.findViewById(R.id.editText4);
        editTextNewPassword = (EditText)view.findViewById(R.id.editText5);
        editTextConfirmPassword = (EditText)view.findViewById(R.id.editText6);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        radioButtonMale = (RadioButton)view.findViewById(R.id.radioButtonMale);
        radioButtonFemale = (RadioButton)view.findViewById(R.id.radioButtonFemale);
        saveButton = (FloatingTextButton)view.findViewById(R.id.saveButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        alert = new AlertDialog.Builder(getActivity());

        //When this fragment starts, we hide the editTexts for password
        editTextCurrentPassword.setVisibility(View.GONE);
        editTextNewPassword.setVisibility(View.GONE);
        editTextConfirmPassword.setVisibility(View.GONE);

        progressBar.setVisibility(View.INVISIBLE);

        userWantsToChangePassword = false;

        //Let us get our user details to populate them into our views
        user = User.findById(User.class, (long) 1);

        editTextUserName.setText(user.getUserName());
        editTextUserEmail.setText(user.getUserEmail());

        if(user.getUserGender().equals("Male")){
            radioButtonMale.setChecked(true);

        }else{
            radioButtonFemale.setChecked(true);
        }



        //Now by default, the checkBox is off and set to "No"
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(((CheckBox)view).isChecked()){//If the checkbox is checked after you pressed it, then:
                    checkBox.setText("Yes");

                    //Reveal the editText to change password
                    editTextCurrentPassword.setVisibility(View.VISIBLE);
                    editTextNewPassword.setVisibility(View.VISIBLE);
                    editTextConfirmPassword.setVisibility(View.VISIBLE);

                    userWantsToChangePassword = true;


                }else{//If the checkbox is unchecked after you pressed it, then:
                    checkBox.setText("No");

                    editTextCurrentPassword.setVisibility(View.GONE);
                    editTextNewPassword.setVisibility(View.GONE);
                    editTextConfirmPassword.setVisibility(View.GONE);

                    userWantsToChangePassword = false;
                }
            }
        });



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.setTitle("Save?");
                alert.setMessage("Save changes?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        saveUserData();
                    }
                });

                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                    }
                });

                alert.show();

            }
        });


        return view;
    }



    private void saveUserData() {

        //First we check if the user wants to change his password
        if(userWantsToChangePassword){

            //Now we check if all the editTexts ie: password editTexts and even the user name and email editText are not empty
            if(!(editTextUserName.getText().toString().trim().equals("") || editTextUserEmail.getText().toString().trim().equals("") || editTextCurrentPassword.getText().toString().trim().equals("") || editTextNewPassword.getText().toString().trim().equals("") || editTextConfirmPassword.getText().toString().trim().equals(""))){

                //Ok, lets check if the given password is same as the one in our database
                String userPassword = user.getUserPassword();

                if(editTextCurrentPassword.getText().toString().trim().equals(userPassword)){

                    //Ok, his password is correct, so lets check if the new password he wants to put matches the confirm password:
                    if(editTextNewPassword.getText().toString().trim().equals(editTextConfirmPassword.getText().toString().trim())){

                        //Ok everything is good, make the network call to change the password
                        makeNetworkCall();

                    }else{
                        Toast.makeText(getActivity(), "Your passwords do not match ", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getActivity(),"Wrong password", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(getActivity(),"Missing field...", Toast.LENGTH_LONG).show();
            }

        }else{
            //User didn't want to change his password, so we just focus on the first userNameEditText, emailEditText and radio Group
            if(editTextUserName.getText().toString().trim().equals("") || editTextUserEmail.getText().toString().trim().equals("")){
                Toast.makeText(getActivity(),"Missing field...", Toast.LENGTH_LONG).show();

            }else{

                //Make a network call to update the user name, email and gender
                makeNetworkCall();

            }


        }

    }




    void makeNetworkCall(){

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.INVISIBLE);


        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();

                        if (response.equals("Successful!")) {

                            //The MYSQL has changed, now set the new user name, email, gender and password of the SQLite
                            user.setUserName(editTextUserName.getText().toString().trim());
                            user.setUserEmail(editTextUserEmail.getText().toString().trim());
                            user.setUserGender(getUserGender());

                            if(userWantsToChangePassword) {
                                user.setUserPassword(editTextNewPassword.getText().toString().trim());
                            }

                            //now save
                            user.save();

                            //Ok now, go back to DashBoard Fragment
                            //Now since we are in EditDashBoard fragment, we need to pop the back stack once to remove
                            //EditDashBoard Fragment to give us DashBoardFragment.
                            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                            fm.popBackStack();

                            MainActivity.showViewPager();

                            //we refresh the DashBoard Fragment
                            DashBoardFragment.refreshDashBoard();

                            //do this since we are going there
                            MainActivity.navigation.setSelectedItemId(R.id.navigation_dashboard);

                        }else{
                            progressBar.setVisibility(View.INVISIBLE);
                            saveButton.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();

                            alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Oops!");
                            alert.setMessage(response);
                            alert.show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), "Failed...", Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                if(userWantsToChangePassword){

                    String oldUserName = user.getUserName();

                    //Now make the network request
                    Map<String, String> params = new HashMap<String, String>();


                    params.put("oldUserName", oldUserName);

                    params.put("newUserName", editTextUserName.getText().toString().trim());
                    params.put("newUserEmail", editTextUserEmail.getText().toString().trim());
                    params.put("userGender", getUserGender());
                    params.put("userPassword", editTextNewPassword.getText().toString().trim());

                    return params;


                }else {
                    //Here we send the old user name, new user name, new email, and the gender to the base

                    String oldUserName = user.getUserName();

                    //Now make the network request
                    Map<String, String> params = new HashMap<String, String>();


                    params.put("oldUserName", oldUserName);

                    params.put("newUserName", editTextUserName.getText().toString().trim());
                    params.put("newUserEmail", editTextUserEmail.getText().toString().trim());
                    params.put("userGender", getUserGender());

                    return params;
                }

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);


    }



    private String getUserGender(){

        int selectedID = radioGroup.getCheckedRadioButtonId();

        RadioButton radioButton = (RadioButton)view.findViewById(selectedID);

        if(radioButton.getText().toString().equals("Male")){
            return "Male";

        }else if(radioButton.getText().toString().equals("Female")){
            return "Female";

        }

        return null;

    }

}
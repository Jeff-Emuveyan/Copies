package Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Classes.User;
import Fragments_Secondary.LoginFragment;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.adapterViewPager;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.fragments;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.navigation;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;

public class LogOutFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    //final String URL_ADDRESS = "http://192.168.43.123/Copies/logIn_logOut.php";
    final String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/logIn_logOut.php";

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
        View v  = inflater.inflate(R.layout.fragment_log_out, container, false);

        Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_SHORT).show();

        user = User.findById(User.class, (long) 1);

        //Just do this
        MainActivity.navigation.setVisibility(View.INVISIBLE);

        //When this Fragment start, we quickly make a netwok call to change the users 'isLoggedIn' to false in our MySQL base
        logOut();
        return v;
    }




    private void logOut() {
        //Make the network call

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equals("Successful!")) {

                            Toast.makeText(getActivity(),response , Toast.LENGTH_SHORT).show();

                            //The MYSQL has changed, now set the user 'isLoggedIn' to false in the SQLite
                            user.setLoggedIn(false);

                            //now save
                            user.save();

                            //Ok now, go to SignUp Fragment

                            //but first remove this logout fragment by popping the backstack
                            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                            fm.popBackStack();

                            MainActivity.showViewPager();

                            //Now, remember, since we are using a viewPager, we need to remove the four main fragments(timeLine, selectTrack...)
                            //and then put SignUp fragment and Login fragment.
                            //This is the sane thing we did when the user signs up or logs in but in reverse order. So:
                            //remove the four fragments we don't need
                            fragments.remove(0);//after removing the first fragment, one fragment remains on the list so we remove it again as so:
                            fragments.remove(0);
                            fragments.remove(0);
                            fragments.remove(0);

                            adapterViewPager.notifyDataSetChanged();

                            //add the new Fragments we need
                            Fragment signUpFragment = new SignUpFragment();
                            Fragment loginFragment = new LoginFragment();

                            fragments = new ArrayList<>();

                            fragments.add(signUpFragment);
                            fragments.add(loginFragment);

                            adapterViewPager.notifyDataSetChanged();

                            vpPager.setAdapter(adapterViewPager);

                            vpPager.setCurrentItem(0);//Display SignUpFragemnt which is now at this position



                        }else{
                           Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();

                            //if it fails, go to timeLine Fragment
                            gotoTimeLineFragment();


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_SHORT).show();

                        //if there is error, we still go to timeLineFragment
                        gotoTimeLineFragment();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                    //Now make the network request
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("userName",user.getUserName());

                    return params;

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);

    }

    private void gotoTimeLineFragment(){


        //Now since we are in logout fragment, we need to pop the back stack once to remove
        //the logout fragment, then we move to timeLine fragment

        android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();

        //Now we need to move the viewPager to timeline fragment
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

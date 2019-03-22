package Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v7.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Adapters.FindAdapter;
import Classes.User;


public class FindFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    SearchView searchView;
    ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private List<User> users;

    //final static String URL_ADDRESS = "http://192.168.43.123/Copies/search.php";
    final static String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/search.php";

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
        View v = inflater.inflate(R.layout.fragment_find, container, false);

        searchView = (SearchView)v.findViewById(R.id.searchView);
        progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        recyclerView = v.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Just do these
        MainActivity.hideLogOutMenu = false;
        //now refresh the optionsMenu
        getActivity().invalidateOptionsMenu();

        //Just do this
        MainActivity.navigation.setVisibility(View.VISIBLE);

        getActivity().setTitle("Copies");


        users = new ArrayList<>();

        progressBar.setVisibility(View.INVISIBLE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nameToFind) {

                //Search for the user in the base
                searchForUser(nameToFind);

                return false;
            }
        });

        return v;
    }


    private void searchForUser(final String nameToFind) {
        progressBar.setVisibility(View.VISIBLE);

        //we need to remove old data anytime the user searches for new ones
        users.clear();
        recyclerView.setAdapter(null);

        //we now use volley to make a network request to send searched user name
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.equals("No user found")) {
                       // Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
                        //Nnah..dont put a toast, itll iritate the user. Not cool
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);

                        }else{
                            //we have a result so:
                            progressBar.setVisibility(View.INVISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);


                            try {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo = null;

                                for(int i= 0; i<ja.length();i++){

                                    jo = ja.getJSONObject(i);

                                    int id = jo.getInt("id");
                                    String userName = jo.getString("userName");
                                    String userPassword = jo.getString("userPassword");
                                    String userEmail = jo.getString("userEmail");
                                    String userGender = jo.getString("userGender");
                                    String signUpPeriod = jo.getString("signUpPeriod");
                                    String profilePictureLink = jo.getString("profilePictureLink");
                                    String totalNumberOfCopies = jo.getString("totalNumberOfCopies");
                                    Boolean isActivated = Boolean.parseBoolean(jo.getString("isActivated"));
                                    Boolean isLoggedIn = Boolean.parseBoolean(jo.getString("isLoggedIn"));

                                    user = new User(String.valueOf(id), userName, userPassword, userEmail, userGender, signUpPeriod, profilePictureLink, totalNumberOfCopies, isActivated, isLoggedIn  );

                                    users.add(user);

                                }//END LOOP

                                adapter = new FindAdapter(users, getActivity());
                                recyclerView.setAdapter(adapter);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getActivity(), "Failed...", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                params.put("userName", nameToFind);

                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);


    }


}

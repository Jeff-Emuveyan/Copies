package Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import BackgroundWorkers.CompressVideo;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Adapters.TimeLineAdapter;
import Classes.User;
import Classes.VideoPost;
import Fragments_Secondary.CameraFragment;
import Interfaces.ILoadMore;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.navigation;


public class TimeLineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    static String URL_ADDRESS;

    private static RecyclerView recyclerView;
    public static TimeLineAdapter adapter;
    private static ArrayList<VideoPost> videoPosts;
    private static ArrayList<User> users;

    static SwipeRefreshLayout mSwipeRefreshLayout;
    static Button reloadButton;
    static ProgressBar progressBar;
    public static CardView cardViewUpload;
    public static TextView textViewUploadStatus;
    public static ProgressBar progressBarUpload;
    ru.dimorinny.floatingtextbutton.FloatingTextButton cancelUploadButton;


    static VideoPost videoPost;
    static User user;

    Parcelable listState;
    LinearLayoutManager linearLayoutManager;

    static Context c;
    AlertDialog.Builder alert;

    public static boolean isUploadingAVideo;


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
        View view = inflater.inflate(R.layout.fragment_time_line, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        reloadButton = (Button) view.findViewById(R.id.reloadButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        cardViewUpload = (CardView) view.findViewById(R.id.cardViewUploading);
        textViewUploadStatus = (TextView) view.findViewById(R.id.textViewUploadStatus);
        progressBarUpload = (ProgressBar) view.findViewById(R.id.progressBarUpload);
        cancelUploadButton = (ru.dimorinny.floatingtextbutton.FloatingTextButton) view.findViewById(R.id.cancelUploadButton);


        c = getActivity();
        alert = new AlertDialog.Builder(getActivity());


        //hide the uploading cardView
        cardViewUpload.setVisibility(View.GONE);

        isUploadingAVideo = false;

        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //When you swipe down to refresh, we load the timeline again.
                loadData(0, "refresh");

            }
        });


        reloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //we load our videos
                loadData(0, "");//0 means we are loading the timeline for the first time. This is not a load more operation.
                //The empty 'operation' means this is not a call to refresh the timeLine.

                reloadButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });


        cancelUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.setTitle("Cancel?");
                alert.setMessage("Do you want to cancel?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub

                        isUploadingAVideo = false;
                        cardViewUpload.setVisibility(View.GONE);

                        if(CompressVideo.isCompressingAVideo){//if the user wants to stop the upload at a time when we are still compressing the video:
                            CameraFragment.compressVideo.cancel(true);// this will stop compression and every upload process.

                        }else{//this means that the compression process is finished and the app has started uploading the video. so:
                            //stop the upload asyncTask
                            CameraFragment.postVideo.cancel(true);
                        }

                    }
                });

                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                    }
                });

                alert.show();


            }
        });


        //Also show the full OptionsMenuItem
        MainActivity.hideLogOutMenu = false;
        getActivity().invalidateOptionsMenu();

        //Just do this
        navigation.setVisibility(View.VISIBLE);
        reloadButton.setVisibility(View.INVISIBLE);

        getActivity().setTitle("Copies");

        //do this since we are going there
        navigation.setSelectedItemId(R.id.navigation_time_line);


        videoPosts = new ArrayList<>();
        users = new ArrayList<>();

        //we load our videos
        loadData(0, "");//0 means we are loading the timeline for the first time. This is not a load more operation.
        //The empty 'operation' means this is not a call to refresh the timeLine.


        return view;
    }


    //****The following three(3) methods --
    // onActivityCreated, onSaveInstanceState and onResume are how we used to save and restore instance state of our recycleView
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable("list");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        listState = linearLayoutManager.onSaveInstanceState();
        outState.putParcelable("list", listState);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (listState != null) {
            linearLayoutManager.onRestoreInstanceState(listState);
        }

    }


    public static void loadData(final int lastVideoPostId, final String operation) {// value of 0 means load the timeline initially. No load more.

        /*final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching...");
        progressDialog.show();*/

        if (lastVideoPostId == 0) {//this means we are to just load the timeline initially.

            //URL_ADDRESS = "http://192.168.43.123/Copies/loadTimeLine.php";
            URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/loadTimeLine.php";

        } else {//This means the time line has been loaded o but we are to load more.

            //URL_ADDRESS = "http://192.168.43.123/Copies/loadMoreTimeLine.php";
            URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/loadMoreTimeLine.php";
        }
        //we now use volley to make a network request
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);

                        //This only concerns refresh where we need to clear the old data when a new data comes.
                        if (operation.equals("refresh")) {
                            videoPosts.clear();
                            users.clear();
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                        }


                        //we just do our normal business

                        try {
                            JSONArray ja = new JSONArray(response);
                            JSONObject jo = null;

                            for (int i = 0; i < ja.length(); i++) {

                                jo = ja.getJSONObject(i);

                                int id = jo.getInt("id");
                                String title = jo.getString("title");
                                String period = jo.getString("period");
                                String imageLink = jo.getString("imageLink");
                                String videoLink = jo.getString("videoLink");
                                String audioLink = jo.getString("audioLink");
                                String numberOfLikes = jo.getString("numberOfLikes");
                                String numberOfCopies = jo.getString("numberOfCopies");
                                String userId = jo.getString("userId");
                                Boolean isLiked = Boolean.parseBoolean(jo.getString("isLiked"));

                                //lets also get the other data we need. We dont need all the data from users table
                                String userName = jo.getString("userName");
                                String userGender = jo.getString("userGender");
                                String totalNumberOfCopies = jo.getString("totalNumberOfCopies");
                                String profilePictureLink = jo.getString("profilePictureLink");


                                videoPost = new VideoPost(id, title, period, imageLink, videoLink, audioLink, numberOfLikes, numberOfCopies, userId, isLiked);

                                //collect the remaining half of the data
                                user = new User(userId, userName, userGender, totalNumberOfCopies, profilePictureLink);

                                //now we want to add the elements to our Lists.
                                //but we also want to insert null values at certain position into the list, this will help use insert Ads later on. So:
                                if (i % 5 == 0) {
                                    //the above means:
                                    //the null value must be in the 6th position. ie after every 5 videos, an add will appear.
                                    //NOTE, based on our math ie 'i % 5 == 0', item at position 0 will constantly be an ad because 0 % 5 == 0.
                                    //Therefore, the first item on our list will always be an ad.
                                    videoPosts.add(null);

                                    //after adding null, add the videoPost into the next position
                                    videoPosts.add(videoPost);//if you do not do this, you will have missing videoPosts in the list.

                                    //now since the elements in both 'videoPosts' and 'users' List must be in the same position, we need also do to
                                    //users what we have done to videoPosts so that their elements will correspond. So:
                                    users.add(null);
                                    users.add(user);
                                } else {

                                    videoPosts.add(videoPost);
                                    users.add(user);
                                }


                            }


                            if (operation.equals("loadMore")) {
                                //we just notify that new data has been added to the list. Thats all we need to do for load more.
                                adapter.notifyDataSetChanged();
                            } else {


                                adapter = new TimeLineAdapter(recyclerView, videoPosts, users, c, null);
                                adapter.setLoadMore(new ILoadMore() {
                                    @Override
                                    public void onLoadMore() {
                                        //This is the code that runs when we want to load more data ie when you scroll to the second to last item

                                        //lets get the id of the last item in the recyclerView
                                        int postionOfLastVideoPost = videoPosts.size() - 1;
                                        VideoPost videoPost = (VideoPost) videoPosts.get(postionOfLastVideoPost);

                                        //yea, you know we add ads to our list so sometimes the last element may be an add hence null.
                                        //So if our last element is an ad, we use the id of the second to last element to 'loadMore'
                                        if (videoPost != null) {
                                            loadData(videoPost.getId(), "loadMore");
                                            //Toast.makeText(c, "loading more", Toast.LENGTH_SHORT).show();
                                        } else {
                                            postionOfLastVideoPost = videoPosts.size() - 2; //get the position of the second to the last element
                                            videoPost = (VideoPost) videoPosts.get(postionOfLastVideoPost);

                                            loadData(videoPost.getId(), "loadMore");
                                        }

                                    }
                                });


                                recyclerView.setAdapter(adapter);


                                //lets just do this here. This tell the swipeLayout to stop the loading animation
                                mSwipeRefreshLayout.setRefreshing(false);

                            }


                        } catch (JSONException e) {
                            //progressDialog.dismiss();
                            e.printStackTrace();

                            //tell the swipeLayout to stop the loading animation
                            mSwipeRefreshLayout.setRefreshing(false);
                        } catch (Exception e) {
                            //progressDialog.dismiss();
                            e.printStackTrace();
                            //tell the swipeLayout to stop the loading animation
                            mSwipeRefreshLayout.setRefreshing(false);
                        }


                        if (response.equals("There are no more videos to load")) {//as the user scrolls,
                            // if we ever get a response that there is no more videos to load:
                            //we tell the user that "There are no more videos to load'.

                            //Now you will expect us to simply now say:
                            //adapter.tellUserThereAreNoMoreVideos();
                            //for no reason, doing that will cause a null pointer exception that 'loadViewHolder' is null
                            //me i donno what to do again, the best is this my workaround:
                            adapter.noMoreVideosToLoad = true;
                            adapter.notifyDataSetChanged();//to refresh
                        }


                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //progressDialog.dismiss();
                        Toast.makeText(c, "Network failure", Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.INVISIBLE);

                        //tell the swipeLayout to stop the loading animation
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (operation.equals("loadMore")) {

                            adapter.showLoadMoreButton();
                            adapter.shouldShowLoadMoreButton = true;

                        }
                        if (operation.equals("loadMore") == false && operation.equals("refresh") == false) {

                            reloadButton.setVisibility(View.VISIBLE);//The reload button should only show if this was an initail loading
                            //of time line. 'reloadButton' should not show for operations caused by trying to load more  or refresh.

                        }


                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                User user = User.findById(User.class, (long) 1);

                //Now make the network request
                Map<String, String> params = new HashMap<String, String>();

                if (!(lastVideoPostId == 0)) {//this means we have received a videoPost id, so we are to load more timeline.
                    params.put("id", String.valueOf(lastVideoPostId));
                }

                params.put("userId", user.getUserId());

                return params;

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(stringRequest);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        //if the fragment is no longer visible, it must mean the user pressed the back button.
        //So we need to cancel any upload operation if there is.
        if (isUploadingAVideo) {//if we are uploading a video, we cancel the upload.

            isUploadingAVideo = false;
            cardViewUpload.setVisibility(View.GONE);

            //stop the asyncTask
            //CompressVideo.cancel(true);
        }
    }


}
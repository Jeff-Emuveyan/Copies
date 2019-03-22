package Fragments_Secondary;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Adapters.TimeLineAdapter;
import Classes.User;
import Classes.VideoPost;
import Interfaces.ILoadMore;
import de.hdodenhof.circleimageview.CircleImageView;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.navigation;


public class ViewPostsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    TextView userNameTextView, totalNumberOfCopiesTextView;
    CircleImageView circleImageView;
    static ImageView imageViewNoVideo;

    String profilePictureImageLink;
    String userName;
    String totalNumberOfCopies;
    static String selectedUserId;


    static String URL_ADDRESS;

    private static RecyclerView recyclerView;
    public static TimeLineAdapter adapter;
    private static ArrayList<VideoPost> videoPosts;
    private static ArrayList<User> users;

    static SwipeRefreshLayout mSwipeRefreshLayout;
    static Button reloadButton;
    static ProgressBar progressBar;


    static VideoPost videoPost;
    static User user;

    Parcelable listState;
    LinearLayoutManager linearLayoutManager;

    static Context c;
    AlertDialog.Builder alert;



    public static boolean isUploadingAVideo;
    static String fragmentName;



    public ViewPostsFragment() {
        // Required empty public constructor
    }



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
        View view = inflater.inflate(R.layout.fragment_view_posts, container, false);

        userNameTextView = (TextView)view.findViewById(R.id.textViewUserName);
        totalNumberOfCopiesTextView = (TextView)view.findViewById(R.id.textViewNumberOfCopies);
        circleImageView = (CircleImageView)view.findViewById(R.id.imageViewProfilePicture);
        imageViewNoVideo = (ImageView)view.findViewById(R.id.imageViewNoVideo);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        reloadButton = (Button)view.findViewById(R.id.reloadButton);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar2);


        //This Fragment is initally intended to be used by only FindPreviewFragment, however we still use it for DashBoardFragment too.
        //When this fragment starts, we collect the necessary details:
        Bundle bundle = getArguments();
        profilePictureImageLink = bundle.getString("profilePictureImageLink");
        selectedUserId = bundle.getString("userId");//id of the user you searched for.
        userName = bundle.getString("userName");//name of the user you searched for.
        totalNumberOfCopies = bundle.getString("totalNumberOfCopies");

        fragmentName = bundle.getString("fragmentName");


        userNameTextView.setText(userName);

        //lets do some settings to our textView
        if(totalNumberOfCopies.trim().equals("0")){

            if(fragmentName != null){//this means that this fragment is being used by DashBoardFragment
                totalNumberOfCopiesTextView.setText("You have not been copied by anyone");
            }else{//if it is findPreviewFragment that is launched this fragment, then:
                totalNumberOfCopiesTextView.setText(userName +" has not been copied by anyone");
            }

        }else if(totalNumberOfCopies.trim().equals("1")){

            if(fragmentName != null){//this means that this fragment is being used by DashBoardFragment
                totalNumberOfCopiesTextView.setText("You have been copied once");
            }else{//if it is findPreviewFragment that is launched this fragment, then:
                totalNumberOfCopiesTextView.setText(userName +" has been copied once");
            }

        }else {

            if(fragmentName != null){//this means that this fragment is being used by DashBoardFragment
                totalNumberOfCopiesTextView.setText("You have been copied a total of " + totalNumberOfCopies + " times");
            }else{//if it is findPreviewFragment that is launched this fragment, then:
                totalNumberOfCopiesTextView.setText(userName + " has been copied a total of " + totalNumberOfCopies + " times");
            }
        }



        //Now for the image, we user picasso to download it for us
        new PicassoClient().downloadImage(getActivity(), profilePictureImageLink, circleImageView);


        c = getActivity();
        alert = new AlertDialog.Builder(getActivity());


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


        //Also show the full OptionsMenuItem
        MainActivity.hideLogOutMenu = false;
        getActivity().invalidateOptionsMenu();

        //Just do this
        navigation.setVisibility(View.VISIBLE);
        reloadButton.setVisibility(View.INVISIBLE);
        imageViewNoVideo.setVisibility(View.GONE);



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
        outState.putParcelable("list",listState);
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

        if(lastVideoPostId == 0) {//this means we are to just load the timeline initially.

            //URL_ADDRESS = "http://192.168.43.123/Copies/loadTimeLineOfAUser.php";
            URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/loadTimeLineOfAUser.php";

        }else{//This means the time line has been loaded o but we are to load more.

            //URL_ADDRESS = "http://192.168.43.123/Copies/loadMoreTimeLineOfAUser.php";
            URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/loadMoreTimeLineOfAUser.php";
        }
        //we now use volley to make a network request
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);

                        //This only concerns refresh where we need to clear the old data when a new data comes.
                        if (operation.equals("refresh")){
                            videoPosts.clear();
                            users.clear();
                            if(adapter != null ){
                                adapter.notifyDataSetChanged();
                            }
                        }


                        if(response.equals("This user has never posted a video")){//this means if we somehow don't get any videos to show (Note: this dosen't apply to our TimeLineFragment who will
                            //always have videos to show. However, here, we may sometimes search for a user who has not posted any video before
                            //So we just do this:
                            imageViewNoVideo.setVisibility(View.VISIBLE);
                            Toast.makeText(c, "This user has no videos", Toast.LENGTH_SHORT).show();

                            recyclerView.setVisibility(View.INVISIBLE);
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
                                String originalVideoPostId = jo.getString("originalVideoPostId");
                                String videoType = jo.getString("videoType");
                                Boolean isLiked = Boolean.parseBoolean(jo.getString("isLiked"));

                                //lets also get the other data we need. We dont need all the data from users table
                                String userName = jo.getString("userName");
                                String userGender = jo.getString("userGender");
                                String totalNumberOfCopies = jo.getString("totalNumberOfCopies");
                                String profilePictureLink = jo.getString("profilePictureLink");


                                videoPost = new VideoPost(id, title, period, imageLink, videoLink, audioLink, numberOfLikes, numberOfCopies, userId, originalVideoPostId, videoType, isLiked);

                                //collect the remaining half of the data
                                user = new User(userName, userGender, totalNumberOfCopies, profilePictureLink);

                                //now we want to add the elements to our Lists.
                                //but we also want to insert null values at certain position into the list, this will help use insert Ads later on. So:
                                if(i % 5 == 0){
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
                                }else{

                                    videoPosts.add(videoPost);
                                    users.add(user);
                                }


                            }



                            if (operation.equals("loadMore")) {
                                //we just notify that new data has been added to the list. Thats all we need to do for load more.
                                adapter.notifyDataSetChanged();
                            } else {


                                adapter = new TimeLineAdapter(recyclerView, videoPosts, users, c, fragmentName);
                                adapter.setLoadMore(new ILoadMore() {
                                    @Override
                                    public void onLoadMore() {
                                        //This is the code that runs when we want to load more data ie when you scroll to the second to last item

                                        //lets get the id of the last item in the recyclerView
                                        int postionOfLastVideoPost = videoPosts.size() - 1;
                                        VideoPost videoPost = (VideoPost) videoPosts.get(postionOfLastVideoPost);

                                        //yea, you know we add ads to our list so sometimes the last element may be an add hence null.
                                        //So if our last element is an ad, we use the id of the second to last element to 'loadMore'
                                        if(videoPost != null){
                                            loadData(videoPost.getId(), "loadMore");
                                            //Toast.makeText(c, "loading more", Toast.LENGTH_SHORT).show();
                                        }else{
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
                        }catch (Exception e) {
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
                            Toast.makeText(c, error.getMessage(), Toast.LENGTH_SHORT).show();
                            error.printStackTrace();
                        }


                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                User user = User.findById(User.class, (long) 1);

                //Now make the network request
                Map<String, String> params = new HashMap<String, String>();

                if(!(lastVideoPostId == 0)) {//this means we have received a videoPost id, so we are to load more timeline.
                    params.put("id", String.valueOf(lastVideoPostId));
                }

                params.put("userId", user.getUserId());
                params.put("selectedUserId", selectedUserId);

                return params;

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(stringRequest);
    }






    //INNER CLASS
    class PicassoClient {


        public void downloadImage(Context c, String imageUrl, ImageView img) {

            if (imageUrl.length() > 0 && imageUrl != null) {
                Picasso.with(c).load(imageUrl)
                        .fit()
                        .centerCrop()
                        .placeholder(R.mipmap.profile_picture_placeholder)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(img);

            } else {
                Picasso.with(c).load(R.mipmap.profile_picture_placeholder).into(img);

            }
        }

    }



}

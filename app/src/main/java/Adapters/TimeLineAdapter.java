package Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Classes.User;
import Classes.VideoPost;
import Fragments.TimeLineFragment;
import Fragments_Secondary.CopiesFragment;
import Fragments_Secondary.DownloadSongFragment;
import Fragments_Secondary.FindPreviewFragment;
import Fragments_Secondary.ViewPostsFragment;
import Interfaces.ILoadMore;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;
import state.bellogate_caliphate.jeffemuveyan.copies.VideoPlayerActivity;

import static Fragments.TimeLineFragment.loadData;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;

/**
 * Created by JEFF EMUVEYAN on 2/3/2018.
 */

public class TimeLineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<VideoPost> videoPosts;
    private List<User> users;
    Context c;

    ImageView enlargedImage;
    Bitmap bitmap;

    String[] texts;

    String fragmentName;

    static String URL_ADDRESS;

    private final int VIEW_TYPE_ITEM = 0, VIEW_TYPE_LOADING = 1, VIEW_TYPE_AD = 2;
    public boolean shouldShowLoadMoreButton = false;
    public boolean noMoreVideosToLoad = false;

    ILoadMore loadMore;

    public LoadViewHolder loadViewHolder;
    public AdViewHolder adViewHolder;
    public static VideoPostHolder holder;


    public TimeLineAdapter(RecyclerView recyclerView, final List<VideoPost> videoPosts, List<User> users, final Context c, String fragmentName) {
        this.videoPosts = videoPosts;
        this.users = users;
        this.c = c;
        this.fragmentName = fragmentName;

        /*The following operations helps us load more data from the base when we scroll to the last item on the list

        This style works 100%, all you have to do is put ur load more method inside the "if" statement.
        But we dont use it because we want to load more data when we reach the third or second to last item.
        This style is difficult to modify so that we can know when we have reached the second or third to last item.
        Also, this 'addOnScrollListener' fires everytime we scroll to the end. This is not bad o but it will be easier if things just happend once. Not
        everytime we scroll to the end.
        This is why we used a simpler style that makes it easy for us to know when we are in any element of the recycler view and also fires once.
        GoTo 'onBindViewHolder' and read towards the end.

         final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                if((visibleItemCount+ firstVisibleItemPosition)>= totalItemCount && firstVisibleItemPosition >=0){
                    //this is what happens when we reach the last item on the list
                    //load more data here

                    Toast.makeText(c, "you have reached the last item", Toast.LENGTH_LONG).show();
                }

            }
        });*/


        //However we use this block of code to determine when to show the load more button because after a network fails, we show our load more button
        //but if we should scroll up and then scroll down, the load more button will disappear. So we need to know if we should show the load more button
        //anytime we scroll down.

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                if((visibleItemCount+ firstVisibleItemPosition)>= totalItemCount && firstVisibleItemPosition >=0){
                    //this is what happens when we reach the last item on the list
                    //we check if we are to show the load more button
                    if(shouldShowLoadMoreButton){
                        showLoadMoreButton();
                    }

                    if(noMoreVideosToLoad){
                        tellUserThereAreNoMoreVideos();
                    }

                }



            }
        });


    }




    @Override
    public int getItemViewType(int position) {

        if(position == videoPosts.size()){
            return VIEW_TYPE_LOADING;

        }else if(videoPosts.get(position) == null ){//This means that we have reached the null parts of the list so we can display an add
            return VIEW_TYPE_AD;

        }

        return VIEW_TYPE_ITEM;
    }


    public void setLoadMore(ILoadMore loadMore){
        this.loadMore = loadMore;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM){
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.video_post, parent, false);

            final VideoPostHolder holder = new VideoPostHolder(v);


            //******************All onClickListeners are to be handled in this method************************
            //**********************************************************************************************

            //this is where we handle what happens when the user presses the like button.
            //Here we need to increase of decrease the number of likes on the textView. This is easy but recycleView resets the data once we scroll off.
            //Below is a clever and easy way we used to ensure that any update we make dosen't get reset.
            //This method involves us directly update the particular videoPost object.
            holder.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    //This is what happens when you press the button to like

                    //we increase the number of likes.
                    VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());

                    int newNumberOfLikes = Integer.parseInt(videoPost.getNumberOfLikes()) + 1;

                    //update the vidoepost with the new value
                    videoPost.setNumberOfLikes(String.valueOf(newNumberOfLikes));

                    holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");

                    //do this too so that the recycle view will know you have liked this post before. Hence the liked button will be 'on' when next you scroll up to it.
                    videoPost.setLiked(true);

                    like_unLikePost(videoPost, holder, "Like");

                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    //This is what happens when you press the button to unlike

                    //we decrease the number of likes.
                    VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());

                    int newNumberOfLikes = Integer.parseInt(videoPost.getNumberOfLikes()) - 1;

                    //update the vidoepost with the new value
                    videoPost.setNumberOfLikes(String.valueOf(newNumberOfLikes));

                    holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");

                    //do this too so that the recycle view will know you have unLiked this post. Hence the liked button will be 'off' when next you scroll up to it.
                    videoPost.setLiked(false);

                    like_unLikePost(videoPost, holder, "unLike");

                }
            });


            //ok what happens when the user clicks on a persons profile picture in the timeLine
            holder.imageViewUserProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //enlarge the image
                    LayoutInflater layout = LayoutInflater.from(c);

                    View v = layout.inflate(R.layout.enlarge_circle_image,null);

                    AlertDialog.Builder alert = new AlertDialog.Builder(c);

                    alert.setView(v);

                    bitmap = ((BitmapDrawable)holder.imageViewUserProfilePicture.getDrawable()).getBitmap();

                    enlargedImage = (ImageView)v.findViewById(R.id.imageView);
                    enlargedImage.setImageBitmap(bitmap);

                    //create an alert
                    AlertDialog a = alert.create();
                    a.show();
                }
            });



            //Ok what happens when the user presses a persons details in the timeLine. We open FindPreview fragment
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    User user = users.get(holder.getAdapterPosition());

                    Bundle bundle = new Bundle();
                    bundle.putString("profilePictureImageLink", user.getProfilePictureLink());
                    bundle.putString("userId", user.getUserId());
                    bundle.putString("userName", user.getUserName());
                    bundle.putString("userGender",user.getUserGender() );
                    bundle.putString("totalNumberOfCopies", user.getTotalNumberOfCopies());

                    Fragment findPreviewFragment  = new FindPreviewFragment();
                    findPreviewFragment.setArguments(bundle);

                    AppCompatActivity appCompatActivity = (AppCompatActivity)c;
                    FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, findPreviewFragment);
                    ft.addToBackStack("TimeLineFragment");
                    ft.commit();


                    //as usual we gotta do this:
                    vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                    MainActivity.viewPagerIsVisible = false;

                }
            });


            //ok what happens when the user clicks on the video thumbnail to play the video
            holder.imageViewThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());

                    //start the VideoPlayerActivity:
                    Intent i = new Intent(c, VideoPlayerActivity.class);
                    i.putExtra("videoLink", videoPost.getVideoLink());

                    c.startActivity(i);

                }
            });


            //ok what happens when the user clicks on the share button.
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());

                    //copyVideoLinkToClipBoard(videoPost.getVideoLink());

                    share(videoPost.getVideoLink());


                }
            });


            //Ok what happens when our user clicks on the 'copy' button
            holder.copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {



                    if(fragmentName != null){
                        if (fragmentName.equals("DashBoardFragment")){//this simply means that 'if we are being used' by the dashBoardFragment' then:

                            final VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());

                            //then, when you click on the copyButton (which at this stage will be titled 'Edit/Delete') we show a menu:
                            PopupMenu popupMenu = new PopupMenu(c, holder.copyButton);
                            popupMenu.inflate(R.menu.videopost_options_menu);
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {

                                    switch(menuItem.getItemId()){

                                        case R.id.edit:
                                            editVideo(videoPost, holder);
                                            break;
                                        case R.id.delete:

                                            AlertDialog.Builder alert = new AlertDialog.Builder(c);

                                            alert.setTitle("Delete");
                                            alert.setMessage("Delete this post?");
                                            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {



                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    // TODO Auto-generated method stub

                                                    Toast.makeText(c, "Deleting video...", Toast.LENGTH_SHORT).show();

                                                    deleteVideo(videoPost.getId(), videoPost.getOriginalVideoPostId(), holder);

                                                }
                                            });


                                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    // TODO Auto-generated method stub
                                                    arg0.cancel();

                                                }
                                            });

                                            alert.show();

                                            break;

                                    }

                                    return false;
                                }
                            });

                            popupMenu.show();

                        }
                    }else{// if we are being used by the timeLineFragment or findPreviewFragment

                            VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());
                            User user = users.get(holder.getAdapterPosition());

                            Bundle bundle = new Bundle();
                            bundle.putString("userGender", user.getUserGender());
                            bundle.putString("audioLink", videoPost.getAudioLink());
                            bundle.putInt("videoPostId", videoPost.getId());

                            Fragment downLoadSongFragment  = new DownloadSongFragment();
                            downLoadSongFragment.setArguments(bundle);

                            AppCompatActivity appCompatActivity = (AppCompatActivity)c;
                            FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.content_frame, downLoadSongFragment);
                            ft.addToBackStack("TimeLineFragment");
                            ft.commit();

                            //as usual we gotta do this:
                            vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                            MainActivity.viewPagerIsVisible = false;

                    }
                }
            });




            //Ok what happens when the user clicks on the number of copies a video has so that he can see all the list of 'copies'
            holder.linearLayoutAsButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    int action = motionEvent.getAction();

                    if(action== MotionEvent.ACTION_DOWN){
                        holder.linearLayoutAsButton.setBackgroundColor(Color.parseColor("#e9e9e9"));

                        return true;

                    }if(action==MotionEvent.ACTION_UP){
                        holder.linearLayoutAsButton.setBackgroundColor(Color.parseColor("#ffffff"));


                        VideoPost videoPost = videoPosts.get(holder.getAdapterPosition());
                        User user = users.get(holder.getAdapterPosition());

                        //start the CopiesFragment
                        Bundle bundle = new Bundle();
                        bundle.putString("thumbNailImageLink", videoPost.getImageLink() );
                        bundle.putString("videoTitle", videoPost.getTitle());
                        bundle.putString("totalNumberOfCopies", videoPost.getNumberOfCopies());
                        bundle.putString("originalVideoPostId", String.valueOf(videoPost.getId()));


                        Fragment copiesFragment  = new CopiesFragment();
                        copiesFragment.setArguments(bundle);

                        AppCompatActivity appCompatActivity = (AppCompatActivity)c;
                        FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, copiesFragment);
                        ft.addToBackStack("CopiesFragment");
                        ft.commit();

                        //as usual we gotta do this:
                        vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                        MainActivity.viewPagerIsVisible = false;


                        return true;
                    }


                    return false;
                }
            });




            return holder;

        }else if (viewType == VIEW_TYPE_AD){


            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ad, parent, false);

            return new AdViewHolder(v);


        }else if (viewType == VIEW_TYPE_LOADING){


            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.load_more, parent, false);

            return new LoadViewHolder(v);

        }


        return null;

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {

        //positionOfItem = position; //viewHolder.getAdapterPosition()

        if (getItemViewType(position) == VIEW_TYPE_ITEM){

            holder = (VideoPostHolder)viewHolder;

            final VideoPost videoPost = videoPosts.get(position);
            final User user = users.get(position);

            //now we check to see if the user has liked this post before
            if (videoPost.isLiked()) {//this means the user liked the post.
                holder.likeButton.setLiked(true);//let the like button be 'on' to show that the post has already been liked
            } else {
                holder.likeButton.setLiked(false);

            }

            holder.textViewUserName.setText(user.getUserName());
            holder.textViewPeriod.setText(videoPost.getPeriod());
            holder.textViewTitle.setText(decodedString(videoPost.getTitle()));// we may receive an encoded string so we need to always decode
            holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");//always get the number of likes for this post from the array.
            holder.textViewCopies.setText(videoPost.getNumberOfCopies()+ " Copies");

            //hide the progressBar
            holder.progressBar.setVisibility(View.INVISIBLE);


            //for the profile picture
            PicassoClient.downloadImage(c, user.getProfilePictureLink(), holder.imageViewUserProfilePicture, "profile_picture");
            //and thumbnail picture
            PicassoClient.downloadImage(c, videoPost.getImageLink(), holder.imageViewThumbnail,"video_thumbnail");

            //for the button
            if(user.getUserGender().equals("Male")){

                holder.copyButton.setTitle("Copy Him");
            }else{
                holder.copyButton.setTitle("Copy Her");
            }


            //lets do some important check, because there are several fragments that are going to be using this Adapter class. So:
            if(fragmentName != null){
                if (fragmentName.equals("DashBoardFragment")){//if the DashBoard fragment is the one using this adapter, we need to change the button title.
                    holder.copyButton.setTitle("Edit / Delete");
                }

                if (fragmentName.equals("CopiesFragment")){//if the Copies fragment is the one using this adapter, we need to hide some views:
                    holder.copyButton.setVisibility(View.GONE);
                    holder.textViewCopies.setVisibility(View.GONE);
                }
            }





        }else if (getItemViewType(position) == VIEW_TYPE_LOADING){

            loadViewHolder = (LoadViewHolder)viewHolder;
            loadViewHolder.progressBar.setIndeterminate(true);

            //make the reload button invisible initially
            loadViewHolder.loadMoreButton.setVisibility(View.INVISIBLE);
            //and this too
            loadViewHolder.textViewNoMoreVideos.setVisibility(View.INVISIBLE);

            //since its just one button, we can leave this onClickListener here
            loadViewHolder.loadMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //this is what happens when you click the load more button

                    //we simply load more
                    //lets get the id of the last item in the recyclerView
                    int postionOfLastVideoPost = videoPosts.size() - 1;
                    VideoPost videoPost = (VideoPost)videoPosts.get(postionOfLastVideoPost);

                    loadData(videoPost.getId(), "loadMore");

                    //Now hide this button
                    loadViewHolder.progressBar.setVisibility(View.VISIBLE);
                    loadViewHolder.loadMoreButton.setVisibility(View.INVISIBLE);

                    //we need to do this too
                    shouldShowLoadMoreButton = false;

                }
            });

        }else if (getItemViewType(position) == VIEW_TYPE_AD){

            adViewHolder = (AdViewHolder)viewHolder;

            AdRequest adRequest = new AdRequest.Builder().build();
            adViewHolder.mAdView.loadAd(adRequest);

        }



        //Now
        //the following operations helps us load more data from the base when we scroll to the second to last item on the list
        if(position == (videoPosts.size() - 2)){//This means we want to start loading when we reach the second to last element
            //Toast.makeText(c, "you have reach the end jeffooo", Toast.LENGTH_LONG).show();

            if (loadMore != null){
                loadMore.onLoadMore();
            }

        }


    }



    @Override
    public int getItemCount() {
        return videoPosts.size()+1; //the extra one is because we add a linearLayout holding the load more progressBar and button
    }



    private void share(String videoLink){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Copies");
        shareIntent.putExtra(Intent.EXTRA_TEXT, videoLink);

        c.startActivity(Intent.createChooser(shareIntent, "Share via"));

    }



    private void like_unLikePost(final VideoPost videoPost, final VideoPostHolder holder, final String operation) {

        //make network request

        URL_ADDRESS = "http://androidtestsite.000webhostapp.com/Copies/like_unlike.php";

        if(fragmentName != null){
            if (fragmentName.equals("CopiesFragment")){//if the Copies fragment is the one using this adapter, we need to hide some views:
                URL_ADDRESS = "http://androidtestsite.000webhostapp.com/Copies/like_unlike_copy.php";
            }
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equals("Done")){

                            //Well, we don't need to do anything again since we have already increased the number of likes.

                        }else{

                            Toast.makeText(c, "something went wrong...", Toast.LENGTH_SHORT).show();

                            //reset the like button back to off or on
                            if (operation.equals("Like")){

                                holder.likeButton.setLiked(false);//off the button

                                //we decrease the number of likes.
                                int newNumberOfLikes = Integer.parseInt(videoPost.getNumberOfLikes()) - 1;

                                //update the vidoepost with the new value
                                videoPost.setNumberOfLikes(String.valueOf(newNumberOfLikes));

                                holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");

                                //do this too so that the recycle view will know you have unLiked this post. Hence the liked button will be 'off' when next you scroll up to it.
                                videoPost.setLiked(false);

                            }else{

                                holder.likeButton.setLiked(true);//on the button

                                //we increase the number of likes.
                                int newNumberOfLikes = Integer.parseInt(videoPost.getNumberOfLikes()) + 1;

                                //update the vidoepost with the new value
                                videoPost.setNumberOfLikes(String.valueOf(newNumberOfLikes));

                                holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");

                                //do this too so that the recycle view will know you have unLiked this post. Hence the liked button will be 'off' when next you scroll up to it.
                                videoPost.setLiked(true);

                            }


                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //Toast.makeText(c, "poor network...try again", Toast.LENGTH_SHORT).show();

                        //reset the like button back to off or on
                        if (operation.equals("Like")){

                            holder.likeButton.setLiked(false);//off the button

                            //we decrease the number of likes.
                            int newNumberOfLikes = Integer.parseInt(videoPost.getNumberOfLikes()) - 1;

                            //update the vidoepost with the new value
                            videoPost.setNumberOfLikes(String.valueOf(newNumberOfLikes));

                            holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");

                            //do this too so that the recycle view will know you have unLiked this post. Hence the liked button will be 'off' when next you scroll up to it.
                            videoPost.setLiked(false);

                        }else{

                            holder.likeButton.setLiked(true);//on the button

                            //we increase the number of likes.
                            int newNumberOfLikes = Integer.parseInt(videoPost.getNumberOfLikes()) + 1;

                            //update the vidoepost with the new value
                            videoPost.setNumberOfLikes(String.valueOf(newNumberOfLikes));

                            holder.textViewLikes.setText(videoPost.getNumberOfLikes()+ " Likes");

                            //do this too so that the recycle view will know you have unLiked this post. Hence the liked button will be 'off' when next you scroll up to it.
                            videoPost.setLiked(true);

                        }

                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                User user = User.findById(User.class, (long) 1);

                Map<String, String> params = new HashMap<String, String>();

                params.put("userId", user.getUserId());
                params.put("videoPostId", String.valueOf(videoPost.getId())); //convert to string
                params.put("operation", operation);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(stringRequest);

    }


    public void showLoadMoreButton() {
        if (loadViewHolder.progressBar != null && loadViewHolder.loadMoreButton != null){
            loadViewHolder.progressBar.setVisibility(View.INVISIBLE);
            //make the reload button visible
            loadViewHolder.loadMoreButton.setVisibility(View.VISIBLE);
        }
    }


    public void tellUserThereAreNoMoreVideos(){

        loadViewHolder.progressBar.setVisibility(View.INVISIBLE);
        loadViewHolder.textViewNoMoreVideos.setVisibility(View.VISIBLE);

    }


    private void copyVideoLinkToClipBoard(String video_Link){

        ClipboardManager clipboardManager = (ClipboardManager)c.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("video_link", video_Link);

        clipboardManager.setPrimaryClip(clipData);//this is where the link is saved on the clipBoard.

        Toast.makeText(c, "link copied", Toast.LENGTH_LONG).show();

    }



    private void editVideo(final VideoPost videoPost, final VideoPostHolder holder){

        //show an alert dialog who has the title of the video on it.
        LayoutInflater layout = LayoutInflater.from(c);

        View view = layout.inflate(R.layout.edit_video_layout,null);

        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        alert.setView(view);

        final EmojiconEditText editTextVideoTitle = (EmojiconEditText) view.findViewById(R.id.emoji_edit_text);
        ImageView emojiButton = (ImageView) view.findViewById(R.id.emoji_button);

        AppCompatActivity appCompatActivity = (AppCompatActivity)c;//lets just flirt with some fancy code. (Note using 'c' will work 100%)

        EmojIconActions emojIcon = new EmojIconActions(appCompatActivity.getApplicationContext(), view, emojiButton, editTextVideoTitle);
        emojIcon.ShowEmojicon();


        if(videoPost.getTitle() == null){//just some safety checks, incase the user posted the video without a title.
            videoPost.setTitle("");
        }

        editTextVideoTitle.setText(decodedString(videoPost.getTitle()));


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                arg0.cancel();

            }
        });



        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub

                //hide the views
                hide_or_show_views(false, holder);

                //String URL_ADDRESS = "http://192.168.43.123/Copies/editVideoTitle.php";
                String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/editVideoTitle.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        URL_ADDRESS,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if(response.equals("Successful!")){

                                    Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                                    //now that the video has been deleted, we reload the recycleView:
                                    ViewPostsFragment.loadData(0, "refresh");

                                    //we also need to reload the timeline fragement so that the videoposts it has will not still show deleted video
                                    TimeLineFragment.loadData(0, "refresh");

                                }else{
                                    Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                                    //we show the view again since we could not edit it
                                    hide_or_show_views(true, holder);
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(c, "Network error", Toast.LENGTH_LONG).show();

                                //we show the view again since we could not delete it
                                hide_or_show_views(true, holder);

                            }
                        }) {

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new HashMap<String, String>();

                        params.put("videopostId", String.valueOf(videoPost.getId()));
                        params.put("newTitle", encodedString(editTextVideoTitle.getText().toString()));

                        return params;
                    }
                };



                RequestQueue requestQueue = Volley.newRequestQueue(c);
                requestQueue.add(stringRequest);


            }
        });




        //create an alert
        AlertDialog a = alert.create();
        a.show();

    }



    private void deleteVideo(final int videoPostId, final String originalVideoPostId, final VideoPostHolder holder){

        //we delete any data that has this id in the videopost table, likes table and copies table

        hide_or_show_views(false, holder);

        //String URL_ADDRESS = "http://192.168.43.123/Copies/deleteVideoPost.php";
        String URL_ADDRESS = "https://androidtestsite.000webhostapp.com/Copies/deleteVideoPost.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.equals("Deleted!!")){

                            Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                            //now that the video has been deleted, we reload the recycleView:
                            ViewPostsFragment.loadData(0, "refresh");

                            //we also need to reload the timeline fragement so that the videoposts it has will not still show deleted video
                            TimeLineFragment.loadData(0, "refresh");

                        }else{
                            Toast.makeText(c, response, Toast.LENGTH_LONG).show();

                            //we show the view again since we could not delete it
                            hide_or_show_views(true, holder);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(c, "Network error", Toast.LENGTH_LONG).show();

                        //we show the view again since we could not delete it
                        hide_or_show_views(true, holder);

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                User user = User.findById(User.class, (long) 1);

                params.put("userId", user.getUserId());//the user id
                params.put("videoPostId", String.valueOf(videoPostId) );//the id of the video to be deleted
                params.put("originalVideoPostId", originalVideoPostId );//the id of the original video post. NOTE: This value will
                //always be null unless this is a copy video.

                return params;
            }
        };



        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(stringRequest);


    }



    private void hide_or_show_views(boolean operation, VideoPostHolder holder){

        //if operation is false, hide the views, else, show them:

        holder.textViewUserName.setEnabled(operation);
        holder.textViewPeriod.setEnabled(operation);

        holder.linearLayout.setEnabled(operation);

        holder.imageViewThumbnail.setEnabled(operation);

        holder.linearLayoutAsButton.setEnabled(operation);
        holder.textViewLikes.setEnabled(operation);

        if(operation) {
            holder.imageViewUserProfilePicture.setVisibility(View.VISIBLE);// hide the image
            holder.textViewTitle.setVisibility(View.VISIBLE);
            holder.textViewCopies.setVisibility(View.VISIBLE);//setEnabled wont work on this view because it has bg color
            holder.copyButton.setVisibility(View.VISIBLE);// setEnabled wont work on this view
            holder.shareButton.setVisibility(View.VISIBLE);//setEnabled wont work on this view
            holder.likeButton.setVisibility(View.VISIBLE);//setEnabled wont work on this view

            //hide the progressBar
            holder.progressBar.setVisibility(View.INVISIBLE);
        }else{
            holder.imageViewUserProfilePicture.setVisibility(View.INVISIBLE);// hide the image
            holder.textViewTitle.setVisibility(View.INVISIBLE);
            holder.textViewCopies.setVisibility(View.INVISIBLE);//setEnabled wont work on this view because it has bg color
            holder.copyButton.setVisibility(View.INVISIBLE);// setEnabled wont work on this view
            holder.shareButton.setVisibility(View.INVISIBLE);//setEnabled wont work on this view
            holder.likeButton.setVisibility(View.INVISIBLE);//setEnabled wont work on this view

            //show the progressBar
            holder.progressBar.setVisibility(View.VISIBLE);
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




    //inner
    public class VideoPostHolder extends RecyclerView.ViewHolder {

        RelativeLayout parentRelativeLayout;
        CircleImageView imageViewUserProfilePicture;
        ImageView imageViewThumbnail, shareButton;
        TextView textViewUserName, textViewPeriod,textViewLikes, textViewCopies;
        hani.momanii.supernova_emoji_library.Helper.EmojiconTextView textViewTitle;
        FloatingTextButton copyButton;
        LinearLayout linearLayout, linearLayoutAsButton;
        LikeButton likeButton;
        ProgressBar progressBar;

        public VideoPostHolder(View itemView) {
            super(itemView);


            parentRelativeLayout = (RelativeLayout)itemView.findViewById(R.id.parentRelativeLayout);
            imageViewUserProfilePicture = (CircleImageView)itemView.findViewById(R.id.imageViewProfilePicture);
            imageViewThumbnail = (ImageView)itemView.findViewById(R.id.imageViewThumbnail);
            textViewUserName = (TextView)itemView.findViewById(R.id.textViewUserName);
            textViewPeriod = (TextView)itemView.findViewById(R.id.textViewPeriod);
            textViewTitle = (hani.momanii.supernova_emoji_library.Helper.EmojiconTextView)itemView.findViewById(R.id.textViewTitle);
            textViewLikes = (TextView)itemView.findViewById(R.id.textViewLikes);
            textViewCopies = (TextView)itemView.findViewById(R.id.textViewCopies);
            copyButton = (FloatingTextButton)itemView.findViewById(R.id.action_button);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.l1);
            linearLayoutAsButton = (LinearLayout)itemView.findViewById(R.id.linearLayoutAsButton);
            likeButton = (LikeButton)itemView.findViewById(R.id.star_button);
            shareButton = (ImageView)itemView.findViewById(R.id.share_button);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar6);
        }
    }


    //inner class
    public class LoadViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;
        public Button loadMoreButton;
        public TextView textViewNoMoreVideos;

        public LoadViewHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBarLoad);
            loadMoreButton = (Button)itemView.findViewById(R.id.loadMoreButton);
            textViewNoMoreVideos = (TextView)itemView.findViewById(R.id.textViewNoMoreVideos);

        }
    }


    //inner class
    public class AdViewHolder extends RecyclerView.ViewHolder {

        private AdView mAdView;

        public AdViewHolder(View itemView) {
            super(itemView);

            mAdView = (AdView)itemView.findViewById(R.id.adView);
        }
    }





    public static String decodedString(String text) {
        String decodedUrl =null;
        try {
            decodedUrl = URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return decodedUrl;
        }
        return decodedUrl;
    }

}


class PicassoClient{

    public static void downloadImage(Context c, String imageUrl, ImageView img, String operation){

        if(imageUrl.length()>0 && imageUrl!= null){

            if(operation.equals("profile_picture")) {
                Picasso.with(c).load(imageUrl)
                        .placeholder(R.drawable.ic_person_black).into(img);
            }
            if(operation.equals("video_thumbnail")) {
                Picasso.with(c).load(imageUrl).placeholder(R.drawable.video_thumb).into(img);
            }

        }else{
            if(operation.equals("profile_picture")) {
                Picasso.with(c).load(R.drawable.ic_person_black)
                        .into(img);
            }
            if(operation.equals("video_thumbnail")) {
                Picasso.with(c).load(R.drawable.video_thumb).into(img);
            }


        }
    }
}

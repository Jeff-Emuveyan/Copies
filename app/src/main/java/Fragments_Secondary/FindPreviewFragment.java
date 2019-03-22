package Fragments_Secondary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static Fragments_Secondary.FindPreviewFragment.progressBar;
import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;


public class FindPreviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String userName, userId;
    String userGender;
    String totalNumberOfCopies;
    String profilePictureImageLink;

    ImageView enlargedImage;
    Bitmap bitmap;

    CircleImageView circleImageView;
    TextView userNameTextView, userGenderTextView, totalNumberOfCopiesTextView, viewPostTextView;

    public static ProgressBar progressBar;

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
        View v = inflater.inflate(R.layout.fragment_find_preview, container, false);

        circleImageView = (CircleImageView)v.findViewById(R.id.imageViewProfilePicture);
        userNameTextView =(TextView)v.findViewById(R.id.user_name);
        userGenderTextView = (TextView)v.findViewById(R.id.user_gender);
        totalNumberOfCopiesTextView = (TextView)v.findViewById(R.id.total_number_of_copies);
        viewPostTextView = (TextView)v.findViewById(R.id.viewPostTextView);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar5);


        //We collect the necessary data from the previous Fragment
        Bundle bundle = getArguments();
        profilePictureImageLink = bundle.getString("profilePictureImageLink");
        userId = bundle.getString("userId");
        userName = bundle.getString("userName");
        userGender = bundle.getString("userGender");
        totalNumberOfCopies = bundle.getString("totalNumberOfCopies");


        userNameTextView.setText("Name: "+userName);
        userGenderTextView.setText("Gender: "+userGender);


        if(totalNumberOfCopies.equals("0")){
            totalNumberOfCopiesTextView.setText(userName+" has not been copied by anyone");
        }else if(totalNumberOfCopies.equals("1")){
            totalNumberOfCopiesTextView.setText(userName+" has been copied once");
        }else{
            totalNumberOfCopiesTextView.setText(userName+" has been copied a total of "+totalNumberOfCopies+" times");
        }




        if(userGender.equals("Male")){
            viewPostTextView.setText("View His Posts");
        }else{
            viewPostTextView.setText("View Her Posts");
        }



        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //enlarge the image
                LayoutInflater layout = LayoutInflater.from(getActivity());

                View v = layout.inflate(R.layout.enlarge_circle_image,null);

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setView(v);

                bitmap = ((BitmapDrawable)circleImageView.getDrawable()).getBitmap();

                enlargedImage = (ImageView)v.findViewById(R.id.imageView);
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
                    bundle.putString("userId", userId);
                    bundle.putString("userName", userName);
                    bundle.putString("totalNumberOfCopies",totalNumberOfCopies );
                    bundle.putString("profilePictureImageLink", profilePictureImageLink);//this means that this video is not a copy. it is original.
                    bundle.putString("fragmentName", null);


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



        //Now for the image, we user picasso to download it for us
        PicassoClient.downloadImage(getActivity(), profilePictureImageLink, circleImageView);

        return v;
    }
}


class PicassoClient {


    public static void downloadImage(Context c, String imageUrl, ImageView img) {

        if (imageUrl.length() > 0 && imageUrl != null) {
            Picasso.with(c).load(imageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(R.mipmap.profile_picture_placeholder)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(img);

            progressBar.setVisibility(View.INVISIBLE);

        } else {
            Picasso.with(c).load(R.mipmap.profile_picture_placeholder).into(img);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
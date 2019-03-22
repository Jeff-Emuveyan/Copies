package Adapters;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import Classes.User;
import Fragments_Secondary.FindPreviewFragment;
import de.hdodenhof.circleimageview.CircleImageView;

import static state.bellogate_caliphate.jeffemuveyan.copies.MainActivity.vpPager;

/**
 * Created by JEFF EMUVEYAN on 1/19/2018.
 */

public class FindAdapter extends RecyclerView.Adapter<FindAdapter.ViewHolder> {

    private List<User> users;
    Context c;

    User user;

    public FindAdapter(List<User> users, Context c) {
        this.users = users;
        this.c = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        user = users.get(position);

        holder.textViewUserName.setText(user.getUserName());
        holder.textViewCopies.setText(user.getUserName() +" has been copied "+user.getTotalNumberOfCopies()+" times");

        PicassoClient_FindFragment.downloadImage(c, user.getProfilePictureLink(), holder.imageViewUserProfilePicture);

    }

    @Override
    public int getItemCount() {
        return users.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageViewUserProfilePicture;
        TextView textViewUserName, textViewCopies;
        CardView cardView;


        public ViewHolder(View itemView) {
            super(itemView);

            imageViewUserProfilePicture = (CircleImageView)itemView.findViewById(R.id.imageViewProfilePicture);
            textViewUserName = (TextView)itemView.findViewById(R.id.textViewUserName);
            textViewCopies = (TextView)itemView.findViewById(R.id.textViewNumberOfCopies);
            cardView = (CardView)itemView.findViewById(R.id.cardView);

            //Now we implement what will happen when an item is clicked

            cardView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    // TODO Auto-generated method stub
                    int action = arg1.getAction();

                    if(action==MotionEvent.ACTION_DOWN){
                        cardView.setBackgroundColor(Color.parseColor("#e9e9e9"));

                        return true;

                    }if(action==MotionEvent.ACTION_UP){
                        cardView.setBackgroundColor(Color.parseColor("#ffffff"));
                        //when the user presses and removes his finger, we:

                        user = users.get(getAdapterPosition());

                        //First we send the user details:
                        Bundle bundle = new Bundle();
                        bundle.putString("profilePictureImageLink", user.getProfilePictureLink());
                        bundle.putString("userName", user.getUserName());
                        bundle.putString("userId", user.getUserId());
                        bundle.putString("userGender",user.getUserGender() );
                        bundle.putString("totalNumberOfCopies", user.getTotalNumberOfCopies());

                        Fragment findPreviewFragment  = new FindPreviewFragment();
                        findPreviewFragment.setArguments(bundle);

                        AppCompatActivity appCompatActivity = (AppCompatActivity)c;
                        FragmentTransaction ft = appCompatActivity.getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, findPreviewFragment);
                        ft.addToBackStack("FindFragment");
                        ft.commit();

                        vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
                        MainActivity.viewPagerIsVisible = false;

                        return true;
                    }
                    return false;
                }
            });


           /* itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });*/

        }
    }
}


class PicassoClient_FindFragment{


    public static void downloadImage(Context c, String imageUrl, ImageView img){

        if(imageUrl.length()>0 && imageUrl!= null){
            Picasso.with(c).load(imageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_black)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(img);

        }else{
            Picasso.with(c).load(R.drawable.ic_person_black).into(img);
        }
    }

}

package BackgroundWorkers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.kosalgeek.android.photoutil.ImageBase64;

import Fragments.DashBoardFragment;
import Fragments.SignUpFragment;

/**
 * Created by JEFF EMUVEYAN on 4/9/2018.
 */

public class ConvertImageToBaseString extends AsyncTask<Bitmap, Integer, String> {

   Context c;
   Bitmap bitmap;
   String fragmentName;


    public ConvertImageToBaseString(Context c, String fragmentName) {
        this.c = c;
        this.fragmentName = fragmentName;
    }




    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //We do our first task of compressing the image here
    }



    @Override
    protected String doInBackground(Bitmap... bitmaps) {

        //we do our second most resource intensive task of getting the image64 from the image
        //bitmap = getCompressedBitmap(intent);
        bitmap = bitmaps[0];

        String image64 =  ImageBase64.encode(bitmap);


        return image64;
    }




    @Override
    protected void onPostExecute(String image64String) {

        if(fragmentName.equals("SignUpFragment")){

            SignUpFragment.signUpUser(image64String);

        }else if (fragmentName.equals("DashBoardFragment")){//this means we are being used by the DashBoardFragment
            DashBoardFragment.sendNewImageToServer(image64String);
        }


    }


}

package Fragments_Secondary;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import BackgroundWorkers.DownloadSong;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;


public class DownloadSongFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static FloatingTextButton getButton, cancelButton;
    public static TextView titleTextView, loadingStatusTextView;
    public static ProgressBar progressBar;
    static Context c;

    AlertDialog.Builder alert;
    public static Handler handler;

    String userGender, audioLink, originalVideoPostId;
    boolean cancelButtonWasPressed;
    public static boolean isDownloading;

    Call call;


    public DownloadSongFragment() {
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
        View view = inflater.inflate(R.layout.fragment_download_song, container, false);

        titleTextView = (TextView)view.findViewById(R.id.titleTextView);
        loadingStatusTextView = (TextView)view.findViewById(R.id.loadingStatusTextView);
        cancelButton = (FloatingTextButton)view.findViewById(R.id.cancelButton);
        getButton= (FloatingTextButton)view.findViewById(R.id.getButton);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar4);

        progressBar.setMax(100);
        progressBar.setIndeterminate(true);


        alert = new AlertDialog.Builder(getActivity());
        handler = new Handler();

        cancelButtonWasPressed = false;
        isDownloading = false;

        c = getActivity();

        //Just do this because the user may choose to press back button
        MainActivity.navigation.setVisibility(View.VISIBLE);


        //We collect needed data from the previous fragment
        Bundle bundle = getArguments();
        userGender = bundle.getString("userGender");
        audioLink = bundle.getString("audioLink");
        originalVideoPostId = String.valueOf(bundle.getInt("videoPostId"));


        if(userGender.equals("Male")){
            titleTextView.setText("Click the button to get the song clip he used...");
        }else{
            titleTextView.setText("Click the button to get the song clip she used...");
        }


        //hide these for now:
        loadingStatusTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);



        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getActivity(), "Fetching...", Toast.LENGTH_SHORT).show();

                isDownloading = true;

                //hide the button
                getButton.setVisibility(View.GONE);
                //reveal these ones:
                loadingStatusTextView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);

                //download the song
                downLoadSong(audioLink);

            }

        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.setTitle("Cancel?");
                alert.setMessage("Do you want to cancel?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        cancelDownLoad();
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




        return view;
    }




    private void downLoadSong(String audioLink) {

        //We are using OkHttp to download because it was used in the video tutorial.
        //NOTE: OkHttp doesnt run process on the UI when its task is complete.
        // This means you will have to manually call 'handler' once you want to run any task on the UI thread.
        Request request = new Request.Builder().url(audioLink).build();

        call = new OkHttpClient().newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                isDownloading = false;

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(!cancelButtonWasPressed){
                            Toast.makeText(c, "Problem downloading song", Toast.LENGTH_SHORT).show();//pressing the cancel button runs 'call.cancel()' which
                        }//indirectly means we are deliberately failing the network, hence onFailure() will run. But We only want that toast to show if there is a genuine network
                        //failure not when we press cancel.
                        //So that statement means: if there was a network failure when the cancel button was not pressed, display that toast.

                        //show the getButton
                        getButton.setVisibility(View.VISIBLE);
                        //hide these ones:
                        loadingStatusTextView.setVisibility(View.GONE);
                        progressBar.setIndeterminate(true);
                        progressBar.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);

                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                //since we are just downloading the song directly from the link, we dont have to do any PHP server error message here.

               //after it downloads the song, we pass the response to our Asyntask.
                //OkHttp can do all by itself o, but we need the onPostExecute method that Asyntask have.
                DownloadSong downloadSong = new DownloadSong(c, originalVideoPostId);
                downloadSong.execute(response);

            }
        });

        //That's all o!

    }




    private void cancelDownLoad() {

        //stop the network call. This will immediately cause onFailure() to run.
        call.cancel();

        //do this so that onFailure() will know weather to display a toast or not
        cancelButtonWasPressed = true;
        isDownloading = false;

        //just to be on the safe side sha:
        DownloadSong.deleteFile();

        //After ending the download, we go back to the timeLineFragment
        android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();

        MainActivity.showViewPager();
    }





    @Override
    public void onDestroy() {
        super.onDestroy();

        //if the fragment is no longer visible, it must mean the user pressed the back button.
        //So we need to cancel any download operation if there is.
        if(isDownloading){
            cancelDownLoad();
        }


    }



}

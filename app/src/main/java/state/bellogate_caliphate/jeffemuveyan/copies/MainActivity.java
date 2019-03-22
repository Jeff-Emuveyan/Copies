package state.bellogate_caliphate.jeffemuveyan.copies;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import Classes.User;
import Fragments.DashBoardFragment;
import Fragments.FindFragment;
import Fragments.LogOutFragment;
import Fragments.SelectTrackFragment;
import Fragments.SignUpFragment;
import Fragments.TimeLineFragment;
import Fragments_Secondary.CameraFragment;
import Fragments_Secondary.LoginFragment;


public class MainActivity extends AppCompatActivity {

    public static BottomNavigationView navigation;
    AlertDialog.Builder alert;

    public static Boolean hideLogOutMenu;

    public static Boolean viewPagerIsVisible;

    public static boolean isSigningUpOrLogginIn;


    public static FragmentStatePagerAdapter adapterViewPager;
    public static ViewPager vpPager;

    public static ArrayList<Fragment> fragments;

    static android.support.v4.app.FragmentManager fm;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            if (vpPager != null) {

                switch (item.getItemId()) {
                    case R.id.navigation_time_line:
                        //to remove some bugs, we say:
                        popBackStack();
                        showViewPager();
                        //Now we reveal the fragment by saying:
                        slideToFragment(0);
                        break;
                    case R.id.navigation_post:
                        popBackStack();
                        showViewPager();
                        //Now we reveal the fragment by saying:
                        slideToFragment(1);
                        break;
                    case R.id.navigation_find:
                        popBackStack();
                        showViewPager();
                        //Now we reveal the fragment by saying:
                        slideToFragment(2);
                        break;
                    case R.id.navigation_dashboard:
                        popBackStack();
                        showViewPager();
                        //Now we reveal the fragment by saying:
                        slideToFragment(3);
                        break;

                }

            }

            return true;

        }
};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //to set our logo in the ActionBar
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setLogo(R.mipmap.copies_logo);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        //custom ActionBar
        //NOTE: Here we set the background of our actionBar to white. This will work but it will also cover the menuItem icon whose
        //color is also white. To solve this, we changed our menuItem color to black. Go to styles.xml to see how we did it.
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));


        setContentView(R.layout.activity_main);

        //Here we initialize the Mobile Ads sdk
        // MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");//Google default id for test launch
        //MobileAds.initialize(this, "ca-app-pub-8833381321617654~4533204866");
        MobileAds.initialize(this, "ca-app-pub-7286744545990292~7568076053");

        fm = getSupportFragmentManager();

        //setDailyNotification();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        vpPager.setOffscreenPageLimit(4);//this determines how many pages get loaded simultaneously
        adapterViewPager = new MainActivity.MyPagerAdapter(fm);

        alert = new AlertDialog.Builder(MainActivity.this);

        //create a folder where our file will be stored
        createFolder();

        hideLogOutMenu = false;
        viewPagerIsVisible = true;
        isSigningUpOrLogginIn = false;

        //Ok,now the app starts, we need to know wether to display the sign up Fragment
        // for the user to sign up or simply display the timeline fragment. so we:
        User user = User.findById(User.class, (long) 1);

        if (user == null) {// if this is null then the user has not sign up, so display SignUpFragment

            Fragment signUpFragment = new SignUpFragment();
            Fragment loginFragment = new LoginFragment();

            fragments = new ArrayList<>();
            fragments.add(signUpFragment);
            fragments.add(loginFragment);

            vpPager.setAdapter(adapterViewPager);

            vpPager.setCurrentItem(0);//Display SignUpFragment


        } else {
            //If the user is not null, then that means a user already exist. So we finally check the user login state
            if (user.getLoggedIn() == true) {
                //we simply display the timeline fragment and make ready the other fragments we will need

                //add the new Fragments we need
                Fragment timeLineFragemnt = new TimeLineFragment();
                Fragment selectTrackFragment = new SelectTrackFragment();
                Fragment findFragment = new FindFragment();
                Fragment dashBoardFragment = new DashBoardFragment();

                fragments = new ArrayList<>();
                fragments.add(timeLineFragemnt);
                fragments.add(selectTrackFragment);
                fragments.add(findFragment);
                fragments.add(dashBoardFragment);

                adapterViewPager.notifyDataSetChanged();

                vpPager.setAdapter(adapterViewPager);

                vpPager.setCurrentItem(0);//The time fragment should be at position 0 at his point

                //Now we want to do something so that when the user swipes through with his fingers, the bottom navigation drawer menu position gets updated
                vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {

                        //so when the user swipes with his fingers, we also move the position of the selected item on the bottom menu:
                        switch (position) {
                            case 0:
                                navigation.setSelectedItemId(R.id.navigation_time_line);
                                break;
                            case 1:
                                navigation.setSelectedItemId(R.id.navigation_post);
                                break;
                            case 2:
                                navigation.setSelectedItemId(R.id.navigation_find);
                                break;
                            case 3:
                                navigation.setSelectedItemId(R.id.navigation_dashboard);
                                break;
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });


            } else {//This means there is a user but the user has logged out. So display the SignUpFragment so the user can login again

                Fragment signUpFragment = new SignUpFragment();
                Fragment loginFragment = new LoginFragment();

                fragments = new ArrayList<>();
                fragments.add(signUpFragment);
                fragments.add(loginFragment);

                vpPager.setAdapter(adapterViewPager);

                vpPager.setCurrentItem(0);//Display SignUpFragment


            }

        }//END IF/ELSE


    }


    void setDailyNotification(){
        //this method sets a weekly reminder every Friday morning.
        Calendar calendar = Calendar.getInstance();

        //set the time when you want to notification to appear: (Our notification appears every day by 6:30:00pm.)
        calendar.set(Calendar.HOUR_OF_DAY, 18);//6pm. (Note: the range is from 00 - 23. So 18 will be 6pm.
        calendar.set(Calendar.MINUTE, 30);//30 minutes
        calendar.set(Calendar.SECOND, 00);//00 seconds

        Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }


    @Override
    public void onBackPressed() {

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //sometimes we hide our view pager. So we may need it to appear again when the back button is pressed
        showViewPager();

        if (fm.getBackStackEntryCount() > 0) {
            //first we must close the drawer
            fm.popBackStack();
        } else {//Now one we cant go back again, we open the drawer before closing the app.
            super.onBackPressed();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //Here we try to regulate when to show the full optionsMenu
        //NOTE: by default, all the menu item will show
        //in SignUpFragment, we set 'hideLogOutMenu' to true, then we call 'invalidatOptionsMenu'.
        //This call will cause 'onCreateOptionsMenu' to be called again where this time 'hideLogOutMenu' is true.
        if (hideLogOutMenu) {

            //Hide the logOut menu
            MenuItem menuItem = menu.findItem(R.id.logout);
            menuItem.setVisible(false);

            //when the app newly starts, the logout menu option should be hidden (as we have done above)
            // but the log in option should be visible:
            MenuItem menuLogIn = menu.findItem(R.id.login);
            menuLogIn.setVisible(true);

        } else {
            //reveal it
            MenuItem menuItem = menu.findItem(R.id.logout);
            menuItem.setVisible(true);

            //when the user has login or signup, we now need to remove the login menu option from the view
            MenuItem menuLogIn = menu.findItem(R.id.login);
            menuLogIn.setVisible(false);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {
            share();
            return true;
        }

        if (id == R.id.login) {
            //start the Login Fragment
            vpPager.setCurrentItem(1);
            return true;
        }

        if (id == R.id.logout) {

            alert.setTitle("LogOut?");
            alert.setMessage("Do you want to logout?");
            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    logOut();
                }
            });

            alert.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                }
            });

            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {

      /* To logout, we should simply delete the user from the database.
      But Sugar orm has a problem: If we delete our only data in the table Sugar Orm wont allow us add any data to the table again.
      So if u try to create a new User object (eg: When u want to signUp again on the same phone, Sugar ORM wont save your data
      Ull continue to see null null null...etc. No matter how i try to use the table again, it wont work.
      So my workaround is to add the boolean method isLoggedIn to my User class.
      So when a user logs out, instead of deleting the user from my table, i simply change the value of 'isLoggedIn' to false.
      If a new user want to sign up on the same phone, i overrite the new user data on the old one. I never delete my data.
      So to logout, we:
      */
        //Start the LogOutFragment
        Fragment fragment = new LogOutFragment();

        popBackStack();//just do this because sometimes the user may try to logout when he is in a second level fragment like EditDashBoard fragment.
        //or SelectStartingPoint fragment so we need to pop the back stack when we want to leave.
        //However, Camera fragment is a third level fragment sha, to logout from there, we need to pop the back stack twice.
        //So lets check if the Camera fragment is the one in view so that we can pop the back stack again:
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (currentFragment instanceof CameraFragment) {
            //pop the back stack again
            popBackStack();
        }


        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.addToBackStack("Main");
            ft.commit();

            vpPager.setVisibility(View.INVISIBLE);//Hide the viewPage because it will still be visible
            MainActivity.viewPagerIsVisible = false;
        }

    }


    private void share(){

        String appLink = "Hey!! Get the hottest new 15 seconds video sharing app at: https://play.google.com/store/apps/details?id=state.bellogate_caliphate.jeffemuveyan.copies";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Copies");
        shareIntent.putExtra(Intent.EXTRA_TEXT, appLink);

        startActivity(Intent.createChooser(shareIntent, "Share via"));


    }


    public static void popBackStack() {

        if (fm.getBackStackEntryCount() > 0) {
            //first we must close the drawer
            fm.popBackStack();
        }
    }


    public static void showViewPager() {

        if (!viewPagerIsVisible) {
            //reveal it
            vpPager.setVisibility(View.VISIBLE);
            viewPagerIsVisible = true;
        }
    }


    private void slideToFragment(final int position) {//This method is necessary to fix a stupid bug
        //the method 'onNavigationItemSelected' from BottomNavigationView runs when the app starts.
        //inside it, in our switch block we called stuffs like 'vpPager.setCurrentItem(0);'
        //this causes a bug crash Exception which says that the 'FragmentManager is still executing transations.
        //To solve this, we create this method and inside it we say:
        adapterViewPager = new MainActivity.MyPagerAdapter(fm);

        //and we do it like this:
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                vpPager.setCurrentItem(position);//slide to the fragment
            }
        });


    }


    //inner class
    public static class MyPagerAdapter extends FragmentStatePagerAdapter {


        public MyPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return fragments.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {

            return fragments.get(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }


        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }


    private void createFolder() {

        if (phoneHasSDcard()) {//if true, then create the folder in the memory card
            //Toast.makeText(MainActivity.this, "i have memory card ooooooooo", Toast.LENGTH_SHORT).show();
            File file = new File(Environment.getExternalStorageDirectory(), "Copies");

            if(!file.exists()){
                file.mkdirs();
            }




        } else {//if scdard is absent or if the phone supports sdcard but the scdard was removed:
            //Toast.makeText(MainActivity.this, "inbuilt memory", Toast.LENGTH_SHORT).show();
            File file = new File(getFilesDir()+"/Copies");
            if(!file.exists()){
                file.mkdirs();
            }



        }
    }


    private static boolean phoneHasSDcard() {

        boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        boolean isThisAnSDSupportedDevice = Environment.isExternalStorageRemovable();

        if (isThisAnSDSupportedDevice && isSDPresent) {//if the phone can support sdcard and the sdcard is present:
            return true;
        } else {
            return false;
        }


    }


    private void deleteAllDataInCopiesFolder() {

        File copiesFolder;


        if (phoneHasSDcard()) {

            copiesFolder = new File(Environment.getExternalStorageDirectory(), "Copies");//get the copies folder in sdCard

        } else {
            copiesFolder = new File(getFilesDir(), "Copies");//get the copies folder in phone memory
        }

        File[] listOfFiles = copiesFolder.listFiles();

        if(listOfFiles != null) {

            for (int i = 0; i < listOfFiles.length; i++) {

                if (listOfFiles[i].isFile() && listOfFiles[i].exists()) {
                    listOfFiles[i].delete();
                }
            }

        }

    }




   @Override
    protected void onDestroy() {
        super.onDestroy();

        //once you destroy the app, we secretly delete our unwanted data in the copies folder:
        deleteAllDataInCopiesFolder();
    }


}
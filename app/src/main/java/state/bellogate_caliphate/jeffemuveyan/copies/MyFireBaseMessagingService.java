package state.bellogate_caliphate.jeffemuveyan.copies;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import Classes.User;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

/**
 * Created by JEFF EMUVEYAN on 8/26/2018.
 */

public class MyFireBaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getNotification() != null){

            //display notification to the user
            displayNotification(remoteMessage.getNotification().getBody());
        }
    }


    private void displayNotification(String body){

        User user = User.findById(User.class, (long) 1);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //set notification sound
        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.copies_logo)
                .setContentTitle("Hello, "+user.getUserName()+"!")
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSoundURI)
                .setContentIntent(pendingIntent);


        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());


    }
}

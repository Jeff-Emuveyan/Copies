package state.bellogate_caliphate.jeffemuveyan.copies;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import Classes.User;
import state.bellogate_caliphate.jeffemuveyan.copies.MainActivity;
import state.bellogate_caliphate.jeffemuveyan.copies.R;

/**
 * Created by JEFF EMUVEYAN on 8/16/2018.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver{

    //This class is used for our daily notification.
    @Override
    public void onReceive(Context context, Intent intent) {

        User user = User.findById(User.class, (long) 1);

        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        PendingIntent pI = PendingIntent.getActivity(context, 100, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.copies)
                        .setContentTitle("Hello, "+user.getUserName()+"!")
                        .setContentText("Click to see new, cool and funny videos on Copies.")
                        .setContentIntent(pI)
                        .setAutoCancel(true);

        try {
            mNotificationManager.notify(100, mBuilder.build());//displays the notification.
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong...", Toast.LENGTH_SHORT).show();
        }
    }
}

package state.bellogate_caliphate.jeffemuveyan.copies;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by JEFF EMUVEYAN on 8/26/2018.
 */

public class MyFireBaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //you can save the users token to your database as you wish.
    }
}

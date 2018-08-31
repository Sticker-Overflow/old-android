package scub3d.stickeroverflow;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import static io.fabric.sdk.android.Fabric.TAG;

/**
 * Created by scub3d on 3/8/18.
 */

public class MessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: ");
        if (remoteMessage.getNotification() != null) {
            Intent in = new Intent();
            in.putExtra("icon", remoteMessage.getNotification().getIcon());
            in.putExtra("body",remoteMessage.getNotification().getBody());
            in.putExtra("sentTime", remoteMessage.getSentTime());
            in.setAction("ReceivedFCM");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
            Log.d(TAG, "onMessageReceived: BROADCASTING MESSAGE");
        }
    }
}

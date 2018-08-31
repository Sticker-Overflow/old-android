package scub3d.stickeroverflow;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by scub3d on 3/9/18.
 */

public interface ServiceCallbacks {
    void transmitNotificationData(RemoteMessage remoteMessage);
}

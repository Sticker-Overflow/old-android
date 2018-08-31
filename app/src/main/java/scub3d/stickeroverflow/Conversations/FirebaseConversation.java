package scub3d.stickeroverflow.Conversations;

/**
 * Created by scub3d on 3/5/18.
 */

public class FirebaseConversation {
    private String id, otherUserId, latestMessage;
    private int numberOfUnreadMessages;

    public FirebaseConversation() {
    }

    public FirebaseConversation(String id, String otherUserId, String latestMessage, int numberOfUnreadMessages) {
        this.id = id;
        this.otherUserId = otherUserId;
        this.latestMessage = latestMessage;
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public int getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }

    public void setNumberOfUnreadMessages(int numberOfUnreadMessages) {
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }
}

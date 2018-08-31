package scub3d.stickeroverflow.Conversations;

import scub3d.stickeroverflow.Users.User;

/**
 * Created by scub3d on 3/1/18.
 */

public class ConversationSummary {
    protected String id, latestMessage;
    protected User otherUser;
    protected long lastModified;
    protected int numberOfUnreadMessages;

    public ConversationSummary() {
    }

    public ConversationSummary(String id, User otherUser, String latestMessage, int numberOfUnreadMessages, long lastModified) {
        this.id = id;
        this.otherUser = otherUser;
        this.latestMessage = latestMessage;
        this.numberOfUnreadMessages = numberOfUnreadMessages;
        this.lastModified = lastModified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public int getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }
    
    public void setNumberOfUnreadMessages(int numberOfUnreadMessages) {
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }
}

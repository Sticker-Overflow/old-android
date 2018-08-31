package scub3d.stickeroverflow.Conversations;

/**
 * Created by scub3d on 3/1/18.
 */

public class Message implements Comparable<Message> {
    private String id, text, senderId;
    private long createdAt;
    private boolean hasBeenRead;

    public Message() {

    }

    public Message(String id, String text, String senderId, long createdAt, boolean hasBeenRead) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.createdAt = createdAt;
        this.hasBeenRead = hasBeenRead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isHasBeenRead() {
        return hasBeenRead;
    }

    public void setHasBeenRead(boolean hasBeenRead) {
        this.hasBeenRead = hasBeenRead;
    }

    public int compareTo(Message compareMessage) {
        long compareCreatedAt = ((Message) compareMessage).getCreatedAt();
        return this.getCreatedAt() < compareCreatedAt ? -1: this.getCreatedAt() > compareCreatedAt ? 1 : 0;
    }
}

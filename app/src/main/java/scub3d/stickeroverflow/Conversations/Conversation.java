package scub3d.stickeroverflow.Conversations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import scub3d.stickeroverflow.Users.User;

/**
 * Created by scub3d on 3/1/18.
 */

public class Conversation extends ConversationSummary {
    private ArrayList<Message> messages;

    public Conversation() {
    }

    public Conversation(String id, User otherUser, String latestMessage, int numberOfUnreadMessages, long lastModified, ArrayList<Message> messages) {
        this.id = id;
        this.otherUser = otherUser;
        this.latestMessage = latestMessage;
        this.numberOfUnreadMessages = numberOfUnreadMessages;
        this.lastModified = lastModified;
        this.messages = messages;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void sortMessages() {
        Collections.sort(this.messages);
    }

}
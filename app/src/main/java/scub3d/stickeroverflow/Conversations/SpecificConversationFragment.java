package scub3d.stickeroverflow.Conversations;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.UUID;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.MainActivity;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Users.User;

import static io.fabric.sdk.android.Fabric.TAG;

/**
 * Created by scub3d on 3/1/18.
 */

public class SpecificConversationFragment extends BaseFragment {

    private SpecificConversationFragment.OnFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private String conversationDataString;
    public Conversation conversation;
    private MessagesAdapter messagesAdapter;
    private boolean isConversationInFirebase = false;
    private Button sendMessageButton;
    private EditText messageToSend;

    public SpecificConversationFragment() {
        // Required empty public constructor
    }

    public static SpecificConversationFragment newInstance() {
        return new SpecificConversationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.specific_conversation_fragment, container, false);
        sendMessageButton = rootView.findViewById(R.id.sendMessageButton);
        messageToSend = rootView.findViewById(R.id.messageToSendEditText);
        recyclerView = rootView.findViewById(R.id.messagesRecyclerView);

        Log.d(TAG, "onCreateView: " + getArguments());

        for(String key : getArguments().keySet()) {
            Log.d(TAG, "onCreate: KEY = " + key);
            Object value = getArguments().get(key);
            Log.d(TAG, "onCreate: VALUE = " + value);
        }

        if(getArguments() != null) {
            isConversationInFirebase = !getArguments().getBoolean("newConversation");
            conversationDataString = getArguments().getString("conversationData");
            conversation = new Gson().fromJson(conversationDataString, Conversation.class);
            conversation.setMessages(new ArrayList<Message>());

            if(getArguments().getBoolean("isFromNotification")) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        createMessageCards();
                        listenForNewMessages();
                    }
                }, 1000);
            } else {
                createMessageCards();
                listenForNewMessages();
            }

            fillInData();
        } else {
            Log.d(TAG, "onCreateView: This shouldn't happen? Maybe?");
        }

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sendMessageButton.getText().toString().equals("") || !sendMessageButton.getText().toString().equals(" ") || !sendMessageButton.getText().toString().equals("  ")) {
                    if(!isConversationInFirebase) {
                        putConversationInFirebase();
                    } else {
                        putMessageInFirebase();
                    }
                }
            }
        });

        return rootView;
    }

    private void fillInData() {
        if(getArguments().getBoolean("isFromNotification")) {
            db.collection("users").document(getArguments().getString("otherUserId")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    conversation.otherUser = documentSnapshot.toObject(User.class);
                    getActivity().setTitle(conversation.getOtherUser().getName());
                }
            });
        } else {
            getActivity().setTitle(conversation.getOtherUser().getName());
        }
    }

    private void createMessageCards() {
        messagesAdapter = new MessagesAdapter(getContext(), this, conversation.getMessages());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(messagesAdapter);
        messagesAdapter.notifyDataSetChanged();
    }

    private ListenerRegistration listenerRegistration;
    private void listenForNewMessages() {
        listenerRegistration = db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").document(conversation.getId()).collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if(documentSnapshots != null ) {
                    for(DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        DocumentSnapshot snapshot = change.getDocument();

                        switch (change.getType()) {
                            case ADDED:
                                Message message = snapshot.toObject(Message.class);
                                message.setHasBeenRead(true);
                                updateMessageReadStatus(message);
                                conversation.addMessage(message);
                                resetUnreadMessageCounter();
                                break;

                            case MODIFIED:
                                break;
                            case REMOVED:
                                Log.d(TAG, "onEvent: This should never happen");
                                break;
                        }
                    }
                    conversation.sortMessages();
                    messagesAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void updateMessageReadStatus(Message message) {
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").document(conversation.getId()).collection("messages").document(message.getId()).update("hasBeenRead", true);    }

    private void resetUnreadMessageCounter() {
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").document(conversation.getId()).update("numberOfUnreadMessages", 0);
    }

    private void putConversationInFirebase() {
        FirebaseConversation firebaseConversation = new FirebaseConversation(conversation.getId(), conversation.getOtherUser().getUid(), "", 0);
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").document(conversation.getId()).set(firebaseConversation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            putMessageInFirebase();
                        }
                    }, 5000);
                }
            }
        });
    }

    private void putMessageInFirebase() {
        final Message newMessage = new Message(UUID.randomUUID().toString(), messageToSend.getText().toString(), ((MainActivity)getActivity()).getFirebaseId(), System.currentTimeMillis(), false);
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").document(conversation.getId()).collection("messages").document(newMessage.getId()).set(newMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    finishSendingMessage(newMessage);
                }
            }
        });
    }

    private void finishSendingMessage(Message message) {
        messageToSend.setText("");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpecificConversationFragment.OnFragmentInteractionListener) {
            mListener = (SpecificConversationFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        listenerRegistration.remove();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
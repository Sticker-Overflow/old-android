package scub3d.stickeroverflow.Conversations;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.MainActivity;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Users.User;

/**
 * Created by scub3d on 3/1/18.
 */

public class ConversationsFragment extends BaseFragment {
    public String TAG = "ConversationsFragment";

    private RecyclerView recyclerView;
    private ConversationsAdapter conversationsAdapter;
    private ArrayList<ConversationSummary> conversationsList;
    private OnFragmentInteractionListener mListener;

    public ConversationsFragment() {}

    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.conversations_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
        getActivity().setTitle("Conversations");

        recyclerView = rootView.findViewById(R.id.conversationRecyclerView);
        conversationsList = new ArrayList<>();

        noConversationsTextView = rootView.findViewById(R.id.noConversations);
        noConversationsTextView.setVisibility(View.GONE);

        conversationsAdapter = new ConversationsAdapter(getContext(), this, conversationsList);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(conversationsAdapter);

        conversationsAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                getMoreConversations();
            }
        });

        prepareConversations();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected void queryTextChangeSearch(String s) {
        ArrayList<ConversationSummary> tempList = new ArrayList();
        if(s.equals("") || s.equals(null)) {
            conversationsAdapter.updateList(conversationsAdapter.conversationsList);
        } else {
            for(ConversationSummary c : conversationsAdapter.conversationsList) {
                if(c.getOtherUser().getName().toLowerCase().contains(s) || c.getLatestMessage().toLowerCase().contains(s)) {
                    tempList.add(c);
                }
            }
            conversationsAdapter.updateList(tempList);
        }
    }

    protected void queryTextSubmitSearch(String s) {

    }

    private DocumentSnapshot cursor;
    private void prepareConversations() {
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").orderBy("lastModified", Query.Direction.DESCENDING)
                .orderBy("otherUserId", Query.Direction.ASCENDING).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        parseQuerySnapshot(task);
                    }
                });
    }

    private void getMoreConversations() {
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").orderBy("lastModified", Query.Direction.DESCENDING)
                .orderBy("otherUserId", Query.Direction.ASCENDING).startAfter(cursor).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        parseQuerySnapshot(task);
                    }
                });
    }

    private void parseQuerySnapshot(Task<QuerySnapshot> ctask) {
        if(ctask.isSuccessful()) {
            for(final DocumentSnapshot conversationDocumentSnapshot : ctask.getResult()) {
                db.collection("users").document(conversationDocumentSnapshot.getString("otherUserId")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot userDocumentSnapshot = task.getResult();
                            ConversationSummary conversationSummary = new ConversationSummary(conversationDocumentSnapshot.getId(), userDocumentSnapshot.toObject(User.class), conversationDocumentSnapshot.getString("latestMessage"), conversationDocumentSnapshot.getDouble("numberOfUnreadMessages").intValue(), conversationDocumentSnapshot.getLong("lastModified"));
                            conversationsList.add(conversationSummary);
                            cursor = conversationDocumentSnapshot;
                            conversationsAdapter.notifyDataSetChanged();
                        }

                        if(task.isComplete()) {
                            if(conversationsAdapter.getItemCount() == 0) {
                                noConversationsTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        } else {
            Log.d(TAG, "Error getting documents: ", ctask.getException());
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


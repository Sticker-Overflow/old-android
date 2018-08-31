package scub3d.stickeroverflow.Users;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.Conversations.ConversationSummary;
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Hackathons.HackathonsAdapter;
import scub3d.stickeroverflow.MainActivity;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Stickers.StickersAdapter;

import static io.fabric.sdk.android.Fabric.TAG;

/**
 * Created by scub3d on 3/1/18.
 */

public class SpecificUserFragment extends BaseFragment {

    private ImageView photo;
    private TextView name, attendedHackathonsTitle, upcomingHackathonsTitle, stickersTitle; // do org thing here too
    private Button messageUserButton;
    private RecyclerView stickersRecyclerView, attendedHackathonsRecyclerView, upcomingHackathonsRecyclerView;

    // put organization shit here

    private StickersAdapter stickersAdapter;
    private HackathonsAdapter attendedHackathonsAdapter, upcomingHackathonsAdapter;

    private User user;
    private String userDataString;

    private SpecificUserFragment.OnFragmentInteractionListener mListener;

    public SpecificUserFragment() {
        // Required empty public constructor
    }

    public static SpecificUserFragment newInstance() {
        return new SpecificUserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.specific_user_fragment, container, false);

        photo = rootView.findViewById(R.id.photo);
        name = rootView.findViewById(R.id.name);
        messageUserButton = rootView.findViewById(R.id.messageUserButton);

        stickersRecyclerView = rootView.findViewById(R.id.stickers);
        attendedHackathonsRecyclerView = rootView.findViewById(R.id.attendedHackathons);
        upcomingHackathonsRecyclerView = rootView.findViewById(R.id.upcomingHackathons);

        attendedHackathonsTitle = rootView.findViewById(R.id.attendedHackathonsTitle);
        upcomingHackathonsTitle = rootView.findViewById(R.id.upcomingHackathonsTitle);
        stickersTitle = rootView.findViewById(R.id.stickersTitle);

        if(getArguments() != null) {
            userDataString = getArguments().getString("userData");
            user = new Gson().fromJson(userDataString, User.class);
            prepareHackathons();
            prepareStickers();
            fillInData();
        } else {
            Log.d(TAG, "onCreateView: This shouldn't happen? Maybe?");
        }
        return rootView;
    }

    private void fillInData() {
        getActivity().setTitle(user.getName());

        Glide.with(this).load(user.getPhotoUrl()).into(photo);
        name.setText(user.getName());

        messageUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfConversationExists(user.getUid());
            }
        });
    }

    private void createAttendedHackathonsCards() {
        attendedHackathonsAdapter = new HackathonsAdapter(getContext(), this, user.getAttendedHackathons());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        attendedHackathonsRecyclerView.setLayoutManager(llm);
        attendedHackathonsRecyclerView.setAdapter(attendedHackathonsAdapter);
        attendedHackathonsAdapter.notifyDataSetChanged();

        if(user.getAttendedHackathons().size() == 0) {
            attendedHackathonsRecyclerView.setVisibility(View.GONE);
            attendedHackathonsTitle.setText(R.string.noHackathonsAttended);
        }
    }

    private void createUpcomingHackathonsCards() {
        upcomingHackathonsAdapter = new HackathonsAdapter(getContext(), this, user.getUpcomingHackathons());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        upcomingHackathonsRecyclerView.setLayoutManager(llm);
        upcomingHackathonsRecyclerView.setAdapter(upcomingHackathonsAdapter);
        upcomingHackathonsAdapter.notifyDataSetChanged();

        if(user.getUpcomingHackathons().size() == 0) {
            upcomingHackathonsRecyclerView.setVisibility(View.GONE);
            upcomingHackathonsTitle.setText(R.string.noHackathonsUpcoming);
        }
    }

    private void createStickerCards() {
        stickersAdapter = new StickersAdapter(getContext(), this, user.getStickers());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        stickersRecyclerView.setLayoutManager(gridLayoutManager);
        stickersRecyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();

        if(user.getStickers().size() == 0) {
            stickersRecyclerView.setVisibility(View.GONE);
            stickersTitle.setText(R.string.noStickersForUserFound);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpecificUserFragment.OnFragmentInteractionListener) {
            mListener = (SpecificUserFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void prepareHackathons() {
        db.collection("users").document(user.getUid()).collection("attendedHackathons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        hackathons.add(documentSnapshot.toObject(HackathonSummary.class));
                    }
                }

                if (task.isComplete()) {
                    user.setAttendedHackathons(hackathons);
                    createAttendedHackathonsCards();
                }
            }
        });

        db.collection("users").document(user.getUid()).collection("upcomingHackathons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        hackathons.add(documentSnapshot.toObject(HackathonSummary.class));
                    }
                }

                if (task.isComplete()) {
                    user.setUpcomingHackathons(hackathons);
                    createUpcomingHackathonsCards();
                }
            }
        });
    }

    public void prepareStickers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).collection("stickers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<StickerSummary> stickerSummaries = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        stickerSummaries.add(documentSnapshot.toObject(StickerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    user.setStickers(stickerSummaries);
                    createStickerCards();
                }
            }
        });
    }

    private boolean foundConversation = false;
    private int conversationsCounter = 0;
    private int conversationsCounterLimit = 0;
    private String tempUserIdHolder = "";

    public void setConversationsCounter(int limit, String userId) {
        conversationsCounter = 0;
        conversationsCounterLimit = limit;
        tempUserIdHolder = userId;
    }

    public void incrementConversationsCounter() {
        conversationsCounter++;
        Log.d(TAG, "incrementConversationsCounter: ?");
        if(counter >= conversationsCounterLimit && !foundConversation) {
            Log.d(TAG, "incrementConversationsCounter: this hsuld happen");
            createNewConversation(tempUserIdHolder);
        }
    }

    public void checkIfConversationExists(final String userId) {
        db.collection("users").document(((MainActivity)getActivity()).getFirebaseId()).collection("conversations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        createNewConversation(userId);
                    } else {
                        setConversationsCounter(task.getResult().getDocuments().size(), userId);
                        for(final DocumentSnapshot myDocumentSnapshot : task.getResult()) {
                            if(myDocumentSnapshot.getString("otherUserId").equals(userId)) {
                                foundConversation = true;
                                db.collection("users").document(myDocumentSnapshot.getString("otherUserId")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            DocumentSnapshot theirDocumentSnapshot = task.getResult();
                                            ConversationSummary conversationSummary = new ConversationSummary(myDocumentSnapshot.getId(), theirDocumentSnapshot.toObject(User.class), myDocumentSnapshot.getString("latestMessage"), myDocumentSnapshot.getDouble("numberOfUnreadMessages").intValue(), myDocumentSnapshot.getLong("lastModified"));
                                            selectConversation(conversationSummary);
                                        }
                                    }
                                });
                            }
                            incrementConversationsCounter();
                        }
                    }
                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

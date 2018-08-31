package scub3d.stickeroverflow.Organizers;

import android.content.Context;
import android.content.Intent;
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
import scub3d.stickeroverflow.Hackathons.SpecificHackathonFragment;
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Hackathons.HackathonsAdapter;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Stickers.StickersAdapter;
import scub3d.stickeroverflow.Users.User;
import scub3d.stickeroverflow.Users.UsersAdapter;

import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 2/8/18.
 */

public class SpecificOrganizerFragment extends BaseFragment {

    private ImageView organizerLogo;
    private TextView organizerName, organizerDescription, hackathonsTextView, stickersTextView;//, membersTextView, adminsTextView;
    private Button organizerWebsiteButton;
    private RecyclerView stickersRecyclerView, hackathonsRecyclerView;//, adminsRecyclerView, membersRecyclerView;

    private StickersAdapter stickersAdapter;
//    private UsersAdapter membersAdapter, adminsAdapter;
    private HackathonsAdapter hackathonsAdapter;

    private String organizerDataString;
    private Organizer organizer;
    private OrganizerSummary importedOrganizerObj;

    private SpecificHackathonFragment.OnFragmentInteractionListener mListener;

    public SpecificOrganizerFragment() {
        // Required empty public constructor
    }

    public static SpecificOrganizerFragment newInstance() {
        return new SpecificOrganizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.specific_organizer_fragment, container, false);

        organizerLogo = rootView.findViewById(R.id.logo);
        organizerName = rootView.findViewById(R.id.name);
        organizerDescription = rootView.findViewById(R.id.description);
        organizerWebsiteButton = rootView.findViewById(R.id.websiteButton);
        stickersRecyclerView = rootView.findViewById(R.id.stickers);
        hackathonsRecyclerView = rootView.findViewById(R.id.hackathons);
//        adminsRecyclerView = rootView.findViewById(R.id.admins);
//        membersRecyclerView = rootView.findViewById(R.id.members);

        hackathonsTextView = rootView.findViewById(R.id.hackathonsTitle);
        stickersTextView = rootView.findViewById(R.id.stickersTitle);
//        membersTextView = rootView.findViewById(R.id.membersTitle);
//        adminsTextView = rootView.findViewById(R.id.adminsTitle);

        if(getArguments() != null) {
            organizerDataString = getArguments().getString("organizerData");

            if(getArguments().getString("organizerDataStructure") == "organizer") {
                organizer = new Gson().fromJson(organizerDataString, Organizer.class);
                prepareHackathons();
//                prepareMembers();
                prepareStickers();
                fillInData();
            } else if(getArguments().getString("organizerDataStructure") == "organizerSummary") {
                importedOrganizerObj = new Gson().fromJson(organizerDataString, OrganizerSummary.class);
                getOrganizerDataFromFirebase();
            }

        } else {
            Log.d(TAG, "onCreateView: This shouldn't happen? Maybe?");
        }
        return rootView;
    }

    private void fillInData() {
        getActivity().setTitle(organizer.getName());

        Glide.with(this).load(organizer.getLogoURL()).into(organizerLogo);

        organizerName.setText(organizer.getName());
        organizerDescription.setText(organizer.getDescription());

        organizerWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(organizer.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void createHackathonCards() {
        hackathonsAdapter = new HackathonsAdapter(getContext(), this, organizer.getHackathons());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        hackathonsRecyclerView.setLayoutManager(llm);
        hackathonsRecyclerView.setAdapter(hackathonsAdapter);
        hackathonsAdapter.notifyDataSetChanged();

        if(organizer.getHackathons().size() == 0) {
            hackathonsRecyclerView.setVisibility(View.GONE);
            hackathonsTextView.setText(R.string.noHackathons);
        }
    }

//    private void createMemberCards() {
//        membersAdapter = new UsersAdapter(getContext(), this, organizer.getMembers());
//        LinearLayoutManager mllm = new LinearLayoutManager(getContext());
//        mllm.setOrientation(LinearLayoutManager.VERTICAL);
//        membersRecyclerView.setLayoutManager(mllm);
//        membersRecyclerView.setAdapter(membersAdapter);
//        membersAdapter.notifyDataSetChanged();
//
//        if(organizer.getMembers().size() == 0) {
//            membersRecyclerView.setVisibility(View.GONE);
//            membersTextView.setText(R.string.noMembersFound);
//        }
//
//        adminsAdapter = new UsersAdapter(getContext(), this, organizer.getAdmins());
//        LinearLayoutManager allm = new LinearLayoutManager(getContext());
//        allm.setOrientation(LinearLayoutManager.VERTICAL);
//        adminsRecyclerView.setLayoutManager(allm);
//        adminsRecyclerView.setAdapter(adminsAdapter);
//        adminsAdapter.notifyDataSetChanged();
//
//        if(organizer.getAdmins().size() == 0) {
//            adminsRecyclerView.setVisibility(View.GONE);
//            adminsTextView.setText(R.string.noAdminsFound);
//        }
//    }

    private void createStickerCards() {
        stickersAdapter = new StickersAdapter(getContext(), this, organizer.getStickers());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        stickersRecyclerView.setLayoutManager(gridLayoutManager);
        stickersRecyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();

        if(organizer.getStickers().size() == 0) {
            stickersRecyclerView.setVisibility(View.GONE);
            stickersTextView.setText(R.string.noStickersFound);
        }
    }

    private void getOrganizerDataFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizers").document(importedOrganizerObj.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    ArrayList<StickerSummary> stickers = new ArrayList<>();
                    ArrayList<HackathonSummary> hackathons = new ArrayList<>();
//                    ArrayList<User> admins = new ArrayList<>();
//                    ArrayList<User> members = new ArrayList<>();

                    organizer = new Organizer(documentSnapshot.getId(),
                            documentSnapshot.getString("name"), documentSnapshot.getString("description"),
                            documentSnapshot.getString("location"), documentSnapshot.getString("url"),
                            documentSnapshot.getString("logoURL"), stickers, hackathons, null, null);// members, admins);

                    prepareHackathons();
                    prepareStickers();
//                    prepareMembers();
                    fillInData();

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpecificHackathonFragment.OnFragmentInteractionListener) {
            mListener = (SpecificHackathonFragment.OnFragmentInteractionListener) context;
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
        db.collection("organizers").document(organizer.getId()).collection("hackathons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        hackathons.add(documentSnapshot.toObject(HackathonSummary.class));
                    }
                }

                if (task.isComplete()) {
                    organizer.setHackathons(hackathons);
                    createHackathonCards();
                }
            }
        });
    }

    public void prepareStickers() {
        db.collection("organizers").document(organizer.getId()).collection("stickers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<StickerSummary> stickerSummaries = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        stickerSummaries.add(documentSnapshot.toObject(StickerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    organizer.setStickers(stickerSummaries);
                    createStickerCards();
                }
            }
        });
    }

//    public void prepareMembers() {
//        db.collection("organizers").document(organizer.getId()).collection("members").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                ArrayList<User> members = new ArrayList<>();
//                ArrayList<User> admins = new ArrayList<>();
//
//                if(task.isSuccessful()) {
//                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
//                        if(documentSnapshot.getBoolean("isAdmin")) {
//                            admins.add(documentSnapshot.toObject(User.class));
//                        } else {
//                            members.add(documentSnapshot.toObject(User.class));
//                        }
//                    }
//                }
//
//                if (task.isComplete()) {
//                    organizer.setAdmins(admins);
//                    organizer.setMembers(members);
//                    createMemberCards();
//                }
//            }
//        });
//    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

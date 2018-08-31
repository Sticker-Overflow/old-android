package scub3d.stickeroverflow.Sponsors;

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
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Hackathons.HackathonsAdapter;
import scub3d.stickeroverflow.Hackathons.SpecificHackathonFragment;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Stickers.StickersAdapter;
import scub3d.stickeroverflow.Users.User;
import scub3d.stickeroverflow.Users.UsersAdapter;

import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 2/8/18.
 */

public class SpecificSponsorFragment extends BaseFragment {

    private ImageView sponsorLogo;
    private TextView sponsorName, sponsorDescription, hackathonsTextView, stickersTextView;//, membersTextView, adminsTextView;
    private Button sponsorWebsiteButton;
    private RecyclerView stickersRecyclerView, hackathonsRecyclerView;//, adminsRecyclerView, membersRecyclerView;

    private StickersAdapter stickersAdapter;
//    private UsersAdapter membersAdapter, adminsAdapter;
    private HackathonsAdapter hackathonsAdapter;

    private String sponsorDataString;
    private Sponsor sponsor;
    private SponsorSummary importedSponsorObj;

    private SpecificHackathonFragment.OnFragmentInteractionListener mListener;

    public SpecificSponsorFragment() {
        // Required empty public constructor
    }

    public static SpecificSponsorFragment newInstance() {
        return new SpecificSponsorFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.specific_sponsor_fragment, container, false);

        sponsorLogo = rootView.findViewById(R.id.logo);
        sponsorName = rootView.findViewById(R.id.name);
        sponsorDescription = rootView.findViewById(R.id.description);
        sponsorWebsiteButton = rootView.findViewById(R.id.websiteButton);
        stickersRecyclerView = rootView.findViewById(R.id.stickers);
        hackathonsRecyclerView = rootView.findViewById(R.id.hackathons);
//        adminsRecyclerView = rootView.findViewById(R.id.admins);
//        membersRecyclerView = rootView.findViewById(R.id.members);

        hackathonsTextView = rootView.findViewById(R.id.hackathonsTitle);
        stickersTextView = rootView.findViewById(R.id.stickersTitle);
//        membersTextView = rootView.findViewById(R.id.membersTitle);
//        adminsTextView = rootView.findViewById(R.id.adminsTitle);

        if(getArguments() != null) {
            sponsorDataString = getArguments().getString("sponsorData");

            if(getArguments().getString("sponsorDataStructure") == "sponsor") {
                sponsor = new Gson().fromJson(sponsorDataString, Sponsor.class);
                prepareHackathons();
//                prepareMembers();
                prepareStickers();
                fillInData();
            } else if(getArguments().getString("sponsorDataStructure") == "sponsorSummary") {
                importedSponsorObj = new Gson().fromJson(sponsorDataString, SponsorSummary.class);
                getSponsorDataFromFirebase();
            }

        } else {
            Log.d(TAG, "onCreateView: This shouldn't happen? Maybe?");
        }
        return rootView;
    }

    private void fillInData() {
        getActivity().setTitle(sponsor.getName());

        Glide.with(this).load(sponsor.getLogoURL()).into(sponsorLogo);

        sponsorName.setText(sponsor.getName());
        sponsorDescription.setText(sponsor.getDescription());

        sponsorWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(sponsor.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void createHackathonCards() {
        hackathonsAdapter = new HackathonsAdapter(getContext(), this, sponsor.getHackathons());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        hackathonsRecyclerView.setLayoutManager(llm);
        hackathonsRecyclerView.setAdapter(hackathonsAdapter);
        hackathonsAdapter.notifyDataSetChanged();

        if(sponsor.getHackathons().size() == 0) {
            hackathonsRecyclerView.setVisibility(View.GONE);
            hackathonsTextView.setText(R.string.noHackathons);
        }
    }

//    private void createMemberCards() {
//        membersAdapter = new UsersAdapter(getContext(), this, sponsor.getMembers());
//        LinearLayoutManager mllm = new LinearLayoutManager(getContext());
//        mllm.setOrientation(LinearLayoutManager.VERTICAL);
//        membersRecyclerView.setLayoutManager(mllm);
//        membersRecyclerView.setAdapter(membersAdapter);
//        membersAdapter.notifyDataSetChanged();
//
//        if(sponsor.getMembers().size() == 0) {
//            membersRecyclerView.setVisibility(View.GONE);
//            membersTextView.setText(R.string.noMembersFound);
//        }
//
//        adminsAdapter = new UsersAdapter(getContext(), this, sponsor.getAdmins());
//        LinearLayoutManager allm = new LinearLayoutManager(getContext());
//        allm.setOrientation(LinearLayoutManager.VERTICAL);
//        adminsRecyclerView.setLayoutManager(allm);
//        adminsRecyclerView.setAdapter(adminsAdapter);
//        adminsAdapter.notifyDataSetChanged();
//
//        if(sponsor.getAdmins().size() == 0) {
//            adminsRecyclerView.setVisibility(View.GONE);
//            adminsTextView.setText(R.string.noAdminsFound);
//        }
//    }

    private void createStickerCards() {
        stickersAdapter = new StickersAdapter(getContext(), this, sponsor.getStickers());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        stickersRecyclerView.setLayoutManager(gridLayoutManager);
        stickersRecyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();

        if(sponsor.getStickers().size() == 0) {
            stickersRecyclerView.setVisibility(View.GONE);
            stickersTextView.setText(R.string.noStickersFound);
        }
    }

    private void getSponsorDataFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sponsors").document(importedSponsorObj.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    ArrayList<StickerSummary> stickers = new ArrayList<>();
                    ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                    ArrayList<User> admins = new ArrayList<>();
                    ArrayList<User> members = new ArrayList<>();

                    sponsor = new Sponsor(documentSnapshot.getId(),
                            documentSnapshot.getString("name"), documentSnapshot.getString("description"),
                            documentSnapshot.getString("location"), documentSnapshot.getString("url"),
                            documentSnapshot.getString("logoURL"), stickers, hackathons, members, admins);

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
        db.collection("sponsors").document(sponsor.getId()).collection("hackathons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot oDocument : task.getResult()) {
                        hackathons.add(oDocument.toObject(HackathonSummary.class));
                    }
                }

                if (task.isComplete()) {
                    sponsor.setHackathons(hackathons);
                    createHackathonCards();
                }
            }
        });
    }

    public void prepareStickers() {
        db.collection("sponsors").document(sponsor.getId()).collection("stickers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<StickerSummary> stickerSummaries = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        stickerSummaries.add(documentSnapshot.toObject(StickerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    sponsor.setStickers(stickerSummaries);
                    createStickerCards();
                }
            }
        });
    }

//    public void prepareMembers() {
//        db.collection("sponsors").document(sponsor.getId()).collection("members").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
//                    sponsor.setAdmins(admins);
//                    sponsor.setMembers(members);
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

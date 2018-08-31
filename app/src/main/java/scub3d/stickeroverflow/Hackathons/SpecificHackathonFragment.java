package scub3d.stickeroverflow.Hackathons;

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
import scub3d.stickeroverflow.Organizers.OrganizerSummary;
import scub3d.stickeroverflow.Organizers.OrganizersAdapter;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Stickers.StickersAdapter;

import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 2/7/18.
 */

public class SpecificHackathonFragment extends BaseFragment {
    private ImageView hackathonSplash, hackathonLogo;
    private TextView hackathonName, hackathonDate, hackathonLocation, stickersTextView, sponsorsTextView, organizersTextView;
    private Button hackathonWebsiteButton, addStickersButton, addOrganizationsButton;
    private RecyclerView stickersRecyclerView, sponsorsRecyclerView, organizersRecyclerView;

    private StickersAdapter stickersAdapter;
    private OrganizersAdapter sponsorsAdapter, organizersAdapter;

    private String hackathonDataString;
    private Hackathon hackathon;
    private HackathonSummary tmpHackathonObj;

    private SpecificHackathonFragment.OnFragmentInteractionListener mListener;

    public SpecificHackathonFragment() {
        // Required empty public constructor
    }

    public static SpecificHackathonFragment newInstance() {
        return new SpecificHackathonFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.specific_hackathon_fragment, container, false);

        hackathonLogo = rootView.findViewById(R.id.logo);
        hackathonSplash = rootView.findViewById(R.id.splash);
        hackathonName = rootView.findViewById(R.id.name);
        hackathonDate = rootView.findViewById(R.id.date);
        hackathonLocation = rootView.findViewById(R.id.location);
        hackathonWebsiteButton = rootView.findViewById(R.id.websiteButton);
        stickersRecyclerView = rootView.findViewById(R.id.stickers);
        sponsorsRecyclerView = rootView.findViewById(R.id.sponsors);
        organizersRecyclerView = rootView.findViewById(R.id.organizers);
        stickersTextView = rootView.findViewById(R.id.stickersTitle);
        sponsorsTextView = rootView.findViewById(R.id.sponsorsTitle);
        organizersTextView = rootView.findViewById(R.id.organizersTitle);

        if(getArguments() != null) {
            hackathonDataString = getArguments().getString("hackathonData");
            if(getArguments().getString("hackathonDataStructure") == "hackathon") {
                hackathon = new Gson().fromJson(hackathonDataString, Hackathon.class);
                prepareOrganizations();
                prepareStickers();
                fillInData();
            } else if(getArguments().getString("hackathonDataStructure") == "hackathonSummary") {
                tmpHackathonObj = new Gson().fromJson(hackathonDataString, HackathonSummary.class);
                getHackathonDataFromFirebase();
            }
        } else {
            Log.d(TAG, "onCreateView: This shouldn't happen? Maybe?");
        }
        return rootView;
    }

    private void fillInData() {
        getActivity().setTitle(hackathon.getName());

        Glide.with(this).load(hackathon.getSplashURL()).into(hackathonSplash);
        Glide.with(this).load(hackathon.getLogoURL()).into(hackathonLogo);

        hackathonName.setText(hackathon.getName());
        hackathonDate.setText(hackathon.getDateString());
        hackathonLocation.setText(hackathon.getLocation());

        hackathonWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(hackathon.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void createSponsorsCards() {
        sponsorsAdapter = new OrganizersAdapter(getContext(), this, hackathon.getSponsors());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        sponsorsRecyclerView.setLayoutManager(llm);
        sponsorsRecyclerView.setAdapter(sponsorsAdapter);
        sponsorsAdapter.notifyDataSetChanged();
        if(hackathon.getSponsors().size() == 0) {
            sponsorsRecyclerView.setVisibility(View.GONE);
            sponsorsTextView.setText(R.string.noSponsorsFound);
        }
    }

    private void createOrganizersCards() {
        organizersAdapter = new OrganizersAdapter(getContext(), this, hackathon.getOrganizers());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        organizersRecyclerView.setLayoutManager(llm);
        organizersRecyclerView.setAdapter(organizersAdapter);
        organizersAdapter.notifyDataSetChanged();
        if(hackathon.getOrganizers().size() == 0) {
            organizersRecyclerView.setVisibility(View.GONE);
            organizersTextView.setText(R.string.noOrganizersFound);
        }
    }

    private void createStickerCards() {
        stickersAdapter = new StickersAdapter(getContext(), this, hackathon.getStickers());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        stickersRecyclerView.setLayoutManager(gridLayoutManager);
        stickersRecyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();
        if(hackathon.getStickers().size() == 0) {
            stickersRecyclerView.setVisibility(View.GONE);
            stickersTextView.setText(R.string.noStickersFound);
        }
    }

    private void getHackathonDataFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("hackathons").document(tmpHackathonObj.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    ArrayList<StickerSummary> stickers = new ArrayList<>();
                    ArrayList<OrganizerSummary> sponsors = new ArrayList<>();
                    ArrayList<OrganizerSummary> organizers = new ArrayList<>();

                    hackathon = new Hackathon(documentSnapshot.getId(), documentSnapshot.getString("name"),
                            documentSnapshot.getString("dateString"), documentSnapshot.getDate("date"),
                            documentSnapshot.getString("location"), documentSnapshot.getString("url"),
                            documentSnapshot.getString("logoURL"), documentSnapshot.getString("splashURL"),
                            stickers, sponsors, organizers);

                    prepareOrganizations();
                    prepareStickers();
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

    public void prepareOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("hackathons").document(hackathon.getId()).collection("sponsors").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<OrganizerSummary> sponsors = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot oDocument : task.getResult()) {
                        sponsors.add(oDocument.toObject(OrganizerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    hackathon.setSponsors(sponsors);
                    createSponsorsCards();
                }
            }
        });

        db.collection("hackathons").document(hackathon.getId()).collection("organizers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<OrganizerSummary> organizers = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot oDocument : task.getResult()) {
                        organizers.add(oDocument.toObject(OrganizerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    hackathon.setOrganizers(organizers);
                    createOrganizersCards();
                }
            }
        });
    }

    public void prepareStickers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("hackathons").document(hackathon.getId()).collection("stickers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<StickerSummary> stickerSummaries = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        stickerSummaries.add(documentSnapshot.toObject(StickerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    hackathon.setStickers(stickerSummaries);
                    createStickerCards();
                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

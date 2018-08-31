package scub3d.stickeroverflow.Stickers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Hackathons.HackathonsAdapter;
import scub3d.stickeroverflow.Organizers.OrganizerSummary;
import scub3d.stickeroverflow.Organizers.OrganizersAdapter;
import scub3d.stickeroverflow.R;

import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 2/10/18.
 */

public class SpecificStickerFragment extends BaseFragment {

    private ImageView stickerImage;
    private TextView stickerName, stickerDescription, stickerOwnerCountTextView, organizationTitle, hackathonsGivenAtTitle;
    private RecyclerView organizationsRecyclerView, hackathonsRecyclerView;
    private Button purchaseStickerButton;

    private String stickerDataString, tmpStickerObj;
    private Sticker sticker;

    private OrganizersAdapter organizationsAdapter;
    private HackathonsAdapter hackathonsAdapter;

    private SpecificStickerFragment.OnFragmentInteractionListener mListener;

    public SpecificStickerFragment() {
        // Required empty public constructor
    }

    public static SpecificStickerFragment newInstance(String param1, String param2) {
        return new SpecificStickerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.specific_sticker_fragment, container, false);

        stickerImage = rootView.findViewById(R.id.stickerImage);
        stickerName = rootView.findViewById(R.id.name);
        stickerDescription = rootView.findViewById(R.id.description);
        organizationsRecyclerView = rootView.findViewById(R.id.organizations);
        hackathonsRecyclerView = rootView.findViewById(R.id.hackathons);
        stickerOwnerCountTextView = rootView.findViewById(R.id.ownerCount);
        purchaseStickerButton = rootView.findViewById(R.id.purchaseStickerButton);

        organizationTitle = rootView.findViewById(R.id.organizationsTitle);
        hackathonsGivenAtTitle = rootView.findViewById(R.id.hackathonsGivenAtTitle);

        if(getArguments() != null) {
            stickerDataString = getArguments().getString("stickerData");
            if(getArguments().getString("stickerDataStructure") == "sticker") {
                sticker = new Gson().fromJson(stickerDataString, Sticker.class);
                prepareHackathons();
                prepareOrganizations();
                fillInData();
            } else if(getArguments().getString("stickerDataStructure") == "stickerSummary") {
                StickerSummary ss = new Gson().fromJson(stickerDataString, StickerSummary.class);
                tmpStickerObj = ss.getId();
                getStickerFromFirebase();
            }
        } else {
            Log.d(TAG, "onCreateView: This shouldn't happen? Maybe?");
        }
        return rootView;
    }

    private void fillInData() {
        getActivity().setTitle(sticker.getName());
        stickerName.setText(sticker.getName());
        stickerDescription.setText(sticker.getDescription());
        stickerOwnerCountTextView.setText(sticker.getNumberOwned() + " users have this sticker");
        purchaseStickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(sticker.getPurchaseUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("stickers/" + sticker.getId() + "/" + sticker.getId() + ".png");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri.toString()).into(stickerImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: " + exception);
            }
        });
    }

    private void getStickerFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "getStickerFromFirebase: " + tmpStickerObj);
        db.collection("stickers").document(tmpStickerObj).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                    ArrayList<OrganizerSummary> organizations = new ArrayList<>();

                    sticker = new Sticker(documentSnapshot.getId(), documentSnapshot.getString("name"),
                            documentSnapshot.getString("description"), documentSnapshot.getString("purchaseUrl"),
                            documentSnapshot.getDouble("numberOfUsersWhoHaveThisSticker").intValue(), hackathons, organizations);

                    prepareOrganizations();
                    prepareHackathons();
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
        if (context instanceof SpecificStickerFragment.OnFragmentInteractionListener) {
            mListener = (SpecificStickerFragment.OnFragmentInteractionListener) context;
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("stickers").document(sticker.getId()).collection("hackathons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot oDocument : task.getResult()) {
                        hackathons.add(oDocument.toObject(HackathonSummary.class));
                    }
                }

                if (task.isComplete()) {
                    sticker.setHackathons(hackathons);
                    createHackathonCards();
                }
            }
        });
    }


    public void prepareOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("stickers").document(sticker.getId()).collection("organizations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<OrganizerSummary> organizations = new ArrayList<>();
                if(task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        organizations.add(document.toObject(OrganizerSummary.class));
                    }
                }

                if (task.isComplete()) {
                    sticker.setOrganizations(organizations);
                    createOrganizationCards();
                }
            }
        });
    }

    private void createHackathonCards() {
        hackathonsAdapter = new HackathonsAdapter(getContext(), this, sticker.getHackathons());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        hackathonsRecyclerView.setLayoutManager(llm);
        hackathonsRecyclerView.setAdapter(hackathonsAdapter);
        hackathonsAdapter.notifyDataSetChanged();

        if(sticker.getHackathons().size() == 0) {
            hackathonsRecyclerView.setVisibility(View.GONE);
            hackathonsGivenAtTitle.setText(R.string.noHackathonsGivenOutAt);
            hackathonsGivenAtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }

    private void createOrganizationCards() {
        organizationsAdapter = new OrganizersAdapter(getContext(), this, sticker.getOrganizations());
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        organizationsRecyclerView.setLayoutManager(llm);
        organizationsRecyclerView.setAdapter(organizationsAdapter);
        organizationsAdapter.notifyDataSetChanged();

        if(sticker.getOrganizations().size() == 0) {
            organizationsRecyclerView.setVisibility(View.GONE);
            organizationTitle.setText(R.string.noOrganizersGivenByFound);
            organizationTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

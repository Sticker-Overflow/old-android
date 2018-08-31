package scub3d.stickeroverflow.Sponsors;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Users.User;

/**
 * Created by scub3d on 2/7/18.
 */

public class SponsorsFragment extends BaseFragment {
    public String TAG = "SponsorsFragment";

    private RecyclerView recyclerView;
    private SponsorsAdapter sponsorsAdapter;
    private ArrayList<Sponsor> sponsorList;
    private SponsorsFragment.OnFragmentInteractionListener mListener;

    public SponsorsFragment() {}

    public static SponsorsFragment newInstance() {
        return new SponsorsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.sponsors_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setTitle("Sponsors");
        setHasOptionsMenu(true);

        recyclerView = rootView.findViewById(R.id.sponsorRecyclerView);
        sponsorList = new ArrayList<>();

        noSponsorsTextView = rootView.findViewById(R.id.noSponsors);
        noSponsorsTextView.setVisibility(View.GONE);

        if(getArguments() != null) {
            hasArguments = true;
            if(getArguments().getBoolean("isAdding")) {
                isAdding = true;
                collectionName = getArguments().getString("collection");
                subCollectionName = "sponsors";
                documentId = getArguments().getString("document");
                currentSponsorSummaries = getArguments().getParcelableArrayList("sponsors");
                sponsorsAdapter = new SponsorsAdapter(getContext(), this, sponsorList, null, true);
            } else {
                Log.d(TAG, "onCreateView: This shouldn't happen");
            }
        } else {
            sponsorsAdapter = new SponsorsAdapter(getContext(), this, sponsorList, null, false);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(sponsorsAdapter);

        sponsorsAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                getMoreSponsors();
            }
        });

        prepareSponsors();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SponsorsFragment.OnFragmentInteractionListener) {
            mListener = (SponsorsFragment.OnFragmentInteractionListener) context;
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
        ArrayList<Sponsor> tempList = new ArrayList();
        if(s.equals("") || s.equals(null)) {
            sponsorsAdapter.updateList(sponsorsAdapter.sponsorList);
        } else {
            for(Sponsor o : sponsorsAdapter.sponsorList) {
                if(o.getName().toLowerCase().contains(s) || o.getLocation().toLowerCase().contains(s) || o.getDescription().toLowerCase().contains(s)) {
                    tempList.add(o);
                }
            }
            sponsorsAdapter.updateList(tempList);
        }
    }

    protected void queryTextSubmitSearch(String s) {

    }

    protected void confirmListSelection() {
        ArrayList<String> dataToSend = new ArrayList<>();
        for(SponsorSummary ss : selectedSponsors) {
            dataToSend.add(new Gson().toJson(ss));
        }
        addToSubcollection(collectionName, documentId, subCollectionName, dataToSend, "sponsor");
    }

    protected void cancelListSelection() {
        selectedSponsors = new ArrayList<>();
        sponsorsAdapter.deselectElements(recyclerView);
        sponsorsAdapter.notifyDataSetChanged();
    }

    private DocumentSnapshot cursor;
    private void prepareSponsors() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sponsors").orderBy("name", Query.Direction.ASCENDING).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       parseQuerySnapshot(task);
                    }
                });
    }

    private void getMoreSponsors() {
        db.collection("sponsors").orderBy("name", Query.Direction.ASCENDING)
                .startAfter(cursor).limit(20).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    parseQuerySnapshot(task);
                    }
                });
    }

    private void parseQuerySnapshot(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            for (final DocumentSnapshot documentSnapshot : task.getResult()) {

                ArrayList<StickerSummary> stickerSummaries = new ArrayList<>();
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                ArrayList<User> members = new ArrayList<>();
                ArrayList<User> admins = new ArrayList<>();

                Sponsor sponsor = new Sponsor(documentSnapshot.getId(),
                        documentSnapshot.getString("name"), documentSnapshot.getString("description"),
                        documentSnapshot.getString("url"), documentSnapshot.getString("location"),
                        documentSnapshot.getString("logoURL"), stickerSummaries, hackathons, members, admins);

                sponsorList.add(sponsor);
                cursor = documentSnapshot;
                sponsorsAdapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "Error getting documents: ", task.getException());
        }

        if(task.isComplete()) {
            if(sponsorsAdapter.getItemCount() == 0) {
                noSponsorsTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


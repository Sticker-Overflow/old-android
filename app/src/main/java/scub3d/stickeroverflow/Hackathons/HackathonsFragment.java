package scub3d.stickeroverflow.Hackathons;

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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.Organizers.OrganizerSummary;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;

/**
 * Created by scub3d on 2/5/18.
 */

public class HackathonsFragment extends BaseFragment {
    public String TAG = "HackathonsFragment";

    private RecyclerView recyclerView;
    private HackathonsAdapter hackathonsAdapter;
    private ArrayList<Hackathon> hackathonList;
    private OnFragmentInteractionListener mListener;

    public HackathonsFragment() {}

    public static HackathonsFragment newInstance() {
        return new HackathonsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.hackathons_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
        getActivity().setTitle("Hackathons");

        recyclerView = rootView.findViewById(R.id.hackathonRecyclerView);
        hackathonList = new ArrayList<>();

        noHackathonsTextView = rootView.findViewById(R.id.noHackathons);
        noHackathonsTextView.setVisibility(View.GONE);

        if(getArguments() != null) {
            hasArguments = true;
            if(getArguments().getBoolean("isAdding")) {
                isAdding = true;
                collectionName = getArguments().getString("collection");
                subCollectionName = "hackathons";
                documentId = getArguments().getString("document");
                currentHackathonSummaries = getArguments().getParcelableArrayList("hackathons");
                hackathonsAdapter = new HackathonsAdapter(getContext(), this, hackathonList, null, true);
            } else {
                Log.d(TAG, "onCreateView: This shouldn't happen");
            }
        } else {
            hackathonsAdapter = new HackathonsAdapter(getContext(), this, hackathonList, null, false);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(hackathonsAdapter);

        hackathonsAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                getMoreHackathons();
            }
        });

        prepareHackathons();

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
        List<Hackathon> tempList = new ArrayList();
        if(s.equals("") || s.equals(null)) {
            hackathonsAdapter.updateList(hackathonsAdapter.hackathonList);
        } else {
            for(Hackathon h : hackathonsAdapter.hackathonList) {
                if(h.getLocation().toLowerCase().contains(s) || h.getName().toLowerCase().contains(s)) {
                    tempList.add(h);
                }
            }
            hackathonsAdapter.updateList(tempList);
        }
    }

    protected void queryTextSubmitSearch(String s) {

    }

    protected void confirmListSelection() {
        ArrayList<String> dataToSend = new ArrayList<>();
        for(HackathonSummary hs : selectedHackathons) {
            dataToSend.add(new Gson().toJson(hs));
        }
        addToSubcollection(collectionName, documentId, subCollectionName, dataToSend, "hackathon");
    }

    protected void cancelListSelection() {
        selectedHackathons = new ArrayList<>();
        hackathonsAdapter.deselectElements(recyclerView);
    }

    private DocumentSnapshot cursor;
    private void prepareHackathons() {
        db.collection("hackathons").orderBy("date", Query.Direction.DESCENDING)
                .orderBy("name", Query.Direction.ASCENDING).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        parseQuerySnapshot(task);
                    }
                });
    }

    private void getMoreHackathons() {
        db.collection("hackathons").orderBy("date", Query.Direction.DESCENDING)
                .orderBy("name", Query.Direction.ASCENDING).startAfter(cursor).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        parseQuerySnapshot(task);
                    }
                });
    }

    private void parseQuerySnapshot(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                ArrayList<StickerSummary> stickers = new ArrayList<>();
                ArrayList<OrganizerSummary> sponsors = new ArrayList<>();
                ArrayList<OrganizerSummary> organizers = new ArrayList<>();

                Hackathon hackathon = new Hackathon(documentSnapshot.getId(), documentSnapshot.getString("name"),
                        documentSnapshot.getString("dateString"), documentSnapshot.getDate("date"),
                        documentSnapshot.getString("location"), documentSnapshot.getString("url"),
                        documentSnapshot.getString("logoURL"), documentSnapshot.getString("splashURL"),
                        stickers, sponsors, organizers);

                hackathonList.add(hackathon);
                cursor = documentSnapshot;
                hackathonsAdapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "Error getting documents: ", task.getException());
        }

        if(task.isComplete()) {
            if(hackathonsAdapter.getItemCount() == 0) {
                noHackathonsTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


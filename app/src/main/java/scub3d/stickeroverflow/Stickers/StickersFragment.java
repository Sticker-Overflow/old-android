package scub3d.stickeroverflow.Stickers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
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
import scub3d.stickeroverflow.Organizers.OrganizerSummary;
import scub3d.stickeroverflow.R;

/**
 * Created by scub3d on 2/10/18.
 */

public class StickersFragment extends BaseFragment {
    public String TAG = "StickersFragment";

    private RecyclerView recyclerView;
    private StickersAdapter stickersAdapter;
    private ArrayList<Sticker> stickerList;
    private DocumentSnapshot cursor;

    private StickersFragment.OnFragmentInteractionListener mListener;

    public StickersFragment() { }

    public static StickersFragment newInstance() {
        return new StickersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.stickers_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
        getActivity().setTitle("Stickers");
        recyclerView = rootView.findViewById(R.id.stickerRecyclerView);
        stickerList = new ArrayList<>();

        noStickersTextView = rootView.findViewById(R.id.noStickers);
        noStickersTextView.setVisibility(View.GONE);

        if(getArguments() != null) {
            hasArguments = true;
            if(getArguments().getBoolean("isAdding")) {
                isAdding = true;
                collectionName = getArguments().getString("collection");
                documentId = getArguments().getString("document");
                subCollectionName = "stickers";
                currentStickers = getArguments().getParcelableArrayList("stickers");
                stickersAdapter = new StickersAdapter(getContext(), this, stickerList, null, true);
            } else {
                Log.d(TAG, "onCreateView: This shouldn't happen");
            }
        } else {
            stickersAdapter = new StickersAdapter(getContext(), this, stickerList, null, false);
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickersAdapter);
        stickersAdapter.notifyDataSetChanged();

        prepareStickers();

        stickersAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                getMoreStickers();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StickersFragment.OnFragmentInteractionListener) {
            mListener = (StickersFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected void queryTextChangeSearch(String s) {
        ArrayList<Sticker> tempList = new ArrayList();
        if(s.equals("") || s.equals(null)) {
            stickersAdapter.updateList(stickersAdapter.stickersList);
        } else {
            for(Sticker sticker : stickersAdapter.stickersList) {
                if(sticker.getName().toLowerCase().contains(s) || sticker.getDescription().toLowerCase().contains(s)) {
                    tempList.add(sticker);
                }
            }
            stickersAdapter.updateList(tempList);
        }
    }

    protected void queryTextSubmitSearch(String s) {

    }

    protected void confirmListSelection() {
        ArrayList<String> dataToSend = new ArrayList<>();
        for(StickerSummary ss : selectedStickers) {
            dataToSend.add(new Gson().toJson(ss));
        }
        addToSubcollection(collectionName, documentId, subCollectionName, dataToSend, "sticker");
    }

    protected void cancelListSelection() {
        selectedStickers = new ArrayList<>();
        stickersAdapter.deselectElements(recyclerView);
    }

    private void prepareStickers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("stickers").orderBy("dateAdded", Query.Direction.DESCENDING)
                .orderBy("name", Query.Direction.ASCENDING).limit(15).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        parseQuerySnapshot(task);
                    }
                });
    }

    private void getMoreStickers() {
        db.collection("stickers").orderBy("name", Query.Direction.ASCENDING)
                .startAfter(cursor).limit(15).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                parseQuerySnapshot(task);
            }
        });
    }

    private void parseQuerySnapshot(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                if(hasArguments && isAdding) {
                    if(!currentStickers.contains(documentSnapshot.getId())) {
                        parseDocumentSnapshot(documentSnapshot);
                    }
                } else {
                    parseDocumentSnapshot(documentSnapshot);
                }
            }
        } else {
            Log.d(TAG, "Error getting documents: ", task.getException());
        }

        if(task.isComplete()) {
            if(stickersAdapter.getItemCount() == 0) {
                noStickersTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void parseDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        ArrayList<OrganizerSummary> organizations = new ArrayList<>();
        ArrayList<HackathonSummary> hackathons = new ArrayList<>();

        Sticker sticker = new Sticker(documentSnapshot.getId(), documentSnapshot.getString("name"),
                documentSnapshot.getString("description"), documentSnapshot.getString("purchaseUrl"),
                documentSnapshot.getDouble("numberOfUsersWhoHaveThisSticker").intValue(), hackathons, organizations);

        stickerList.add(sticker);
        cursor = documentSnapshot;
        stickersAdapter.notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


package scub3d.stickeroverflow.Organizers;

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

public class OrganizersFragment extends BaseFragment {
    public String TAG = "OrganizersFragment";

    private RecyclerView recyclerView;
    private OrganizersAdapter organizersAdapter;
    private ArrayList<Organizer> organizerList;
    private OrganizersFragment.OnFragmentInteractionListener mListener;

    public OrganizersFragment() {}

    public static OrganizersFragment newInstance() {
        return new OrganizersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.organizers_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setTitle("Organizers");
        setHasOptionsMenu(true);

        recyclerView = rootView.findViewById(R.id.organizerRecyclerView);
        organizerList = new ArrayList<>();

        noOrganizersTextView = rootView.findViewById(R.id.noOrganizers);
        noOrganizersTextView.setVisibility(View.GONE);

        if(getArguments() != null) {
            hasArguments = true;
            if(getArguments().getBoolean("isAdding")) {
                isAdding = true;
                collectionName = getArguments().getString("collection");
                subCollectionName = "organizers";
                documentId = getArguments().getString("document");
                currentOrganizerSummaries = getArguments().getParcelableArrayList("organizers");
                organizersAdapter = new OrganizersAdapter(getContext(), this, organizerList, null, true);
            } else {
                Log.d(TAG, "onCreateView: This shouldn't happen");
            }
        } else {
            organizersAdapter = new OrganizersAdapter(getContext(), this, organizerList, null, false);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(organizersAdapter);

        organizersAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                getMoreOrganizers();
            }
        });

        prepareOrganizers();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof scub3d.stickeroverflow.Organizers.OrganizersFragment.OnFragmentInteractionListener) {
            mListener = (scub3d.stickeroverflow.Organizers.OrganizersFragment.OnFragmentInteractionListener) context;
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
        ArrayList<Organizer> tempList = new ArrayList();
        if(s.equals("") || s.equals(null)) {
            organizersAdapter.updateList(organizersAdapter.organizerList);
        } else {
            for(Organizer o : organizersAdapter.organizerList) {
                if(o.getName().toLowerCase().contains(s) || o.getLocation().toLowerCase().contains(s) || o.getDescription().toLowerCase().contains(s)) {
                    tempList.add(o);
                }
            }
            organizersAdapter.updateList(tempList);
        }
    }

    protected void queryTextSubmitSearch(String s) {

    }

    protected void confirmListSelection() {
        ArrayList<String> dataToSend = new ArrayList<>();
        for(OrganizerSummary os : selectedOrganizers) {
            dataToSend.add(new Gson().toJson(os));
        }
        addToSubcollection(collectionName, documentId, subCollectionName, dataToSend, "organizer");
    }

    protected void cancelListSelection() {
        selectedOrganizers = new ArrayList<>();
        organizersAdapter.deselectElements(recyclerView);
        organizersAdapter.notifyDataSetChanged();
    }

    private DocumentSnapshot cursor;
    private void prepareOrganizers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizers").orderBy("name", Query.Direction.ASCENDING).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       parseQuerySnapshot(task);
                    }
                });
    }

    private void getMoreOrganizers() {
        db.collection("organizers").orderBy("name", Query.Direction.ASCENDING)
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

                ArrayList<StickerSummary> stickers = new ArrayList<>();
                ArrayList<HackathonSummary> hackathons = new ArrayList<>();
                ArrayList<User> members = new ArrayList<>();
                ArrayList<User> admins = new ArrayList<>();

                Organizer organizer = new Organizer(documentSnapshot.getId(),
                        documentSnapshot.getString("name"), documentSnapshot.getString("description"),
                        documentSnapshot.getString("url"), documentSnapshot.getString("location"),
                        documentSnapshot.getString("logoURL"), stickers, hackathons, members, admins);

                organizerList.add(organizer);
                cursor = documentSnapshot;
                organizersAdapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "Error getting documents: ", task.getException());
        }

        if(task.isComplete()) {
            if(organizersAdapter.getItemCount() == 0) {
                noOrganizersTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}


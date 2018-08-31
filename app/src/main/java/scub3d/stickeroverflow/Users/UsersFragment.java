package scub3d.stickeroverflow.Users;

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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.MainActivity;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;
import scub3d.stickeroverflow.Stickers.StickerSummary;

/**
 * Created by scub3d on 3/1/18.
 */

public class UsersFragment extends BaseFragment {
    public String TAG = "UsersFragment";

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private ArrayList<User> userList;
    private UsersFragment.OnFragmentInteractionListener mListener;
    private TextView noUsersTextView;

    public UsersFragment() {}

    public static UsersFragment newInstance() {
        return new UsersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.users_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setTitle("Users");
        setHasOptionsMenu(true);

        recyclerView = rootView.findViewById(R.id.userRecyclerView);
        userList = new ArrayList<>();

        noUsersTextView = rootView.findViewById(R.id.noUsers);
        noUsersTextView.setVisibility(View.GONE);

        usersAdapter = new UsersAdapter(getContext(), this, userList);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(usersAdapter);

        usersAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                getMoreUsers();
            }
        });

        prepareUsers();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof scub3d.stickeroverflow.Users.UsersFragment.OnFragmentInteractionListener) {
            mListener = (scub3d.stickeroverflow.Users.UsersFragment.OnFragmentInteractionListener) context;
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
        ArrayList<User> tempList = new ArrayList();
        if(s.equals("") || s.equals(null)) {
            usersAdapter.updateList(usersAdapter.usersList);
        } else {
            for(User u : usersAdapter.usersList) {
                if(u.getName().toLowerCase().contains(s)) {
                    tempList.add(u);
                }
            }
            usersAdapter.updateList(tempList);
        }
    }

    protected void queryTextSubmitSearch(String s) {

    }

    private DocumentSnapshot cursor;
    private void prepareUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").orderBy("name", Query.Direction.ASCENDING).limit(20).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        parseQuerySnapshot(task);
                    }
                });
    }

    private void getMoreUsers() {
        db.collection("users").orderBy("name", Query.Direction.ASCENDING)
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
                ArrayList<HackathonSummary> upcomingHackathons = new ArrayList<>();
                ArrayList<HackathonSummary> attendedHackathons = new ArrayList<>();

                User user = new User(documentSnapshot.getId(), documentSnapshot.getBoolean("isAdmin"),
                        documentSnapshot.getBoolean("isSuperUser"), documentSnapshot.getString("name"),
                        documentSnapshot.getString("photoUrl"), documentSnapshot.getString("memberOrganizationId"),
                        upcomingHackathons, attendedHackathons, stickerSummaries);

                if(!user.getUid().equals(((MainActivity)getActivity()).getFirebaseId())) {
                    userList.add(user);
                    usersAdapter.notifyDataSetChanged();
                }

                cursor = documentSnapshot;
            }
        } else {
            Log.d(TAG, "Error getting documents: ", task.getException());
        }

        if(task.isComplete()) {
            if(usersAdapter.getItemCount() == 0) {
                noUsersTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
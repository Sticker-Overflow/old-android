package scub3d.stickeroverflow;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import scub3d.stickeroverflow.Conversations.Conversation;
import scub3d.stickeroverflow.Conversations.ConversationSummary;
import scub3d.stickeroverflow.Conversations.Message;
import scub3d.stickeroverflow.Conversations.SpecificConversationFragment;
import scub3d.stickeroverflow.Hackathons.SpecificHackathonFragment;
import scub3d.stickeroverflow.Hackathons.Hackathon;
import scub3d.stickeroverflow.Hackathons.HackathonSummary;
import scub3d.stickeroverflow.Hackathons.HackathonsFragment;
import scub3d.stickeroverflow.Organizers.OrganizersFragment;
import scub3d.stickeroverflow.Organizers.SpecificOrganizerFragment;
import scub3d.stickeroverflow.Organizers.Organizer;
import scub3d.stickeroverflow.Organizers.OrganizerSummary;
import scub3d.stickeroverflow.Sponsors.SpecificSponsorFragment;
import scub3d.stickeroverflow.Sponsors.Sponsor;
import scub3d.stickeroverflow.Sponsors.SponsorSummary;
import scub3d.stickeroverflow.Sponsors.SponsorsFragment;
import scub3d.stickeroverflow.Stickers.SpecificStickerFragment;
import scub3d.stickeroverflow.Stickers.Sticker;
import scub3d.stickeroverflow.Stickers.StickerSummary;
import scub3d.stickeroverflow.Stickers.StickersFragment;
import scub3d.stickeroverflow.Users.SpecificUserFragment;
import scub3d.stickeroverflow.Users.User;

import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 2/9/18.
 */

public class BaseFragment extends Fragment {
    protected View rootView;
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final Map<String, Class> dataStructureMap;
    static {
        dataStructureMap = new HashMap<>();
        dataStructureMap.put("hackathon", HackathonSummary.class);
        dataStructureMap.put("organizer", OrganizerSummary.class);
        dataStructureMap.put("sponsor", SponsorSummary.class);
        dataStructureMap.put("sticker", StickerSummary.class);
    }

    public String collectionName = null;
    public String documentId = null;
    public String subCollectionName = null;
    public ArrayList<StickerSummary> currentStickers = null;
    public ArrayList<HackathonSummary> currentHackathonSummaries = null;
    public ArrayList<OrganizerSummary> currentSponsorSummaries = null;
    public ArrayList<OrganizerSummary> currentOrganizerSummaries = null;

    public boolean showCheckBoxes = false;

    public TextView noStickersTextView, noHackathonsTextView, noOrganizersTextView, noSponsorsTextView, noConversationsTextView;

    public ArrayList<StickerSummary> selectedStickers = new ArrayList<>();
    public ArrayList<HackathonSummary> selectedHackathons = new ArrayList<>();
    public ArrayList<OrganizerSummary> selectedOrganizers = new ArrayList<>();
    public ArrayList<SponsorSummary> selectedSponsors = new ArrayList<>();

    protected boolean isAdding = false;
    protected boolean hasArguments = false;

    protected void setUpStickerAdding(String collection, String document, ArrayList<StickerSummary> currentStickers) {
        Bundle bundle = new Bundle();
        StickersFragment stickersFragment = new StickersFragment();
        bundle.putBoolean("isAdding", true);
        bundle.putString("collection", collection);
        bundle.putString("document", document);
        bundle.putParcelableArrayList("stickers", currentStickers);
        stickersFragment.setArguments(bundle);
        handleFragmentTransition(rootView, stickersFragment, "Tag");
    }

    protected void setUpOrganizerAdding(String collection, String document, ArrayList<OrganizerSummary> currentOrganizers) {
        Bundle bundle = new Bundle();
        OrganizersFragment organizersFragment = new OrganizersFragment();
        bundle.putBoolean("isAdding", true);
        bundle.putString("collection", collection);
        bundle.putString("document", document);
        bundle.putParcelableArrayList("organizers", currentOrganizers);
        organizersFragment.setArguments(bundle);
        handleFragmentTransition(rootView, organizersFragment, "Tag");
    }

    protected void setUpSponsorAdding(String collection, String document, ArrayList<SponsorSummary> currentSponsors) {
        Bundle bundle = new Bundle();
        SponsorsFragment sponsorsFragment = new SponsorsFragment();
        bundle.putBoolean("isAdding", true);
        bundle.putString("collection", collection);
        bundle.putString("document", document);
        bundle.putParcelableArrayList("sponsors", currentSponsors);
        sponsorsFragment.setArguments(bundle);
        handleFragmentTransition(rootView, sponsorsFragment, "Tag");
    }

    protected void setUpHackathonAdding(String collection, String document, ArrayList<HackathonSummary> currentHackathons) {
        Bundle bundle = new Bundle();
        HackathonsFragment hackathonsFragment = new HackathonsFragment();
        bundle.putBoolean("isAdding", true);
        bundle.putString("collection", collection);
        bundle.putString("document", document);
        bundle.putParcelableArrayList("hackathons", currentHackathons);
        hackathonsFragment.setArguments(bundle);
        handleFragmentTransition(rootView, hackathonsFragment, "Tag");
    }

    protected void handleFragmentTransition(View rootView, Fragment newFragment, String stackTag) {
        FrameLayout fr = (FrameLayout) rootView.getParent();
        getFragmentManager().beginTransaction().replace(fr.getId(), newFragment, stackTag).addToBackStack(null).commit();
    }

    public void selectOrganizer(Organizer organizer, String organizerDataStructure) {
        bundleOrganizerData(new Gson().toJson(organizer), organizerDataStructure);
    }

    public void selectOrganizer(OrganizerSummary organizerSummary, String organizerDataStructure) {
       bundleOrganizerData(new Gson().toJson(organizerSummary), organizerDataStructure);
    }

    private void bundleOrganizerData(String organizerData, String organizerDataStructure) {
        Bundle bundle = new Bundle();
        SpecificOrganizerFragment specificOrganizationFragment = new SpecificOrganizerFragment();
        bundle.putString("organizerData", organizerData);
        bundle.putString("organizerDataStructure", organizerDataStructure);
        specificOrganizationFragment.setArguments(bundle);
        handleFragmentTransition(rootView, specificOrganizationFragment, "Tag");
    }

    public void selectSponsor(Sponsor sponsor, String sponsorDataStructure) {
        bundleSponsorData(new Gson().toJson(sponsor), sponsorDataStructure);
    }

    public void selectSponsor(SponsorSummary sponsorSummary, String sponsorDataStructure) {
        bundleSponsorData(new Gson().toJson(sponsorSummary), sponsorDataStructure);
    }

    private void bundleSponsorData(String sponsorData, String sponsorDataStructure) {
        Bundle bundle = new Bundle();
        SpecificSponsorFragment specificSponsorFragment = new SpecificSponsorFragment();
        bundle.putString("sponsorData", sponsorData);
        bundle.putString("sponsorDataStructure", sponsorDataStructure);
        specificSponsorFragment.setArguments(bundle);
        handleFragmentTransition(rootView, specificSponsorFragment, "Tag");
    }

    public void selectHackathon(Hackathon hackathon, String hackathonDataStructure) {
        bundleHackathonData(new Gson().toJson(hackathon), hackathonDataStructure);
    }

    public void selectHackathon(HackathonSummary hackathonSummary, String hackathonDataStructure) {
       bundleHackathonData(new Gson().toJson(hackathonSummary), hackathonDataStructure);
    }

    private void bundleHackathonData(String hackathonData, String hackathonDataStructure) {
        Bundle bundle = new Bundle();
        SpecificHackathonFragment specificHackathonFragment = new SpecificHackathonFragment();
        bundle.putString("hackathonData", hackathonData);
        bundle.putString("hackathonDataStructure", hackathonDataStructure);
        specificHackathonFragment.setArguments(bundle);
        handleFragmentTransition(rootView, specificHackathonFragment, "Tag");
    }

    public void selectSticker(StickerSummary sticker, String stickerDataStructure) {
        bundleStickerData(new Gson().toJson(sticker), stickerDataStructure);
    }

    public void selectSticker(Sticker stickerSelected, String stickerDataStructure) {
        bundleStickerData(new Gson().toJson(stickerSelected), stickerDataStructure);
    }

    private void bundleStickerData(String stickerData, String stickerDataStructure) {
        Bundle bundle = new Bundle();
        SpecificStickerFragment specificStickerFragment = new SpecificStickerFragment();
        bundle.putString("stickerData", stickerData);
        bundle.putString("stickerDataStructure", stickerDataStructure);
        specificStickerFragment.setArguments(bundle);
        handleFragmentTransition(rootView, specificStickerFragment, "Tag");
    }

    public void selectUser(User userSelected) {
        bundleUserData(new Gson().toJson(userSelected));
    }

    private void bundleUserData(String userData) {
        Bundle bundle = new Bundle();
        SpecificUserFragment specificUserFragment = new SpecificUserFragment();
        bundle.putString("userData", userData);
        specificUserFragment.setArguments(bundle);
        handleFragmentTransition(rootView, specificUserFragment, "Tag");
    }

    public void selectConversation(ConversationSummary selectedConversation) {
        bundleConversationData(new Gson().toJson(selectedConversation), false);
    }

    private void bundleConversationData(String conversationData, boolean isNewConversation) {
        Bundle bundle = new Bundle();
        SpecificConversationFragment specificConversationFragment = new SpecificConversationFragment();
        bundle.putString("conversationData", conversationData);
        bundle.putBoolean("newConversation", isNewConversation);
        specificConversationFragment.setArguments(bundle);
        Conversation conversation = new Gson().fromJson(conversationData, Conversation.class);
        handleFragmentTransition(rootView, specificConversationFragment, "Conversation:" + conversation.getId());
    }

    public void createNewConversation(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Conversation conversation = new Conversation(UUID.randomUUID().toString(), documentSnapshot.toObject(User.class), "", 0, System.currentTimeMillis(), new ArrayList<Message>());
                    bundleConversationData(new Gson().toJson(conversation), true);
                }
            }
        });
    }

    public int counter = 0;
    public int counterLimit = 0;
    public void setCounter(int limit) {
        counter = 0;
        counterLimit = limit;
    }

    public void incrementCounter() {
        counter++;
        if(counter >= counterLimit) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    public void addToSubcollection(String collection, String document, String subCollection, ArrayList<String> dataArray, final String dataType) {
        setCounter(dataArray.size());
        for(String data : dataArray) {
            Item i = (Item)new Gson().fromJson(data, dataStructureMap.get(dataType));
            db.collection(collection).document(document).collection(subCollection).document(i.getId())
                    .set(new Gson().fromJson(data, dataStructureMap.get(dataType)))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            incrementCounter();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document: ", e);
                            incrementCounter();
                        }
                    });
        }
    }

    public MenuItem cancel, confirm, search;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.top_bar, menu);
        cancel = menu.findItem(R.id.action_cancel);
        confirm = menu.findItem(R.id.action_confirm);
        search = menu.findItem(R.id.action_search);

        if(!isAdding) {
            cancel.setVisible(false);
            confirm.setVisible(false);
            try {
                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        queryTextSubmitSearch(s);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        queryTextChangeSearch(s);
                        return false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            search.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_confirm) {
            confirmListSelection();
            return true;
        } else if (id == R.id.action_cancel) {
            cancelListSelection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void queryTextSubmitSearch(String s) {}
    protected void queryTextChangeSearch(String s) {}
    protected void confirmListSelection() {}
    protected void cancelListSelection() {}

}

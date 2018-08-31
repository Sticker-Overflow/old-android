package scub3d.stickeroverflow.Organizers;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;

import static io.fabric.sdk.android.Fabric.TAG;

/**
 * Created by scub3d on 2/7/18.
 */

public class OrganizersAdapter extends RecyclerView.Adapter<OrganizersAdapter.ViewHolder> {
    private Context mContext;
    public ArrayList<Organizer> organizerList, displayedList;
    private ArrayList<OrganizerSummary> organizerSummaryList;
    private BaseFragment baseFragment;
    private boolean isSummary, isForAdding;
    private OnBottomReachedListener onBottomReachedListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mOrganizerName, mOrganizerLocation;
        public ImageView mOrganizerLogo, checkBox;
        private CardView cardview;
        public boolean selected;

        public ViewHolder(View view) {
            super(view);
            cardview = itemView.findViewById(R.id.organizerCard);
            mOrganizerName = view.findViewById(R.id.organizerName);
            mOrganizerLogo = view.findViewById(R.id.organizerLogo);
            mOrganizerLocation = view.findViewById(R.id.organizerLocation);
            checkBox = view.findViewById(R.id.checkBox);
            selected = false;
        }
    }

    public OrganizersAdapter(Context mContext, BaseFragment baseFragment, ArrayList<Organizer> organizerList, String ignoreThis, boolean isForAdding) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.organizerList = organizerList;
        this.displayedList = organizerList;
        this.isSummary = false;
        this.isForAdding = isForAdding;
    }

    public OrganizersAdapter(Context mContext, BaseFragment baseFragment, ArrayList<OrganizerSummary> organizerSummaryList) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.organizerSummaryList = organizerSummaryList;
        this.isSummary = true;
        this.isForAdding = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final OrganizerSummary organizer;
        if(isSummary) {
            organizer = organizerSummaryList.get(position);
        } else {
            organizer = displayedList.get(position);
        }
        Log.d(TAG, "onBindViewHolder: " + organizer.getLocation());
        holder.mOrganizerName.setText(organizer.getName());
        holder.mOrganizerLocation.setText(organizer.getLocation());
        Glide.with(mContext).load(organizer.getLogoURL()).into(holder.mOrganizerLogo);

        if(!isForAdding) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isForAdding) {
                    if(isSummary) {
                        baseFragment.selectOrganizer(organizerSummaryList.get(position), "organizerSummary");
                    } else {
                        baseFragment.selectOrganizer(displayedList.get(position), "organizer");
                    }
                } else {
                    if(baseFragment.currentOrganizerSummaries != null) {
                        // This doesn't work, try id :
                        if (!baseFragment.currentOrganizerSummaries.contains(organizer)) {
                            Log.d(TAG, "onClick: " + holder.selected);
                            if(holder.selected) {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
                                baseFragment.selectedOrganizers.remove(organizer);
                            } else {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_black_24dp);
                                baseFragment.selectedOrganizers.add(organizer);
                            }
                            holder.selected = !holder.selected;
                            if(getItemCount() == 0) {
                                baseFragment.noOrganizersTextView.setVisibility(View.VISIBLE);
                            } else {
                                baseFragment.noOrganizersTextView.setVisibility(View.GONE);
                            }
                        } else {
                            displayedList.remove(organizer);
                        }
                    }
                }
            }
        });

        if(!isSummary) {
            if (position == displayedList.size() - 1){
                onBottomReachedListener.onBottomReached(position);
            }
        }

    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){
        this.onBottomReachedListener = onBottomReachedListener;
    }

    @Override
    public int getItemCount() {
        if(isSummary) {
            return organizerSummaryList.size();
        } else {
            return displayedList.size();
        }
    }

    public void removeAt(int position) {
        displayedList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, displayedList.size());
    }

    public void updateList(ArrayList<Organizer> list) {
        displayedList = list;
        notifyDataSetChanged();
    }

    public void deselectElements(RecyclerView recyclerView) {
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            final ViewHolder holder = (OrganizersAdapter.ViewHolder)recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            holder.selected = false;
            holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
        }
    }
}


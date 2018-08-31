package scub3d.stickeroverflow.Sponsors;

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

public class SponsorsAdapter extends RecyclerView.Adapter<SponsorsAdapter.ViewHolder> {
    private Context mContext;
    public ArrayList<Sponsor> sponsorList, displayedList;
    private ArrayList<SponsorSummary> sponsorSummaryList;
    private BaseFragment baseFragment;
    private boolean isSummary, isForAdding;
    private OnBottomReachedListener onBottomReachedListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mSponsorName, mSponsorLocation;
        public ImageView mSponsorLogo, checkBox;
        private CardView cardview;
        public boolean selected;

        public ViewHolder(View view) {
            super(view);
            cardview = itemView.findViewById(R.id.sponsorCard);
            mSponsorName = view.findViewById(R.id.sponsorName);
            mSponsorLogo = view.findViewById(R.id.sponsorLogo);
            mSponsorLocation = view.findViewById(R.id.sponsorLocation);
            checkBox = view.findViewById(R.id.checkBox);
            selected = false;
        }
    }

    public SponsorsAdapter(Context mContext, BaseFragment baseFragment, ArrayList<Sponsor> sponsorList, String ignoreThis, boolean isForAdding) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.sponsorList = sponsorList;
        this.displayedList = sponsorList;
        this.isSummary = false;
        this.isForAdding = isForAdding;
    }

    public SponsorsAdapter(Context mContext, BaseFragment baseFragment, ArrayList<SponsorSummary> sponsorSummaryList) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.sponsorSummaryList = sponsorSummaryList;
        this.isSummary = true;
        this.isForAdding = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sponsor_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final SponsorSummary sponsor;
        if(isSummary) {
            sponsor = sponsorSummaryList.get(position);
        } else {
            sponsor = displayedList.get(position);
        }
        Log.d(TAG, "onBindViewHolder: " + sponsor.getLocation());
        holder.mSponsorName.setText(sponsor.getName());
        holder.mSponsorLocation.setText(sponsor.getLocation());
        Glide.with(mContext).load(sponsor.getLogoURL()).into(holder.mSponsorLogo);

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
                        baseFragment.selectSponsor(sponsorSummaryList.get(position), "sponsorSummary");
                    } else {
                        baseFragment.selectSponsor(displayedList.get(position), "sponsor");
                    }
                } else {
                    if(baseFragment.currentSponsorSummaries != null) {
                        // This doesn't work, try id :
                        if (!baseFragment.currentSponsorSummaries.contains(sponsor)) {
                            Log.d(TAG, "onClick: " + holder.selected);
                            if(holder.selected) {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
                                baseFragment.selectedSponsors.remove(sponsor);
                            } else {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_black_24dp);
                                baseFragment.selectedSponsors.add(sponsor);
                            }
                            holder.selected = !holder.selected;
                            if(getItemCount() == 0) {
                                baseFragment.noSponsorsTextView.setVisibility(View.VISIBLE);
                            } else {
                                baseFragment.noSponsorsTextView.setVisibility(View.GONE);
                            }
                        } else {
                            displayedList.remove(sponsor);
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
            return sponsorSummaryList.size();
        } else {
            return displayedList.size();
        }
    }

    public void removeAt(int position) {
        displayedList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, displayedList.size());
    }

    public void updateList(ArrayList<Sponsor> list) {
        displayedList = list;
        notifyDataSetChanged();
    }

    public void deselectElements(RecyclerView recyclerView) {
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            final ViewHolder holder = (SponsorsAdapter.ViewHolder)recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            holder.selected = false;
            holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
        }
    }
}


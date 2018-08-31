package scub3d.stickeroverflow.Hackathons;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;

/**
 * Created by scub3d on 2/6/18.
 */

public class HackathonsAdapter extends RecyclerView.Adapter<HackathonsAdapter.ViewHolder> {
    private Context mContext;
    private BaseFragment baseFragment;
    public List<Hackathon> hackathonList, displayedList;
    private List<HackathonSummary> hackathonSummaryList;
    private boolean isSummary, isForAdding;
    private OnBottomReachedListener onBottomReachedListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mHackathonName, mHackathonLocation, mHackathonDateString;
        public ImageView mHackathonLogo, checkBox;
        private CardView cardview;
        private boolean selected = false;
        public ViewHolder(View view) {
            super(view);
            cardview = itemView.findViewById(R.id.hackathonCard);
            mHackathonName = view.findViewById(R.id.hackathonName);
            mHackathonLocation = view.findViewById(R.id.hackathonLocation);
            mHackathonDateString = view.findViewById(R.id.hackathonDateString);
            mHackathonLogo = view.findViewById(R.id.hackathonLogo);
            checkBox = view.findViewById(R.id.checkBox);
        }
    }

    public HackathonsAdapter(Context mContext, BaseFragment baseFragment, ArrayList<Hackathon> hackathonList, String ignoreThis, boolean isForAdding) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.hackathonList = hackathonList;
        this.displayedList = hackathonList;
        this.isSummary = false;
        this.isForAdding = isForAdding;
    }

    public HackathonsAdapter(Context mContext, BaseFragment baseFragment, ArrayList<HackathonSummary> hackathonSummaryList) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.hackathonSummaryList = hackathonSummaryList;
        this.isSummary = true;
        this.isForAdding = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.hackathon_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final HackathonSummary hackathon;
        if(isSummary) {
            hackathon = hackathonSummaryList.get(position);
        } else {
            hackathon = displayedList.get(position);
        }

        holder.mHackathonName.setText(hackathon.getName());
        holder.mHackathonLocation.setText(hackathon.getLocation());
        holder.mHackathonDateString.setText(hackathon.getDateString());
        Glide.with(mContext).load(hackathon.getLogoURL()).into(holder.mHackathonLogo);

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
                        baseFragment.selectHackathon(hackathonSummaryList.get(position), "hackathonSummary");
                    } else {
                        baseFragment.selectHackathon(displayedList.get(position), "hackathon");
                    }
                } else {
                    if(baseFragment.currentHackathonSummaries != null) {
                        // This doesn't work. Try searching by id for :
                        if (!baseFragment.currentHackathonSummaries.contains(hackathon)) {
                            if(holder.selected) {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
                                baseFragment.selectedHackathons.remove(hackathon);
                            } else {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_black_24dp);
                                baseFragment.selectedHackathons.add(hackathon);
                            }
                            holder.selected = !holder.selected;

                            if(getItemCount() == 0) {
                                baseFragment.noHackathonsTextView.setVisibility(View.VISIBLE);
                            } else {
                                baseFragment.noHackathonsTextView.setVisibility(View.GONE);
                            }
                        } else {
                            displayedList.remove(hackathon);
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
            return hackathonSummaryList.size();
        } else {
            return displayedList.size();
        }
    }

    public void removeAt(int position) {
        displayedList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, displayedList.size());
    }

    public void updateList(List<Hackathon> list) {
        displayedList = list;
        notifyDataSetChanged();
    }

    public void deselectElements(RecyclerView recyclerView) {
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            final ViewHolder holder = (HackathonsAdapter.ViewHolder)recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            holder.selected = false;
            holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
        }
    }

}


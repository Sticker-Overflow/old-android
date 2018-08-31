package scub3d.stickeroverflow.Stickers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;
import static android.content.ContentValues.TAG;

/**
 * Created by scub3d on 2/9/18.
 */

public class StickersAdapter extends RecyclerView.Adapter<scub3d.stickeroverflow.Stickers.StickersAdapter.ViewHolder> {

    private Context mContext;
    private BaseFragment baseFragment;
    public ArrayList<Sticker> stickersList, displayedList;
    public ArrayList<StickerSummary> stickerSummaryList;
    private boolean isSummary, isForAdding, isResult;
    private OnBottomReachedListener onBottomReachedListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mStickerImage, checkBox;
        private CardView cardview;
        private TextView percentMatch;
        private boolean selected = false;

        public ViewHolder(View view) {
            super(view);
            cardview = itemView.findViewById(R.id.stickerCard);
            mStickerImage = view.findViewById(R.id.stickerCardImage);
            checkBox = view.findViewById(R.id.checkBox);
            percentMatch = view.findViewById(R.id.percentMatch);
        }
    }

    public StickersAdapter(Context mContext, BaseFragment baseFragment, ArrayList<Sticker> stickersList, String ignoreThis, boolean isForAdding) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.stickersList = stickersList;
        this.displayedList = stickersList;
        this.isSummary = false;
        this.isForAdding = isForAdding;
    }

    public StickersAdapter(Context mContext, BaseFragment baseFragment, ArrayList<StickerSummary> stickersList) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.stickerSummaryList = stickersList;
        this.isSummary = true;
        this.isForAdding = false;
        this.isResult = false;
    }

    public StickersAdapter(Context mContext, BaseFragment baseFragment, ArrayList<StickerSummary> stickersList, boolean isResult) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.stickerSummaryList = stickersList;
        this.isSummary = true;
        this.isForAdding = false;
        this.isResult = isResult;
    }

    @Override
    public scub3d.stickeroverflow.Stickers.StickersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if(!isResult) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_card, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_result_card, parent, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final scub3d.stickeroverflow.Stickers.StickersAdapter.ViewHolder holder, final int position) {
        StickerSummary sticker;
        if(isSummary) {
            sticker = stickerSummaryList.get(position);
        } else {
            sticker = displayedList.get(position);
        }

        finishSetup(holder, position, sticker);
    }

    private void finishSetup(final ViewHolder holder, final int position, final StickerSummary sticker) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("stickers/" + sticker.getId() + "/" + sticker.getId() + ".png");
        // download bytes. Save them to a global dict as teh value, id is key
        // then load via bytes from that dict

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(mContext).load(imageURL).into(holder.mStickerImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: " + exception);
            }
        });

        if(isResult) {
            holder.percentMatch.setText(String.format("%.2f", sticker.getPercentMatch() * 100) + "% match");
        } else {
            if(!isForAdding) {
                holder.checkBox.setVisibility(View.GONE);
            } else {
                holder.checkBox.setVisibility(View.VISIBLE);
            }
        }

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isForAdding) {
                    if(isSummary) {
                        baseFragment.selectSticker(stickerSummaryList.get(position), "stickerSummary");
                    } else {
                        baseFragment.selectSticker(displayedList.get(position), "sticker");
                    }
                } else {
                    if(baseFragment.currentStickers != null) {
                        if (!baseFragment.currentStickers.contains(sticker)) {
                            if(holder.selected) {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
                                baseFragment.selectedStickers.remove(sticker);
                            } else {
                                holder.checkBox.setImageResource(R.mipmap.ic_check_box_black_24dp);
                                baseFragment.selectedStickers.add(sticker);
                            }
                            holder.selected = !holder.selected;

                            if(getItemCount() == 0) {
                                baseFragment.noStickersTextView.setVisibility(View.VISIBLE);
                            } else {
                                baseFragment.noStickersTextView.setVisibility(View.GONE);
                            }
                        } else {
                            displayedList.remove(sticker);
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
            return stickerSummaryList.size();
        } else {
            return displayedList.size();
        }
    }

    public void removeAt(int position) {
        displayedList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, displayedList.size());
    }

    public void updateList(ArrayList<Sticker> list) {
        displayedList = list;
        notifyDataSetChanged();
    }

    public void deselectElements(RecyclerView recyclerView) {
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            final ViewHolder holder = (StickersAdapter.ViewHolder)recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            holder.selected = false;
            holder.checkBox.setImageResource(R.mipmap.ic_check_box_outline_blank_black_24dp);
        }
    }

}

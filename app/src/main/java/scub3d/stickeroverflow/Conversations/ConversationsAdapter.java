package scub3d.stickeroverflow.Conversations;

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

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;

/**
 * Created by scub3d on 3/1/18.
 */

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {

    private Context mContext;
    private BaseFragment baseFragment;
    public ArrayList<ConversationSummary> conversationsList, displayedList;
    private OnBottomReachedListener onBottomReachedListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mUserPhoto;
        private CardView cardview;
        private TextView mUserName, mLatestMessage, mUnreadMessageCounter;

        public ViewHolder(View view) {
            super(view);
            cardview = itemView.findViewById(R.id.conversationCard);
            mUserPhoto = itemView.findViewById(R.id.userPhoto);
            mUserName = itemView.findViewById(R.id.userName);
            mLatestMessage = itemView.findViewById(R.id.latestMessage);
            mUnreadMessageCounter = itemView.findViewById(R.id.unreadMessagesCounter);
        }
    }

    public ConversationsAdapter(Context mContext, BaseFragment baseFragment, ArrayList<ConversationSummary> conversationsList) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.conversationsList = conversationsList;
        this.displayedList = conversationsList;
    }

    @Override
    public ConversationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ConversationsAdapter.ViewHolder holder, final int position) {
        final ConversationSummary conversationSummary = displayedList.get(position);
        Glide.with(mContext).load(conversationSummary.getOtherUser().getPhotoUrl()).into(holder.mUserPhoto);
        holder.mUserName.setText(conversationSummary.getOtherUser().getName());
        holder.mLatestMessage.setText(conversationSummary.getLatestMessage());
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseFragment.selectConversation(displayedList.get(position));
            }
        });

        if(conversationSummary.getNumberOfUnreadMessages() > 0) {
            holder.mUnreadMessageCounter.setVisibility(View.VISIBLE);
            holder.mUnreadMessageCounter.setText(String.valueOf(conversationSummary.getNumberOfUnreadMessages()));
        } else {
            holder.mUnreadMessageCounter.setVisibility(View.GONE);
        }

        if (position == displayedList.size() - 1) {
            onBottomReachedListener.onBottomReached(position);
        }
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    public void updateList(ArrayList<ConversationSummary> list) {
        displayedList = list;
        notifyDataSetChanged();
    }

}

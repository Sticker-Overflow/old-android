package scub3d.stickeroverflow.Users;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

import scub3d.stickeroverflow.BaseFragment;
import scub3d.stickeroverflow.OnBottomReachedListener;
import scub3d.stickeroverflow.R;

/**
 * Created by scub3d on 3/1/18.
 */

public class UsersAdapter extends RecyclerView.Adapter<scub3d.stickeroverflow.Users.UsersAdapter.ViewHolder> {

    private Context mContext;
    private BaseFragment baseFragment;
    public ArrayList<User> usersList, displayedList;
    private OnBottomReachedListener onBottomReachedListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mUserPhoto;
        private CardView cardview;
        private TextView mUserName;

        public ViewHolder(View view) {
            super(view);
            cardview = itemView.findViewById(R.id.userCard);
            mUserPhoto = itemView.findViewById(R.id.userPhoto);
            mUserName = itemView.findViewById(R.id.userName);
        }
    }

    public UsersAdapter(Context mContext, BaseFragment baseFragment, ArrayList<User> usersList) {
        this.mContext = mContext;
        this.baseFragment = baseFragment;
        this.usersList = usersList;
        this.displayedList = usersList;
    }

    @Override
    public scub3d.stickeroverflow.Users.UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final scub3d.stickeroverflow.Users.UsersAdapter.ViewHolder holder, final int position) {
        final User user = displayedList.get(position);
        Glide.with(mContext).load(user.getPhotoUrl()).into(holder.mUserPhoto);
        holder.mUserName.setText(user.getName());
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseFragment.selectUser(displayedList.get(position));
            }
        });

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


    public void updateList(ArrayList<User> list) {
        displayedList = list;
        notifyDataSetChanged();
    }
}
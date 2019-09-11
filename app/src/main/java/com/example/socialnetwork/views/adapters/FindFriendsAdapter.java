package com.example.socialnetwork.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.FindFriends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsAdapter extends FirebaseRecyclerAdapter<FindFriends, FindFriendsAdapter.FindFriendsViewHolder> {
    public interface ItemClickListener {
        void onItemClick(String userKey);
    }
    private Context context;
    private ItemClickListener onItemClickListener;


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FindFriendsAdapter(@NonNull FirebaseRecyclerOptions<FindFriends> options, Context context, ItemClickListener onClickItem) {
        super(options);
        this.context = context;
        this.onItemClickListener  = onClickItem;
    }

    @Override
    protected void onBindViewHolder(@NonNull FindFriendsAdapter.FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {

        holder.setAllUsersFullName(model.getFullname());
        holder.setAllUsersImage(model.getProfileimage());
        holder.setAllUsersStatus(model.getStatus());
        holder.bind(getRef(position).getKey(),onItemClickListener);
    }

    @NonNull
    @Override
    public FindFriendsAdapter.FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.all_users_display_layouts, viewGroup, false);
        return new FindFriendsViewHolder(view);
    }

    public class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setAllUsersFullName(String fullname) {
            TextView username = mView.findViewById(R.id.all_users_profile_full_name);
            username.setText(fullname);
        }

        public void setAllUsersImage(String profileimage) {
            CircleImageView image = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.add_post_high).into(image);
        }

        public void setAllUsersStatus(String status){
            TextView userStatus = mView.findViewById(R.id.all_users_status);
            userStatus.setText(status);
        }

        public void bind(final String userKey, final FindFriendsAdapter.ItemClickListener listner) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listner.onItemClick(userKey);
                }
            });
        }
    }

}

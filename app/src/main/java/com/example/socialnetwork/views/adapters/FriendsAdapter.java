package com.example.socialnetwork.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends FirebaseRecyclerAdapter<Friends, FriendsAdapter.FriendsViewHolder> {
    private Context context;
    private ItemClickListener onItemClickListener;

    public interface ItemClickListener {
        void onItemClick(String usersIDs, String userName);
    }


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FriendsAdapter(@NonNull FirebaseRecyclerOptions<Friends> options, Context context, ItemClickListener onItemClickListener) {
        super(options);
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    protected void onBindViewHolder(@NonNull final FriendsAdapter.FriendsViewHolder holder, int position, @NonNull Friends model) {
        holder.setDate(model.getDate());
        final String usersIDs = getRef(position).getKey();
        holder.usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        final String userName = dataSnapshot.child("fullname").getValue().toString();
                        holder.setAllUsersFullName(userName);
                        holder.bind(usersIDs, userName, onItemClickListener);
                    }

                    if (dataSnapshot.hasChild("profileimage")) {
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        holder.setAllUsersImage(myProfileImage);
                    }

                    if (dataSnapshot.hasChild("userState")){
                        String type = dataSnapshot.child("userState").child("type").getValue().toString();
                        if (type.equals("online")){
                            holder.onlineStatusView.setVisibility(View.VISIBLE);
                        } else {
                            holder.onlineStatusView.setVisibility(View.INVISIBLE);
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @NonNull
    @Override
    public FriendsAdapter.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.all_users_display_layouts, viewGroup, false);
        return new FriendsViewHolder(view);
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView onlineStatusView;

        DatabaseReference usersRef;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        }

        public void setAllUsersFullName(String fullname) {
            TextView username = mView.findViewById(R.id.all_users_profile_full_name);
            username.setText(fullname);
        }

        public void setAllUsersImage(String profileimage) {
            CircleImageView image = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.add_post_high).into(image);
        }

        public void setDate(String date) {
            TextView userStatus = mView.findViewById(R.id.all_users_status);
            String friendshipYears = "Friends since: "+date;
            userStatus.setText(friendshipYears);
        }

        public void bind(final String usersIDs, final String userName, final ItemClickListener onItemClickListener) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(usersIDs, userName);
                }
            });
        }
    }

}

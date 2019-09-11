package com.example.socialnetwork.views.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Posts;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends FirebaseRecyclerAdapter<Posts, PostsAdapter.PostsViewHolder> {
    public interface ItemClickListener {
        void onItemClick(String postKey);
        void onLikeButtonClick(String postKey);
        void onCommentButtonClick(String postKey);
    }

    public interface LikeClickListener {

    }
    private Context context;
    private ItemClickListener onItemClickListener;


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public PostsAdapter(@NonNull FirebaseRecyclerOptions<Posts> options, Context context, ItemClickListener onClickItem) {
        super(options);
        this.context = context;
        this.onItemClickListener  = onClickItem;
    }


    @Override
    protected void onBindViewHolder(@NonNull PostsAdapter.PostsViewHolder holder, int position, @NonNull Posts model) {
        final String postKey = getRef(position).getKey();

        holder.setFullname(model.getFullname());
        holder.setDescription(model.getDescription());
        holder.setProfileimage(model.getProfileimage());
        holder.setPostimage(context, model.getPostImage());
        holder.setUpdateMessage("has been updated a post " + model.getDate() + " " + model.getTime());
        holder.bind(getRef(position).getKey(),onItemClickListener);
        holder.setLikeButtonStatus(postKey);
    }

    @NonNull
    @Override
    public PostsAdapter.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.all_posts_layout, viewGroup, false);
        return new PostsViewHolder(view);
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton likePostButton,commentPostButton;
        TextView displayNoOfLikes;
        public ShimmerFrameLayout mShimmerViewContainer;

        String currentUserId;
        int countLikes;

        DatabaseReference likesRef;


        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mShimmerViewContainer = mView.findViewById(R.id.shimmer_view_container);
            mShimmerViewContainer.startShimmerAnimation();
            likePostButton = mView.findViewById(R.id.like_button);
            commentPostButton = mView.findViewById(R.id.comment_button);
            displayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setFullname(String fullname) {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage) {
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.add_post_high).into(image);
        }

        public void setDescription(String description) {
            TextView PostDescription = mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context context, String postimageurl) {
            final ImageView postImage = mView.findViewById(R.id.post_image);
            try {
                Glide.with(context).load(postimageurl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                postImage.setVisibility(View.VISIBLE);
                                mShimmerViewContainer.stopShimmerAnimation();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(postImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setUpdateMessage(String updateMessage) {
            TextView postImage = mView.findViewById(R.id.text);
            postImage.setText(updateMessage);

        }

        public void bind(final String postKey, final PostsAdapter.ItemClickListener listner) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listner.onItemClick(postKey);
                }
            });

            likePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listner.onLikeButtonClick(postKey);
                }
            });

            commentPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listner.onCommentButtonClick(postKey);
                }
            });
        }


        public void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        String likes = Integer.toString(countLikes) + " likes";
                        displayNoOfLikes.setText(likes);
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        String likes = Integer.toString(countLikes) + " likes";
                        displayNoOfLikes.setText(likes);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}

package com.example.socialnetwork.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Comments;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class CommentsAdapter extends FirebaseRecyclerAdapter<Comments, CommentsAdapter.CommentsViewHolder> {
    private Context context;


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CommentsAdapter(@NonNull FirebaseRecyclerOptions<Comments> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull CommentsAdapter.CommentsViewHolder holder, int position, @NonNull Comments model) {
        holder.setUsername(model.getUsername());
        holder.setComment(model.getComment());
        holder.setDate(model.getDate());
        holder.setTime(model.getTime());
    }

    @NonNull
    @Override
    public CommentsAdapter.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.all_comments_layout, viewGroup, false);
        return new CommentsViewHolder(view);
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username) {
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText("@" +username+"  ");
        }

        public void setComment(String comment) {
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date) {
            TextView myCommentDate = mView.findViewById(R.id.comment_date);
            myCommentDate.setText("  Date:  " +date);
        }

        public void setTime(String time) {
            TextView myCommentTime = mView.findViewById(R.id.comment_time);
            myCommentTime.setText("  Time:  "+time);
        }

    }

}

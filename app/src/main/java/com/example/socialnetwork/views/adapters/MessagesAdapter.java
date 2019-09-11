package com.example.socialnetwork.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int SENT_MESSAGE = 0, RECEIVED_MESSAGE = 2;
    private Context context;
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private String messageSenderId;
    private Messages messages;
    private String fromUserId;
    private String fromMessageType;
    private ReceiverViewHolder receiverHolder;


    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        switch (viewType) {
            case SENT_MESSAGE:
                View sender = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_of_sender, parent, false);
                return new SenderViewHolder(sender);
            default:
                View receiver = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_of_receiver, parent, false);
                return new ReceiverViewHolder(receiver);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(true);
        messages = userMessagesList.get(position);
        fromUserId = messages.getFrom();
        fromMessageType = messages.getType();
        int viewType = getItemViewType(position);
        switch (viewType) {
            case SENT_MESSAGE:
                SenderViewHolder senderHolder = (SenderViewHolder) holder;
                senderHolder.senderMessageText.setBackgroundResource(R.drawable.buttons);
                senderHolder.senderMessageText.setTextColor(Color.WHITE);
                senderHolder.senderMessageText.setGravity(Gravity.START);
                senderHolder.senderMessageText.setText(messages.getMessage());
                break;
            default:
                receiverHolder = (ReceiverViewHolder) holder;
                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
                userDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.add_post_high).into(receiverHolder.receiverProfileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (fromMessageType.equals("text")) {
                    receiverHolder.receiverMessageText.setBackgroundResource(R.drawable.card_background);
                    receiverHolder.receiverMessageText.setTextColor(Color.WHITE);
                    receiverHolder.receiverMessageText.setGravity(Gravity.START);
                    receiverHolder.receiverMessageText.setText(messages.getMessage());
                }
                break;
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        public TextView receiverMessageText;
        public CircleImageView receiverProfileImage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (userMessagesList.get(position).from.equals(messageSenderId)) {
            return SENT_MESSAGE;
        } else {
            return RECEIVED_MESSAGE;
        }
    }

}


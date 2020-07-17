package com.example.teamworkchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<MessageClass> mMessages;

    FirebaseUser mUser;

    public MessageAdapter(Context mContext, List<MessageClass> mMessages) {
        this.mContext = mContext;
        this.mMessages = mMessages;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.chat_item_right_layout, parent, false);
            return new MessageAdapter.MessageViewHolder(view);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.chat_item_left_layout, parent, false);
            return new MessageAdapter.MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {
        MessageClass messageClass = mMessages.get(position);

        holder.show_message.setText(messageClass.getMessage());

        if (position == mMessages.size() - 1) {
            if (messageClass.getIsSeen()) {
                holder.text_seen.setText("Seen");
            } else {
                holder.text_seen.setText("Delivered");
            }
        } else {
            holder.text_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public TextView text_seen;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            text_seen = itemView.findViewById(R.id.text_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessages.get(position).getSender().equals(mUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}